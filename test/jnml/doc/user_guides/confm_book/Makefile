include ../support/include.mk

F =     ../confm_intro/confm_intro.xml \
	../data_modeling/data_modeling.xml \
	../yang/yang.xml \
	../generating_classes/generating_classes.xml \
	../netconf_sessions/netconf_sessions.xml \
	../confm_dhcp/using_confm.xml \
	../confm_hl_features/high_level_features.xml \
	../classes/classes.xml \
	../config_elems/config_elems.xml \
	../netconf_extensions/netconf_extensions.xml \
	../notifications/notifications.xml

PDF_OUT = ../output/pdf/confm.pdf
HTML_OUT = ../output/html/confm.html

all:	confm.xml $(PDF_OUT) $(HTML_OUT) mv

confm.xml:	$(F) Makefile
	echo > confm.xml;
	for i in $(F); do\
	   cat $$i >> confm.xml; \
	done
mv:
	vsn="`cat ../../../vsn.mk | grep CONFM | sed s/CONFMVSN=//`"; \
	mv ../output/pdf/confm.pdf  ../output/pdf/confm_user_guide-$${vsn}.pdf

clean: 
	rm -f $(PDF_OUT) $(HTML_OUT) *.out *.out0 *~ confm.xml 2> /dev/null
	rm -f ../output/pdf/confm_user_guide-*.pdf
