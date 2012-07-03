#!/bin/sh

. ./init.sh

for router in `echo $RS`; do
    echo $router
    mkdir $router 2> /dev/null
    # Now massage confd.conf to fit
    sed -e "s/HTTP_BASE/${HTTP_BASE}/g" \
        -e "s/HTTPS_BASE/${HTTPS_BASE}/g" \
        -e "s/NETCONF_SSH_BASE/${NETCONF_SSH_BASE}/g" \
        -e "s/NETCONF_TCP_BASE/${NETCONF_TCP_BASE}/g" \
        -e "s/IPC_BASE/${IPC_BASE}/g" \
     < confd.conf.in > ${router}/confd.conf
    

    (
	cd ${router};
	ln -s ${CONFD_DIR} CONFD_DIR 2> /dev/null 
	ln -s ${Q} QUAGGA_DIR  2> /dev/null
	mkdir log 2>/dev/null; 
	mkdir db 2>/dev/null
	)
    
    # and finally the populated initial dbs
    cp init/${router}.xml ${router}/db/quagga.xml
    cp $Q/db/aaa_init.xml ${router}/db/
    HTTP_BASE=`expr ${HTTP_BASE} + 1`
    HTTPS_BASE=`expr ${HTTPS_BASE} + 1`
    NETCONF_SSH_BASE=`expr ${NETCONF_SSH_BASE} + 1`
    NETCONF_TCP_BASE=`expr ${NETCONF_TCP_BASE} + 1`
    IPC_BASE=`expr ${IPC_BASE} + 1`
done
