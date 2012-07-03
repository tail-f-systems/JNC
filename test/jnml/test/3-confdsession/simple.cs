<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
	  xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          mount="/">
  <elem name="hosts" >
    <elem name="host" minOccurs="0" maxOccurs="64"  sortOrder="snmp">      
      <elem name="name" type="xs:string" key="true" />
      <elem name="enabled" type="xs:boolean"/>
      <elem name="numberOfServers" type="xs:unsignedInt"/>
    </elem>
  </elem>  
</confspec>

