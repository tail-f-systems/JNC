######################################################################
# JAVA INSTANT NETCONF MANAGER Test
# (C) 2007 Tail-f Systems
#
######################################################################

# Where is ConfD installed? Make sure CONFD_DIR points it out
CONFD_DIR ?= ../../../confd_dir

# Include standard ConfD build definitions and rules
include $(CONFD_DIR)/src/confd/build/include.mk

# In case CONFD_DIR is not set (correctly), this rule will trigger
$(CONFD_DIR)/src/confd/build/include.mk:
	@echo 'Where is ConfD installed? Set $$CONFD_DIR to point it out!'
	@echo ''
	@exit 1

include ../include.mk

######################################################################
all: simple.fxs simple.h actions multi.fxs \
        types.fxs SIMPLE.jar MULTI.jar MISC.jar TestCase.class test.class \
	test_actions.class test_types.class test_misc.class $(CDB_DIR) \
	aaa_cdb.fxs ssh-keydir

actions: actions.o
	$(CC) -o actions actions.o $(LIBS)

actions.o: simple.h actions.c
	$(CC) -I $(CONFD_DIR)/include -c actions.c

bug:	TYPES.jar

clean:
	rm -rf *.xso *.fxs *.class *.log simple types multi misc \
	simple_classes *.data *.jar $(CDB_DIR) actions simple.h actions.o \
	ssh-keydir nctrace.txt

start: test_cases

test: all test_cases

stop:
	$(CONFD) --stop || true

cli:
	$(CONFD_DIR)/bin/confd_cli --user=admin --groups=admin --interactive --host 127.0.0.1 || echo Exit

######################################################################

test.class : test.java 
	$(JAVAC) -classpath $(CLASSPATH)  $<

test_actions.class : test_actions.java 
	$(JAVAC) -classpath $(CLASSPATH)  $<

test_types.class : test_types.java
	$(JAVAC) -classpath $(CLASSPATH)  $<

test_misc.class : test_misc.java MISC.jar
	$(JAVAC) -classpath $(CLASSPATH):MISC.jar  $<

TestCase.class : TestCase.java test.java
	$(JAVAC) -classpath $(CLASSPATH)  $<

######################################################################

#aaa_cdb.fxs:
#	cp -p $(CONFD_DIR)/etc/confd/aaa_cdb.fxs .

#ssh-keydir:
#	ln -s $(CONFD_DIR)/etc/confd/ssh ssh-keydir

######################################################################

%.xso: %.cs
	$(CONFDC) --confspec-compile $*.cs

simple.fxs: simple.xso rt2852.xso rt2870.xso rt2905.xso rt3363.xso rt3254.xso rt3556.xso rt3831a.xso rt3831b.xso action_setup.xso
	$(CONFDC) --confspec-link -o $@ -u 'http://tail-f.com/ns/simple/1.0' simple.xso rt2852.xso rt2870.xso rt2905.xso rt3363.xso rt3254.xso rt3556.xso rt3831a.xso rt3831b.xso action_setup.xso

mountpoint.fxs:	mountpoint.xso st.fxs
	$(CONFDC) -f st.fxs --confspec-link mountpoint.xso

%.fxs:	%.xso
	$(CONFDC) --confspec-link $*.xso

%.fxs:	%.yang
	$(CONFDC) -c $*.yang

######################################################################
$(CDB_DIR):
	-mkdir -p $(CDB_DIR)
	cp *_init.xml rt3538.xml multi.xml $(CDB_DIR)

######################################################################

SIMPLE.jar: simple.fxs
	rm -rf simple || true
	rm -rf SIMPLE.jar
	mkdir simple
	$(CONFDC) --confm-emit-java simple --case-insensitive-naming --java-class-naming 3 simple.fxs
	$(JAVAC) -classpath $(CLASSPATH) simple/*.java
	# Creating JAR file SIMPLE.jar
	jar cfv SIMPLE.jar simple/*.class


TYPES.jar: types.fxs
	rm -rf types || true
	rm -rf TYPES.jar
	mkdir types
	$(CONFDC) --confm-emit-java types --java-class-naming 2 types.fxs
	$(JAVAC) -classpath $(CLASSPATH) types/*.java
	# Creating JAR file TYPES.jar
	jar cfv TYPES.jar types/*.class

MULTI.jar:	multi.fxs
	rm -rf multi || true
	rm -rf MULTI.jar
	mkdir multi
	$(CONFDC) --confm-emit-java multi --java-class-naming 2 multi.fxs
	$(JAVAC) -classpath $(CLASSPATH) multi/*.java
	# Creating JAR file MULTI.jar
	jar cfv MULTI.jar multi/*.class

MISC.jar: mountpoint.fxs mountee.fxs augment.fxs st.fxs
	rm -rf misc || true
	rm -rf MISC.jar
	mkdir misc
	mkdir misc/mountpoint
	$(CONFDC) -f mountee.fxs augment.fxs st.fxs --confm-emit-java misc/mountpoint --java-class-naming 2 mountpoint.fxs
	$(JAVAC) -classpath $(CLASSPATH) misc/mountpoint/*.java
	# Creating JAR file MISC.jar
	(cd misc; jar cfv ../MISC.jar mountpoint/*.class)

s:
	$(JAVAC) -classpath $(CLASSPATH) simple/*.java	

######################################################################
# Test Cases
#
test_cases:  t1 t2 t3 t4

# 
t1:
	#----- Test 1 -----
	# TEST ConfM data types
	#
	$(JAVA) -classpath $(CLASSPATH) test_types
	# OK

t2: stop
	#----- Test 2 -----
	# Testing ConfM
	$(CONFD) -c confd.conf
	$(JAVA) -classpath $(CLASSPATH) test
	$(MAKE) stop
	# OK

t3: stop
	sleep 4
	#----- Test 3 -----
	# Testing ConfM actions
	$(CONFD) -c confd.conf
	./actions &
	sleep 3
	$(JAVA) -classpath $(CLASSPATH) test_actions
	$(MAKE) stop
	# OK

t4: stop
	sleep 4
	#----- Test 4 -----
	# Testing misc stuff
	$(CONFD) -c confd.conf
	$(JAVA) -classpath $(CLASSPATH):MISC.jar test_misc
	# OK

ttt:
	$(JAVA) -classpath $(CLASSPATH) test_actions
