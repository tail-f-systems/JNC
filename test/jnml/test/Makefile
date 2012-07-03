include subdirs.mk

SUBDIRS = 1-element \
	  2-session \
	  3-confdsession \
          4-notifications \
          5-confm \
          6-readwrite \
          10-dhcp \
	  11-yang \
	  12-device \
	  13-partial_keys \
	  20-revisioning \
	  21-mount-namespace \
	  22-datamodel-test

# still uses confspec
#	  14-vpnapp
# depends on smi.fxs. no longer available
#	  15-snmp \

LOCALALL = localall

localall:
	cd ..; ant
