######################################################################
# JAVA NETCONF MANAGER LIBRARY Test
# (C) 2007 Tail-f Systems
#
######################################################################

include ../../../vsn.mk

CONFM_DIR ?= $(shell (cd ../../confm_dir; pwd))
CONFD_DIR = $(shell (cd ../../../confd_dir; pwd))

check:

JARS = $(CONFD_DIR)/java/jar
CLASSPATH = $(JARS)/conf-api.jar:$(JARS)/log4j-1.2.14.jar:$(JARS)/aspectjrt-1.6.5.jar:$(CONFM_DIR)/jar/ganymed/ganymed-ssh2-build251beta1.jar:$(CONFM_DIR)/jar/INM.jar:$(CONFM_DIR)/jar/ConfM.jar:.

CDB_DIR= ./confd-cdb
CONFD= $(CONFD_DIR)/bin/confd
CONFDC= $(CONFD_DIR)/bin/confdc
CONFMC= $(CONFM_DIR)/bin/confdc
JAVAC=javac
JAVA=java
PYANG=$(CONFM_DIR)/bin/pyang
