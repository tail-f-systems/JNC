<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          targetNamespace="http://tail-f.com/examples/dhcp/1.0">
  <elem name="dhcp">
    <elem name="logFacility" type="loglevel"/>
    <elem name="maxLeaseTime" minOccurs="0" type="xs:unsignedLong"/>
    <elem name="leaseTime" type="xs:positiveInteger" default="4711"/>
    
    <elem name="subNets">
      <elem name="subNet" minOccurs="0" maxOccurs="1024">
        <elem name="net" type="confd:inetAddressIP" key="true"/>
        <elem name="mask" type="confd:inetAddressIP" key="true"/>
      </elem>
      <elem name="range" minOccurs="0">
        <elem name="loAddr" type="confd:inetAddressIP" minOccurs="0"/>
        <elem name="hiAddr" type="confd:inetAddressIP" minOccurs="0"/>
      </elem>
    </elem>
  </elem>
  
  <xs:simpleType name="loglevel">
    <xs:restriction base="xs:string">
      <xs:enumeration value="kern"/>
      <xs:enumeration value="mail"/>
      <xs:enumeration value="local7"/>
    </xs:restriction>
  </xs:simpleType>
</confspec>
