<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          targetNamespace="http://movik.net/ns/movik-system/1.0">

  <xs:simpleType name="nameType">
    <xs:restriction base="xs:string">
      <xs:maxLength value="63"/>
    </xs:restriction>
  </xs:simpleType>
  <xs:simpleType name="adminOperType">
    <xs:restriction base="xs:string">
      <xs:enumeration value="down"/>
      <xs:enumeration value="up"/>
    </xs:restriction>
  </xs:simpleType>
  <xs:simpleType name="enableDisableType">
    <xs:restriction base="xs:string">
      <xs:enumeration value="disable"/>
      <xs:enumeration value="enable"/>
    </xs:restriction>
  </xs:simpleType>
  <xs:simpleType name="slotType">
    <xs:restriction base="xs:unsignedShort">
      <xs:minInclusive value="1"/>
      <xs:maxInclusive value="12"/>
    </xs:restriction>
  </xs:simpleType>
  <xs:simpleType name="HACommandsType">
    <xs:restriction base="xs:string">
      <xs:enumeration value="switchOver"/>
    </xs:restriction>
  </xs:simpleType>  
</confspec>

