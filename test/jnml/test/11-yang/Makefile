######################################################################
# JAVA NETCONF MANAGER LIBRARY Test
# (C) 2007 Tail-f Systems
#
######################################################################

include ../include.mk

JAVA=java -Xmx200m

######################################################################

all: check test.class TestCase.class $(CDB_DIR) aaa_cdb.fxs ietf-inet-types.fxs ietf-yang-types.fxs simple.fxs ssh-keydir ietf.jar types.jar

clean:
	rm -rf simple.cs *.xso *.fxs $(CDB_DIR) ssh-keydir *.class *.log types.jar ietf.jar ietf types

start: test_cases

test:	all test_cases

stop:
	$(CONFD) --stop || true

#####################################################################
#

ietf.jar: ietf-yang-types.fxs ietf-inet-types.fxs
	rm -rf ietf || true
	rm -rf ietf.jar
	mkdir ietf
	mkdir ietf/yang
	mkdir ietf/inet
	$(CONFDC) --confm-emit-java ietf/yang --java-package yang --java-class-naming 3 ietf-yang-types.fxs
	$(CONFDC) --confm-emit-java ietf/inet --java-package inet --java-class-naming 3 ietf-inet-types.fxs
	$(JAVAC) -classpath $(CLASSPATH) ietf/yang/*.java
	$(JAVAC) -classpath $(CLASSPATH) ietf/inet/*.java
	(cd ietf; jar cfv ../ietf.jar inet/*.class yang/*.class)

types.jar: ietf-inet-types.fxs ietf-yang-types.fxs ietf.jar types.fxs
	rm -rf types || true
	rm -rf types.jar
	mkdir types
	$(CONFDC) -f ietf-inet-types.fxs -f ietf-yang-types.fxs --confm-emit-java types --java-class-naming 3 types.fxs
	$(JAVAC) -classpath $(CLASSPATH):ietf.jar types/*.java
	(cd types; jar cfv types.jar *.class)

######################################################################

test.class : test.java ietf.jar types.jar 
	$(JAVAC) -classpath $(CLASSPATH):types.jar:ietf.jar $<

TestCase.class : TestCase.java test.java
	$(JAVAC) -classpath $(CLASSPATH)  $<

######################################################################

aaa_cdb.fxs:
	cp -p $(CONFD_DIR)/etc/confd/aaa_cdb.fxs .

ietf-inet-types.fxs:
	cp -p $(CONFD_DIR)/etc/confd/ietf-inet-types.fxs .

ietf-yang-types.fxs:
	cp -p $(CONFD_DIR)/etc/confd/ietf-yang-types.fxs .

ssh-keydir:
	ln -s $(CONFD_DIR)/etc/confd/ssh ssh-keydir

######################################################################

simple.fxs: simple.yang
	$(CONFDC) -c -o $@  $<	

types.fxs: types.yang
	$(CONFDC) -c -o $@  $<	

######################################################################
$(CDB_DIR):
	-mkdir -p $(CDB_DIR)
	cp *_init.xml $(CDB_DIR)

######################################################################
# Test Cases
#
test_cases:   t1

# 
t1: 	stop	
	#----- Test 1 -----
	$(CONFD) -c confd.conf
	$(JAVA) -classpath $(CLASSPATH):types.jar:ietf.jar test
	# OK

.PRECIOUS:	*.yang
