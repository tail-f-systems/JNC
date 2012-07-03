H="v1 v2 v3"
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

all: $(CONFD) simple_v1.fxs simple_v2.fxs simple_v3.fxs
	./setup.sh

restart: stop clean all start

start: stop
	./start.sh

stop:
	./stop.sh; 

clean:
	rm -rf `echo $(H)` *.fxs simple_v1.cs simple_v2.cs simple_v3.cs

clean-all:
	$(MAKE) clean
	$(MAKE) all

confspecify: simple_v1.cs simple_v2.cs simple_v3.cs

simple_v1.cs: simple_v1.yang
	pyang -f cs simple_v1.yang > simple_v1.cs

simple_v2.cs: simple_v2.yang
	pyang -f cs simple_v2.yang > simple_v2.cs

simple_v3.cs: simple_v3.yang
	pyang -f cs simple_v3.yang > simple_v3.cs

# In case CONFD_DIR is not set (correctly), this rule will trigger
$(CONFD):
	@echo 'Where is ConfD installed? Set $$CONFD_DIR to point it out!'
	@echo ''
	@exit 1

######################################################################
##
## Start individual confd servers
##
start-v1:
	./start.sh v1

start-v2:
	./start.sh v2

start-v3:
	./start.sh v3

######################################################################
##
## Stop individual routers
##
istop:
	env CONFD_IPC_PORT=$$(($(IPC_BASE) + $(COUNT))) \
		confd --stop 

stop-v1:
	@$(MAKE) COUNT=0 istop

stop-v2:
	@$(MAKE) COUNT=1 istop

stop-v3:
	@$(MAKE) COUNT=2 istop

######################################################################
icli:
	confd_cli --port  $$(($(IPC_BASE) + $(COUNT))) \
	          --user=admin --groups=admin \
	          --interactive || echo Exit
cli-v1:
	@$(MAKE) COUNT=0 icli

cli-v2:
	@$(MAKE) COUNT=1 icli

cli-v3:
	@$(MAKE) COUNT=2 icli

######################################################################
istatus:
	env CONFD_IPC_PORT=$$(($(IPC_BASE) + $(COUNT))) \
		confd --status 

status-v1:
	@$(MAKE) COUNT=0 istatus

status-v2:
	@$(MAKE) COUNT=1 istatus

status-v3:
	@$(MAKE) COUNT=2 istatus

######################################################################
i_nc:
	netconf-console-tcp \
            --port $$(($(NC_TCP_BASE) + $(COUNT))) \
            --get-config --xpath /dhcp	

nc-v1:
	@$(MAKE) COUNT=0 i_nc

nc-v2:
	@$(MAKE) COUNT=1 i_nc

nc-v3:
	@$(MAKE) COUNT=2 i_nc

%.fxs: %.yang
	$(CONFDC) -c -o $@ $<
