include ../support/include.mk

CONFDC=../../../../bin/confdc

XML = generating_classes.xml

all:	$(XML) $(PDF_OUT)

%:      %.m3
	chmod a+w $@ 2> /dev/null || true
	../../../../bin/m3  < $< > $@ && chmod a-w $@

dhcp: dhcp.fxs
	$(CONFDC) --confm-emit-java src/dhcp dhcp.fxs
	touch dhcp.ok

dhcp.fxs: dhcp.xso
	$(CONFDC) -l -o dhcp.fxs dhcp.xso

dhcp.xso: dhcp.cs
	$(CONFDC) -c dhcp.cs

clash: clash.fxs
	$(CONFDC) --confm-emit-java src/clash clash.fxs
	touch clash.ok

clash.fxs: clash.xso
	$(CONFDC) -l -o clash.fxs clash.xso

clash.xso: clash.cs
	$(CONFDC) -c clash.cs

clean: 
	rm -f $(PDF_OUT) $(XML) dhcp.ok clash.ok *.xso *.fxs *.out *.out0 *~ 2> /dev/null
