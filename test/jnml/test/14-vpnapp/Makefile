include ../include.mk

JAVA_VERSION = $(shell java -version 2>&1)
NON_SUPPORTED_JAVA_VERSION = "1\.4"

all:
	@if echo "$(JAVA_VERSION)" | grep -q "$(NON_SUPPORTED_JAVA_VERSION)" 2>&1; then \
	    echo "Skipping: not supported for JDK-1.4"; \
	else \
	    CONFM_DIR=$(CONFM_DIR) \
	    CONFD_DIR=$(CONFD_DIR) $(MAKE) start stop; \
	fi

start:
	cd $(CONFM_DIR)/examples/VpnApp && CONFM_DIR=$(CONFM_DIR) \
                                           CONFD_DIR=$(CONFD_DIR) ant prepareAll

stop:
	cd $(CONFM_DIR)/examples/VpnApp && CONFM_DIR=$(CONFM_DIR) \
                                           CONFD_DIR=$(CONFD_DIR) \
                                           ant unprepareAll

test:	all

clean:
	cd $(CONFM_DIR)/examples/VpnApp; CONFM_DIR=$(CONFM_DIR) \
                                         CONFD_DIR=$(CONFD_DIR) ant clean
