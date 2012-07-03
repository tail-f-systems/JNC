 include ../include.mk

all: 
	cd $(CONFM_DIR)/examples/dhcp/confd; \
	  CONFD_DIR=$(CONFD_DIR) CONFM_DIR=$(CONFM_DIR) $(MAKE) stop all start 
	cd $(CONFM_DIR)/examples/dhcp; \
	  CONFM_DIR=$(CONFM_DIR) CONFD_DIR=$(CONFD_DIR) ant generate

test:	all
	cd $(CONFM_DIR)/examples/dhcp; \
          data=`ant  run`; \
          echo $$data \
            | grep ALLTESTOK; ret=$$?; \
	  cd confd; CONFM_DIR=$(CONFM_DIR) CONFD_DIR=$(CONFD_DIR) $(MAKE) stop; echo XXX $$data XXX; exit $$ret

clean:
	cd $(CONFM_DIR)/examples/dhcp/confd; \
	  CONFM_DIR=$(CONFM_DIR) CONFD_DIR=$(CONFD_DIR) $(MAKE) clean

