######################################################################
# INM demo
# (C) 2006 Tail-f Systems
#
# See the README file for more information
######################################################################

usage:
	@echo "make all          Build all"
	@echo "make clean        Remove all built and intermediary files"
	@echo "make start        Start ConfD daemon "
	@echo "make stop         Stop any ConfD daemon"
	@echo "make query        Run query against ConfD"
	@echo "make cli          Start the ConfD Command Line Interface"

######################################################################
# Where is ConfD installed? Make sure CONFD_DIR points it out

ifndef CONFD_DIR
$(error "Enviroment variable CONFD_DIR has not been set. Please set this variable, e.g. by sourcing the rc script located in your ConfD installation directory.") 
endif

# Include standard ConfD build definitions and rules
include $(CONFD_DIR)/src/confd/build/include.mk

# In case CONFD_DIR is not set (correctly), this rule will trigger
$(CONFD_DIR)/src/confd/build/include.mk:
	@echo 'Where is ConfD installed? Set $$CONFD_DIR to point it out!'
	@echo ''
	@exit 1

######################################################################
# Example specific definitions and rules

CONFD_HOST  ?= localhost
CONFD_FLAGS = --addloadpath $(CONFD_DIR)/etc/confd
START_FLAGS ?=

all: simple.fxs ssh-keydir $(CDB_DIR)/simple.xml
	@echo "Build complete"

simple.xsd: simple.fxs
	$(CONFDC) --emit-xsd simple.xsd simple.fxs

$(CDB_DIR)/aaa_init.xml: aaa_init.xml $(CDB_DIR)
	@cp $< $@

$(CDB_DIR)/%.xml: %.xml $(CDB_DIR)
	cp $< $(CDB_DIR)
	@touch $(CDB_DIR)/*.xml

######################################################################
clean:	iclean

######################################################################
start:  stop 
	$(CONFD) -c confd.conf $(CONFD_FLAGS)

######################################################################
stop:
	$(CONFD) --stop || true

######################################################################
cli:
	$(CONFD_DIR)/bin/confd_cli --user=admin --groups=admin --interactive \
		--host $(CONFD_HOST) || echo Exit

######################################################################
query:
	$(CONFD_DIR)/bin/netconf-console --port 2022 --host $(CONFD_HOST) --get
