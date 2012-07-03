######################################################################
# JAVA INSTANT NETCONF MANAGER Test
# (C) 2007 Tail-f Systems
#
######################################################################

include ../include.mk

######################################################################

all: check simple.fxs SIMPLE.jar TestCase.class test.class

clean:
	rm -rf *.xso *.fxs *.class *.log simple simple_classes SIMPLE.jar

start: test_cases

test:	all test_cases

######################################################################

test.class : test.java 
	$(JAVAC) -classpath $(CLASSPATH)  $<

TestCase.class : TestCase.java test.java
	$(JAVAC) -classpath $(CLASSPATH)  $<


######################################################################

simple.xso: simple.cs
	$(CONFDC) -c simple.cs

simple.fxs: simple.xso
	$(CONFDC) -l -o $@ -u  'http://tail-f.com/ns/simple/1.0' $<	

######################################################################

SIMPLE.jar: simple.fxs
	rm -rf simple || true
	rm -rf SIMPLE.jar
	mkdir simple
	$(CONFDC) --confm-emit-java simple simple.fxs
	$(JAVAC) -classpath $(CLASSPATH) simple/*.java
	# Creating JAR file SIMPLE.jar
	jar cfv SIMPLE.jar simple/*.class


######################################################################
# Test Cases
#
test_cases:  t1

# 
t1: 
	#----- Test 1 -----
	# Test read and write XML files
	$(JAVA) -classpath $(CLASSPATH) test
	# OK

