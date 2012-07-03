<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          targetNamespace="http://tail-f.com/ns/test/msp/1.0"
          mount="/">


 <xs:simpleType name="expected-format-type">
   <xs:restriction base="xs:string">
     <xs:enumeration value="bx-u"/>
     <xs:enumeration value="foo"/>
     <xs:enumeration value="bar"/>
   </xs:restriction>
 </xs:simpleType>

 <xs:simpleType name="dot1ad">
   <xs:restriction base="xs:string">
     <xs:enumeration value="88a8"/>
     <xs:enumeration value="8100"/>
     <xs:enumeration value="bar"/>
   </xs:restriction>
 </xs:simpleType>


  <elem name="mspp">
    <elem name="interface">
      <elem name="Ethernet" minOccurs="0" maxOccurs="256">
        <desc>Display Ethernet port configuration</desc>
        <elem name="port" type="xs:string" key="true"/>
        <elem name="disable" minOccurs="0"/>
        <elem name="default-etree-leaf-endpoint" minOccurs="0" hidden="true"/>
        <elem name="pm-enable" minOccurs="0"/>
        <elem name="description" minOccurs="0" type="xs:string"/>
        <elem name="dot1ad-ethertype" type="dot1ad" default="8100"/>
        <elem name="duplex" type="xs:string" minOccurs="0"/>
        <elem name="crc-threshold" type="xs:int" minOccurs="0"/>
        <elem name="mtu" type="xs:int" default="9600"/>
        <elem name="negotiation-auto" minOccurs="0"/>
        <elem name="speed" type="xs:int" minOccurs="0"/>
        <elem name="bundle-id" minOccurs="0" maxOccurs="1">
          <elem name="id" type="xs:int" key="true"/>
          <elem name="lacp" minOccurs="0">
            <elem name="mode" type="xs:string" default="active"/>
            <elem name="port-priority" type="xs:int" 
                  default="32768"/>
            <elem name="period" type="xs:string" default="long"/>
          </elem>
        </elem>
        
        <elem name="mirror-destination" minOccurs="0"/>
        <elem name="mirror" type="xs:string" minOccurs="0"/>
        
        <elem name="qos" minOccurs="0">
          <desc>Ethernet QoS configuration</desc>
          <elem name="storm-control" type="xs:string" minOccurs="0"/>
          <elem name="scheduler" type="xs:string" minOccurs="0"/>
          <elem name="filter-policy-input" type="xs:string" minOccurs="0"/>
          <elem name="filter-policy-output" type="xs:string" minOccurs="0"/>
          <elem name="untagged-qos" minOccurs="0">
            <elem name="fc" type="xs:int"/>
            <elem name="color" type="xs:int"/>
          </elem>
          <elem name="map-priority-qos" type="xs:string" minOccurs="0"/>
          <elem name="map-qos-priority" type="xs:string" minOccurs="0"/>
          <elem name="map-dscp-qos" type="xs:string" minOccurs="0"/>
        </elem>
        
        <elem name="pluggable-module" minOccurs="0">
          <desc>Ethernet Pluggable module configuration</desc>
          <elem name="expected-format" type="expected-format-type" 
                minOccurs="0"/>
          <elem name="expected-rate" type="xs:int" minOccurs="0"/>
          <elem name="expected-wavelength" type="xs:unsignedInt" minOccurs="0"/>
          <elem name="expected-wdm-type" type="xs:int" minOccurs="0"/>
          <elem name="mismatch-check" minOccurs="0"/>
        </elem>
        
        <elem name="spanning-tree" minOccurs="0">
          <desc>Ethernet spanning tree configuration</desc>
          <elem name="edge-port" minOccurs="0"/>
          <elem name="hello-time" type="xs:int" default="2"/>
          <elem name="change-detection" minOccurs="0"/>
          <elem name="cost" type="xs:int" minOccurs="0"/>
          <elem name="port-priority" type="xs:int" default="128"/>
          <elem name="instance" minOccurs="0" maxOccurs="256">
            <elem name="instance" type="xs:int" key="true"/>
            <elem name="cost" type="xs:int" minOccurs="0"/>
            <elem name="port-priority" type="xs:int" 
                  default="128"/>
          </elem>
        </elem>
        
        <elem name="vlan" minOccurs="0" maxOccurs="4094">
          <desc>Ethernet VLAN configuration</desc>
          <elem name="vlan" type="xs:int" key="true"/>
          <elem name="vid" type="xs:int" minOccurs="0"/>
          <elem name="dot1ad-outer-vlan-id" type="xs:int" minOccurs="0"/>
          <elem name="pbits" minOccurs="0" type="xs:int"/>
          <elem name="protocol" minOccurs="0" type="xs:string"/>
          <elem name="catch-all" minOccurs="0"/>
          <elem name="endpoint" minOccurs="0"/>
          <elem name="map" minOccurs="0">
            <elem name="input" minOccurs="0">
              <elem name="vid" minOccurs="0" maxOccurs="16">
                <elem name="vid" type="xs:int" key="true"/>
                <elem name="to" type="xs:int"/>
              </elem>
              <elem name="pbit" minOccurs="0" maxOccurs="8">
                <elem name="pcb" type="xs:int" key="true"/>
                <elem name="to" type="xs:int"/>
              </elem>
              <elem name="protocol" minOccurs="0" maxOccurs="1">
                <elem name="nameOrNumber" type="xs:string" key="true"/>
                <elem name="to" type="xs:int"/>
              </elem>
            </elem>
            <elem name="output" minOccurs="0">
              <elem name="vid" minOccurs="0" maxOccurs="256">
                <elem name="vid" type="xs:int" key="true"/>
                <elem name="to" type="xs:int"/>
              </elem>
            </elem>
          </elem>
          
          <elem name="mep" minOccurs="0" maxOccurs="256">
            <elem name="level" type="xs:int" key="true"/>
            <elem name="ma" type="xs:string" minOccurs="0"/>
            <elem name="id" type="xs:int" minOccurs="0"/>
            <elem name="ccm-priority" type="xs:int" default="7"/>
            <elem name="ccm-tx" minOccurs="0"/>
            <elem name="disable" minOccurs="0"/>
            <elem name="remote-mep" minOccurs="0" maxOccurs="256">
              <elem name="mepid" type="xs:int" key="true"/>
            </elem>
          </elem>
          <elem name="mirror" type="xs:int" minOccurs="0"/>
          <elem name="qos" minOccurs="0">
            <elem name="filter-policy-input" type="xs:string" minOccurs="0"/>
            <elem name="filter-policy-output" type="xs:string" minOccurs="0"/>
            <elem name="override-qos" minOccurs="0">
              <elem name="fc" type="xs:int"/>
              <elem name="color" type="xs:int"/>
            </elem>
            <elem name="service-policy-input" type="xs:string" minOccurs="0"/>
            <elem name="service-policy-output" type="xs:string" minOccurs="0"/>
            <elem name="policer-input" type="xs:string" minOccurs="0"/>
            <elem name="policer-output" type="xs:string" minOccurs="0"/>
          </elem>
        </elem>
        
        <elem name="storm-ctrl" minOccurs="0">
          <elem name="type" type="xs:int"/>
          <!-- or Max 40% link rate-->
          <elem name="limit-count" type="xs:int"/>
        </elem>
      </elem>
    </elem>
  </elem>
</confspec>
