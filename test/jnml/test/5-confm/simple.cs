<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
	  xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          mount="/">



  <xs:simpleType name="wait-to-restore-timer-type">
    <xs:restriction base="xs:int">
      <xs:minInclusive value="10"/>
      <xs:maxInclusive value="720"/>
      <xs:pattern value="[0-9]+0"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="guard-timer-type">
    <xs:restriction base="xs:int">
      <xs:minInclusive value="10"/>
      <xs:maxInclusive value="2000"/>
      <xs:pattern value="[0-9]+0"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="BandwidthRangeType">
    <xs:restriction base="xs:unsignedLong">
      <xs:minInclusive value="1"/>
      <xs:maxInclusive value="10000000000"/>
    </xs:restriction>
  </xs:simpleType>


  <elem name="hosts" minOccurs="0">
    <desc>The hosts container contains dynamic host objects.
      They are all unqiue. (Ref #1)
    </desc>

    <elem name="a">
      <elem name="as" minOccurs="0" maxOccurs="12">
	<elem name="aIndex" type="xs:int" key="true"/>
	<elem name="aVal" type="xs:string"/>
      </elem>
    </elem>

    <elem name="b">
      <elem name="as" minOccurs="0" maxOccurs="12">
	<elem name="bIndex" type="xs:int" key="true"/>
	<elem name="bVal" type="xs:string"/>
      </elem>
    </elem>

    <elem name="reservedwords">
      <elem name="if">
        <elem name="interface" type="xs:string" />
        <elem name="public">
          <elem name="interface">
            <elem name="new" >
              <elem name="foobar" type="xs:int" default="9999"/>
            </elem>
          </elem>
        </elem>
      </elem>
    </elem>

    <elem name="mana" type="xs:unsignedInt" minOccurs="0" maxOccurs="1"/>
    <elem name="host" minOccurs="0" maxOccurs="64"  sortOrder="snmp">      
      <desc>
        A host is a dynamic member of the hosts container.
        The unique keys are the "names". (Ref #2)
      </desc>
      <elem name="name" type="xs:string" key="true" />
      <elem name="enabled" type="xs:boolean"/>
      <elem name="disabled" minOccurs="0"/>   <!-- existance element -->
      <elem name="numberOfServers" type="xs:unsignedInt">
        <desc> Shows the number of servers for this host.
          The number of servers is an integer value and it is
          probably never up to date. (Ref #3)
        </desc>
      </elem>
      <elem name="encryption" type="HashAlgo"/>
      <elem name="address" type="confd:inetAddressIPv4"/>      
      <elem name="mtu" type="MTUType"/>      
      <elem name="flags" type="myBitsType"/>
      <elem name="flags2" type="myBitsType2"/>
      <elem name="servers" minOccurs="0">
	<elem name="server" minOccurs="0" maxOccurs="unbounded">
	  <elem name="ip" type="ipType" key="true"/>
	  <elem name="port" type="portType" key="true"/>
	  <elem name="id" type="xs:string" unique="id"/>
	  <elem name="qos" type="xs:unsignedInt" default="15192"/>
	</elem>
      </elem>      
      <elem name="baud-rate" type="BaudRateType"/>      
      <elem name="secPort" type="secPortType"/>      
      <elem name="secPort2" type="secPortType2"/>      
      <elem name="gzz" type="gzzType"/>
      <elem name="alist" type="alistType"/>
            
      <elem name="highPriorityServer" type="serverType"/>
      <elem name="lowPriorityServer" type="serverType"/>
    </elem>      
    <!-- dynamic structured type -->
    <elem name="dzzt" minOccurs="0" maxOccurs="5" type="dzztType"/>            

    <elem name="ulong" type="xs:unsignedLong" />
    <elem name="ulong2" type="LongType" />


  </elem>


  <xs:simpleType name="LongType">
    <xs:restriction base="xs:unsignedLong">
      <xs:maxInclusive value="15"/>
    </xs:restriction>
  </xs:simpleType>

  
  <structuredType name="serverType">
    <desc>
      A serverType is a common structured type for
      many of the server types in the simple demo. (Ref #4)
    </desc>
    <elem name="ip" type="confd:inetAddressIP"/>
    <elem name="port" type="confd:inetPortNumber"/>
  </structuredType>

  <structuredType name="dzztType">
    <desc>
      A dzztType is a common structured type for
      dizzy objects.
    </desc>
    <elem name="dizzy" type="xs:token" key="true"/>
    <elem name="timer" type="xs:unsignedByte"/>
  </structuredType>
  
  <xs:simpleType name="alistType">
    <xs:list itemType="xs:integer"/>
  </xs:simpleType>

  <xs:simpleType name="vlanid-string-list-type">
    <xs:restriction base="alistType">
      <xs:maxLength value="16"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="gzzType">
    <xs:union memberTypes="secPortType HashAlgo xs:unsignedInt"/>
  </xs:simpleType>
      
  <xs:simpleType name="secPortType2">
    <xs:restriction base="secPortType">
      <xs:maxInclusive value="2400"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="secPortType">
    <xs:restriction base="portType">
      <xs:minInclusive value="2048"/>
    </xs:restriction>
  </xs:simpleType>
    
  <xs:simpleType name="HashAlgo">
    <xs:restriction base="xs:string">
      <xs:enumeration value="default"/>
      <xs:enumeration value="md5"/>
      <xs:enumeration value="sha1"/>
      <xs:enumeration value="sha256"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="BaudRateType">
    <xs:restriction base="xs:int">
      <xs:enumeration value="9600"/>
      <xs:enumeration value="1200"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="MTUType">
    <xs:restriction base="xs:unsignedShort">
      <xs:minInclusive value="1000"/>
    </xs:restriction>
  </xs:simpleType>
  
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

  <bitsType name="myBitsType">
    <field bit="0" label="U"/>
    <field bit="1" label="compressed"/>
    <field bit="12" label="dirty"/>
  </bitsType>

  <bitsType name="myBitsType2" size="64">
    <field bit="0" label="U2"/>
    <field bit="2" label="compressed2"/>
    <field bit="33" label="dirty2"/>
  </bitsType>

  
  
</confspec>

