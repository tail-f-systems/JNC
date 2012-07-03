######################################################################
# NETCONF Extension example
# (C) 2006 Tail-f Systems
#
# See the README file for more information
######################################################################

usage:
	@echo "See README file for more instructions"
	@echo "make all      Build all example files"
	@echo "make clean    Remove all built and intermediary files"
	@echo "make start    Start ConfD daemon and example agent"
	@echo "make stop     Stop any ConfD daemon and example agent"
	@echo "make query    Run NETCONF query against ConfD"
	@echo "make cli      Start the ConfD Command Line Interface"

######################################################################
# Where is ConfD installed? Make sure CONFD_DIR points it out
CONFD_DIR ?= ../../..

# Include standard ConfD build definitions and rules
include $(CONFD_DIR)/src/confd/build/include.mk

# In case CONFD_DIR is not set (correctly), this rule will trigger
$(CONFD_DIR)/src/confd/build/include.mk:
	@echo 'Where is ConfD installed? Set $$CONFD_DIR to point it out!'
	@echo ''
	@exit 1

######################################################################
# Example specific definitions and rules

CONFD_FLAGS ?=
START_FLAGS ?=

SRC	= math.c math-capi.c
OBJS	= $(SRC:.c=.o)

LIBS	+= -lexpat

all:	math math-capi math-rpc.fxs aaa_cdb.fxs $(CDB_DIR) ssh-keydir
	@echo "Build complete"

$(OBJS):	$(SRC)

math-capi.o: math-rpc.h

math: math.o
	 $(CC) math.o $(LIBS) $(CFLAGS) -o $@

math-capi: math-capi.o
	 $(CC) math-capi.o $(LIBS) $(CFLAGS) -o $@ 

######################################################################
clean:	iclean
	-rm -rf math math-capi math-rpc.fxs math-rpc.h 2> /dev/null || true

start-confd:  stop 
	$(CONFD) -c ./confd.conf $(CONFD_FLAGS)

start: start-confd
	LD_LIBRARY_PATH=$(CONFD_SO) ./math-capi -t

######################################################################
stop:
	### Stopping any confd daemon
	$(CONFD) --stop || true
	$(KILLALL) math || true
	$(KILLALL) math-capi || true

######################################################################
cli:
	$(CONFD_DIR)/bin/confd_cli --user=admin --groups=admin \
	--interactive || echo Exit

######################################################################
query:
	$(CONFD_DIR)/bin/netconf-console-tcp cmd-math.xml

query2:
	$(CONFD_DIR)/bin/netconf-console-tcp cmd-math2.xml

query3:
	$(CONFD_DIR)/bin/netconf-console-tcp cmd-math3.xml

query2-bad1:
	$(CONFD_DIR)/bin/netconf-console-tcp cmd-math2-bad1.xml

query2-bad2:
	$(CONFD_DIR)/bin/netconf-console-tcp cmd-math2-bad2.xml

query3-bad:
	$(CONFD_DIR)/bin/netconf-console-tcp cmd-math3-bad.xml

######################################################################
