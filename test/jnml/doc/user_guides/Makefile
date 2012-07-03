
include ../../vsn.mk



INMDIRS= support \
	inm_intro \
	classes \
	config_elems \
	netconf_sessions \
	netconf_extensions \
	notifications 



CONFMDIRS = $(INMDIRS) \
	data_modeling \
	yang \
	confm_intro \
	confm_dhcp \
	generating_classes \
	confm_hl_features



all: 
	$(MAKE) inm
	$(MAKE) dclean
	$(MAKE) confm

subdirs:
	@set -e ; \
	  for d in echo $(SUBDIRS) ; do \
	    if [ -f $$d/Makefile ]; then \
	      ( cd $$d && $(MAKE) $${target}) || exit 1 ; \
	    fi ; \
	  done


## conditional m3 compilations of a lot of files


inm:
	@set -e ; \
	  for d in echo $(INMDIRS) inm_book ; do \
	    if [ -f $$d/Makefile ]; then \
	      ( cd $$d && env INM=true $(MAKE) all) || exit 1 ; \
	    fi ; \
	  done

confm:
	@set -e ; \
	  for d in echo $(CONFMDIRS) confm_book ; do \
	    if [ -f $$d/Makefile ]; then \
	      ( cd $$d && env CONFM=true $(MAKE) all) || exit 1 ; \
	    fi ; \
	  done


dclean:
	env target=clean \
            SUBDIRS="$(INMDIRS) $(CONFMDIRS)" \
            $(MAKE) subdirs

clean: dclean
	env target=clean \
            SUBDIRS="inm_book confm_book" \
            $(MAKE) subdirs




