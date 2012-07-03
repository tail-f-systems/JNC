######################################################################
# JAVA NETCONF MANAGER LIBRARY Test
# (C) 2007 Tail-f Systems
#
######################################################################

include ../include.mk


JAVA=java -Xmx200m

######################################################################

all:

test:	smitest tc2test framework target community mpd notif vacm vacm2 \
	snmpv2_smi

smitest:
	-mkdir smi 2>/dev/null
	$(CONFDC) --confm-emit-java smi --java-package smi \
		--java-class-naming 5 $(CONFD_DIR)/etc/confd/snmp/smi.fxs
	$(JAVAC) -classpath $(CLASSPATH) smi/*.java


tc2test:
	-mkdir SNMPv2_TC 2>/dev/null
	$(CONFDC) --confm-emit-java SNMPv2_TC --java-package SNMPv2_TC \
		--java-class-naming 5 \
                $(CONFD_DIR)/etc/confd/snmp/SNMPv2-TC.fxs \
		-f $(CONFD_DIR)/etc/confd/snmp/smi.fxs 
	$(JAVAC) -classpath $(CLASSPATH) SNMPv2_TC/*.java

framework:
	-mkdir SNMP_FRAMEWORK_MIB 2>/dev/null
	$(CONFDC) --confm-emit-java SNMP_FRAMEWORK_MIB \
                  --java-package SNMP_FRAMEWORK_MIB \
		--java-class-naming 5 \
                $(CONFD_DIR)/etc/confd/snmp/SNMP-FRAMEWORK-MIB.fxs \
		-f $(CONFD_DIR)/etc/confd/snmp
	$(JAVAC) -classpath $(CLASSPATH):SNMP_FRAMEWORK_MIB SNMP_FRAMEWORK_MIB/*.java

target:
	-mkdir SNMP_TARGET_MIB 2>/dev/null
	$(CONFDC) --confm-emit-java SNMP_TARGET_MIB \
                  --java-package SNMP_TARGET_MIB \
		--java-class-naming 5 \
                $(CONFD_DIR)/etc/confd/snmp/SNMP-TARGET-MIB.fxs \
		-f $(CONFD_DIR)/etc/confd/snmp
	$(JAVAC) -classpath $(CLASSPATH):SNMP_TARGET_MIB SNMP_TARGET_MIB/*.java

community:
	-mkdir SNMP_COMMUNITY_MIB 2>/dev/null
	$(CONFDC) --confm-emit-java SNMP_COMMUNITY_MIB \
                  --java-package SNMP_COMMUNITY_MIB \
		--java-class-naming 5 \
                $(CONFD_DIR)/etc/confd/snmp/SNMP-COMMUNITY-MIB.fxs \
		-f $(CONFD_DIR)/etc/confd/snmp
	$(JAVAC) -classpath $(CLASSPATH):SNMP_COMMUNITY_MIB SNMP_COMMUNITY_MIB/*.java

mpd:
	-mkdir SNMP_MPD_MIB 2>/dev/null
	$(CONFDC) --confm-emit-java SNMP_MPD_MIB \
                  --java-package SNMP_MPD_MIB \
		--java-class-naming 5 \
                $(CONFD_DIR)/etc/confd/snmp/SNMP-MPD-MIB.fxs \
		-f $(CONFD_DIR)/etc/confd/snmp
	$(JAVAC) -classpath $(CLASSPATH) SNMP_MPD_MIB/*.java

notif:
	-mkdir SNMP_NOTIFICATION_MIB 2>/dev/null
	$(CONFDC) --confm-emit-java SNMP_NOTIFICATION_MIB \
                  --java-package SNMP_NOTIFICATION_MIB \
		--java-class-naming 5 \
                $(CONFD_DIR)/etc/confd/snmp/SNMP-NOTIFICATION-MIB.fxs \
		-f $(CONFD_DIR)/etc/confd/snmp
	$(JAVAC) -classpath $(CLASSPATH) SNMP_NOTIFICATION_MIB/*.java

vacm:
	-mkdir SNMP_USER_BASED_SM_MIB 2>/dev/null
	$(CONFDC) --confm-emit-java SNMP_USER_BASED_SM_MIB \
                  --java-package SNMP_USER_BASED_SM_MIB \
		--java-class-naming 5 \
                $(CONFD_DIR)/etc/confd/snmp/SNMP-USER-BASED-SM-MIB.fxs \
		-f $(CONFD_DIR)/etc/confd/snmp
	$(JAVAC) -classpath $(CLASSPATH) SNMP_USER_BASED_SM_MIB/*.java

vacm2:
	-mkdir SNMP_VIEW_BASED_ACM_MIB 2>/dev/null
	$(CONFDC) --confm-emit-java SNMP_VIEW_BASED_ACM_MIB \
                  --java-package SNMP_VIEW_BASED_ACM_MIB \
		--java-class-naming 5 \
                $(CONFD_DIR)/etc/confd/snmp/SNMP-VIEW-BASED-ACM-MIB.fxs \
		-f $(CONFD_DIR)/etc/confd/snmp
	$(JAVAC) -classpath $(CLASSPATH) SNMP_VIEW_BASED_ACM_MIB/*.java


snmpv2_smi:
	-mkdir SNMPv2_SMI  2>/dev/null
	-mkdir smi  2>/dev/null
	$(CONFDC) --confm-emit-java smi \
                  --java-package smi \
		--java-class-naming 2 \
                $(CONFD_DIR)/etc/confd/snmp/smi.fxs 

	$(CONFDC) --confm-emit-java SNMPv2_SMI \
                  --java-package SNMPv2_SMI \
		--java-class-naming 2 \
                $(CONFD_DIR)/etc/confd/snmp/SNMPv2-SMI.fxs \
		-f $(CONFD_DIR)/etc/confd/snmp/smi.fxs

	$(JAVAC) -classpath $(CLASSPATH) SNMPv2_SMI/*.java smi/*.java



clean:
	-rm -rf smi SNMPv2_TC SNMP_COMMUNITY_MIB SNMP_MPD_MIB\
         SNMP_NOTIFICATION_MIB  \
	SNMP_USER_BASED_SM_MIB  SNMP_VIEW_BASED_ACM_MIB \
	SNMP_FRAMEWORK_MIB  SNMP_TARGET_MIB SNMPv2_SMI smi \
	2>/dev/null

