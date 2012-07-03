<?xml version="1.0" encoding="utf-8"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
	  xmlns:confd="http://tail-f.com/ns/confd/1.0"
	  xmlns:xs="http://www.w3.org/2001/XMLSchema"
	  targetNamespace="http://acme.example.com/simple/1.0">
  <elem name="hosts">
    <elem name="host" minOccurs="0" maxOccurs="64">      
      <elem name="name" type="xs:string" key="true" />
      <elem name="enabled" type="xs:boolean"/>
      <elem name="servers" minOccurs="0">
	<elem name="server" minOccurs="0" maxOccurs="unbounded">
	  <elem name="ip" type="ipType" key="true"/>
	  <elem name="port" type="portType" key="true"/>
	  <elem name="id" type="xs:string" unique="id"/>
	  <elem name="qos" type="xs:unsignedInt" default="15192"/>
	</elem>
      </elem>      
    </elem>
  </elem>
  <xs:simpleType name="ipType">
    <xs:restriction base="xs:string">
      <xs:minLength value="7"/>
      <xs:maxLength value="15"/>
      <xs:pattern value="(([0-1]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\.){1,3}([0-1]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])"/>      
    </xs:restriction>
  </xs:simpleType>
  <xs:simpleType name="portType">
    <xs:restriction base="xs:unsignedInt">
      <xs:minInclusive value="1024"/>
      <xs:maxInclusive value="65535"/>
    </xs:restriction>
  </xs:simpleType>
</confspec>
