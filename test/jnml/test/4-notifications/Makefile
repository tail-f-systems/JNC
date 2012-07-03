######################################################################
# JAVA NETCONF MANAGER LIBRARY Test
# (C) 2007 Tail-f Systems
#
######################################################################

include ../include.mk
JAVA=java -Xmx200m



######################################################################

all: check test1.class TestCase.class \
     Notifier.class CmdReader.class \
     $(CDB_DIR) aaa_cdb.fxs notif.fxs ssh-keydir

clean:
	rm -rf *.xso *.fxs $(CDB_DIR) ssh-keydir *.class *.log tmp

a:
	echo $(CLASSPATH)

start: test_cases

test:	all test_cases

stop:
	$(CONFD) --stop || true


start_notifier: 
	$(JAVA) -classpath $(CLASSPATH) Notifier


start_confd:
	$(CONFD) -i -v -c confd.conf

start_test:
	$(JAVA) -classpath $(CLASSPATH) test1	


######################################################################

test1.class : test1.java 
	$(JAVAC) -classpath $(CLASSPATH)  $<

TestCase.class : TestCase.java test.java
	$(JAVAC) -classpath $(CLASSPATH)  $<

CmdReader.class : CmdReader.java 
	$(JAVAC) -classpath $(CLASSPATH)  $<

Notifier.class : Notifier.java 
	$(JAVAC) -classpath $(CLASSPATH)  $<


######################################################################

aaa_cdb.fxs:
	cp -p $(CONFD_DIR)/etc/confd/aaa_cdb.fxs .

ssh-keydir:
	ln -s $(CONFD_DIR)/etc/confd/ssh ssh-keydir


######################################################################

notif.xso: notif.cs
	$(CONFDC) -c notif.cs

notif.fxs: notif.xso
	$(CONFDC) -l -o $@ -u  'http://tail-f.com/ns/test/notif/1.0' $<	

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
	$(JAVA) -classpath $(CLASSPATH) Notifier s i d u y q &	
	$(JAVA) -classpath $(CLASSPATH) test1 >t1.log
	sleep 15
	$(CONFD) --stop			
	cat t1.log
	grep linkDown t1.log > tmp
	test -s tmp	
	grep linkUp t1.log > tmp
	test -s tmp	
	# OK


