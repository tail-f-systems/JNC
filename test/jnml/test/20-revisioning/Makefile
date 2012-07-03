include ../include.mk

all: 
	cd $(CONFM_DIR)/examples/revisioning/confd; \
	  CONFM_DIR=$(CONFM_DIR) \
            CONFD_DIR=$(CONFD_DIR) $(MAKE) stop clean all start 
	cd $(CONFM_DIR)/examples/revisioning; \
	  CONFM_DIR=$(CONFM_DIR) \
            CONFD_DIR=$(CONFD_DIR) ant generate

test:	all
	cd $(CONFM_DIR)/examples/revisioning; \
          data=`ant  run`; \
          echo $$data \
            | grep ALLTESTOK; ret=$$?; \
	  cd confd; CONFM_DIR=$(CONFM_DIR) \
            CONFD_DIR=$(CONFD_DIR) $(MAKE) stop; echo XXX $$data XXX; exit $$ret

clean:
	cd $(CONFM_DIR)/examples/revisioning/confd; \
	  CONFM_DIR=$(CONFM_DIR) \
            CONFD_DIR=$(CONFD_DIR) $(MAKE) clean
