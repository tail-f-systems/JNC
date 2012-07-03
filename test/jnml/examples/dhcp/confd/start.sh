#!/bin/sh


## ./start   (starts all systems)
## ./start left  (start just one system)

. ./init.sh
p=`pwd`

if [ $# = 1 ]; then
    RS=$1
    case $1 in
	left)
	    break;;
	right)
	    IPC_BASE=`expr ${IPC_BASE} + 1`
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


