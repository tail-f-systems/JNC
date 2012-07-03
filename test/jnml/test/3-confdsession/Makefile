######################################################################
# JAVA NETCONF MANAGER LIBRARY Test
# (C) 2007 Tail-f Systems
#
######################################################################

include ../include.mk


######################################################################

all: check test.class TestCase.class \
     $(CDB_DIR) aaa_cdb.fxs simple.fxs ssh-keydir

clean:
	rm -rf *.xso *.fxs $(CDB_DIR) ssh-keydir *.class *.log

start: test_cases

test:	all test_cases

stop:
	$(CONFD) --stop || true


######################################################################

test.class : test.java $(JARFILE)
	$(JAVAC) -classpath $(CLASSPATH)  $<

TestCase.class : TestCase.java test.java
	$(JAVAC) -classpath $(CLASSPATH)  $<


######################################################################

aaa_cdb.fxs:
	cp -p $(CONFD_DIR)/etc/confd/aaa_cdb.fxs .

ssh-keydir:
	ln -s $(CONFD_DIR)/etc/confd/ssh ssh-keydir


######################################################################

simple.xso: simple.cs
	$(CONFDC) -c simple.cs

simple.fxs: simple.xso
	$(CONFDC) -l -o $@ -u  'http://tail-f.com/ns/simple/1.0' $<	

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
	$(JAVA) -classpath $(CLASSPATH) test
	# OK

