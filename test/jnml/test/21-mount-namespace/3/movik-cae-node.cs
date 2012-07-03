<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          xmlns:movik-system="http://movik.net/ns/movik-system/1.0"
          targetNamespace="http://movik.net/ns/movik-cae-node/1.0"
          mountNamespace="http://movik.net/ns/movik-system/1.0"
          mount="/node">
    <!-- CAE operational mode ATM/Ethernet 3GPP/LTE Monitor/Bypass/Inline etc., -->
    <elem name="operationalMode">
      <elem name="standard" type="signalingStandardType" default="3gpp">
        <desc>Signaling standards to be used</desc>
      </elem>
      <elem name="service" type="serviceType" default="inline">
        <desc>Service type to be used</desc>
      </elem>
      <elem name="opticalBypass" type="movik-system:enableDisableType" default="disable">
        <desc>Enable optical bypass unit</desc>
      </elem>
    </elem>

    <elem name="obs" minOccurs="0" maxOccurs="unbounded" config="false">
      <callpoint id="obs" type="external"/>
      <elem name="id" type="xs:unsignedInt" key="true"/>
      <elem name="operStatus" type="movik-system:adminOperType"/>
      <elem name="bypassStatus" type="bypassStatusType"/>
      <elem name="lastSwitchCommand" type="bypassSwitchCommandsType">
        <desc> last command issued </desc>
      </elem>

      <action name="obsCcommand">
        <actionpoint type="external" id="obs"/>
        <confirmText defaultOption="no">Do you want to proceed?</confirmText>
        <params>
          <elem name="type" type="bypassSwitchCommandsType" default="clear"/>
        </params>
        <result>
          <elem name="reply" type="xs:string"/>
        </result>
      </action>

      <!-- power supply -->
      <elem name="powerSupply" minOccurs="0" maxOccurs="unbounded" generatedName="obsPowerSupply">
        <callpoint id="obsPowerSupply" type="external"/>
        <elem name="id" type="xs:unsignedInt" key="true"/>
        <elem name="operStatus" type="movik-system:adminOperType"/>
      </elem>

    </elem>
</confspec>
