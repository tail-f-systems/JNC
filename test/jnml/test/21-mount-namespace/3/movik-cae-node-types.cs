<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          targetNamespace="http://movik.net/ns/movik-cae-node/1.0">

  <xs:simpleType name="signalingStandardType">
    <xs:restriction base="xs:string">
      <xs:enumeration value="3gpp"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="serviceType">
    <xs:restriction base="xs:string">
      <xs:enumeration value="bypass"/>
      <xs:enumeration value="monitor"/>
      <xs:enumeration value="inline"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="bypassSwitchCommandsType">
    <xs:restriction base="xs:string">
      <xs:enumeration value="forceOpticalBypass"/>
      <xs:enumeration value="manualNoBypass"/>
      <xs:enumeration value="clear"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="bypassSwitchDebugCommandsType">
    <xs:restriction base="xs:string">
      <xs:enumeration value="forceNoBypass"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="bypassStatusType">
    <xs:restriction base="xs:string">
      <xs:enumeration value="bypassed"/>
      <xs:enumeration value="none"/>
    </xs:restriction>
  </xs:simpleType>

</confspec>
