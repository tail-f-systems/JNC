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
</confspec>


