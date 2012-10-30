#!/bin/sh

NC=./netconf-console
ROUTER1=olive1.lab
ROUTER2=olive2.lab
USER=admin
PASS=Admin99

$NC --user $USER --password $PASS --host $ROUTER1 --port 22 \
        --rpc get-schema.xml -s raw | awk 'BEGIN{p=0}/<xsd:schema/{p=1}p{print;}/<\/xsd:schema/{p=0;}' > junos-system.xsd

./junos-xsd2yang -o junos-system.yang junos-system.xsd
