<!-- -*- nxml -*- -->
<chapter>
  <title>INM Introduction</title>

M3_IF CONFM
  <section>
    <title>About INM</title>
    <body>
      <p>
        INM is the lowest layer library in the ConfM product. All the
        generated Java classes extend classes from the INM library.
        INM also contains all NETCONF specific classes, such as 
        classes for Session management and actual NETCONF commands.
      </p>

      <p>
        INM also comes with a detailed API specification in Javadoc
        format.
      </p> 

      <p>
        When we manage devices that are not defined by a confspec, 
        we must use the INM classes. The INM classes are unaware of
        data model and they simply manipulate a general XML tree.
        No runtime checks are made on restrictions in the data model, 
        thus using INM instead of ConfM makes the risk of sending
        bad data to the agent higher. The advantage of finding errors
        in the manager as opposed to getting error messages back from the
        agent is of course better debug-ability.
      </p>
    </body>
  </section>

M3_ELSIF INM
  <section>
    <title>About INM</title>
    <body>
      <p>
	This User Guide describes the Instant NETCONF Manager (INM) Java
	library. It provides an overview of the architecture of the
	library, its APIs including usages examples, and guidelines on
	how to extend the feature set.
      </p>
      <p>
	This guide is designed for developers who are planning to use
	the library. To use this document, you need a broad
	understanding of the Java programming language, XML, and the
	NETCONF protocol. Pointers to additional sources of information
	can be found in the <em>Recommended Reading</em> section.
      </p>

      <p>
        INM also comes with a detailed API specification in Javadoc
        format.
      </p> 

      <p>
	The examples in this guide requires access to a NETCONF server
	implementation. 
      </p>
    </body>
  </section>
M3_ENDIF

