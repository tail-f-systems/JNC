
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <netdb.h>
#include <sys/poll.h>

#include <confd.h>

/* to get netconf protocol element tag names */
#include <confd_netconf_proto.h>

#include "math-rpc.h"

static int ctlsock, workersock;
static struct confd_daemon_ctx *dctx;

static int get_ctlsock(struct addrinfo *addr)
{
    int sock;

    if ((sock =
	 socket(addr->ai_family, addr->ai_socktype, addr->ai_protocol)) < 0)
	return -1;
    if (confd_connect(dctx, sock, CONTROL_SOCKET,
		      addr->ai_addr, addr->ai_addrlen) != CONFD_OK) {
	close(sock);
	return -1;
    }
    return sock;
}

static int get_workersock(struct addrinfo *addr)
{
    int sock;

    if ((sock =
	 socket(addr->ai_family, addr->ai_socktype, addr->ai_protocol)) < 0)
	return -1;
    if (confd_connect(dctx, sock, WORKER_SOCKET,
		      addr->ai_addr, addr->ai_addrlen) != CONFD_OK) {
	close(sock);
	return -1;
    }
    return sock;
}

static int init_action(struct confd_user_info *uinfo)
{
    confd_action_set_fd(uinfo, workersock);
    return CONFD_OK;
}

static int math(struct confd_user_info *uinfo,
		struct xml_tag *name,
		confd_hkeypath_t *kp,
		confd_tag_value_t *params,
		int nparams)
{
    confd_tag_value_t reply[1];
    confd_value_t *operand;
    int op1, op2, result = 0;

    /*
      we know that we get exactly 3 parameters;
         add | sub BEGIN
         operand 1 2      (C_LIST)
         add | sub END
    */

    operand = CONFD_GET_LIST(CONFD_GET_TAG_VALUE(&params[1]));
    op1 = CONFD_GET_INT32(&operand[0]);
    op2 = CONFD_GET_INT32(&operand[1]);
    switch (CONFD_GET_TAG_TAG(&params[0])) {
    case math_add:
	printf("\nmath: calculating %d + %d\n", op1, op2);
	result = op1 + op2;
	break;
    case math_sub:
	printf("\nmath: calculating %d - %d\n", op1, op2);
	result = op1 - op2;
	break;
    }

    /* to test error handling, let's treat a result of -1 after
       subtraction as an error.
       we will return the following XML error:
         <rpc-error>
           <error-type>application</error-type>
           <error-tag>zero-result</error-tag>
           <error-severity>error</error-severity>
           <error-info>
             <math3 xmlns="http://example.com/math">
               <sub>
                 <operand>7</operand>
                 <operand>8</operand>
               </sub>
             </math3>
           </error-info>
         </rpc-error>
    */
    if (CONFD_GET_TAG_TAG(&params[0]) == math_sub && result == -1) {
	int i = 0;
	confd_tag_value_t rpcerror[13];
	CONFD_SET_TAG_XMLBEGIN(&rpcerror[i++], nc_rpc_error, nc__ns);
	CONFD_SET_TAG_STR(&rpcerror[i++], nc_error_type, "application");
	CONFD_SET_TAG_STR(&rpcerror[i++], nc_error_tag, "zero-result");
	CONFD_SET_TAG_STR(&rpcerror[i++], nc_error_severity, "error");
	CONFD_SET_TAG_XMLBEGIN(&rpcerror[i++], nc_error_info, nc__ns);
	CONFD_SET_TAG_XMLBEGIN(&rpcerror[i++], math_math3, math__ns);
	CONFD_SET_TAG_XMLBEGIN(&rpcerror[i++], math_sub, math__ns);
        CONFD_SET_TAG_LIST(&rpcerror[i++], math_operand, operand, 2);
	CONFD_SET_TAG_XMLEND(&rpcerror[i++], math_sub, math__ns);
	CONFD_SET_TAG_XMLEND(&rpcerror[i++], math_math3, math__ns);
	CONFD_SET_TAG_XMLEND(&rpcerror[i++], nc_error_info, nc__ns);
	CONFD_SET_TAG_XMLEND(&rpcerror[i++], nc_rpc_error, nc__ns);
	confd_action_reply_values(uinfo, rpcerror, i);
    } else {
	CONFD_SET_TAG_INT32(&reply[0], math_result, result);
	confd_action_reply_values(uinfo, reply, 1);
    }

    return CONFD_OK;
}


