include ../support/include.mk

F =     ../inm_intro/intro.xml \
	../classes/classes.xml \
	../config_elems/config_elems.xml \
	../netconf_sessions/netconf_sessions.xml \
	../netconf_extensions/netconf_extensions.xml \
	../notifications/notifications.xml

PDF_OUT = ../output/pdf/inm.pdf
HTML_OUT = ../output/html/inm.html

all:	inm.xml $(PDF_OUT) $(HTML_OUT) mv

inm.xml:	$(F) Makefile
	echo > inm.xml;
	for i in $(F); do\
	   cat $$i >> inm.xml; \
	done
mv:
	vsn="`cat ../../../vsn.mk | grep INM | sed s/INMVSN=//`"; \
	mv ../output/pdf/inm.pdf  ../output/pdf/inm_user_guide-$${vsn}.pdf

clean: 
	rm -f $(PDF_OUT) $(HTML_OUT) *.out *.out0 *~ inm.xml 2> /dev/null
	rm -f ../output/pdf/inm_user_guide-*.pdf
