<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          mount="/">
  <elem name="rt2852">
    <elem name="queue-profile" minOccurs="0" maxOccurs="unbounded">
      <elem name="queue-profile-name" type="xs:string" key="true"/>
      <elem name="number-of-queues" type="xs:unsignedInt" minOccurs="0"/>
      <elem name="queue" minOccurs="0" maxOccurs="unbounded">
        <elem name="queue-id" type="xs:unsignedInt" key="true"/>
        <elem name="forwarding-class" type="xs:unsignedInt" minOccurs="0"/>
      </elem>
    </elem>
  </elem>
</confspec>
