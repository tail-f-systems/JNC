###-*-makefile-*-   ; force emacs to enter makefile-mode
PS2PDF		= ps2pdf -
PDFPAGES  := $(MANPAGES:%.$(MANSECTION)=%.$(MANSECTION).pdf)
