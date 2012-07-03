#include <sys/poll.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/poll.h>
#include <string.h>

#include <stdio.h>
#include <expat.h>

#define BUFSIZE 8192

/* these are the states in our simple state machine.  the state machine
   is driven by the XML callbacks start_tag and data */
enum state {
    ST_INIT,
    ST_READ_OP,
    ST_READ_OPERAND1,
    ST_READ_OPERAND1_DATA,
    ST_READ_OPERAND2,
    ST_READ_OPERAND2_DATA,
    ST_DONE};    

/* global state variable */
enum state state;

enum operation {
    OP_ADD,
    OP_SUB
};

/* global operation variable */
enum operation op;

/* global operand variables */
int operand1;
int operand2;

/* write a reply to the manager */
static void rpc_reply(int res)
{
    char buf[BUFSIZE];
    sprintf(buf, 
	    "<result xmlns='http://example.com/math'>%d</result>\n",
	    res);
    write(1, buf, strlen(buf));
}

/* write an error to the manager and terminate properly */
static void rpc_error(char *str, const char *xtra)
{
    char buf[BUFSIZE];
    sprintf(buf, 
	    "<rpc-error>\n"
	    "  <error-type>application</error-type>\n"
	    "  <error-tag>operation-failed</error-tag>\n"
	    "  <error-severity>error</error-severity>\n"
            "  <error-message xml:lang='en'>\n"
	    "    %s%s\n"
	    "  </error-message>\n"
            "</rpc-error>\n",
	    str, xtra);
    write(1, buf, strlen(buf));
    exit(0);
}

static void XMLCALL start_tag(void *data, const char *tag, const char **attr)
{
    switch (state) {
    case ST_INIT:
	if (strcmp(tag, "math") == 0) {
	    state = ST_READ_OP;
	}
	else
	    rpc_error("bad rpc from ConfD: ", tag);
	break;
    case ST_READ_OP:
	if (strcmp(tag, "add") == 0) {
	    op = OP_ADD;
	    state = ST_READ_OPERAND1;
	}
	else if (strcmp(tag, "sub") == 0) {
	    op = OP_SUB;
	    state = ST_READ_OPERAND1;
	}
	else
	    rpc_error("unknown operation: ", tag);
	break;
    case ST_READ_OPERAND1:
	if (strcmp(tag, "operand") == 0) {
	    state = ST_READ_OPERAND1_DATA;
	}
	else
	    rpc_error("expected operand, got: ", tag);
	break;
    case ST_READ_OPERAND2:
	if (strcmp(tag, "operand") == 0) {
	    state = ST_READ_OPERAND2_DATA;
	}
	else
	    rpc_error("expected operand, got: ", tag);
	break;
    default:
	rpc_error("unknown element: ", tag);
    }
}

#define IS_WS(x) (((x) == ' ' || (x) == '\t' || (x) == '\n' || (x) == '\r'))

static void strip(const char *src, char *src_end, char *dst)
{
    char *p, *e, *r;

    p = (char *)src;
    e = src_end;
    r = dst;
    dst[0] = '\0';
    /* skip leading whitespace */
    while (p != src_end && IS_WS(*p)) {
	p++;
    }
    if (p == src_end)
	return;
    /* skip trailing whitespace */
    while (e > p && IS_WS(*e)) {
	e--;
    }
    e++;
    /* copy rest */
    do {
	*r = *p;
	r++;
	p++;
    } while (p != e);
    *r = '\0';
}

/* this callback is invoked by expat whenever we get some data, including
   whitespace only data. */
static void XMLCALL data(void *data, const XML_Char *s, int len)
{
    char content[BUFSIZE];

    /* accept empty data */
    strip(s, ((char *)s)+len, content);
    if (content[0] == '\0')
	return;

    switch (state) {
    case ST_READ_OPERAND1_DATA:
	operand1 = atoi(content);
	state = ST_READ_OPERAND2;
	break;
    case ST_READ_OPERAND2_DATA:
	operand2 = atoi(content);
	state = ST_DONE;
	break;
    default:
	rpc_error("unknown data: ", s);
    }
}

int main(int argc, char *argv[]) {
    int done;
    int len;
    char buf[BUFSIZE];
    int res;

    XML_Parser p;
     
    if ((p = XML_ParserCreate(NULL)) == NULL) {
	fprintf(stderr, "Couldn't allocate memory for parser\n");
	exit(1);
    }
     
    XML_SetElementHandler(p, start_tag, NULL);
    XML_SetCharacterDataHandler(p, data);

    /* read the complete RPC from stdin; ConfD signals end-of-input
       by closing stdin. */
    for (;;) {
	len = fread(buf, 1, BUFSIZE, stdin);
	  
	if (ferror(stdin)) {
	    fprintf(stderr, "Read error\n");
	    exit(1);
	}
	  
	done = feof(stdin);
	  
	state = ST_INIT;
	if (XML_Parse(p, buf, len, done) == XML_STATUS_ERROR) {
	    fprintf(stderr,
		    "Parse error at line %d u:\n%s\n",
		    (int)XML_GetCurrentLineNumber(p),
		    XML_ErrorString(XML_GetErrorCode(p)));
	    exit(1);
	}
	  
	if (done)
	    break;
    }

    /* stdin is closed, thus we have read all data.  make sure that we're
       in correct state. */
     
    if (state != ST_DONE)
	rpc_error("incomplete message", "");
     
    switch(op) {
    case OP_ADD:
	res = operand1 + operand2;
	break;
    case OP_SUB:
	res = operand1 - operand2;
	break;
    }

    /* reply to ConfD and then terminate gracefully, to indicate end of
       our reply */
    rpc_reply(res);

    return 0;
}
