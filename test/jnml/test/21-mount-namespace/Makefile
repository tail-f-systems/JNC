include ../include.mk

all:	
	cd 1; CONFM_DIR=$(CONFM_DIR) \
            CONFD_DIR=$(CONFD_DIR)  $(MAKE) $@
	cd 2; CONFM_DIR=$(CONFM_DIR) \
            CONFD_DIR=$(CONFD_DIR)  $(MAKE) $@
	cd 3; CONFM_DIR=$(CONFM_DIR) \
            CONFD_DIR=$(CONFD_DIR)  $(MAKE) $@

test:	all	
	cd 1; CONFM_DIR=$(CONFM_DIR) \
            CONFD_DIR=$(CONFD_DIR)  $(MAKE) $@
	cd 2; CONFM_DIR=$(CONFM_DIR) \
            CONFD_DIR=$(CONFD_DIR)  $(MAKE) $@
	cd 3; CONFM_DIR=$(CONFM_DIR) \
            CONFD_DIR=$(CONFD_DIR)  $(MAKE) $@

clean:	
	cd 1; CONFM_DIR=$(CONFM_DIR) \
            CONFD_DIR=$(CONFD_DIR)  $(MAKE) $@
	cd 2; CONFM_DIR=$(CONFM_DIR) \
            CONFD_DIR=$(CONFD_DIR)  $(MAKE) $@
	cd 3;  CONFM_DIR=$(CONFM_DIR) \
            CONFD_DIR=$(CONFD_DIR)  $(MAKE) $@




