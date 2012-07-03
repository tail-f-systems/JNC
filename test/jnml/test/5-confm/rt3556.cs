<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          mount="/">
  <xs:simpleType name="patternType">
    <xs:restriction base="xs:nonNegativeInteger">
      <xs:pattern value="1"/>
      <xs:pattern value="2"/>
    </xs:restriction>
  </xs:simpleType>
  
  <elem name="rt3556" minOccurs="0">
    <elem name="foo" minOccurs="0" type="patternType"/>
  </elem>
</confspec>