int main(int argc, char **argv)
{
    char confd_port[16];
    struct addrinfo hints;
    struct addrinfo *addr = NULL;
    int debuglevel = CONFD_SILENT;
    int c;
    char *p, *dname;
    int i;
    struct confd_action_cbs acb;
    struct pollfd set[2];
    int ret;

    snprintf(confd_port, sizeof(confd_port), "%d", CONFD_PORT);
    memset(&hints, 0, sizeof(hints));
    hints.ai_family = PF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;

    while ((c = getopt(argc, argv, "Ddtpur:c:")) != -1) {
	switch (c) {
	case 'd':
	    debuglevel = CONFD_DEBUG;
	    break;
	case 't':
	    debuglevel = CONFD_TRACE;
	    break;
	case 'p':		/* undocumented */
	    debuglevel = CONFD_PROTO_TRACE;
	    break;
	case 'c':
	    if ((p = strchr(optarg, '/')) != NULL)
		*p++ = '\0';
	    else
		p = confd_port;
	    if (getaddrinfo(optarg, p, &hints, &addr) != 0) {
		if (p != confd_port) {
		    *--p = '/';
		    p = "/port";
		} else {
		    p = "";
		}
		fprintf(stderr, "%s: Invalid address%s: %s\n",
			argv[0], p, optarg);
		exit(1);
	    }
	    break;
	default:
	    fprintf(stderr,
		    "Usage: %s [-dt] [-c address[/port]]\n",
		    argv[0]);
	    exit(1);
	}
    }

    if (addr == NULL &&
	(i = getaddrinfo("127.0.0.1", confd_port, &hints, &addr)) != 0)
	/* "Can't happen" */
	confd_fatal("%s: Failed to get address for ConfD: %s\n",
		    argv[0], gai_strerror(i));
    if ((dname = strrchr(argv[0], '/')) != NULL)
	dname++;
    else
	dname = argv[0];
    /* Init library */
    confd_init(dname, stderr, debuglevel);

    if ((dctx = confd_init_daemon(dname)) == NULL)
	confd_fatal("Failed to initialize ConfD\n");
    if ((ctlsock = get_ctlsock(addr)) < 0)
	confd_fatal("Failed to connect to ConfD\n");
    if ((workersock = get_workersock(addr)) < 0)
	confd_fatal("Failed to connect to ConfD\n");

    memset(&acb, 0, sizeof(acb));
    strcpy(acb.actionpoint, "math3");
    acb.init = init_action;
    acb.action = math;
    if (confd_register_action_cbs(dctx, &acb) != CONFD_OK)
	confd_fatal("Couldn't register action callbacks\n");
    if (confd_register_done(dctx) != CONFD_OK)
	confd_fatal("Couldn't complete callback registration");

    while (1) {

	set[0].fd = ctlsock;
	set[0].events = POLLIN;
	set[0].revents = 0;

	set[1].fd = workersock;
	set[1].events = POLLIN;
	set[1].revents = 0;

	if (poll(set, 2, -1) < 0)
	    confd_fatal("Poll failed\n");

	/* Check for I/O */

	if (set[0].revents & POLLIN) { /* ctlsock */
	    if ((ret = confd_fd_ready(dctx, ctlsock)) == CONFD_EOF) {
		confd_fatal("Control socket closed\n");
	    } else if (ret == CONFD_ERR && confd_errno != CONFD_ERR_EXTERNAL) {
		confd_fatal("Error on control socket request: %s (%d): %s\n",
		     confd_strerror(confd_errno), confd_errno, confd_lasterr());
	    }
	}

	if (set[1].revents & POLLIN) { /* workersock */
	    if ((ret = confd_fd_ready(dctx, workersock)) == CONFD_EOF) {
		confd_fatal("Worker socket closed\n");
	    } else if (ret == CONFD_ERR && confd_errno != CONFD_ERR_EXTERNAL) {
		confd_fatal("Error on worker socket request: %s (%d): %s\n",
		     confd_strerror(confd_errno), confd_errno, confd_lasterr());
	    }
	}

    }
}
