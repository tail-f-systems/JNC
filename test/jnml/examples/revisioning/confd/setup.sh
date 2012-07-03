#!/bin/sh

if [ -z "${CONFD_DIR}" ]; then
    echo 'Where is ConfD installed? Set $CONFD_DIR to point it out!'
    exit 1
fi

. ./init.sh

for system in `echo $RS`; do
    echo $system
    mkdir $system 2> /dev/null
    # Now massage confd.conf to fit
    sed -e "s/HTTP_BASE/${HTTP_BASE}/g" \
        -e "s/HTTPS_BASE/${HTTPS_BASE}/g" \
        -e "s/NETCONF_SSH_BASE/${NETCONF_SSH_BASE}/g" \
        -e "s/NETCONF_TCP_BASE/${NETCONF_TCP_BASE}/g" \
        -e "s/IPC_BASE/${IPC_BASE}/g" \
        -e "s#CONFD_DIR#${CONFD_DIR}#g" \
     < confd.conf.in > ${system}/confd.conf
    
    (cd ${system}; ln -s ../ssh-keydir 2>/dev/null; 
     mkdir log 2>/dev/null; 
     mkdir db 2>/dev/null)
    
    # and finally the populated initial dbs
    # cp init/${system}.xml ${system}/db/isc.xml
    cp init/simple_${system}.xml ${system}/db/
    cp init/aaa_init.xml ${system}/db/
    cp simple_${system}.fxs ${system}
    HTTP_BASE=`expr ${HTTP_BASE} + 1`
    HTTPS_BASE=`expr ${HTTPS_BASE} + 1`
    NETCONF_SSH_BASE=`expr ${NETCONF_SSH_BASE} + 1`
    NETCONF_TCP_BASE=`expr ${NETCONF_TCP_BASE} + 1`
    IPC_BASE=`expr ${IPC_BASE} + 1`
done
