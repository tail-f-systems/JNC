ifeq ($(SUBDIRS),)
SUBDIRS	= c_src doc src
endif

ANT=ant

all:	$(LOCALALL) 
	@set -e ; \
	  for d in $(SUBDIRS) ; do \
	    if [ -f $$d/Makefile ]; then \
	      ( cd $$d && $(MAKE) all) || exit 1 ; \
	    elif [ -f $$d/build.xml ]; then \
              ( cd $$d && $(ANT) $@ ) || exit 1 ; \
	    fi ; \
	  done

clean: $(LOCALCLEAN)
	@set -e ; \
	  for d in $(SUBDIRS) ; do \
	    if [ -f $$d/Makefile ]; then \
	      ( cd $$d && $(MAKE) $@ ) || exit 1 ; \
	    elif [ -f $$d/build.xml ]; then \
              ( cd $$d && $(ANT) $@ ) || exit 1 ; \
	    fi ; \
	  done

test:	$(LOCALTEST)
	@set -e ; \
	  for d in $(SUBDIRS) ; do \
	    if [ -f $$d/Makefile ]; then \
	      ( cd $$d && $(MAKE) $@ ) || exit 1 ; \
	    elif [ -f $$d/build.xml ]; then \
              ( cd $$d && $(ANT) $@ ) || exit 1 ; \
            fi ; \
	  done
