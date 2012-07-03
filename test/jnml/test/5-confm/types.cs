<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:inet="urn:ietf:params:xml:ns:yang:inet-types"
          xmlns:ieee="urn:ietf:params:xml:ns:yang:ieee-types"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          targetNamespace="http://tail-f.com/ns/example/types/1.0">
  <!-- Element structure -->

  <elem name="types">
    <elem name="basicTypes">    
      <elem name="string" minOccurs="0" type="xs:string"/>
<!--      <elem name="int" minOccurs="0" type="intType"/> -->
      <elem name="myBoolean" type="xs:boolean" default="true"/>
      <elem name="base64Binary" type="xs:base64Binary"
            default="Y2FybmFsIHBsZWFzdXJlLg=="/>
      <elem name="float" minOccurs="0" type="xs:float"/>
      <elem name="decimal" type="myDecimalType" default="61.12"/>
      <elem name="double" type="xs:double" default="6.6767"/>
      <elem name="anyURI" type="xs:anyURI" default="http://acme.com"/>
      <elem name="bitsType" type="myBitsType" default="foo bar"/>
    </elem>
    <elem name="enumerationType">    
      <elem name="decimalEnumeration" type="decimalEnumerationType"
            default="6.66"/>
      <elem name="mixedCaseStringEnumeration"
            type="mixedCaseStringEnumerationType"
            default="toBeOrNotToBe"/>
    </elem>
    <elem name="dateTypes">
      <elem name="pointInTime">    
        <elem name="dateTime" type="xs:dateTime"
              default="2001-10-26T21:32:52+02:00"/>
      </elem>
      <elem name="periodsOfTime">    
        <elem name="date" type="xs:date" default="2001-10-26Z"/>
        <elem name="gYearMonth" type="xs:gYearMonth"
              default="2001-10+02:00"/>
        <elem name="gYear" type="xs:gYear" default="2006"/>
      </elem>
      <elem name="recurringPointInTime">    
        <elem name="time" type="xs:time" default="21:32:51.12679"/>
      </elem>
      <elem name="recurringPeriodInTime">    
        <elem name="gMonthDay" type="xs:gMonthDay"
              default="--11-01-04:00"/>
        <elem name="gMonth" type="xs:gMonth" default="--11Z"/>
        <elem name="gDay" type="xs:gDay" default="---15"/>
        <elem name="duration" type="xs:duration"
              default="P1Y2M3DT5H20M30.123S"/> 
      </elem>
    </elem>
    <elem name="confdTypes">
      <elem name="inetAddressIPv4" type="confd:inetAddressIPv4"
            default="192.168.2.1"/>
      <elem name="inetAddressIPv6" type="confd:inetAddressIPv6"
            default="192:168:2:1:3:1:4:89"/>
      <elem name="size" type="confd:size" default="S234M"/>
      <elem name="MD5DigestString" type="confd:MD5DigestString"
	    default="$1$fB$NlWVsVPs/9q60aFdS6uSdQ=="/>
      <elem name="DES3CBCEncryptedString"
	    type="confd:DES3CBCEncryptedString"
            default="$3$HAYSHETY2YA8AJJA"/>
      <elem name="AESCFB128EncryptedString"
            type="confd:AESCFB128EncryptedString"
            default="$4$HAYSHETY2YA8AJJA"/>
    </elem>
    <elem name="listTypes">    
      <elem name="listOfDurations" type="listOfDurationsType"
            default="P1Y2M3DT5H20M30.123S"/>
      <elem name="listOfDurationEnumerations"
            type="listOfDurationEnumerationsType"
            default="PT130S P1Y2M3DT5H20M30.123S"/> 
    </elem>
    <elem name="customTypes" minOccurs="0">    
      <elem name="myUnion" type="intEnumerationUnionType" default="always"/>
      <elem name="myCustomUnion" type="intEnumerationUnionType"
            default="always"/>
      <elem name="myBoolean" type="xs:boolean" default="true"/>
      <elem name="myCustomBoolean" type="myCustomBooleanType" default="true"/>
    </elem>
    <elem name="displayWhenTypes" minOccurs="0">    
      <elem name="string" type="xs:string" minOccurs="0">
        <displayWhen value="../myBoolean = 'true'"/>
      </elem>
      <elem name="myBoolean" type="xs:boolean" default="true"/>
    </elem>
    <elem name="keyrefTypes">
      <elem name="foo" minOccurs="0" maxOccurs="8">
        <elem name="bar" type="xs:string" key="true"/>
      </elem>
      <elem minOccurs="0" name="fooKeyref" keyref="../foo/bar"/>
    </elem>
  </elem>
  
  <!-- Type definitions -->

  <xs:simpleType name="myDecimalType">
    <xs:restriction base="xs:decimal">
      <xs:fractionDigits value="3"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="myCustomBooleanType">
    <xs:restriction base="xs:boolean"/>
  </xs:simpleType>
  
  <bitsType name="myBitsType">
    <field bit="0" label="foo"/>
    <field bit="2" label="bar"/>
    <field bit="7" label="baz"/>
  </bitsType>
  
  <xs:simpleType name="decimalEnumerationType">
    <xs:restriction base="myDecimalType">
      <xs:enumeration value="6.486"/>
      <xs:enumeration value="6.66"/>
      <xs:enumeration value="5.2"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="mixedCaseStringEnumerationType">
    <xs:restriction base="xs:string">
      <xs:enumeration value="toBeOrNotToBe"/>
      <xs:enumeration value="thatIsTheQuestion"/>
    </xs:restriction>
  </xs:simpleType>
  
  <xs:simpleType name="listOfDurationsType">
    <xs:list itemType="xs:duration"/>
  </xs:simpleType>
  
  <xs:simpleType name="listOfDurationEnumerationsType">
    <xs:list itemType="durationEnumerationsType"/>
  </xs:simpleType>  

  <xs:simpleType name="durationEnumerationsType">
    <xs:restriction base="xs:duration">
      <xs:enumeration value="PT130S"/>
      <xs:enumeration value="P1DT2S"/>
      <xs:enumeration value="P1Y2M3DT5H20M30.123S"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="intEnumerationUnionType">
    <xs:union memberTypes="xs:int stringEnumerationType"/>
  </xs:simpleType>

  <xs:simpleType name="stringEnumerationType">
    <xs:restriction base="xs:string">
      <xs:enumeration value="always"/>
      <xs:enumeration value="never"/>
    </xs:restriction>
  </xs:simpleType>
</confspec>
