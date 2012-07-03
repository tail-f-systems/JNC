######################################################################
# JAVA NETCONF MANAGER LIBRARY Test
# (C) 2007 Tail-f Systems
#
######################################################################

include ../include.mk


JAVA=java -Xmx200m

######################################################################

all: check \
     $(CDB_DIR) simple.fxs msp.fxs ssh-keydir \
	SIMPLE.jar MSP.jar test.class TestCase.class


clean:
	rm -rf *.xso *.fxs *.jar $(CDB_DIR) ssh-keydir *.class *.log simple msp


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

SIMPLE.jar: simple.fxs
	rm -rf simple || true
	rm -rf SIMPLE.jar
	mkdir simple
	$(CONFDC) --confm-emit-java simple --java-class-naming 2 simple.fxs
	$(JAVAC) -classpath $(CLASSPATH) simple/*.java
	jar cfv SIMPLE.jar simple/*.class

MSP.jar: msp.fxs
	rm -rf msp || true
	rm -rf MSP.jar
	mkdir msp
	$(CONFDC) --confm-emit-java msp --java-class-naming 2 msp.fxs
	$(JAVAC) -classpath $(CLASSPATH) msp/*.java
	jar cfv MSP.jar msp/*.class

######################################################################


simple.fxs: simple.yang
	$(CONFDC) -c  $<	

msp.fxs:	msp.cs
	$(CONFDC) -c msp.cs
	$(CONFDC) -l msp.xso

######################################################################
$(CDB_DIR):
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
	# OK

.PRECIOUS:	*.yang
