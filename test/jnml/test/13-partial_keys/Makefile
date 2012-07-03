######################################################################
# JAVA NETCONF MANAGER LIBRARY Test
# (C) 2007 Tail-f Systems
#
######################################################################

include ../include.mk


JAVA=java -Xmx200m

######################################################################

all: check simple.fxs SIMPLE.jar test.class TestCase.class \
     $(CDB_DIR) aaa_cdb.fxs ssh-keydir

clean:
	rm -rf  simple.cs *.xso *.fxs $(CDB_DIR) ssh-keydir *.class *.log simple simple_classes SIMPLE.jar

start: test_cases

test:	all test_cases

stop:
	$(CONFD) --stop || true


######################################################################

test.class : test.java
	$(JAVAC) -classpath $(CLASSPATH):SIMPLE.jar  $<

TestCase.class : TestCase.java test.java
	$(JAVAC) -classpath $(CLASSPATH)  $<


######################################################################

aaa_cdb.fxs:
	cp -p $(CONFD_DIR)/etc/confd/aaa_cdb.fxs .

ssh-keydir:
	ln -s $(CONFD_DIR)/etc/confd/ssh ssh-keydir


######################################################################

%.cs:   %.yang
	$(PYANG) -f cs -o $*.cs $*.yang

simple.xso: simple.cs
	$(CONFDC) --confspec-compile simple.cs

simple.fxs: simple.xso
	$(CONFDC) --confspec-link -o $@  $<	

######################################################################

SIMPLE.jar: simple.fxs
	rm -rf simple || true
	rm -rf SIMPLE.jar
	mkdir simple
	$(CONFDC) --confm-emit-java simple --java-class-naming 2 simple.fxs
	$(JAVAC) -classpath $(CLASSPATH) simple/*.java
	# Creating JAR file SIMPLE.jar
	jar cfv SIMPLE.jar simple/*.class

######################################################################
$(CDB_DIR):
	-mkdir -p $(CDB_DIR)
	cp *_init.xml $(CDB_DIR)


confd_start:
	$(CONFD) -c confd.conf

######################################################################
# Test Cases
#
test_cases:   t1

# 
t1: 	stop	
	#----- Test 1 -----
	$(CONFD) -c confd.conf
	$(JAVA) -classpath $(CLASSPATH) test
	# OK

.PRECIOUS:	*.yang
