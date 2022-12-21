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

all: notif.fxs ssh-keydir $(CDB_DIR) confd.conf notifier
	@echo "Build complete"

notifier: notifier.o
	 $(CC) notifier.o $(LIBS) $(CFLAGS) -ansi -pedantic -o $@

notifier.o: notif.h

notif.xsd: notif.fxs
	$(CONFDC) --emit-xsd notif.xsd notif.fxs

$(CDB_DIR)/aaa_init.xml: ../confd/aaa_init.xml $(CDB_DIR)
	@cp $< $@

$(CDB_DIR)/%.xml: %.xml $(CDB_DIR)
	cp $< $(CDB_DIR)
	@touch $(CDB_DIR)/*.xml

clean:	iclean

confd.conf: ../confd/confd.conf
	awk -lreadfile -f confd.awk $^ >| $@

start:  stop
	$(CONFD) -c confd.conf $(CONFD_FLAGS)

start_notifier:
	./notifier

stop:
	$(CONFD) --stop || true
