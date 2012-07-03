<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          mount="/">

  
  <xs:simpleType name="md-fault-alarm-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="default-md"/>
      <xs:enumeration value="zappper"/>
    </xs:restriction>
  </xs:simpleType>
  

  <elem name="rt2870">
    <elem name="msp" minOccurs="0">
      <elem name="spanning-tree" minOccurs="0">
        <elem name="mst" minOccurs="0" maxOccurs="unbounded">
          <elem name="instance-id" type="xs:int" key="true"/>
          <elem name="disable" minOccurs="0"/>
          <elem name="force-version" type="xs:string" minOccurs="0"/>
          <elem name="forward-time" type="xs:string" minOccurs="0"/>
          <elem name="hello-time" type="xs:string" minOccurs="0"/>
          <elem name="max-age" type="xs:string" minOccurs="0"/>
          <elem name="max-hops" type="xs:string" minOccurs="0"/>
          <elem name="priority" type="xs:string" minOccurs="0"/>
          <elem name="transmit-hold-count" type="xs:string" 
                default="6"/>
          <elem name="region" minOccurs="0">
            <elem name="region-name" type="xs:string" minOccurs="0"/>
            <elem name="revision" type="xs:string" minOccurs="0"/>
          </elem>
          <elem name="instance" minOccurs="0" maxOccurs="24">
            <elem name="instance-id" type="xs:int" key="true"/>
            <elem name="vlans" minOccurs="0" maxOccurs="4094">
              <elem name="vlan" type="xs:string" key="true"/>
            </elem>
            <elem name="priority" type="xs:string" minOccurs="0"/>
          </elem>
        </elem>
      </elem>
    </elem>
  </elem>
  <elem name="rt3538">
    <elem name="msp">
      <elem name="md" minOccurs="0" maxOccurs="256">
        <elem name="level" type="xs:int" key="true"/>
        <elem name="format" type="xs:string" default="string"/>
        <elem name="md-name" type="xs:string" minOccurs="0"/>
        <elem name="description" type="xs:string" minOccurs="0"/>
        <elem name="fault-alarms" minOccurs="0"/>
        <elem name="ma" minOccurs="0" maxOccurs="256">
          <elem name="ma-name" type="xs:string" key="true"/>
          <elem name="description" type="xs:string" minOccurs="0"/>
          <elem name="format" type="xs:string" default="string"/>
          <elem name="two-octet" type="xs:int" minOccurs="0"/>
          <elem name="auto-detect" minOccurs="0"/>
          <elem name="fault-alarms" type="md-fault-alarm-type" 
                default="default-md"/>
          <elem name="ccm-interval" type="xs:string" default="1s"/>
          <elem name="ccm-rdi" minOccurs="0"/>
        </elem>
      </elem>
    </elem>
  </elem>
</confspec>
