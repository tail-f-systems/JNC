<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          targetNamespace="http://movik.net/ns/movik-system/1.0"
          mount="/">
  <!-- see movik-system-types.cs for data types -->
  <!-- node -->
  <elem name="node">
    <!-- application -->
    <elem name="application" minOccurs="0" maxOccurs="unbounded">
      <elem name="component" type="nameType" key="true"/>
      <elem name="operStatus" type="enableDisableType"/>
    </elem>
  </elem>
  
  <!-- card -->
  <elem name="card" minOccurs="0" maxOccurs="unbounded">
    <!-- application-specific validation -->
    <validate id="card" type="external">
      <dependency>.</dependency>
    </validate>

    <elem name="slot" type="slotType" key="true"/>
    <elem name="adminState" type="adminOperType" default="up"/>

    <!-- power supply -->
    <elem name="powerSupply" minOccurs="0" maxOccurs="unbounded" config="false">
      <callpoint id="cardPowerSupply" type="external"/>
      <elem name="id" type="xs:unsignedShort" key="true"/>
      <elem name="present" type="xs:boolean"/>
      <elem name="operStatus" type="adminOperType"/>
    </elem>

    <!--HA commands -->
    <elem name="ha" config="false"> 
      <callpoint id="card" type="external"/>
      <action name="command">
        <actionpoint type="external" id="cardHa"/>
        <confirmText defaultOption="no">Do you want to proceed?</confirmText>
        <params>
          <elem name="type" type="HACommandsType"/>
        </params>
        <result>
          <elem name="reply" type="xs:string"/>
        </result>
      </action>
    </elem>

  </elem>  
</confspec>


