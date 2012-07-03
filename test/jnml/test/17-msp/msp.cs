<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          targetNamespace="http://www.lg-nortel.com/ns/example/wdmpon/1.0"
          mount="/">

  <xs:simpleType name="string-list">
    <xs:list itemType="xs:string"/>
  </xs:simpleType>

  <xs:simpleType name="qos-pcp-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="pcp1"/>
      <xs:enumeration value="pcpc2"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="qos-de-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="depcp1"/>
      <xs:enumeration value="depcpc2"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="qos-policer-eir-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="adepcp1"/>
      <xs:enumeration value="adepcpc2"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="qos-policer-ebs-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="adepcp1"/>
      <xs:enumeration value="adepcpc2"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="qos-policer-cir-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="adepcp1"/>
      <xs:enumeration value="adepcpc2"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="qos-policer-cbs-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="adepcp1"/>
      <xs:enumeration value="adepcpc2"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="policer-algorithm">
    <xs:restriction base="xs:string">
      <xs:enumeration value="a1"/>
      <xs:enumeration value="a2"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="dscp-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="sa1"/>
      <xs:enumeration value="sa2"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="connection-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="c1"/>
      <xs:enumeration value="sa2"/>
      <xs:enumeration value="e-tree"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="vlan-tag-ether-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="adepcp1"/>
      <xs:enumeration value="8100"/>
      <xs:enumeration value="adepcpc2"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="mtu-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="foo"/>
      <xs:enumeration value="9600"/>
      <xs:enumeration value="9216"/>
      <xs:enumeration value="bad"/>
    </xs:restriction>
  </xs:simpleType>



  <xs:simpleType name="portType">
    <xs:annotation>
      <xs:documentation>slot(1:8:a:b)/port(1:2)/subport(1:32)</xs:documentation>
    </xs:annotation>
    <!-- <typepoint id="port_type"/>  -->
    <xs:restriction base="xs:string"/>
  </xs:simpleType>

  <xs:simpleType name="string39">
    <xs:restriction base="xs:string">
      <xs:maxLength value="39"/>
    </xs:restriction>
  </xs:simpleType>
  
  <xs:simpleType name="string40">
    <xs:restriction base="xs:string">
      <xs:maxLength value="40"/>
    </xs:restriction>
  </xs:simpleType>
  
  <xs:simpleType name="string80">
    <xs:restriction base="xs:string">
      <xs:maxLength value="80"/>
    </xs:restriction>
  </xs:simpleType>
  
  <xs:simpleType name="dot1d-ether-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="88a8"/>
      <xs:enumeration value="8100"/>
    </xs:restriction>
  </xs:simpleType>
  
  <xs:simpleType name="mc-mtu-type">
    <xs:restriction base="xs:int">
      <xs:minInclusive value="64"/>
      <xs:maxInclusive value="9574"/>
    </xs:restriction>
  </xs:simpleType>
  
  
  <xs:simpleType name="duplex-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="full"/>
      <xs:enumeration value="half"/>
    </xs:restriction>
  </xs:simpleType>
  
  <xs:simpleType name="bundle-id-type">
    <xs:restriction base="xs:int">
      <xs:minInclusive value="1"/>
      <xs:maxInclusive value="8"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="lacp-mode-type">
    <xs:restriction base="xs:token">
      <xs:enumeration value="active"/>
      <xs:enumeration value="passive"/>
    </xs:restriction>
  </xs:simpleType>
  <xs:simpleType name="lacp-port-priority-type">
    <xs:restriction base="xs:int">
      <xs:minInclusive value="0"/>
      <xs:maxInclusive value="65535"/>
    </xs:restriction>
  </xs:simpleType>
  <xs:simpleType name="lacp-period-type">
    <xs:restriction base="xs:token">
      <xs:enumeration value="long"/>
      <xs:enumeration value="short"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="mirror-dir-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="input"/>
      <xs:enumeration value="output"/>
      <xs:enumeration value="bidirectional"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="int1000">
    <xs:restriction base="xs:int">
      <xs:minInclusive value="1"/>
      <xs:maxInclusive value="1000"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="up-down-type">
    <xs:restriction base="xs:token">
      <xs:enumeration value="up"/>
      <xs:enumeration value="down"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="enable-disable-type">
    <xs:restriction base="xs:token">
      <xs:enumeration value="enable"/>
      <xs:enumeration value="disable"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="yes-no-type">
    <xs:restriction base="xs:token">
      <xs:enumeration value="yes"/>
      <xs:enumeration value="no"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="expected-format-type">
    <xs:restriction base="xs:token">
      <xs:enumeration value="bx-u"/>
      <xs:enumeration value="bx-d"/>
      <xs:enumeration value="sx"/>
      <xs:enumeration value="dx"/>
      <xs:enumeration value="zx"/>
    </xs:restriction>
  </xs:simpleType>
  <xs:simpleType name="expected-wdm-type">
    <xs:restriction base="xs:token">
      <xs:enumeration value="cwdm"/>
      <xs:enumeration value="dwdm"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="speed-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="100"/>
      <xs:enumeration value="1000"/>
      <xs:enumeration value="10000"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="hello-time-type">
    <xs:restriction base="xs:int">
      <xs:minInclusive value="1"/>
      <xs:maxInclusive value="10"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="stp-port-priority-type">
    <xs:restriction base="xs:int">
      <xs:minInclusive value="0"/>
      <xs:maxInclusive value="240"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="stp-instance-type">
    <xs:restriction base="xs:int">
      <xs:minInclusive value="1"/>
      <xs:maxInclusive value="64"/>
    </xs:restriction>
  </xs:simpleType>


  <xs:simpleType name="vlanid-string-list-type">
    <xs:restriction base="string-list">
      <!--<xs:maxLength value="16"/>-->
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="vlanid-type">
    <xs:restriction base="xs:int">
      <xs:minInclusive value="1"/>
      <xs:maxInclusive value="4094"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="pbits-type">
    <xs:restriction base="xs:int">
      <xs:minInclusive value="0"/>
      <xs:maxInclusive value="7"/>
    </xs:restriction>
  </xs:simpleType>
  <xs:simpleType name="pbits-list-type">
    <xs:list itemType="pbits-type"/>
  </xs:simpleType>

  <xs:simpleType name="vlanid-zero-type">
    <xs:restriction base="xs:int">
      <xs:minInclusive value="0"/>
      <xs:maxInclusive value="4094"/>
    </xs:restriction>
  </xs:simpleType>
  <xs:simpleType name="vlanid-minus-type">
    <xs:restriction base="xs:int">
      <xs:minInclusive value="-1"/>
      <xs:maxInclusive value="4094"/>
    </xs:restriction>
  </xs:simpleType>
  <xs:simpleType name="vlanid-none-untag-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="none"/>
      <xs:enumeration value="untagged"/>
    </xs:restriction>
  </xs:simpleType>
  <xs:simpleType name="vlanid-none-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="none"/>
    </xs:restriction>
  </xs:simpleType>
  <xs:simpleType name="vlanid-none-all-untag-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="none"/>
      <xs:enumeration value="all"/>
      <xs:enumeration value="untagged"/>
    </xs:restriction>
  </xs:simpleType>
  <xs:simpleType name="vlanid-untag-type">
    <xs:restriction base="xs:string">
      <xs:enumeration value="untagged"/>
    </xs:restriction>
  </xs:simpleType>
  <xs:simpleType name="vlanid-none-all-untag-num-type">
    <xs:union memberTypes="vlanid-type vlanid-none-all-untag-type"/>
  </xs:simpleType>
  <xs:simpleType name="vlanid-none-num-type">
    <xs:union memberTypes="vlanid-type vlanid-none-type"/>
  </xs:simpleType>
  <xs:simpleType name="vlanid-none-untag-num-type">
    <xs:union memberTypes="vlanid-type vlanid-none-untag-type"/>
  </xs:simpleType>
  <xs:simpleType name="vlanid-number-list">
    <xs:list itemType="vlanid-type"/>
  </xs:simpleType>
  <xs:simpleType name="vlanid-list-all-type">
    <xs:union memberTypes="vlanid-number-list vlanid-none-untag-type"/>
  </xs:simpleType>

  <xs:simpleType name="string16">
    <xs:restriction base="xs:string">
      <xs:minLength value="0"/>
      <xs:maxLength value="16"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="mep-id-type">
    <xs:restriction base="xs:int">
      <xs:minInclusive value="1"/>
      <xs:maxInclusive value="8191"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="qos-fc-type">
    <xs:restriction base="xs:int">
      <xs:minInclusive value="0"/>
      <xs:maxInclusive value="7"/>
    </xs:restriction>
  </xs:simpleType>
  <xs:simpleType name="qos-color-type">
    <xs:restriction base="xs:token">
      <xs:enumeration value="green"/>
      <xs:enumeration value="yellow"/>
    </xs:restriction>
  </xs:simpleType>



  <xs:simpleType name="pec-type">
         <xs:restriction base="xs:token">
             <xs:enumeration value="EARU1112"/>
             <xs:enumeration value="EABU1111"/>
             <xs:enumeration value="EABU1112"/>
             <xs:enumeration value="EARU11x4"/>
             <xs:enumeration value="EARU11x3RF"/>
             <xs:enumeration value="EARU11x4RF"/>
             <xs:enumeration value="EARU11x3"/>
             <xs:enumeration value="EABU2112"/>
             <xs:enumeration value="EABU2113"/>
             <xs:enumeration value="EABU2212"/>
             <xs:enumeration value="EABU2213"/>
             <xs:enumeration value="Teldat"/>
             <xs:enumeration value="Comtrend"/>
         </xs:restriction>
     </xs:simpleType>


  <elem name="msp">

    <elem name="pon-access">
     <elem name="ont" minOccurs="0" maxOccurs="256">
    
       <elem name="name" type="string39" key="true"/>
       <elem name="pec" type="pec-type" minOccurs="0"/>
       <elem name="auto-registration" minOccurs="0"/>
       <elem name="serial-number" type="xs:string" minOccurs="0"/>
       <elem name="mac" type="xs:string" minOccurs="0"/>
       <elem name="disable" minOccurs="0"/>
       <elem name="pon-interface" type="xs:string"/>
       <elem name="domain" type="xs:string" minOccurs="0"/>
 
       <elem name="interface">
         <elem name="Ethernet" minOccurs="0" maxOccurs="256">
           <elem name="port" type="portType" key="true"/>
           <elem name="disable" minOccurs="0"/>
           <elem name="uplink" minOccurs="0"/>
           <elem name="pm-enable" minOccurs="0"/>
           <elem name="description" minOccurs="0" type="string40"/>
           <elem name="dot1ad-ethertype" type="dot1d-ether-type" 
                 default="8100"/>
           <elem name="duplex" type="duplex-type" default="full"/>
           <elem name="mtu" type="mtu-type" default="9216"/>
           <elem name="negotiation-auto" minOccurs="0"/>
           <elem name="speed" type="speed-type" minOccurs="0"/>
           <elem name="mirror-destination" minOccurs="0"/>
           <elem name="mirror" type="mirror-dir-type" minOccurs="0"/>
         </elem>
       </elem>
     </elem>
    </elem>
    



    <elem name="interface">
      <elem name="Ethernet" minOccurs="0" maxOccurs="256">
        <desc>Display Ethernet port configuration</desc>
        <!-- <callpoint id="Ethernet_hook" transactionHook="node" type="external"/> -->
        <!--<validate id="val_if_eth" type="external">
            <dependency>.</dependency>
            </validate>-->
        <elem name="port" type="portType" key="true"/>
        <elem name="disable" minOccurs="0"/>
        <elem name="pm-enable" minOccurs="0"/>
        <elem name="description" minOccurs="0" type="string40"/>
        <elem name="dot1ad-ethertype" type="dot1d-ether-type" default="8100"/>
        <elem name="mtu" type="mtu-type" default="9600"/>
        <elem name="negotiation-auto" minOccurs="0"/>
        <elem name="speed" type="speed-type" minOccurs="0"/>
        <elem name="duplex" type="duplex-type" minOccurs="0"/>
        <elem name="bundle-id" minOccurs="0" maxOccurs="1">
          <!-- <callpoint id="Ethernet_lacp_hook" transactionHook="node"  -->
          <!--            type="external"/> -->
          <!--<validate id="val_if_eth_lacp" type="external">
              <dependency>.</dependency>
              </validate>-->
          <elem name="id" type="bundle-id-type" key="true"/>
          <elem name="lacp" minOccurs="0">
            <elem name="mode" type="lacp-mode-type" default="active"/>
            <elem name="port-priority" type="lacp-port-priority-type" 
                  default="32768"/>
            <elem name="period" type="lacp-period-type" default="long"/>
          </elem>
        </elem>
        <elem name="mirror-destination" minOccurs="0"/>
        <elem name="mirror" type="mirror-dir-type" minOccurs="0"/>
        <elem name="crc-threshold" type="int1000" default="100"/>
        <!-- operational data test -->
        <elem name="status" config="false">
          <callpoint id="interfaces_status" type="external"/>
          <elem name="link-status" type="up-down-type" minOccurs="0"/>
          <elem name="admin-state" type="enable-disable-type" minOccurs="0"/>
          <elem name="speed" type="speed-type" minOccurs="0"/>
          <elem name="duplex" type="duplex-type" minOccurs="0"/>
          <elem name="negotiation-auto" type="yes-no-type" minOccurs="0"/>
          <elem name="mtu" type="xs:int"/>
        </elem>
        <elem name="qos" minOccurs="0">
          <desc>Ethernet QoS configuration</desc>
          <elem name="storm-control" type="xs:string" minOccurs="0"/>
          <elem name="scheduler" type="xs:string" minOccurs="0"/>
          <elem name="filter-policy-input" keyref="/msp/qos/access-list/acl-name"  minOccurs="0"/>
          <elem name="filter-policy-output" keyref="/msp/qos/access-list/acl-name" minOccurs="0"/>
          <elem name="untagged-qos" minOccurs="0">
            <elem name="fc" type="qos-fc-type"/>
            <elem name="color" type="qos-color-type"/>
          </elem>
          <elem name="map-priority-qos" keyref="/msp/qos/map/priority-qos/map-name" minOccurs="0"/>
          <elem name="map-qos-priority" keyref="/msp/qos/map/qos-priority/map-name" minOccurs="0"/>
          <elem name="map-dscp-qos" keyref="/msp/qos/map/dscp-qos/map-name" 
                minOccurs="0"/>
        </elem>
        <elem name="pluggable-module" minOccurs="0">
          <desc>Ethernet Pluggable module configuration</desc>
          <!-- <callpoint id="Ethernet_pluggable_module_hook" transactionHook="node"  -->
          <!--            type="external"/> -->
          <!--<validate id="val_if_eth_pluggable" type="external">
              <dependency>.</dependency>
              </validate>-->
          <elem name="expected-format" type="expected-format-type" minOccurs="0"/>
          <elem name="expected-rate" type="speed-type" minOccurs="0"/>
          <elem name="expected-wavelength" type="xs:unsignedInt" minOccurs="0"/>
          <elem name="expected-wdm-type" type="expected-wdm-type" minOccurs="0"/>
          <elem name="mismatch-check" minOccurs="0"/>
        </elem>
        <elem name="spanning-tree" minOccurs="0">
          <desc>Ethernet spanning tree configuration</desc>
          <!-- <callpoint id="Ethernet_stp_hook" transactionHook="node"  -->
          <!--            type="external"/> -->
          <elem name="edge-port" minOccurs="0"/>
          <elem name="hello-time" type="hello-time-type" default="2"/>
          <elem name="change-detection" minOccurs="0"/>
          <elem name="cost" type="xs:int" minOccurs="0"/>
          <elem name="port-priority" type="stp-port-priority-type" default="128"/>
          <elem name="instance" minOccurs="0" maxOccurs="256">
            <elem name="instance" type="stp-instance-type" key="true"/>
            <elem name="cost" type="xs:int" minOccurs="0"/>
            <elem name="port-priority" type="stp-port-priority-type" 
                  default="128"/>
          </elem>
        </elem>
        <elem name="vlan" minOccurs="0" maxOccurs="4094">
          <desc>Ethernet VLAN configuration</desc>
          <!-- <callpoint id="Ethernet_vlan_hook" transactionHook="node"  -->
          <!--            type="external"/> -->
          <!--<validate id="val_if_eth_vlan" type="external">
              <dependency>.</dependency>
              </validate>-->
          <elem name="vlan" keyref="/msp/vlan/vid" key="true"/>
          <elem name="vid" type="vlanid-string-list-type" minOccurs="0"/>
          <elem name="dot1ad-outer-vlan-id" type="vlanid-type" minOccurs="0"/>
          <elem name="pbits" minOccurs="0" type="pbits-list-type"/>
          <elem name="protocol" minOccurs="0" type="xs:string"/>
          <elem name="catch-all" minOccurs="0"/>
          <elem name="endpoint" minOccurs="0"/>
          <elem name="map" minOccurs="0">
            <elem name="input" minOccurs="0">
              <elem name="vid" minOccurs="0" maxOccurs="16">
                <elem name="vid" type="vlanid-none-all-untag-num-type" 
                      key="true"/>
                <elem name="to" type="vlanid-none-num-type"/>
              </elem>
              <elem name="pbit" minOccurs="0" maxOccurs="8">
                <elem name="pcb" type="xs:int" key="true"/>
                <elem name="to" type="vlanid-type"/>
              </elem>
              <elem name="protocol" minOccurs="0" maxOccurs="1">
                <elem name="nameOrNumber" type="string16" key="true"/>
                <elem name="to" type="vlanid-type"/>
              </elem>
            </elem>
            <elem name="output" minOccurs="0">
              <elem name="vid" minOccurs="0" maxOccurs="256">
                <elem name="vid" type="vlanid-none-all-untag-num-type" 
                      key="true"/>
                <elem name="to" type="vlanid-none-untag-num-type"/>
              </elem>
            </elem>
          </elem>
          <elem name="mep" minOccurs="0" maxOccurs="256">
            <!-- <callpoint id="Ethernet_mep_hook" transactionHook="node"  -->
            <!--            type="external"/> -->
            <elem name="level" type="xs:int" key="true"/>
            <elem name="ma" type="xs:string" minOccurs="0"/>
            <elem name="id" type="xs:int" minOccurs="0"/>
            <elem name="ccm-priority" type="xs:int" default="7"/>
            <elem name="ccm-tx" minOccurs="0"/>
            <elem name="disable" minOccurs="0"/>
            <elem name="remote-mep" minOccurs="0" maxOccurs="256">
              <elem name="mepid" type="mep-id-type" key="true"/>
            </elem>
          </elem>
          <elem name="mirror" type="mirror-dir-type" minOccurs="0"/>
          <elem name="qos" minOccurs="0">
            <elem name="filter-policy-input" keyref="/msp/qos/access-list/acl-name" minOccurs="0"/>
            <elem name="filter-policy-output" keyref="/msp/qos/access-list/acl-name" minOccurs="0"/>
            <elem name="override-qos" minOccurs="0">
              <elem name="fc" type="qos-fc-type"/>
              <elem name="color" type="qos-color-type"/>
            </elem>
            <elem name="service-policy-input" 
                  keyref="/msp/qos/classifier/classifier-name" minOccurs="0"/>
            <elem name="service-policy-output" 
                  keyref="/msp/qos/classifier/classifier-name" minOccurs="0"/>
            <elem name="policer-input" keyref="/msp/qos/policer/policer-name" 
                  minOccurs="0"/>
            <elem name="policer-output" keyref="/msp/qos/policer/policer-name" 
                  minOccurs="0"/>
          </elem>
        </elem>
      </elem>
    </elem>

    <elem name="qos">
      <elem name="access-list" minOccurs="0" maxOccurs="512">
        <elem name="acl-name" type="string80" key="true"/>
      </elem>
      <elem name="classifier" minOccurs="0" maxOccurs="256">
        <!-- <callpoint id="qos_classifier_hook" transactionHook="node"  -->
        <!--            type="external"/> -->
        <elem name="classifier-name" type="string39" key="true"/>
      </elem>
      <elem name="map">
        <elem name="priority-qos" minOccurs="0" maxOccurs="256">
          <elem name="map-name" type="string39" key="true"/>
          <elem name="priority" minOccurs="0" maxOccurs="256">
            <elem name="pcp" type="qos-pcp-type" key="true"/>
            <elem name="de" type="qos-de-type" key="true"/>
            <elem name="qos">
              <elem name="fc" type="qos-fc-type"/>
              <elem name="color" type="qos-color-type"/>
            </elem>
          </elem>
        </elem>
        <elem name="qos-priority" minOccurs="0" maxOccurs="256">
          <elem name="map-name" type="string39" key="true"/>
          <elem name="qos" minOccurs="0" maxOccurs="256">
            <elem name="fc" type="qos-fc-type" key="true"/>
            <elem name="color" type="qos-color-type" key="true"/>
            <elem name="priority">
              <elem name="pcp" type="qos-pcp-type"/>
              <elem name="de" type="qos-de-type"/>
            </elem>
          </elem>
        </elem>
        <elem name="dscp-qos" minOccurs="0" maxOccurs="256">
          <elem name="map-name" type="string39" key="true"/>
          <elem name="dscp" minOccurs="0" maxOccurs="256">
            <elem name="dscp" type="dscp-type" key="true"/>
            <elem name="qos">
              <elem name="fc" type="qos-fc-type"/>
              <elem name="color" type="qos-color-type"/>
            </elem>
          </elem>
        </elem>
      </elem>

      <elem name="policer" minOccurs="0" maxOccurs="256">
        <elem name="policer-name" type="string39" key="true"/>
        <elem name="algorithm" type="policer-algorithm" minOccurs="0"/>
        <elem name="color-aware" minOccurs="0"/>
        <elem name="cir" minOccurs="0">
          <elem name="value" type="xs:int"/>
          <elem name="unit" type="qos-policer-cir-type"/>
        </elem>
        <elem name="cbs" minOccurs="0">
          <elem name="value" type="xs:int"/>
          <elem name="unit" type="qos-policer-cbs-type"/>
        </elem>
        <elem name="eir" minOccurs="0">
          <elem name="value" type="xs:int"/>
          <elem name="unit" type="qos-policer-eir-type"/>
        </elem>
        <elem name="ebs" minOccurs="0">
          <elem name="value" type="xs:int"/>
          <elem name="unit" type="qos-policer-ebs-type"/>
        </elem>
      </elem>
    </elem>

    <elem name="vlan" minOccurs="0" maxOccurs="4094">
      <desc>Global VLAN profile configuration</desc>
      <!--<validate id="val_vlan" type="external">
          <dependency>.</dependency>
          </validate>-->
      <elem name="vid" type="vlanid-type" key="true"/>
      <elem name="description" type="string80" minOccurs="0"/>
      <elem name="tag-ethertype" type="vlan-tag-ether-type" default="8100"/>
      <elem name="connection-type" type="connection-type" default="e-tree"/>
    </elem>
  </elem>
</confspec>


