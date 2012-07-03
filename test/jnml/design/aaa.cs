<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:confd="http://tail-f.com/ns/confd/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          mount="/">

  <xs:simpleType name="dataOperationType">
    <xs:restriction base="xs:string">
      <xs:enumeration value="r"/>
      <xs:enumeration value="rw"/>
      <xs:enumeration value="rx"/>
      <xs:enumeration value="rwx"/>
      <xs:enumeration value="wx"/>
      <xs:enumeration value="x"/>
      <xs:enumeration value="w"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="cmdOperationType">
    <xs:restriction base="xs:string">
      <xs:enumeration value="r"/>
      <xs:enumeration value="rx"/>
      <xs:enumeration value="x"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="Action">
    <xs:restriction base="xs:string">    
      <xs:enumeration value="accept"/>
      <xs:enumeration value="reject"/>
      <xs:enumeration value="accept_log"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="passwdStr">
    <xs:restriction base="confd:MD5DigestString">
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="levelInt">
    <xs:restriction base="xs:int">
      <xs:minInclusive value="0"/>
      <xs:maxInclusive value="15"/>
    </xs:restriction>
  </xs:simpleType>

  <xs:simpleType name="modeStr">
    <xs:union memberTypes="xs:string builtinModes"/>
  </xs:simpleType> 

  <xs:simpleType name="builtinModes">
    <xs:restriction base="xs:string">
      <xs:enumeration value="exec"/>
      <xs:enumeration value="configure"/>
    </xs:restriction>
  </xs:simpleType>

  <elem name="aaa">
    <!-- 
         If we wanted to populate aaa.fxs using external data we
         would have provided a callpoint for external (or Javascript) here:

         <callpoint id="aaa" type="external"/>
         
         The confd_aaa_bridge.c is such an example implementation of a C
         program which registers on a callpoint with id equal to "aaa".
         
         In this example this callpoint has been added as a annotation
         in bridge.csa. This way we can have a single aaa.cs file for
         both implementations.
    -->
    <elem name="authentication">
      <elem name="users">
        <elem name="user" minOccurs="0" maxOccurs="1024">
          <elem name="name" type="xs:string" key="true"/>
	  <elem name="uid" type="xs:int" />
	  <elem name="gid" type="xs:int" />
          <elem name="password" type="passwdStr"/>
          <elem name="ssh_keydir" type="xs:string"/>
          <elem name="homedir" type="xs:string"/>
        </elem>
      </elem>
      <elem name="groups">
        <elem name="group" minOccurs="0" maxOccurs="32">
          <elem name="name" type="xs:string" key="true"/>
	  <elem name="gid" type="xs:int" minOccurs="0"/>
          <elem name="users" type="xs:string"/>
        </elem>
      </elem>
    </elem>

    <elem name="authorization">

      <elem name="cmdrules">
       <elem name="cmdrule" minOccurs="0" maxOccurs="unbounded">
	 <elem name="index" type="xs:unsignedInt" key="true">
	   <indexedView/>
	 </elem>
	 
         <!-- context is either "cli", "webui", "netconf" "*", or any context
              as provided by maapi -->
         
         <elem name="context" type="xs:string"/>
         <elem name="command" type="xs:string"/>
         <elem name="group" type="xs:string"/>
         <elem name="ops" type="cmdOperationType"/>
         <elem name="action" type="Action"/>
       </elem>
      </elem>
      <elem name="datarules">
       <elem name="datarule" minOccurs="0" maxOccurs="unbounded">
         <elem name="index" type="xs:unsignedInt" key="true">
	   <indexedView/>
	 </elem>
         <elem name="namespace" type="xs:string"/>
         <elem name="context" type="xs:string" default="*"/>
         <elem name="keypath" type="xs:string"/>
         <elem name="group" type="xs:string"/>
         <elem name="ops" type="dataOperationType"/>
         <elem name="action" type="Action"/>
       </elem>
      </elem>
    </elem>

    <elem name="ios" minOccurs="0">

      <!-- -->
      <elem name="level" minOccurs="2" maxOccurs="unbounded">
	<elem name="nr" type="levelInt" key="true"/>
	<elem name="secret" type="passwdStr" minOccurs="0"/>
	<elem name="password" type="passwdStr" minOccurs="0"/>
	<elem name="prompt" type="xs:string" default="\h# "/>
      </elem>

      <!-- -->
      <elem name="privilege" minOccurs="0" maxOccurs="unbounded">
	<elem name="mode" type="modeStr" key="true"/>
	<elem name="level" minOccurs="1" maxOccurs="15">
	  <elem name="nr" type="levelInt" key="true"/>
	  <elem name="command" minOccurs="0" maxOccurs="unbounded">
	    <elem name="name" type="xs:string" key="true"/>
	  </elem>
	</elem>
      </elem>
    </elem>
  </elem>
</confspec>
