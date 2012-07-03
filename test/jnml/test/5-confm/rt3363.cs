<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          mount="/">
  <elem name="rt3363">
    <elem name="msp">
      <elem name="platform">
        <elem name="s0" minOccurs="0" maxOccurs="1024">
          <elem name="slot" type="xs:string" key="true"/>
          <elem name="disable" type="xs:string" minOccurs="0"/>
        </elem>
        <elem name="s1" minOccurs="0" maxOccurs="8">
          <elem name="slot" type="xs:string" key="true"/>
          <elem name="disable" type="xs:string" minOccurs="0"/>
        </elem>
        <elem name="s2" minOccurs="0" maxOccurs="8">
          <elem name="slot" type="xs:string" key="true"/>
          <elem name="disable" type="xs:string" minOccurs="0"/>
        </elem>
      </elem>
    </elem>
  </elem>
</confspec>
