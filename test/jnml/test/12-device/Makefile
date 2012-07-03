######################################################################
# JAVA NETCONF MANAGER LIBRARY Test
# (C) 2007 Tail-f Systems
#
######################################################################


include $(CONFD_DIR)/src/confd/build/include.mk
include ../include.mk

CFLAGS  += $(EXPAT_INC)
LIBS    += $(EXPAT_LIB)

JAVA=java -Xmx200m

######################################################################
c:
	echo $(CLASSPATH)

all: check \
     $(CDB_DIR) notif.fxs simple.fxs msp.fxs notif.fxs ssh-keydir \
	SIMPLE.jar MSP.jar NOTIF.jar test.class TestCase.class \
	notifier_builtin_replay_store


notifier_builtin_replay_store: notifier_builtin_replay_store.o notif.h
	$(CC) notifier_builtin_replay_store.o $(LIBS) $(CFLAGS) -o $@

notifier_builtin_replay_store.o: notifier_builtin_replay_store.c notif.h

%.h: %.fxs
	$(CONFDC) --emit-h $*.h $<


clean:
	rm -rf *.h *.fxs $(CDB_DIR) ssh-keydir *.class *.log simple msp \
	*.xso interface.* global.data notif notifier_builtin_replay_store *.o \
	*.jar


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


NOTIF.jar: notif.fxs
	rm -rf notif || true
	rm -rf NOTIF.jar
	mkdir notif
	$(CONFDC) --confm-emit-java notif --java-class-naming 2 notif.fxs
	$(JAVAC) -classpath $(CLASSPATH) notif/*.java
	jar cfv NOTIF.jar notif/*.class


######################################################################


simple.fxs: simple.yang
	$(CONFDC) -c  $<	

msp.fxs:	msp.cs
	$(CONFDC) --confspec-compile msp.cs
	$(CONFDC) --confspec-link msp.xso

######################################################################
$(CDB_DIR):
	-mkdir -p $(CDB_DIR)
	cp *_init.xml $(CDB_DIR)


start:
	$(CONFD) -c confd.conf --addloadpath $(CONFD_DIR)/etc/confd
	LD_LIBRARY_PATH=$(CONFD_SO) ./notifier_builtin_replay_store  -o -s 1&


cstart:
	$(CONFD) -c confd.conf --addloadpath $(CONFD_DIR)/etc/confd


######################################################################
# Test Cases
#
test_cases:   stop start t1

# 
t1: 	
	$(JAVA) -classpath $(CLASSPATH) test


.PRECIOUS:	*.yang
