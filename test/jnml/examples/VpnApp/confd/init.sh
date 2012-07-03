#!/bin/sh


if [ ! -d ${CONFD_DIR} ]; then
    echo "bad dir structure"
    exit
fi

CONFD=${CONFD_DIR}/bin/confd
Q=${CONFD_DIR}/examples/demo/quagga
IPC_BASE=4000
HTTP_BASE=8008
HTTPS_BASE=8888
NETCONF_SSH_BASE=2022
NETCONF_TCP_BASE=2032

RS="edgerouter-west edgerouter-north edgerouter-east"
