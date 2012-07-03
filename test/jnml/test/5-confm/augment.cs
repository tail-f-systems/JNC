<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          xmlns:mp="http://tail-f.com/ns/mountpoint/1.0"
          targetNamespace="http://tail-f.com/ns/augment/1.0">
  <augment targetNode="/mp:mountpointTop/augmentpoint">
    <elem name="a" minOccurs="0">
      <elem name="b" type="xs:int"/>
    </elem>
  </augment>
</confspec>
