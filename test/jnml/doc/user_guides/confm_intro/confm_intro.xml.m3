<!-- -*- nxml -*- -->
<chapter>
  <title>ConfM Introduction</title>
  <section>
    <title>About This Document</title>
    <body>
      <p>
        This User Guide describes the ConfM configuration management
        library. It provides an overview of the architecture
        of the  library, its APIs including usages examples, and
        guidelines on how to extend the feature set.
      </p>

      <p>
        ConfM also comes with a detailed API specification in Javadoc
        format.
      </p> 

      <p>
        This guide is designed for developers who are planning to use
        the library. To use this document, we need a broad
        understanding of the Java programming language, XML, and the
        NETCONF protocol. Pointers to additional sources of information
        can be found in the <em>Recommended Reading</em> section.
      </p>

      <p>
        The examples in this guide requires access to either Tail-f's
        ConfD/INA NETCONF server implementation or another NETCONF
        server. If we communicate with 
        a Tail-F's NETCONF server, we can utilize the data model at the server
        and compile a set of Java classes explicitly for the data model
        at the managed device whereas if we communicate with another
        NETCONF server, we must revert to a lower lever API that is 
        not aware of the data model at the managed device.
        All examples have been tested with the Tail-f
        NETCONF agent,
      </p>

    </body>
  </section>
  <section>
    <title>ConfM Overview</title>
    <body>
      <p>
        <em>ConfM</em> is a Java library
        that allows developers to rapidly integrate a fully featured
        type safe <em>NETCONF</em> client into existing network management
        applications.
      </p>

      <p>
        The NETCONF protocol provides a programmatic interface
        enabling management applications to configure and monitor
        network elements. NETCONF uses XML for payload and
        protocol messages. It provides standardized mechanisms to
        install, retrieve, manipulate, and delete the configuration
        and operational data on network elements.
      </p>

      <p>
        Development of a southbound NETCONF interface requires
        implementation of the basic NETCONF operations, session
        management, and the use of a secure transport protocol. ConfM
        provides these features via a light weight class
        library. Developers work with configuration and operational 
        data using a tree
        structured object model. This hides the complexity of the NETCONF
        protocol and its payload. Once an application or user initiates 
        a task, for
        example changes the IP address on a specific interface, all
        aspects of the transaction are handled by the library. The
        figure below shows how ConfM fits into an
        existing element management application.
      </p>

      <img src="../pics/confm.gif"
           width="14cm"
           caption="ConfM in an existing EMS/NMS"/>

      <p>
        ConfM is a layered library which is aware of
        the data model at the managed device. It sits on top INM
        (Instant NETCONF Manager library) which is data model
        agnostic, i.e. it can be used to manage any NETCONF device
        without previous knowledge about the data being modeled by the
        device. The ConfM library is layered on top of the INM
        library. Using Java terminology it extends INM.
      </p>

      <p>
        ConfM is a Java library. It is by no means an NMS solution itself.
        ConfM is typically a core component in an EMS/NMS solution. It is
        typically used to manipulate pieces of configuration data through
        its generated Java classes and the NETCONF parts of the library
        are typically used in the Mediation layer to communicate
        with the managed devices.
      </p>
    </body>
  </section>

  <section>
    <title>Recommended Reading</title>
    <body>
      <p>
        If the following resources are not familiar we recommend
        a closer look at them before we proceed:
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

      <p>
        Furthermore the data modeling language (confspecs) used by 
        ConfD/INA (from now on only refered to as ConfD) must be
        understood. The data models defined through
        confspecs are compiled into Java classes that are used to
        manipulate instances of configuration trees.
      </p>
      <p>
        The next chapter in this User Guide describes the <em>confspec</em>
        data modeling language used by Tail-f's NETCONF servers.
        The chapter contains a thorough explanation
        of the XML based confspec language used in ConfD to model
        the configuration as well as the operational data of a device.
      </p>

    </body>
  </section>
  
  <section>
    <title>ConfM, INM and the NETCONF Protocol</title>
    <body>
      <p>
        The NETCONF protocol is an IETF standard which defines a
        programmatic interface enabling applications to automate the
        configuration and monitoring of network elements.
      </p>

      <p>
        The NETCONF protocol can conceptually be partitioned into four
        layers, and the INM library is structured in a similar way.
        <em>Transport interface</em> defines methods for the transport
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
   (3) | Operations  |   | get-config, edit-config     |   |                     |
       +-------------+   +-----------------------------+   |                     | 
              |                        |                   |   NetconfSession    |
       +-------------+   +-----------------------------+   |                     |
   (2) |     RPC     |   |       rpc, rpc-reply        |   |                     |
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
        ConfM and INM support all of these operations and 
        capabilities but also
        makes it possible to write custom capabilities and
        operations. This is described in the <em>NETCONF
        Extensions</em> chapter in this document.
      </p>

      <p>
        The Tail-f NETCONF servers provides additional custom
        capabilities also supported by ConfM:
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
        ConfD and INA User Guides.
      </p>
    </body>
  </section>

  

  <section>
    <title>ConfM Introductory Example</title>
    <body>
      <p>
        In this section we provide a simple introductory example on how
        to use ConfM. The assumption is that we are configuring a
        NETCONF device running a Tail-f NETCONF server. The configuration
        of the device is defined in the file <em>hosts.cs</em>:
      </p>
      <code caption="hosts.cs"><![CDATA[
<?xml version="1.0"?>
<confspec xmlns="http://tail-f.com/ns/confspec/1.0"
          xmlns:xs="http://www.w3.org/2001/XMLSchema"
          targetNamespace="http://acme.com/ns/simple/1.0">
  <elem name="hosts" >
    <elem name="host" minOccurs="0" maxOccurs="64">      
      <elem name="name" type="xs:string" key="true" />
      <elem name="enabled" type="xs:boolean" default="true"/>
      <elem name="numberOfServers" type="xs:unsignedInt"/>
    </elem>
  </elem>  
</confspec>
]]></code> 

      <p>
        The above confspec defines the data model for the managed device.
        The notation used by confspecs is as described in the 
        <em>Confspec Data Modeling</em> chapter.
      </p>

      <p>
        At the manager we wish to use exactly the same
        file to compile our Java classes that can be used to manipulate
        instances of a configuration tree.
      </p>

      <p>
        We start by compiling the data model file into 
        an <em>fxs</em> file. This is a binary data model file that can be
        loaded into the management system at the
        managed device. The whole process of compiling the data model
        from the device and using the output of that compilation in the
        EMS/NMS code is illustrated by the following picture:
      </p>


      <img src="../pics/confm-classes.jpg"
           width="16cm"
           caption="Using the device data model in the EMS"/>


      <p>
        The first stage is to generate a <em>hosts.fxs</em> file from
        a <em>hosts.cs</em> data model file. The second stage is to
        use the <em>--confm-emit-java</em> confdc option generate Java
        code which can be used to create and manipulate configuration
        trees for the <c>http://acme.com/ns/simple/1.0</c> namespace.
      </p>

      <pre> <![CDATA[
# confdc -c hosts.cs
# confdc -l -o hosts.fxs -- hosts.xso
# mkdir src/gen
# confdc --confm-emit-java src/gen hosts.fxs
Generating ConfM Java classes from the namespace: 'http://acme.com/ns/simple/1.0'
Java package: simple
Generating container element class: src/gen/Hosts.java
Generating dynamic element class: src/gen/Host.java
Generating root class: src/gen/Simple.java
Generating Javadoc package documentation: src/gen/package.html
done
]]></pre> 

      <p>
        Before looking at the NETCONF parts in this example, let us start
        by having a look at the generated Java code. 
        First we have a top level class, <em>Simple.java</em> which looks like:
      </p>
      <code caption="Simple.java"><![CDATA[
public class Simple {

    public static final String NAMESPACE = "http://acme.com/ns/simple/1.0";

    public static final String PREFIX = "simple";

    public static void enable() {
        Container.setPackage(NAMESPACE,"simple");
    }
      
      ]]></code>

      <p>
        Before we start to use the 
        <em>"http://acme.com/ns/simple/1.0"</em> namespace
        we must call <em>Simple.enable()</em>. This will register this 
        namespace
        with the ConfM XML parser as a namespace. In effect this means that
        when we parse XML data from this namespace, we will get a tree
        of objects consisting of our generated classes.
        More on this later.
      </p>

      <p>
        Next we take a look at the signatures for the public methods
        in the generated top level class <em>Hosts.java</em>. 
      </p>
      <code caption="Hosts.java"><![CDATA[
      
package simple;

import com.tailf.confm.*;
import com.tailf.inm.*;
/**
 * This class represents a "hosts" element
 * from the namespace "http://acme.com/ns/simple/1.0".
 */
public class Hosts extends Container {
    public Hosts() 
    public Host getHost(String name)
    public ElementChildrenIterator hostIterator() 
    public Host addHost(Host host)
    public Host addHost(String name)
    public Host addHost()
    public void deleteHost(String name)
}]]></code>
      <p>
        Now we have enough code to actually do something. Consider the following snippet
        of Java code:
      </p>
      <code caption="Creating a tree"><![CDATA[
            Simple.enable();
            Hosts h = new Hosts();
            h.addHost("Jupiter");
            h.addHost("Saturn");
            System.out.println(h.toXMLString());

            ]]></code>
      <p>
        The above code snippet creates a tree and prints:
      </p>
      <pre><![CDATA[
<hosts xmlns="http://acme.com/ns/simple/1.0" 
       xmlns:simple="http://acme.com/ns/simple/1.0">
   <host>
      <name>Jupiter</name>
   </host>
   <host>
      <name>Saturn</name>
   </host>
</hosts>

]]></pre>
      <p>
        The above demonstrates how we easily can create a partial
        configuration tree.
        Snippets of the configuration similar to the one above 
        can be sent on the NETCONF protocol to a managed device. 
        The NETCONF protocol defines a number of operations to be
        performed on the tree. If for example we wanted to delete
        the two hosts "Jupiter" and "Saturn" we could have done:
      </p>
      <code caption="Deleting hosts"><![CDATA[
            Simple.enable();
            Hosts h = new Hosts();
            h.addHost("Jupiter").markDelete();
            h.addHost("Saturn").markDelete();
            System.out.println(h.toXMLString());
            ]]></code>
      <p>
        The above would produce:
      </p>
      <pre><![CDATA[
<hosts xmlns="http://acme.com/ns/simple/1.0" 
       xmlns:simple="http://acme.com/ns/simple/1.0">
   <host nc:operation="delete">
      <name>Jupiter</name>
   </host>
   <host nc:operation="delete">
      <name>Saturn</name>
   </host>
</hosts>]]></pre>

      <p>
        This data could be passed to the <em>editConfig</em> INM method
        to be sent to the managed device using the NETCONF protocol.
      </p>

      <p>
        Finally, let us take a look at the generated code in the 
        dynamic element class <em>Host.java</em>. The signatures look
        like:
      </p>
      <code caption="Host.java"><![CDATA[
package simple;

import com.tailf.confm.*;
import com.tailf.inm.*;

public class Host extends Container {
    public Host() 
    public Host(String nameValue)
    public Host clone() 
    public com.tailf.confm.xs.String getNameValue()
    public void setNameValue(String nameValue)
    public com.tailf.confm.xs.Boolean getEnabledValue()
    public void setEnabledValue(boolean enabledValue)
    public void unsetEnabledValue()
    public void addEnabled()
    public com.tailf.confm.xs.UnsignedInt getNumberOfServersValue()
    public void setNumberOfServersValue(long numberOfServersValue)
    public void unsetNumberOfServersValue()
    public void addNumberOfServers()
}]]></code>

      <p>
        The above does not cover all of the generated methods. It just
        shows the most important ones. The details of the generated code
        and how that code maps against the various constructs we find
        in confspecs is described in detail in the chapter
        <em>Generating a Class Hierarchy</em>.
      </p>
      <p>
        In general the code in <em>Host.java</em> allows us to
        create and manipulate individual <em>Host</em> objects.
        All Host objects extend the library <em>Container</em>
        class which in its turn extends the general <em>Element</em>
        class. The above mentioned methods <em>markDelete</em> and
        <em>toXMLString</em> are both methods of the top level 
        <em>Element</em> class.
      </p>
      <p>
        Now that we have briefly showed how to create and manipulate
        configuration tree instances for the 
        <c>http://acme.com/ns/simple/1.0</c> namespace we continue to
        show some examples that actually make use of the 
        configuration tree. We start with a method the reads the entire
        configuration and returns a <em>Hosts</em> object. The method
        uses a <em>Device</em> parameter which will be briefly described
        later.
      </p>

M3_SET_DIR ../../../examples/confm_intro/src/app/

      <code caption="getConfig()"><![CDATA[
M3_INCLUDE Main.java getConfig
]]></code>

      <p>
        The Device class is a ConfM help class that is
        used to communicate with a managed device. We can associate
        a number of <em>DeviceUser</em> objects to a Device object.
        The DeviceUser object maps a local user name to
        a set of credentials at the device. Thus to initialize 
        our Device object we do:
      </p>
      <code caption="Initializing the device"><![CDATA[
String emsUserName = "bobby";
DeviceUser duser = new DeviceUser(emsUserName, "admin", "secretpass");
Device dev = new Device("mydev", duser, "netconf.tail-f.com", 8023);
dev.connect(emsUserName);
dev.newSession("config");
]]></code>
    
      <p>
        The above code connects the <em>Device</em> over the SSH protocol
        to the NETCONF server at the managed device.  It authenticates
        with the <em>Device</em> using the supplied <em>DeviceUser</em>, 
        finally it creates a session. Each session runs inside an
        SSH channel. Session are named with a string and we can have 
        multiple sessions towards the same managed device.
        The <em>getConfig</em> method could be used as follows:
      </p>
      <code caption="Listing the hosts"><![CDATA[
M3_INCLUDE Main.java listHosts
]]></code>
      <p>
        The above code shows how retrieve a Hosts object
        and traverse it.
      </p>
      <p>
        As another example that traverses the configuration, we 
        show a method <em>updateConfig</em> which removes
        all Host elements named "joe"
      </p>
      <code caption="Delete some hosts"><![CDATA[
M3_INCLUDE Main.java updateConfig
]]></code>
      
      <p>
        The code assumes an initialized Device object. 
        It first retrieves the configuration from the device,
        then walks through the configuration and marks all elements
        where the name element  equals "joe" with the "delete"
        attribute.
        Finally the code issues the <em>editConfig</em> RPC method
        towards the managed device.
      </p>
      <p>
        As a final example we show code which reintroduces a
        host element called "joe".
      </p>
      <code caption="Creating elements"><![CDATA[
M3_INCLUDE Main.java revertConfig
]]></code>

      <p>
        The <em>Device</em> class provides the SSH services to
        us. It is implementing these services using lower layer INM classes.
        An architectural overview of how the different classes interact with
        each other is illustrated in the following picture:
      </p>

      <img src="../pics/ems-details.jpg"
           width="11cm"
           caption="Layered architecture"/>


    </body>
  </section>

  <section>
    <title>Type Safe Configuration Tree Manipulation</title>
    <body>
      <p>
        The main advantage of the ConfM Java class generations
        is type safety. The generated classes both constrain and guide the
        programmer. The generated classes clearly show how the tree
        can be constructed, manipulated and traversed.
      </p>
      <p>
        Since all tree manipulation code is generated, it is hard or
        almost impossible to construct invalid trees. The structure
        of the tree is constrained by the compiled classes themselves,
        leading to compile time checks for the tree construction.
      </p>
      <p>
        Furthermore, run-time checks are performed when
        we manipulate the tree. The confspec data model may impose
        restrictions on the value space. 
        Individual leafs can also have restrictions, e.g. integers
        can have range restrictions, strings can have regular expression
        restrictions.
        </p>
        <p>
          Whenever we use any of the constructors or any of the generated 
          <em>set</em> methods, such as <em>setNumberOfServerValue</em> in 
          the Host class, all restrictions are checked on the 
          value. If the value does not fall within the value space for the
          leaf, a <em>ConfMException</em> is thrown.
        </p>
    </body>
  </section>

  <!-- HÅKAN: THIS SECTION SHOULD BE TURNED INTO A WHITEPAPER INSTEAD
  <section>
    <title>Where does ConfM sit in an NMS architecture?</title>
    <body>
      <p>
        Many NMS solutions have a layered approach where the
        NMS have the following three layers.
      </p>
      <ol>
        <li>
          <p>
            At the top layer resides the <em>Service model</em>. 
            Here typical concepts are things like 
            "Provision a new customer" or 
            "Increase bandwidth for customer X"
          </p>
          <p>
            The Service model defines concepts that make sense for
            a service provider. It is typically closely integrated with the
            day to day business flows for an organization. Thus it
            must be easy and fast to make changes to the Service models 
            as new business scenarios are discovered.
          </p>
        </li>

        <li>
          <p>
            Below the Service model resides the <em>Resource model</em>. This
            is where individual devices are modeled. Usually this is
            done in XML
            or some other proprietary modeling language. The task of
            the resource model is to provide a mapping from the 
            service model to actual device manipulations. Thus the
            Service model task to "provision new customer" in, for example, an ADSL provider scenario includes a series of
            manipulations of switches, routers and DSLAMs. The devices
            are still
            represented at an abstract level where the individual
            devices and their configurations
            that are involved in the configuration change are
            represented as NMS local data structures.
          </p>
        </li>

        <li>
          <p>
            Finally we have the <em>Mediation layer</em>. The task of this
            layer is to map changes to the NMS local data structures in
            the Resource model to actual configuration change 
            commands on the actual device. This typically includes
            CLI scripting over e.g. telnet or SSH today.
          </p>
        </li>
      </ol>

      <p>
        NETCONF as a technology promises to simplify the above
        architecture. Several of the above may become considerably
        simplified. First, the resource model could go away. Since 
        devices themselves have a stringent XML model that 
        defines the device configuration, the Resource model at the NMS
        and the XML (confspec) model of the device are the same.
        Secondly the Mediation layer also becomes different and
        considerably easier and more stable. There is no longer any
        CLI scripting necessary to execute the actual configuration changes.
        The NETCONF protocol defines how to execute the configuration
        changes.
      </p>
      <p>
        Another aspect of an NMS solution that may be simplified
        and more robust is error management. 
        With traditional CLI methods, where there is typically no
        proper candidate configuration management, and no proper
        rollback management, the NMS must be prepared to rollback
        configuration changes in a much more tedious way that when using
        NETCONF.
      </p>
      <p>
        Thus at a high level, NETCONF as a technology promises to
        drastically change and simplify the NMS. 
      </p>
    </body>
  </section>
  -->

  <section>
    <title>INM Examples</title>
    <body>
      <p>
        The purpose of INM is to provide a Java API for managing
        configuration and operational data on NETCONF enabled devices. 
        We typically use the INM classes when we manage NETCONF enabled 
        devices that are not described by a confspec.
        INM
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
        available in the ConfM distribution and can be run as is. The
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
        If we have access to a Tail-f NETCONF server (ConfD or INA)
        we can use the following confspec to run the upcoming
        examples:
      </p>

M3_SET_DIR ./

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
          The <em>Device</em> class is part of the ConfM package.
          With INM we must use the lower layer methods to 
          connect and authenticate to the managed device.
        </p>
        <p>
          If we run the above, the following configuration is
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
          If we run the above, the configuration is manipulated on the
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
          used instead, provided that the NETCONF server we talk to
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
          This example is also available in the ConfM distribution. Try
          it out and see what happens.
        </p>
      </body>
    </subsection>
  </section>
</chapter>
