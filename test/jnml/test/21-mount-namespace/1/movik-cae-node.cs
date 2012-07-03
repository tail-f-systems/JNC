<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          xmlns:movik-system="http://movik.net/ns/movik-system/1.0"
          targetNamespace="http://movik.net/ns/movik-cae-node/1.0"
          mountNamespace="http://movik.net/ns/movik-system/1.0"
          mount="/node">
  <!-- CAE operational mode ATM/Ethernet 3GPP/LTE Monitor/Bypass/Inline
       etc., -->
  <elem name="operationalMode">
    <elem name="standard" type="movik-system:signalingStandardType"
          default="3gpp">
      <desc>Signaling standards to be used</desc>
    </elem>
    <elem name="service" type="movik-system:serviceType" default="inline">
      <desc>Service type to be used</desc>
    </elem>
    <elem name="opticalBypass" type="movik-system:enableDisableType"
          default="disable">
      <desc>Enable optical bypass unit</desc>
    </elem>
  </elem>
</confspec>
