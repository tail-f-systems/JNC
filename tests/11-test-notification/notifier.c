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
#include <time.h>
#include <sys/time.h>

#include <confd_lib.h>
#include <confd_dp.h>

#include "notif.h"

#define OK(val) (assert((val) == CONFD_OK))

static int ctlsock, workersock;
static struct confd_daemon_ctx *dctx;
static struct confd_notification_ctx *live_ctx;

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

static void getdatetime(struct confd_datetime *datetime)
{
    struct tm tm;
    struct timeval tv;

    gettimeofday(&tv, NULL);
    gmtime_r(&tv.tv_sec, &tm);

    memset(datetime, 0, sizeof(*datetime));
    datetime->year = 1900 + tm.tm_year;
    datetime->month = tm.tm_mon + 1;
    datetime->day = tm.tm_mday;
    datetime->sec = tm.tm_sec;
    datetime->micro = tv.tv_usec;
    datetime->timezone = 0;
    datetime->timezone_minutes = 0;
    datetime->hour = tm.tm_hour;
    datetime->min = tm.tm_min;
}

static void send_notification(confd_tag_value_t *vals, int nvals)
{
    struct confd_datetime eventTime;

    getdatetime(&eventTime);
    OK(confd_notification_send(live_ctx, &eventTime, vals, nvals));
}

static void send_notifup_1(int index, int flags1, int flags2)
{
    confd_tag_value_t vals[9];
    int i = 0;

    CONFD_SET_TAG_XMLBEGIN(&vals[i], notif_link_up,       notif__ns);  i++;
    CONFD_SET_TAG_UINT32(&vals[i],   notif_if_index,      index);      i++;
    CONFD_SET_TAG_XMLBEGIN(&vals[i], notif_link_property, notif__ns);  i++;
    CONFD_SET_TAG_UINT32(&vals[i],   notif_flags,        flags1);     i++;
    CONFD_SET_TAG_XMLEND(&vals[i],   notif_link_property, notif__ns);  i++;
    CONFD_SET_TAG_XMLBEGIN(&vals[i], notif_link_property, notif__ns);  i++;
    CONFD_SET_TAG_UINT32(&vals[i],   notif_flags,        flags2);     i++;
    CONFD_SET_TAG_XMLEND(&vals[i],   notif_link_property, notif__ns);  i++;
    CONFD_SET_TAG_XMLEND(&vals[i],   notif_link_up,       notif__ns);  i++;
    send_notification(vals, i);
}

static void send_notifup_2(int index, int flags1, int val1, int val2)
{
    confd_tag_value_t vals[15];
    int i = 0;

    CONFD_SET_TAG_XMLBEGIN(&vals[i], notif_link_up,       notif__ns);  i++;
    CONFD_SET_TAG_UINT32(&vals[i],   notif_if_index,      index);      i++;
    CONFD_SET_TAG_XMLBEGIN(&vals[i], notif_link_property, notif__ns);  i++;
    CONFD_SET_TAG_XMLTAG(&vals[i],   notif_newly_added,   notif__ns);  i++;
    CONFD_SET_TAG_UINT32(&vals[i],   notif_flags,        flags1);     i++;
    CONFD_SET_TAG_XMLBEGIN(&vals[i], notif_extensions,   notif__ns);  i++;
    CONFD_SET_TAG_UINT32(&vals[i],   notif_name,         1);          i++;
    CONFD_SET_TAG_UINT32(&vals[i],   notif_value,        val1);       i++;
    CONFD_SET_TAG_XMLEND(&vals[i],   notif_extensions,   notif__ns);  i++;
    CONFD_SET_TAG_XMLBEGIN(&vals[i], notif_extensions,   notif__ns);  i++;
    CONFD_SET_TAG_UINT32(&vals[i],   notif_name,         2);          i++;
    CONFD_SET_TAG_UINT32(&vals[i],   notif_value,        val2);       i++;
    CONFD_SET_TAG_XMLEND(&vals[i],   notif_extensions,   notif__ns);  i++;
    CONFD_SET_TAG_XMLEND(&vals[i],   notif_link_property, notif__ns);  i++;
    CONFD_SET_TAG_XMLEND(&vals[i],   notif_link_up,       notif__ns);  i++;
    send_notification(vals, i);
}