M3_IF INM

  <section>
    <title>Instant NETCONF Manager Overview</title>
    <body>
      <p>
        The <em>Instant NETCONF Manager</em> (INM) is a Java library
        that allows developers to rapidly integrate a fully featured
        <em>NETCONF</em> client into existing network management
        applications.
      </p>

      <p>
        The NETCONF protocol provides a programmatic interface
        enabling management applications to configure and monitor
        network elements. NETCONF uses <em>XML</em> for payload and
	protocol messages. It provides standardized mechanisms to
	install, retrieve, manipulate, and delete the configuration
	and operational data on network elements.
      </p>
      
      <p>

        Development of a southbound NETCONF interface requires
        implementation of the basic NETCONF operations, session
        management, and the use of a secure transport protocol. INM
        provides these features via a light weight class
        library. Developers work with configuration and operational data using a tree
        structured object model. This hides the complexity of the NETCONF
        protocol and its payload. Once an application or user initiates a task, for
        example changes the IP address on a specific interface, all
        aspects of the transaction are handled by the library. The
        figure below shows how the Instant NETCONF Manager fits into an
        existing element management application.
      </p>

      <img src="../pics/inm.gif"
           width="12cm"
           caption="NETCONF client in an existing EMS/NMS"/>

      <p>
        INM is data model agnostic, i.e. it can be used to manage any
        NETCONF device without previous knowledge about the data being
        modeled by the device. This is in stark contrast to ConfM which
        requires exact knowledge of the data model at the managed device.
      </p>
    </body>
  </section>

  <section>
    <title>Recommended Reading</title>
    <body>
      <p>
        If the following resources are not familiar we recommend
        a closer look at them before you proceed:
      </p>
      
      <dl termtype="em">
        <dt>RFC 4741: NETCONF Configuration Protocol</dt>
        <dd><c>ftp://ftp.rfc-editor.org/in-notes/rfc4741.txt</c></dd>
        <dt>An XPATH tutorial</dt>
        <dd><c>http://www.w3schools.com/xpath/default.asp</c></dd>
      </dl>
      
      <p>
        Other topics introduced later in this document may be
        studied in-depth using the following resources:
      </p>

      <dl termtype="em">
	<dt>An XML tutorial</dt>
	<dd><c>http://www.w3schools.com/xml/default.asp</c></dd>
	<dt>An XML Schema tutorial</dt>
	<dd><c>http://www.w3schools.com/schema/default.asp</c></dd>
	<dt>RFC 4742: Using the NETCONF Configuration Protocol over
	Secure Shell (SSH)</dt>
	<dd><c>ftp://ftp.rfc-editor.org/in-notes/rfc4742.txt</c></dd>
      </dl>

    </body>
  </section>
  
  <section>
    <title>INM and the NETCONF Protocol</title>
    <body>
      <p>
        The NETCONF protocol is an IETF standard which defines a
        programmatic interface enabling applications to automate the
        configuration and monitoring of network elements.
      </p>

      <p>
        The NETCONF protocol can conceptually be partitioned into four
        layers, and the INM library is structured in a similar way. <em>Transport interface</em> defines methods for the transport
        protocol. <em>NetconfSession</em> contains the RPC-based
        mechanism and the standard NETCONF
        operations and <em>Element</em> represents the configuration
        and operational data as a tree of elements.
      </p>
      
      <code caption="NETCONF protocol layers"><![CDATA[
        Layer                      Example                          Java
       +-------------+   +-----------------------------+   +---------------------+
   (4) |   Content   |   |     Configuration data      |   |       Element      |
       +-------------+   +-----------------------------+   +---------------------+
              |                        |                              |
       +-------------+   +-----------------------------+   +---------------------+
   (3) | Operations  |   | <get-config>, <edit-config> |   |                     |
       +-------------+   +-----------------------------+   |                     | 
              |                        |                   |   NetconfSession    |
       +-------------+   +-----------------------------+   |                     |
   (2) |     RPC     |   |    <rpc>, <rpc-reply>       |   |                     |
       +-------------+   +-----------------------------+   +---------------------+ 
              |                        |                              |
       +-------------+   +-----------------------------+   +---------------------+
   (1) |  Transport  |   |   BEEP, SSH, SSL, console   |   | Transport interface |
       |   Protocol  |   |                             |   | SSHTransport        | 
       +-------------+   +-----------------------------+   +---------------------+ 
]]></code>            

      <p>
        The NETCONF protocol defines a set of operations which can be
        used to manipulate configurations:
      </p>

      <ul>
        <li><c>&lt;get-config&gt;</c></li>
        <li><c>&lt;edit-config&gt;</c></li>
        <li><c>&lt;delete-config&gt;</c></li>
        <li><c>&lt;lock&gt;</c></li>
        <li><c>&lt;unlock&gt;</c></li>
        <li><c>&lt;get&gt;</c></li>
        <li><c>&lt;close-session&gt;</c></li>
        <li><c>&lt;kill-session&gt;</c></li>
        <li><c>&lt;commit&gt;</c></li>
        <li><c>&lt;discard-changes&gt;</c></li>
        <li><c>&lt;copy-config&gt;</c></li>
        <li><c>&lt;validate&gt;</c></li>
      </ul>
      
      <p>
        The NETCONF protocol also defines a set of mandatory and
        optional capabilities:
      </p>

      <ul>
        <li><c>:writable-running</c></li>
        <li><c>:candidate</c></li>
        <li><c>:confirmed-commit</c></li>
        <li><c>:rollback-on-error</c></li>
        <li><c>:validate</c></li>
        <li><c>:startup</c></li>
        <li><c>:url</c></li>
        <li><c>:xpath</c></li>
      </ul>

      <p>
        All operations and capabilities are described in detail in RFC 4741.
      </p>

      <p>
        INM supports all of these operations and capabilities but also
        makes it possible to write custom capabilities and
        operations. This is described in the <em>NETCONF
        Extensions</em> chapter in this document.
      </p>

      <p>
        The Tail-f ConfD NETCONF server provides additional custom
        capabilities also supported by INM:
	<dl termtype="code">
	  <dt>:transaction</dt>
	  <dd>A capability which introduces a number of custom
	  operations to provide transaction semantics</dd>
	  <dt>:with-defaults</dt>
	  <dd>A capability which introduces an alternative handling of
	  default values</dd>
	</dl>
	These custom
	capabilities and related operations are described in the
	ConfD User Guide.
      </p>
    </body>
  </section>

