


CONFD= $(CONFD_DIR)/bin/confd
CONFDC= $(CONFD_DIR)/bin/confdc
CONFMC= $(CONFM_DIR)/bin/confdc
CDB_DIR = ./confd-cdb

JAVA=java -Xmx200m
JAVAC=javac


CLASSPATH=$(CONFM_DIR)/jar/INM.jar:$(CONFM_DIR)/jar/ConfM.jar:$(CONFM_DIR)/jar/ganymed/ganymed-ssh2-build251beta1.jar:.

P = movik/fxs


######################################################################

all:  $(CDB_DIR) movik-system.fxs movik-cae-node.fxs movik-cae-ethernet.fxs \
	$(P)/movik-system.fxs $(P)/movik-cae-node.fxs $(P)/movik-cae-ethernet.fxs \
	ssh-keydir \
	sys_java cae_java \
	SYS.jar CAE.jar test.class TestCase.class

clean:
	rm -rf *.xso *.fxs *.jar $(CDB_DIR) ssh-keydir *.class *.log movik


test:	all test_cases

stop:
	$(CONFD) --stop || true


######################################################################

test.class : test.java
	$(JAVAC) -classpath $(CLASSPATH)  $<

TestCase.class : TestCase.java test.java
	$(JAVAC) -classpath $(CLASSPATH)  $<


######################################################################


ssh-keydir:
	ln -s $(CONFD_DIR)/etc/confd/ssh ssh-keydir


sys_java:	$(P)/movik-system.fxs
	$(CONFMC) --confm-emit-java movik/sys --java-class-naming 2 --java-package movik.sys -f $(P)/movik-cae-node.fxs -- $(P)/movik-system.fxs

cae_java:     $(P)/movik-cae-node-types.xso
	$(CONFMC) -l --java-package movik.cae -o $(P)/movik-cae-node-types.fxs  -- $(P)/movik-cae-node-types.xso
	$(CONFMC) --confm-emit-java movik/cae --java-class-naming 2 --java-package movik.cae  -- $(P)/movik-cae-node-types.fxs


SYS.jar: $(P)/movik-system.fxs
	$(JAVAC) -classpath $(CLASSPATH) movik/sys/*.java
	jar cfv SYS.jar movik/sys/*.class

CAE.jar: 
	$(JAVAC) -classpath $(CLASSPATH):SYS.jar movik/cae/*.java
	jar cfv CAE.jar movik/cae/*.class



## use confdc from confm that supports --java-package
$(P)/%.xso:	%.cs
	$(CONFMC) -c $< -o $@

$(P)/movik-system.fxs:	$(P)/movik-system.xso $(P)/movik-system-types.xso
	$(CONFMC) -l --java-package movik.sys -o $@ $(P)/movik-system.xso $(P)/movik-system-types.xso

$(P)/movik-cae-node.fxs:	$(P)/movik-cae-node.xso $(P)/movik-cae-node-types.xso
	$(CONFMC) -l --java-package movik.cae -o $@ -f $(P)/movik-system.fxs -- $(P)/movik-cae-node.xso $(P)/movik-cae-node-types.xso

$(P)/movik-cae-ethernet.fxs:	$(P)/movik-cae-ethernet.xso
	$(CONFMC) -l --java-package movik.cae -o $@ -f $(P)/movik-system.fxs -- $(P)/movik-cae-ethernet.xso


## recompile .cs files for confd use confd confdc
%.xso:	%.cs
	$(CONFDC) -c $< -o $@

movik-system.fxs:	movik-system.xso movik-system-types.xso
	$(CONFDC) -l  -o $@ movik-system.xso movik-system-types.xso

movik-cae-node.fxs:	movik-cae-node.xso movik-cae-node-types.xso
	$(CONFDC) -l  -o $@ -f movik-system.fxs  -- movik-cae-node.xso movik-cae-node-types.xso

movik-cae-ethernet.fxs:	movik-cae-ethernet.xso
	$(CONFDC) -l  -o $@ -f movik-system.fxs  -- movik-cae-ethernet.xso


######################################################################
$(CDB_DIR):
	mkdir -p $(P)
	mkdir -p movik/sys
	mkdir -p movik/cae
	-mkdir -p $(CDB_DIR)
	cp *_init.xml $(CDB_DIR)


start:
	$(CONFD) -c confd.conf --addloadpath $(CONFD_DIR)/etc/confd

######################################################################
# Test Cases
#
test_cases:   stop start t1

# 
t1: 	
	$(JAVA) -classpath $(CLASSPATH) test

