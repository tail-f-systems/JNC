<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          xmlns:st="http://tail-f.com/ns/st/1.0"
          targetNamespace="http://tail-f.com/ns/mountpoint/1.0">
  <elem name="mountpointTop">
    <elem name="mountpoint" minOccurs="0">    
      <elem name="foo" minOccurs="0" type="xs:string"/>
    </elem>
    <elem name="augmentpoint" minOccurs="0">    
      <elem name="royal" minOccurs="0" type="xs:string"/>
    </elem>
    <elem name="structuredType1" type="st"/>
    <elem name="structuredType2" type="st:st"/>
  </elem>
  
  <structuredType name="st">
    <elem name="bappelsin" minOccurs="0">    
      <elem name="gubbadjavel" type="xs:string"/>
    </elem>
  </structuredType>
</confspec>
