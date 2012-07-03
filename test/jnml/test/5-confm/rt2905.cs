<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          mount="/">
  <elem name="rt2905">
    <elem name="msp" >
      <elem name="interface">
        <elem name="Ethernet" minOccurs="0" maxOccurs="unbounded">
          <elem name="slot" type="xs:int" key="true"/>
          <elem name="port" type="xs:int" key="true"/>
          <elem name="subport" type="xs:int" key="true"/>
          <elem name="disable" minOccurs="0"/>
          <!--0 (none) .. 4094, default to none-->
          <elem name="default-vlan-id" type="xs:int" minOccurs="0"/>
          <elem name="default-etree-leaf-endpoint" minOccurs="0"/>
          
          <elem name="lacp" minOccurs="0">
            <elem name="disable" minOccurs="0"/>
            <elem name="mode" type="xs:int" minOccurs="0"/>
            <elem name="port-priority" type="xs:int" default="32768"/>
            <elem name="period" type="xs:int" minOccurs="0"/>
          </elem>
          <elem name="qos" minOccurs="0">
            <elem name="storm-control" type="xs:string" minOccurs="0"/>
            <elem name="scheduler" type="xs:string" minOccurs="0"/>
            <elem name="filter-policy-input" type="xs:string" minOccurs="0"/>
          </elem>
        </elem>
      </elem>
    </elem>
  </elem>
</confspec>
