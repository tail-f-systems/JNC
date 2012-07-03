<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          xmlns:movik-system="http://movik.net/ns/movik-system/1.0"
          targetNamespace="http://movik.net/ns/movik-cae-ethernet/1.0"
          mountNamespace="http://movik.net/ns/movik-system/1.0"
          mount="/ethernet">

  <!-- epl -->
  <elem name="epl" minOccurs="0" maxOccurs="unbounded">
    <elem name="name" type="movik-system:nameType" key="true"/>
    <elem name="adminState" type="movik-system:adminOperType" default="up"/>

    <elem name="north">
      <elem name="lag" minOccurs="0" generatedName="EplNorthLag">
        <elem name="name" keyref="/movik-system:ethernet/lag/name" key="true"/>
      </elem>

      <elem name="port" minOccurs="0" generatedName="EplNorthPort">
        <elem name="slot" keyref="/movik-system:ethernet/port/slot" key="true"/>
        <elem name="subSlot" keyref="/movik-system:ethernet/port/subSlot" key="true"/>
        <elem name="port" keyref="/movik-system:ethernet/port/port" key="true"/>
      </elem>
    </elem>
  </elem>
  
</confspec>
