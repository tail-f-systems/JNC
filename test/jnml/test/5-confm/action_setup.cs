<!-- M3_BEGIN part1 -->
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          targetNamespace="http://tail-f.com/ns/simple/1.0"
          mount="/">
  <elem name="actions">
    <action name="halt">
      <actionpoint id="actions" type="external"/>
    </action>
    <action name="shutdown">
      <actionpoint id="actions" type="external"/>
      <params>
        <elem name="timeout" type="xs:duration" default="PT60S"/>
        <elem name="message" type="xs:string" minOccurs="0"/>
        <elem name="flags" type="xs:positiveInteger" constant="42"/>
        <elem name="options">
          <elem name="rebootAfterShutdown" type="xs:boolean"
                default="false"/>
          <elem name="forceFsckAfterReboot" type="xs:boolean"
                default="false"/>
          <elem name="powerOffAfterShutdown" type="xs:boolean"
                default="true"/>
        </elem>
        <elem name="flags2" type="xs:positiveInteger" constant="42"/>
      </params>
    </action>        
    <action name="setSystemClock">
      <actionpoint id="actions" type="external"/>
      <params>
        <elem name="clockSettings" type="xs:dateTime"/>
        <elem name="utc" type="xs:boolean" default="true"/>
        <elem name="syncHarwdareClock" type="xs:boolean" default="false">
          <desc>Make sure that the hardware clock synchronized.</desc>
        </elem>
      </params>
      <result>
        <elem name="systemClock" type="xs:dateTime"/>
        <elem name="hardwareClock" type="xs:dateTime"/>
      </result>
    </action>
    <elem name="dyn" maxOccurs="64">
      <elem name="dynKey" type="xs:string" key="true"/>
      <action name="halt">
        <actionpoint id="actions" type="external"/>
      </action>
    </elem>
  </elem>
</confspec>
