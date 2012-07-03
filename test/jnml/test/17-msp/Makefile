######################################################################
# JAVA NETCONF MANAGER LIBRARY Test
# (C) 2007 Tail-f Systems
#
######################################################################

include ../include.mk


JAVA=java -Xmx200m

######################################################################

all: check \
     $(CDB_DIR) msp.fxs ssh-keydir \
	MSP.jar test.class TestCase.class


clean:
	rm -rf *.xso *.fxs *.jar $(CDB_DIR) ssh-keydir *.class *.log simple wdmpon


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


MSP.jar: msp.fxs
	rm -rf wdmpon || true
	rm -rf MSP.jar
	mkdir wdmpon
	$(CONFDC) --confm-emit-java wdmpon --java-class-naming 5 msp.fxs
	$(JAVAC) -classpath $(CLASSPATH) wdmpon/*.java
	jar cfv MSP.jar wdmpon/*.class


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
