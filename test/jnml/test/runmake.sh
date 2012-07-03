#!/bin/sh

# This script is called from bitten/Makefile, the only
# purpose it set CONFM_DIR and CONFD_DIR to correct values
# prior to running the tests

makeprog=$1
target=$2

CONFM_DIR=`(cd ../confm_dir; pwd)`
CONFD_DIR=`(cd ../../confd_dir; pwd)`

export CONFM_DIR
export CONFD_DIR

eval ${makeprog} ${target}
