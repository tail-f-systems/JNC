HIQ_PICS=$(wildcard *.png)
LOQ_PICS=$(HIQ_PICS:%.png=%.jpg)

.SUFFIXES: .png

all: loqpics

loqpics: $(HIQ_PICS:%.png=%.jpg)

%.jpg: %.png
	convert -quality 75% $< $@
