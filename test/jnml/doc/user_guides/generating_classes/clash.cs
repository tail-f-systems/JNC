<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          targetNamespace="http://tail-f.com/examples/dhcp/1.0">
  <elem name="foo">
    <elem name="bar">
      <elem name="baz">
        <elem name="bingo" type="xs:string"/>
      </elem>
    </elem>
    <elem name="zip">
      <elem generatedName="fingal" name="baz">
        <elem name="bingo" type="xs:string"/>
      </elem>
    </elem>
  </elem>
</confspec>
