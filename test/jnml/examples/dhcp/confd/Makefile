H="left right"
IPC_BASE=4000
NC_TCP_BASE=2032
OSNAME:=$(shell uname -s)
CONFD=$(CONFD_DIR)/bin/confd
CONFDC=$(CONFD_DIR)/bin/confdc
CDB_DIR=./confd-cdb

ifeq ($(OSNAME),QNX)
KILLALL=slay -f
else
ifeq ($(OSNAME),SunOS)
KILLALL=pkill -x
else
KILLALL=killall
endif
endif

all: $(CONFD) dhcpd.fxs
	./setup.sh

restart: stop clean all start

start:
	./start.sh

stop:
	./stop.sh; 

clean:
	rm -rf `echo $(H)` *.fxs

clean-all:
	$(MAKE) clean
	$(MAKE) all

# In case CONFD_DIR is not set (correctly), this rule will trigger
$(CONFD):
	@echo 'Where is ConfD installed? Set $$CONFD_DIR to point it out!'
	@echo ''
	@exit 1

######################################################################
##
## Start individual confd servers
##
start-left:
	./start.sh left

start-right:
	./start.sh right

######################################################################
##
## Stop individual routers
##
istop:
	env CONFD_IPC_PORT=$$(($(IPC_BASE) + $(COUNT))) \
		confd --stop 

stop-left:
	@$(MAKE) COUNT=0 istop

stop-right:
	@$(MAKE) COUNT=1 istop

######################################################################
icli:
	confd_cli --port  $$(($(IPC_BASE) + $(COUNT))) \
	          --user=admin --groups=admin \
	          --interactive || echo Exit
cli-left:
	@$(MAKE) COUNT=0 icli

cli-right:
	@$(MAKE) COUNT=1 icli

######################################################################
istatus:
	env CONFD_IPC_PORT=$$(($(IPC_BASE) + $(COUNT))) \
		confd --status 


status-left:
	@$(MAKE) COUNT=0 istatus

status-right:
	@$(MAKE) COUNT=1 istatus

######################################################################
i_nc:
	netconf-console-tcp \
            --port $$(($(NC_TCP_BASE) + $(COUNT))) \
            --get-config --xpath /dhcp	

nc-left:
	@$(MAKE) COUNT=0 i_nc

nc-right:
	@$(MAKE) COUNT=1 i_nc

%.fxs: %.yang
	$(CONFDC) -c -o $@ $<

