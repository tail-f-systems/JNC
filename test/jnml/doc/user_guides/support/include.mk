XML_SOURCES :=  $(wildcard *.xml)
HTML_OUT  := $(XML_SOURCES:%.xml=../output/html/%.html)
PDF_OUT   := $(XML_SOURCES:%.xml=../output/pdf/%.pdf)

../output/html/%.html:	%.xml
	set -x;\
	awk -f ../support/htmlquote.awk $<  > foo.out0; \
	title=`cat ./title`;\
	cat ../support/HEAD | sed "s/TITLE/$${title}/" > HEAD.out;\
	cat HEAD.out  foo.out0 ../support/TAIL > foo.out;\
	xsltproc --nonet ../support/ttguide.xsl foo.out  > $@;\
	mkdir -p $(dir $@)pics;
	cp -p ../pics/*.png ../pics/*.jpg $(dir $@)pics 2> /dev/null || true

../output/pdf/%.pdf:	%.xml
	title=`cat ./title`;\
	cat ../support/HEAD | sed "s/TITLE/$${title}/" > HEAD.out;\
	cat HEAD.out $< ../support/TAIL > foo.out;\
	env FOP_OPTS="-Xmx1024m -Djava.awt.headless=true" fop -xsl ../support/userguide-fo.xsl -xml foo.out -pdf $@