static void send_notifdown(int index)
{
    confd_tag_value_t vals[3];
    int i = 0;

    CONFD_SET_TAG_XMLBEGIN(&vals[i], notif_link_down,     notif__ns);  i++;
    CONFD_SET_TAG_UINT32(&vals[i],   notif_if_index,      index);      i++;
    CONFD_SET_TAG_XMLEND(&vals[i],   notif_link_down,     notif__ns);  i++;
    send_notification(vals, i);
}

int main(int argc, char **argv)
{
    char confd_port[16];
    struct addrinfo hints;
    struct addrinfo *addr = NULL;
    int debuglevel = CONFD_SILENT;
    int i;
    int c;
    char *p, *dname;
    struct confd_notification_stream_cbs ncb;
    struct pollfd set[3];
    int ret;

    snprintf(confd_port, sizeof(confd_port), "%d", CONFD_PORT);
    memset(&hints, 0, sizeof(hints));
    hints.ai_family = PF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;

    while ((c = getopt(argc, argv, "dtprc:")) != -1) {
        switch (c) {
        case 'd':
            debuglevel = CONFD_DEBUG;
            break;
        case 't':
            debuglevel = CONFD_TRACE;
            break;
        case 'p':
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
                    "Usage: %s [-dtpr] [-c address[/port]]\n",
                    argv[0]);
            exit(1);
        }
    }

    if (addr == NULL &&
        ((i = getaddrinfo("127.0.0.1", confd_port, &hints, &addr)) != 0))
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

    memset(&ncb, 0, sizeof(ncb));
    ncb.fd = workersock;
    ncb.get_log_times = NULL;
    ncb.replay = NULL;
    strcpy(ncb.streamname, "interface");
    ncb.cb_opaque = NULL;
    if (confd_register_notification_stream(dctx, &ncb, &live_ctx) != CONFD_OK) {
        confd_fatal("Couldn't register stream %s\n", ncb.streamname);
    }
    if (confd_register_done(dctx) != CONFD_OK) {
        confd_fatal("Failed to complete registration \n");
    }

    printf("notifier started\n");
    fflush(stdout);

    while (1) {
        set[0].fd = ctlsock;
        set[0].events = POLLIN;
        set[0].revents = 0;

        set[1].fd = 0;
        set[1].events = POLLIN;
        set[1].revents = 0;

        switch (poll(set, 2, -1)) {
        case -1:
            break;

        default:
            /* Check for I/O */
            if (set[0].revents & POLLIN) { /* ctlsock */
                if ((ret = confd_fd_ready(dctx, ctlsock)) == CONFD_EOF) {
                    confd_fatal("Control socket closed\n");
                } else if (ret == CONFD_ERR &&
                           confd_errno != CONFD_ERR_EXTERNAL) {
                    confd_fatal("Error on control socket request: "
                                "%s (%d): %s\n", confd_strerror(confd_errno),
                                confd_errno, confd_lasterr());
                }
            }
            if (set[1].revents & (POLLIN|POLLHUP)) { /* stdin */
                char c;
                if (!read(0, &c, 1))
                    exit(0);
                switch (c) {
                case 'u':
                    printf("sending link-up notification\n");
                    send_notifup_1(1, 2112, 32);
                    break;
                case 'i':
                    printf("sending link-up notification\n");
                    send_notifup_1(2, 42, 4668);
                    break;
                case 'y':
                    printf("sending link-up second notification\n");
                    send_notifup_2(2, 42, 3, 4668);
                    break;
                case 'd':
                    printf("sending link-down notification\n");
                    send_notifdown(1);
                    break;
                case 'm':
                    printf("sending 10000 random notifications\n");
                    int i;
                    for (i = 0; i < 10000; i++) {
                        long int n = random();
                        if (n < RAND_MAX/4)
                            send_notifup_1(1, 2112, 32);
                        else
                            if (n < RAND_MAX/4*2)
                                send_notifup_1(2, 42, 4668);
                            else
                                if (n < RAND_MAX/4*3)
                                    send_notifup_2(2, 42, 3, 4668);
                                else
                                    send_notifdown(1);
                    }
                    break;
                case '\n':
                    break;
                default:
                    printf("unknown character <%c>\n", c);
                    break;
                }
            }
        }
    }
}
