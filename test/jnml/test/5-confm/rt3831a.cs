<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          mount="/">
  <elem name="rt3831a" minOccurs="0">
    <elem name="igmp">
      <elem name="foo" type="xs:string"/>
    </elem>
    
    <elem name="pon-access">
      <elem name="ONT">
        <elem name="igmp">
          <elem name="foo" type="xs:string"/>
        </elem>
      </elem>
    </elem>
    
    <elem name="oper">
      <elem name="ont-igmp">
        <elem name="foo" type="xs:string"/>
      </elem>
    </elem>
  </elem>
</confspec>
