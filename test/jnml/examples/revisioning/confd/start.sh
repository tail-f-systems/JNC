#!/bin/sh

## ./start   (starts all systems)
## ./start v1  (start just one system)

. ./init.sh
p=`pwd`

if [ $# = 1 ]; then
    RS=$1
    case $1 in
	v1)
	    break;;
	v2)
	    IPC_BASE=`expr ${IPC_BASE} + 1`
	    break;;
	v3)
	    IPC_BASE=`expr ${IPC_BASE} + 2`
	    break;;
	*)
	    echo "bad system"
	    exit 1
    esac
fi

for system in `echo $RS`; do
    echo starting $system
    cd ${p}/${system};
    env sname=${system} CONFD_IPC_PORT=${IPC_BASE} \
        ${CONFD} -c confd.conf 
    IPC_BASE=`expr ${IPC_BASE} + 1`
done


