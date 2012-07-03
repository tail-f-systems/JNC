# Where is ConfD installed? Make sure CONFD_DIR points it out!
CONFD_DIR?=../../trunk/confd_dir
CONFD=$(CONFD_DIR)/bin/confd
CONFDC=$(CONFD_DIR)/bin/confdc
PYANG=/home/jocke/bin/pyang
CLASSPATH=.:build/classes:../build/jar/NML.jar:../build/jar/INM.jar:../build/jar/ConfD.jar:../ganymed/ganymed-ssh2-build251beta1.jar
JAVAC=javac
JAVA=java
JAVA_FLAGS=-enableassertions
CDB_DIR=confd-cdb
AAA_URI="http://tail-f.com/ns/aaa/1.1"
CONFD_HOST=127.0.0.1

usage:
	@echo "make HostsTest  - Build hosts aware config trees"
	@echo "make QuaggaTest - Build quagga aware config trees"
	@echo "make SimpleGet  - Get schema aware config trees using NETCONF"
	@echo "make start      - Start the ConfD daemon"
	@echo "make stop       - Stop the ConfD daemon"
	@echo "make check      - Check the confspec and yang files"
	@echo "make query      - Issue a netconf-console query"

HostsTest: classes HostsTest.class
	$(JAVA) $(JAVA_FLAGS) -classpath $(CLASSPATH) HostsTest

QuaggaTest: classes QuaggaTest.class
	$(JAVA) $(JAVA_FLAGS) -classpath $(CLASSPATH) QuaggaTest

SimpleGet: classes SimpleGet.class
	$(JAVA) $(JAVA_FLAGS) -classpath $(CLASSPATH) SimpleGet

start: stop setup
	$(CONFD) -i -c confd.conf

stop:
	$(CONFD) --stop || true

check: hosts.yang.ok hosts.fxs quagga.fxs

query:
	$(CONFD_DIR)/bin/netconf-console --host $(CONFD_HOST) --get

# Helper targets

classes:
	ant

setup: cdb hosts.fxs quagga.fxs aaa_cdb.fxs $(CDB_DIR)/aaa_init.xml \
	$(CDB_DIR)/hosts.xml $(CDB_DIR)/quagga.xml ssh-keydir

cdb:
	if [ ! -d $(CDB_DIR) ]; then \
		mkdir $(CDB_DIR); \
	fi

hosts.fxs: hosts.xso
	$(CONFDC) -l -o hosts.fxs hosts.xso

hosts.xso: hosts.cs
	$(CONFDC) -c hosts.cs

quagga.fxs: quagga.xso ipsec.xso
	$(CONFDC) -l -o quagga.fxs quagga.xso ipsec.xso

quagga.xso: quagga.cs
	$(CONFDC) -c quagga.cs

ipsec.xso: ipsec.cs
	$(CONFDC) -c ipsec.cs

aaa_cdb.fxs: aaa.xso
	$(CONFD_DIR)/bin/confdc  -l -o $@ -u $(AAA_URI) $<

aaa.xso: $(CONFD_DIR)/src/confd/aaa/aaa.cs
	$(CONFDC) -c $<

$(CDB_DIR)/aaa_init.xml: aaa_init.xml
	cp aaa_init.xml $(CDB_DIR)

$(CDB_DIR)/hosts.xml: hosts.xml
	cp hosts.xml $(CDB_DIR)

$(CDB_DIR)/quagga.xml: quagga.xml
	cp quagga.xml $(CDB_DIR)

ssh-keydir:
	ln -s $(CONFD_DIR)/etc/confd/ssh $@

%.class: %.java
	$(JAVAC) $(JAVAC_FLAGS) -classpath $(CLASSPATH) $<

hosts.yang.ok: hosts.yang
	pyang hosts.yang
	touch hosts.yang.ok

clean:
	ant clean
	rm -fr $(CDB_DIR) ssh-keydir
	-rm -f hosts devel.log confderr.log.* *.log *.db netconf/
	-rm -rf *.ok *.class *.fxs *.xso || true
