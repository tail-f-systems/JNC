usage:
	@echo "make all          Build all"
	@echo "make clean        Remove all built and intermediary files"
	@echo "make start        Start ConfD daemon"
	@echo "make stop         Stop any ConfD daemon"

ifndef CONFD_DIR
$(error "Enviroment variable CONFD_DIR has not been set.")
endif

# Include standard ConfD build definitions and rules
include $(CONFD_DIR)/src/confd/build/include.mk

# In case CONFD_DIR is not set (correctly), this rule will trigger
$(CONFD_DIR)/src/confd/build/include.mk:
	@echo 'Where is ConfD installed? Set $$CONFD_DIR to point it out!'
	@echo ''
	@exit 1

# Example specific definitions and rules
CONFD_HOST ?= localhost
CONFD_FLAGS = --addloadpath $(CONFD_DIR)/etc/confd
START_FLAGS ?=

all: bits.fxs ssh-keydir $(CDB_DIR)/bits.xml
	@echo "Build complete"

bits.xsd: bits.fxs
	$(CONFDC) --emit-xsd bits.xsd bits.fxs

$(CDB_DIR)/aaa_init.xml: ../confd/aaa_init.xml $(CDB_DIR)
	@cp $< $@

$(CDB_DIR)/%.xml: %.xml $(CDB_DIR)
	cp $< $(CDB_DIR)
	@touch $(CDB_DIR)/*.xml

clean:	iclean

start:  stop
	$(CONFD) -c ../confd/confd.conf $(CONFD_FLAGS)

stop:
	$(CONFD) --stop || true
