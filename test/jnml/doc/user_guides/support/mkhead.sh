#!/bin/sh

rm -f HEAD.src 2> /dev/null
../../../../bin/m3 < HEAD.src.m3 >  HEAD.src
chmod a-w HEAD.src

if [ ! -z "$INM" ]; then
    v=`cat ../../../vsn.mk | grep INM | sed 's/.*=//'`
    sed -e "s/@VSN@/$v/" \
        -e "s/@DATE@/`date +'%B %d, %Y'`/g" HEAD.src > HEAD
elif [ ! -z "$CONFM" ]; then
    v=`cat ../../../vsn.mk | grep CONFM | sed 's/.*=//'`
    sed -e "s/@VSN@/$v/" \
        -e "s/@DATE@/`date +'%B %d, %Y'`/g" HEAD.src > HEAD
else
    echo "Need INM or CONFM in environment to build"
    exit 1
fi
