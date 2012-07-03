#!/bin/sh

## ./start   (starts all routers)
## ./start edgerouter-west  (start just one router)

. ./init.sh
set -x
p=`pwd`

if [ $# = 1 ]; then
    RS=$1
    case $1 in
	edgerouter-west)
	    break;;
	edgerouter-north)
	    IPC_BASE=`expr ${IPC_BASE} + 1`
	    break;;
	edgerouter-east)
	    IPC_BASE=`expr ${IPC_BASE} + 2`
	    break;;
	*)
	    echo "bad router"
	    exit 1
    esac
fi

for router in `echo $RS`; do
    echo starting $router
    cd ${p}/${router};
    env sname=${router} CONFD_IPC_PORT=${IPC_BASE} \
        ${CONFD} -c confd.conf --start-phase0  \
        --ignore-initial-validation
    env sname=${router} CONFD_IPC_PORT=${IPC_BASE} ${CONFD} --start-phase1
    LD_LIBRARY_PATH=${CONFD_DIR}/lib CONFD_IPC_PORT=${IPC_BASE} \
          ${Q}/bin/quagga-proxy >  \
              log/quagga-proxy 2>&1 &
    env sname=${router} CONFD_IPC_PORT=${IPC_BASE} ${CONFD} --start-phase2
    IPC_BASE=`expr ${IPC_BASE} + 1`
done


