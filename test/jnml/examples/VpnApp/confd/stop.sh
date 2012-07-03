#!/bin/sh

. ./init.sh

for router in `echo $RS`; do
    echo stopping $router
    env CONFD_IPC_PORT=${IPC_BASE} ${CONFD} --stop
    IPC_BASE=`expr ${IPC_BASE} + 1`
done


