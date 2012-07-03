#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#include <unistd.h>
#include <sys/poll.h>
#include <syslog.h>
#include <confd.h>
#include <confd_maapi.h>

#include "simple.h"

static int ctlsock, workersock;
static struct confd_daemon_ctx *dctx;

static int init_action(struct confd_user_info *uinfo);
static int do_action(struct confd_user_info *uinfo, struct xml_tag *name,
		     confd_hkeypath_t *kp, confd_tag_value_t *params, int n);
static void main_loop(int do_phase0);
extern void fail(char *fmt, ...);

int main(int argc, char **argv) {
     struct sockaddr_in addr;
     int debuglevel = CONFD_TRACE;
     struct confd_action_cbs acb;
     confd_init("actions_daemon", stderr, debuglevel);
     addr.sin_addr.s_addr = inet_addr("127.0.0.1");
     addr.sin_family = AF_INET;
     addr.sin_port = htons(CONFD_PORT);

     if (confd_load_schemas((struct sockaddr*)&addr,
			    sizeof (struct sockaddr_in)) != CONFD_OK)
	 fail("confd_load_schemas() failed\n");

     if ((dctx = confd_init_daemon("actions_daemon")) == NULL)
	  fail("confd_init_daemon() failed\n");

     if ((ctlsock = socket(PF_INET, SOCK_STREAM, 0)) < 0 ) 
	  confd_fatal("Failed to open ctlsocket\n");
    
     if (confd_connect(dctx, ctlsock, CONTROL_SOCKET, (struct sockaddr*)&addr, 
		       sizeof (struct sockaddr_in)) < 0) 
	  confd_fatal("confd_connect() failed\n");

    
     if ((workersock = socket(PF_INET, SOCK_STREAM, 0)) < 0 ) 
	  confd_fatal("Failed to open workersocket\n");

     if (confd_connect(dctx, workersock, WORKER_SOCKET,
		       (struct sockaddr*)&addr, 
		       sizeof (struct sockaddr_in)) < 0) 
	  confd_fatal("confd_connect() failed\n");

     memset(&acb, 0, sizeof(acb));
     strcpy(acb.actionpoint, "actions");
     acb.init = init_action;
     acb.action = do_action;
     
     if (confd_register_action_cbs(dctx, &acb) != CONFD_OK)
	  fail("Couldn't register action callbacks");

     if (confd_register_done(dctx) != CONFD_OK)
	  fail("Couldn't complete callback registration");

     main_loop(0);
     close(ctlsock);
     close(workersock);
     confd_release_daemon(dctx);
     return 0;
}

static void main_loop(int do_phase0) {
    struct pollfd set[3];
    int ret;

    while (1) {
	set[0].fd = ctlsock;
	set[0].events = POLLIN;
	set[0].revents = 0;
	set[1].fd = workersock;
	set[1].events = POLLIN;
	set[1].revents = 0;

	if (poll(set, 2, -1) < 0)
	    fail("Poll failed");

	if (set[0].revents & POLLIN) {
	    if ((ret = confd_fd_ready(dctx, ctlsock)) == CONFD_EOF) {
		fail("Control socket closed");
	    } else if (ret == CONFD_ERR && confd_errno != CONFD_ERR_EXTERNAL) {
		fail("Error on control socket request: %s (%d): %s",
		     confd_strerror(confd_errno), confd_errno, confd_lasterr());
	    }
	}

	if (set[1].revents & POLLIN) {
	    if ((ret = confd_fd_ready(dctx, workersock)) == CONFD_EOF) {
		fail("Worker socket closed");
	    } else if (ret == CONFD_ERR && confd_errno != CONFD_ERR_EXTERNAL) {
		fail("Error on worker socket request: %s (%d): %s",
		     confd_strerror(confd_errno), confd_errno, confd_lasterr());
	    }
	}
    }
}

static int init_action(struct confd_user_info *uinfo) {
     int ret = CONFD_OK;
     printf("init_action() called\n");
     confd_action_set_fd(uinfo, workersock);
     return ret;
}

static int do_action(struct confd_user_info *uinfo, struct xml_tag *name,
		     confd_hkeypath_t *kp, confd_tag_value_t *params, int n) {
     int i;
     char buf[BUFSIZ];
     confd_tag_value_t reply[12];
     printf("do_action() called\n");
     
     for (i = 0; i < n; i++) {
	  confd_pp_value(buf, sizeof(buf), CONFD_GET_TAG_VALUE(&params[i]));
	  printf("param %2d: %9u:%-9u, %s\n", i, CONFD_GET_TAG_NS(&params[i]),
		 CONFD_GET_TAG_TAG(&params[i]), buf);
     }
     
     switch (name->tag) {
     case simple_halt:
	  printf("halt\n");
	  break;
     case simple_shutdown:
	  printf("shutdown\n");
	  break;
     case simple_setSystemClock:
	  printf("setSystemClock\n");
	  // System clock
          struct confd_datetime systemClock;
          systemClock.year = 2001;
          systemClock.month = 11;
          systemClock.day = 21;
          systemClock.hour = 11;
          systemClock.min = 10;
          systemClock.sec = 9;
          systemClock.micro = 200;
          systemClock.timezone = 0;
          systemClock.timezone_minutes = 0;          
          CONFD_SET_TAG_DATETIME(&reply[0], simple_systemClock, systemClock);
	  // Hardware clock
          struct confd_datetime hardwareClock;
          hardwareClock.year = 2001;
          hardwareClock.month = 11;
          hardwareClock.day = 21;
          hardwareClock.hour = 11;
          hardwareClock.min = 10;
          hardwareClock.sec = 9;
          hardwareClock.micro = 200;
          hardwareClock.timezone = 0;
          hardwareClock.timezone_minutes = 0;          
	  CONFD_SET_TAG_DATETIME(&reply[1], simple_hardwareClock,
				 hardwareClock);
	  confd_action_reply_values(uinfo, reply, 2);
	  break;
     default:
	  printf("Got bad operation: %d", name->tag);
	  return CONFD_ERR;
     }

     return CONFD_OK;
}

void fail(char *fmt, ...) {
     va_list ap;
     char buf[BUFSIZ];
     va_start(ap, fmt);
     snprintf(buf, sizeof(buf), "%s, exiting", fmt);
     vsyslog(LOG_ERR, buf, ap);
     va_end(ap);
     exit(1);
}
