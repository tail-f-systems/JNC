#!/bin/sh

. ./init.sh

${CONFD} --stop 2>&1 > /dev/null

for system in `echo $RS`; do
    echo stopping $system
    env CONFD_IPC_PORT=${IPC_BASE} ${CONFD} --stop
    IPC_BASE=`expr ${IPC_BASE} + 1`
done


