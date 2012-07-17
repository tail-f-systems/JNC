# (C) 2012 Tail-f Systems

usage:
	@echo "make clean        Remove all built and intermediary files"
	@echo "make all          Build all"
	@echo "make start        Start ConfD daemon"
	@echo "make stop         Stop any ConfD daemon"

######################################################################
# Where is ConfD installed? Make sure CONFD_DIR points it out

ifndef CONFD_DIR
$(error "Enviroment variable CONFD_DIR has not been set. Please set this \
         variable, e.g. by sourcing the rc script located in your ConfD \
         installation directory.") 
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

all: typedef-test.fxs ssh-keydir $(CDB_DIR)/typedef-test.xml
	@echo "Build complete"

typedef-test.xsd: typedef-test.fxs
	$(CONFDC) --emit-xsd typedef-test.xsd typedef-test.fxs

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
	$(CONFD) --stop 2> /dev/null || true
