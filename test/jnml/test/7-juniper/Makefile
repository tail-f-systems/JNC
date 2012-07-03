######################################################################
# JAVA NETCONF MANAGER LIBRARY Test
# (C) 2007 Tail-f Systems
#
######################################################################

include ../include.mk


######################################################################

all: check test.class TestCase.class

clean:
	rm -rf *.class

start: test_cases

test:	all test_cases

stop:
	$(CONFD) --stop || true


######################################################################

test.class : test.java 
	$(JAVAC) -classpath $(CLASSPATH)  $<


######################################################################
# Test Cases
#
test_cases:   t1

# 
t1: 	stop	
	#----- Test 1 -----
	$(JAVA) -classpath $(CLASSPATH) test
	# OK