M3_ENDIF

  <section>
    <title>INM Examples</title>
    <body>
      <p>
        The purpose of INM is to provide a Java API for managing
        configuration and operational data on NETCONF enabled devices. INM
        provides two main classes for this purpose:
      </p>

      <dl termtype="em">
        <dt>Element</dt>
        <dd>Represents configuration and operational data trees</dd>
        <dt>NetconfSession</dt>
        <dd>Performs appropriate NETCONF operations using
        <em>Element</em> trees</dd>
      </dl>

      <p>
        The Element and NetconfSession classes are described in
        detail later in this document and in the Javadoc
        documentation.
      </p>

      <p>
        To give you a quick start on how these classes can be used we
        provide three initial examples. These examples are
        available in the INM distribution and can be run as is. The
        examples assume that a NETCONF device is running on
        <c>netconf.example.com:2022</c> using SSH for transport.
      </p>

      <!--
      <p>
      Feel free
      to use this public NETCONF device for your own experimentation as
      long as you are aware of that this device is automatically 
      reinitialized each GMT 23:05.
      </p>
      -->

      <p>
	The examples below require that the
	NETCONF device on <c>netconf.example.com:2022</c> provides
	access to configuration data as defined by the following XML
	schema:
      </p>

      <code caption="simple.xsd"><![CDATA[
<xs:schema targetNamespace="http://example.com/ns/simple/1.0"
           xmlns="http://example.com/ns/simple/1.0"
           xmlns:simple="http://example.com/ns/simple/1.0"
           xmlns:xs="http://www.w3.org/2001/XMLSchema"
           elementFormDefault="qualified"
           attributeFormDefault="unqualified"
           xml:lang="en">
  <xs:element name="hosts">
    <xs:complexType>
      <xs:sequence>
        <xs:element name="host" minOccurs="0" maxOccurs="64">
          <xs:complexType>
            <xs:sequence>
              <xs:element name="name" type="xs:string"/>
              <xs:element name="enabled" type="xs:boolean" 
                          minOccurs="0" confd:default="true"/>
              <xs:element name="numberOfServers" type="xs:unsignedInt"/>
            </xs:sequence>
          </xs:complexType>
        </xs:element>
      </xs:sequence>
    </xs:complexType>
    <xs:key name="key_hosts_host">
      <xs:selector xpath="simple:host"/>
      <xs:field xpath="simple:name"/>
    </xs:key>
  </xs:element>
</xs:schema>
]]></code>

      <p>
	If you have access to Tail-f's ConfD NETCONF device you can
	use the following <em>confspec</em> file to run the examples:
      </p>

      <code caption="simple.cs"><![CDATA[
M3_INCLUDE ../../../../examples/inm/demo/simple.cs ALL
]]></code>
    </body>
    
    <subsection>
      <title>Example 1: Get the Configuration</title>
      <body>
        <p>
          In the first example we just connect to the NETCONF device,
          ask for its configuration, convert it to XML and print it
          to the console.
        </p>

        <code caption="GetConfig.java"><![CDATA[
M3_INCLUDE ../../../examples/inm_intro/GetConfig.java ALL
]]></code>

        <p>
          If you run the above the following configuration is
          extracted from the device and printed on the console:
        </p>

        <pre><![CDATA[
$ java -classpath .:../INM.jar:../ganymed/ganymed-ssh2-build251beta1.jar GetConfig
<hosts xmlns="http://example.com/ns/simple/1.0">
   <host>
      <name>cecilia</name>
      <enabled>true</enabled>
      <numberOfServers>5</numberOfServers>
   </host>
   <host>
      <name>ellen</name>
      <enabled>true</enabled>
      <numberOfServers>4711</numberOfServers>
   </host>
   <host>
      <name>joe</name>
      <enabled>true</enabled>
      <numberOfServers>5</numberOfServers>
   </host>
   <host>
      <name>vera</name>
      <enabled>false</enabled>
      <numberOfServers>42</numberOfServers>
   </host>
</hosts>
]]></pre>

        <p>
          The extracted device configuration contains four hosts each
          with a unique name.
        </p>

        <p>
          Note how the configuration conforms to the XML schema
          introduced above.
        </p>

      </body>
    </subsection>

    <subsection>
      <title>Example 2: Update the Configuration</title>
      <body>
        <p>
          In this example we extract only a part of the device
          configuration using a subtree filter. We manipulate that
          part and send it back to the device.
        </p>
        
        <code caption="UpdateConfig.java"><![CDATA[
M3_INCLUDE ../../../examples/inm_intro/UpdateConfig.java ALL
]]></code>

        <p>
          A subtree filter is a tree structure as well, so we use a
          call to <em>Element.create</em> to instantiate a subtree
          filter specifying that <em>host</em> entries with
          <em>numberOfServers</em> equal to "5" should be chosen. We
          then call <em>NetconfSession.getConfig</em> with the subtree
          filter to extract the appropriate hosts. We then locally
          marks the host with <em>name</em> equals to "joe" for
          deletion with a call to
          <em>Element.markDelete</em>. Finally we send the change
          back to the device with a call to
          <em>NetconfSession.editConfig</em>.
        </p>

        <p>
          If you run the above the configuration is manipulated on the
          device and the following is printed on the console:
        </p>
          
        <pre><![CDATA[
$ java -classpath .:../INM.jar:../ganymed/ganymed-ssh2-build251beta1.jar UpdateConfig
Current config:
<hosts xmlns="http://example.com/ns/simple/1.0">
   <host>
      <name>cecilia</name>
      <enabled>true</enabled>
      <numberOfServers>5</numberOfServers>
   </host>
   <host>
      <name>joe</name>
      <enabled>true</enabled>
      <numberOfServers>5</numberOfServers>
   </host>
</hosts>

Resulting config:
<hosts xmlns="http://example.com/ns/simple/1.0">
   <host>
      <name>cecilia</name>
      <enabled>true</enabled>
      <numberOfServers>5</numberOfServers>
   </host>
   <host>
      <name>ellen</name>
      <enabled>true</enabled>
      <numberOfServers>4711</numberOfServers>
   </host>
   <host>
      <name>vera</name>
      <enabled>false</enabled>
      <numberOfServers>42</numberOfServers>
   </host>
</hosts>
]]></pre>       

        <p>
          In this example we used a subtree filter to extract a set of
          hosts. As an alternative an XPATH expression could have been
          used instead provided that the NETCONF server we talk to
	  supports the <c>:xpath</c> capability.
        </p>
      </body>
    </subsection>
    
    <subsection>
      <title>Example 3: Revert the Configuration</title>
      <body>
	<p>
          In this example we revert the configuration back to its
	  original content. In example 2 we removed a host which we
	  here reinsert.
	</p>
        
        <code caption="RevertConfig.java"><![CDATA[
M3_INCLUDE ../../../examples/inm_intro/RevertConfig.java ALL
]]></code>

	<p>
	  This example is also available in the INM distribution. Try
	  it out and see what happens.
	</p>
      </body>
    </subsection>
  </section>


M3_IF INM
  <section>
    <title>Next Steps</title>
    <body>
      <p>
        The remaining chapters deal with the following topics:
      </p>
      
      <dl termtype="em">
        <dt>An overview of the INM classes</dt>
        <dd>Briefly goes through all classes provided by INM.</dd>
        <dt>Elements</dt>
        <dd>Describes the functionality provided by the Element
        class</dd>
        <dt>NETCONF sessions</dt>
        <dd>Describes the functionality provided by the NetconfSession
        class</dd> 
        <dt>NETCONF extensions</dt>
        <dd>Describes how to implement custom capabilities and
        operations.</dd>
      </dl>
      
      <p>
        The INM distribution also comes bundled with Javadoc documentation.
      </p>
    </body>
  </section>


M3_ENDIF



</chapter>
