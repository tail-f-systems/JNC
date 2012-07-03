H="edgerouter-west edgerouter-north edgerouter-east"
IPC_BASE=4000
NC_TCP_BASE=2032
CONFD=$(CONFD_DIR)/bin/confd
CONFDC=$(CONFD_DIR)/bin/confdc

all: $(CONFD) quagga
	./setup.sh

quagga:
	if [ ! -f $(CONFD_DIR)/examples.confspec/demo/quagga/db/aaa_init.xml ]; then \
		echo "** Need to build the quagga example first"; \
		echo "** cd $(CONFD_DIR)/examples.confspec/demo/quagga/; make all"; \
		exit 1; \
        fi		

restart: stop clean all start

## This is required for all target to work, run just once
initial: $(CONFDC)
	cd $(CONFD_DIR)/examples.confspec/demo/quagga/; $(MAKE) clean all

$(CONFD):
	@echo 'Where is ConfD installed? Set $$CONFD_DIR to point it out!'
	@echo ''
	@exit 1

$(CONFDC):
	@echo 'Where is ConfD installed? Set $$CONFD_DIR to point it out!'
	@echo ''
	@exit 1

start:
	./start.sh

stop:
	./stop.sh; \
	killall quagga-proxy || true


clean:
	rm -rf `echo $(H)`

allclean:	clean
	cd $(CONFD_DIR)/examples.confspec/demo/quagga/; \
	$(MAKE) clean

######################################################################
##
## Start individual routers
##
start-west:
	./start.sh edgerouter-west
start-north:
	./start.sh edgerouter-north
start-east:
	./start.sh edgerouter-east

######################################################################
##
## Stop individual routers
##
istop:
	env CONFD_IPC_PORT=$$(($(IPC_BASE) + $(COUNT))) \
		$(CONFD_DIR)/bin/confd --stop 

stop-west:
	@$(MAKE) COUNT=0 istop

stop-north:
	@$(MAKE) COUNT=1 istop

stop-east:
	@$(MAKE) COUNT=2 istop

######################################################################
icli:
	$(CONFD_DIR)/bin/confd_cli --port  $$(($(IPC_BASE) + $(COUNT))) \
                         --user=admin --groups=admin \
			--interactive || echo Exit
cli:	cli-west

cli-west:
	@$(MAKE) COUNT=0 icli
cli-north:
	@$(MAKE) COUNT=1 icli
cli-east:
	@$(MAKE) COUNT=2 icli

######################################################################
istatus:
	env CONFD_IPC_PORT=$$(($(IPC_BASE) + $(COUNT))) \
		$(CONFD_DIR)/bin/confd --status  2> /dev/null || true



status-west:
	@$(MAKE) COUNT=0 istatus 

status-north:
	@$(MAKE) COUNT=1 istatus 

status-east:
	@$(MAKE) COUNT=2 istatus 


status:
	@west=`$(MAKE) COUNT=0 istatus | grep started`; \
	north=`$(MAKE) COUNT=1 istatus | grep started`; \
	east=`$(MAKE) COUNT=2 istatus | grep started`; \
	echo west $${west}; \
	echo north $${north}; \
	echo east $${east}; exit 0; 


######################################################################
i_nc:
	$(CONFD_DIR)/bin/netconf-console-tcp \
            --port $$(($(NC_TCP_BASE) + $(COUNT))) \
            --get-config --xpath /system/vpn/ipsec	

nc:	nc-west

nc-west:
	@$(MAKE) COUNT=0 i_nc

nc-north:
	@$(MAKE) COUNT=1 i_nc

nc-east:
	@$(MAKE) COUNT=2 i_nc
