<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:cs="http://tail-f.com/ns/confspec/1.0"
          xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          mount="/">
  <xs:simpleType name="E1PortType">
    <xs:restriction base="xs:negativeInteger"/>
  </xs:simpleType>
  
  <xs:simpleType name="e1-port-type" cs:generatedName="altE1PortType">
    <xs:restriction base="xs:positiveInteger"/>
  </xs:simpleType>

  <elem name="rt3254" minOccurs="0">
    <elem name="foo" type="E1PortType"/>
    <elem name="bar" type="e1-port-type"/>
  </elem>
</confspec>
