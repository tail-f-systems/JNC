######################################################################
# JAVA NETCONF MANAGER LIBRARY Test
# (C) 2007 Tail-f Systems
#
######################################################################

include ../include.mk

######################################################################

test:	check build_and_run

all:	check build

build:	test.class TestCase.class
	@echo "Build complete"

build_and_run:	build run

run:	test_cases


######################################################################

test.class : test.java $(JARFILE)
	$(JAVAC) -classpath $(CLASSPATH)  $<

TestCase.class : TestCase.java test.java
	$(JAVAC) -classpath $(CLASSPATH)  $<


######################################################################
clean:	
	-rm -rf *.class || true


start:
	$(JAVA) -classpath $(CLASSPATH) Test


######################################################################
# Test Cases
#
test_cases:   t1

# 
t1:  
	#----- Test 1 -----
	$(JAVA) -classpath $(CLASSPATH) test
	# OK

