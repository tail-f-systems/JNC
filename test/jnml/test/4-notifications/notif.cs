<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
	  xmlns:confd="http://tail-f.com/ns/confd/1.0"
	  xmlns:xs="http://www.w3.org/2001/XMLSchema"
	  targetNamespace="http://tail-f.com/ns/test/notif/1.0">

  <elem name="interfaces">
    <elem name="interface" minOccurs="0" maxOccurs="unbounded">
      <elem name="ifIndex" type="xs:unsignedInt" key="true"/>
      <elem name="desc" type="xs:string" minOccurs="0"/>
    </elem>
  </elem>

  <notification name="linkUp">
    <elem name="ifIndex" keyref="/interfaces/interface/ifIndex"/>
    <elem name="extraId" minOccurs="0" type="xs:string"/>
    <elem name="linkProperty" minOccurs="0" maxOccurs="64">
      <elem name="newlyAdded" minOccurs="0"/>
      <elem name="flags" type="xs:unsignedInt" default="0"/>
      <elem name="extensions" minOccurs="0" maxOccurs="64">
	<elem name="name" type="xs:unsignedInt"/>
	<elem name="value" type="xs:unsignedInt"/>
      </elem>
    </elem>
  </notification>

  <notification name="linkDown">
    <elem name="ifIndex" keyref="/interfaces/interface/ifIndex"/>
  </notification>

</confspec>
