#!/bin/sh

NC=./netconf-console
ROUTER=myrouter
USER=admin
PASS=admin

$NC --user $USER --password $PASS --host $ROUTER --port 22 \
        --rpc get-schema.xml -s raw | awk 'BEGIN{p=0}/<xsd:schema/{p=1}p{print;}/<\/xsd:schema/{p=0;}' > junos-system.xsd

./junos-xsd2yang -o junos-system.yang junos-system.xsd
