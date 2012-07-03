<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
	  xmlns:confd="http://tail-f.com/ns/confd/1.0"
	  xmlns:xs="http://www.w3.org/2001/XMLSchema"
	  targetNamespace="http://tail-f.com/ns/example/quagga/1.0"
	  mount="/system">
  <xs:simpleType name="EncryptionAlgo">
    <xs:restriction base="xs:string">
      <xs:enumeration value="des"/>
      <xs:enumeration value="3des"/>
      <xs:enumeration value="blowfish"/>
      <xs:enumeration value="cast128"/>
      <xs:enumeration value="aes"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="EncryptionAlgo2">
    <xs:restriction base="xs:string">
      <xs:enumeration value="default"/>
      <xs:enumeration value="des"/>
      <xs:enumeration value="3des"/>
      <xs:enumeration value="blowfish"/>
      <xs:enumeration value="cast128"/>
      <xs:enumeration value="aes"/>
    </xs:restriction>
  </xs:simpleType>
  
  <xs:simpleType name="HashAlgo">
    <xs:restriction base="xs:string">
      <xs:enumeration value="md5"/>
      <xs:enumeration value="sha1"/>
      <xs:enumeration value="sha256"/>
    </xs:restriction>
  </xs:simpleType>
  
  <xs:simpleType name="HashAlgo2">
    <xs:restriction base="xs:string">
      <xs:enumeration value="default"/>
      <xs:enumeration value="md5"/>
      <xs:enumeration value="sha1"/>
      <xs:enumeration value="sha256"/>
    </xs:restriction>
  </xs:simpleType>
  
  <xs:simpleType name="InOut">
    <xs:restriction base="xs:string">
      <xs:enumeration value="in"/>
      <xs:enumeration value="out"/>
    </xs:restriction>
  </xs:simpleType>

  <notification name="saEstablished">
    <elem name="tunnelName" keyref="/system/vpn/ipsec/tunnel/name"/>
    <elem name="spi" type="xs:integer"/>
  </notification>
  
  <notification name="saExpired">
    <elem name="tunnelName" keyref="/system/vpn/ipsec/tunnel/name"/>
    <elem name="spi" type="xs:integer"/>
  </notification>
  
  <notification name="noPolicy">
    <elem name="local-net" type="confd:inetAddressIPv4" />
    <elem name="local-net-mask" type="confd:inetAddressIPv4" />
    <elem name="remote-endpoint" type="confd:inetAddressIPv4" /> 
    <elem name="remote-net" type="confd:inetAddressIPv4" />
    <elem name="remote-net-mask" type="confd:inetAddressIPv4" />
    <elem name="direction" type="InOut" />
  </notification>
  
  <elem name="vpn" minOccurs="0">
    <elem name="ipsec">
      <elem name="defaults">
	<elem name="encryption-algo" type="EncryptionAlgo" default="des"/>
	<elem name="hash-algo" type="HashAlgo" default="md5"/>
      </elem>
      
      <elem name="tunnel"  minOccurs="0" maxOccurs="unbounded">
	<elem name="name" type="xs:string" key="true" />
	<elem name="local-endpoint" type="confd:inetAddressIPv4" />
	<elem name="local-net" type="confd:inetAddressIPv4" />
	<elem name="local-net-mask" type="confd:inetAddressIPv4" />
	<elem name="remote-endpoint" type="confd:inetAddressIPv4" /> 
	<elem name="remote-net" type="confd:inetAddressIPv4" />
	<elem name="remote-net-mask" type="confd:inetAddressIPv4" />
	<elem name="pre-shared-key" type="xs:string" />
	<elem name="encryption-algo" type="EncryptionAlgo2"
	      default="default"/>
	<elem name="hash-algo" type="HashAlgo2" default="default"/>
      </elem>
    </elem>
  </elem>
</confspec>
