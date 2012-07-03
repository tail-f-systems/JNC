include ../support/include.mk

XML = intro.xml

all:	title $(XML) $(PDF_OUT)

clean: 
	rm -f $(PDF_OUT) $(XML) title *.out *.out0 *~ 2> /dev/null

%:      %.m3
	chmod a+w $@ 2> /dev/null || true
	../../../../bin/m3  < $< > $@ && chmod a-w $@
