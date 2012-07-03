<!-- -*- nxml -*- -->

M3_SET_DIR ../../../examples/dhcp/

<chapter>
  <title>Using ConfM</title>
  <section>
    <title>Introduction</title>
    <body>
      <p>
        In this chapter we in detail go through how to use the Tail-F
        ConfM Configuration Management library. How to generate Java files from a confspec and 
        how to use the generated Java code in a real world example.
      </p>
      <p>
        The example we will use in this chapter is a scenario where
        we have a setup with two managed devices, each running a 
        NETCONF capable DHCP server. The setup which is part of the
        ConfM example collection is driven by two nodes, each running
        ConfD with a small confspec modeling the configuration of the
        ISC dhcpd server (<c>http://www.isc.org/sw/dhcp/</c>). 
      </p>
      <p>
        We will show Java code that make use of the generated Java 
        classes from the confspec which manipulates the configuration
        of two DHCP servers in various ways.
      </p>

    </body>
  </section>


  <section>
    <title>Compiling the Confspec</title>
    <body>
      <p>
        The first thing we need to do is to compile the confspec(s)
        defining the aspects of the device we are interested in.
        We must first produce an <em>fxs</em> file and then subsequently
        using that fxs file, generate our Java classes.
      </p>

      <p>
        The <em>dhcpd.cs</em> confspec defines the configuration of
        the managed device. It may not necessarily define all configuration
        aspects of the managed device. In this particular example it
        does not. Since the nodes we will communicate with are running
        <em>ConfD</em> those devices also at least consist of the 
        mandatory Tail-F AAA confspec module for authentication. We will
        ignore that XML module here and only perform configuration
        changes towards the dhcp part of the devices.
      </p>

      <p>
        The confspec looks like:
      </p>

      <code caption="The dhcp confspec"><![CDATA[
M3_INCLUDE confd/dhcpd.cs ALL
     ]]></code>        
      
      <p>
        It is sufficiently complex to demonstrate a wide range of
        features. For example it contains a <em>structuredType</em> 
        a <em>simpleType</em> and <em>dynamic elements</em>. 
        Dynamic elements is confspec terminology and it refers to
        e.g. the multiple instances we can have of
        <em>/dhcp/SubNets/subnet</em>.
      </p>

      <p>
        We compile the confspec as:
      </p>

      <code caption="Compilation"><![CDATA[
# confc -c dhcpd.cs
# confdc -l -o dhcpd.fxs  dhcpd.xso
# mkdir src/dhcp
# confdc --confm-emit-java  src/dhcp dhcpd.fxs
Generating ConfM Java classes from the namespace: 'http://tail-f.com/examples/dhcp/1.0'
Java package: dhcp
Generating container class: src/dhcp/Dhcp.java
Generating container class: src/dhcp/SubNets.java
Generating alias ref class: src/dhcp/SubNet.java
Generating container class: src/dhcp/SharedNetworks.java
Generating dynamic class: src/dhcp/SharedNetwork.java
Generating container class: src/dhcp/SharedNetworkSubNets.java
Generating alias ref class: src/dhcp/SubNetsSubNet.java
Generating container class: src/dhcp/SubNetworkType.java
Generating container class: src/dhcp/Range.java
Generating type class: src/dhcp/Loglevel.java
Generating doc file: "src/dhcp/package.html"
done
      ]]></code>
      
      <p>
        The above compiled and linked the confspec into an fxs
        file and finally emitted our Java classes that we will use
        to manipulate the configuration. The Java classes end up in
        the <em>src/dhcp/</em> directory and they belong to a package named
        "dhcp". It is possible to specify the package name explicitly
        through the <em>--java-package</em> confdc option. If omitted
        the package name defaults to the XML prefix of the namespace
        which in this case is the last part of the URI for the 
        XML namespace.
      </p>

      <p>
        For each container in the confspec we have the corresponding 
        Java class. Of particular importance is the top-level 
        <em>Dhcp</em> class.  It contains the method <em>enable</em>.
        This method registers this namespace to be schema aware. Once we have
        registered a namespace it will be possible to use the generated
        Java classes for that namespace. If unregistered we can only use
        the techniques described in the INM chapters for that namespace.
      </p>
        
    </body>
  </section>


  <section>
    <title>The Device Class</title>
    <body>
      <p>
        Before getting started with some code we must describe 
        the <em>Device</em> class.
      </p>
      <p>
        The purpose of the Device class is to provide
        a common class that can be used to represent a managed 
        device.
      </p>
      <p>
        Each device has a set of <em>DeviceUser</em> objects
        associated to it. A DeviceUser is just a
        data container that maps a local username to authentication
        information at the managed device. Since we are speaking 
        NETCONF to the managed devices, we have to authenticate ourselves
        to the managed device prior to performing any configuration
        changes. 
      </p>
      <p>
        Typically a management system has its own layer of 
        authentication. An end user logs in to the management system.
        The DeviceUser class is used to map a local
        user to an authentication method at a device. The following
        simple fictitious code fragment creates a device and associates an
        auth method to it.
      </p>
      <code caption="Initializing a Device"><![CDATA[
String emsuserName = "bobby";
String remoteUser = "admin";
String remotePassword = "secret";

DeviceUser u = new DeviceUser(localuser, remoteUser, remotePassword);
Device device = new Device(devicename, u, deviceIp, devicePort);
]]></code>
      <p>
        Now given the Device instance we can simply call
        <em>device.connect(localUser)</em> to establish an SSH
        connection to the NETCONF agent residing at
        <em>deviceIp:devicePort</em>
      </p>
      <p>
        Once we have connected to a device, we must also establish
        at least one NETCONF session. Each NETCONF session resides
        inside an SSH channel, thus we can have several simultaneous
        sessions towards the same device. A typical usage scenario 
        is to have a special session for NETCONF notifications.
      </p>
    </body>
  </section>

M3_SET_DIR ../../../examples/dhcp/src/app/

  <section>
    <title>The Setup</title>
    <body>
      <p>
        In this chapter we have the setup with two NETCONF enabled
        DHCP servers. Each server is running a ConfD instance which
        has an "admin" user with password "secret". We declare and
        initialize two devices, called <em>left</em> and <em>right</em>.
        Here is the initialization code:
      </p>
      <code caption="Initializing the setup"><![CDATA[

M3_INCLUDE Main.java init

]]></code>
      
      <p>
        The statement <em>left.connect("bobby");</em> means to  SSH connect
        the <em>left</em> device with the auth structure associated
        to local user "bobby".
      </p>
      <p>
        Once we are connected, we have an SSH socket to the device, and
        we also have an established NETCONF session i.e. we have exchanged
        NETCONF <em>hello</em> messages with the device. Each NETCONF session
        runs inside an SSH channel. Our first example
        check that we are talking to the device we think we are talking to.
      </p>
    <code caption="Checking the capabilities"><![CDATA[

M3_INCLUDE Main.java testCapas

]]></code>
    <p> 
      We check that the device support confirmed commit and
      we also ensure that the device supports the same XML namespace
      as the one we have in our generated Java classes.
    </p>
    


    </body>
  </section>


  <section>
    <title>Reading the Configuration</title>
    <body>
      <p>
        Our first example is really simple, we just get the configuration, 
        and print it.
      </p>
           <code caption="Get full configuration"><![CDATA[

M3_INCLUDE Main.java getConfig

]]></code> 

      <p>
        First we pick up our named <em>NetconfSession</em>.
        We have to give the <em>getConfig</em> method a filter. If
        we do not, we will get the entire configuration - here we are only
        concerned with the part of the configuration which is described
        by <em>dhcpd.cs</em>. If we execute the above we get:
      </p>
      <code caption="The running configuration"><![CDATA[
<dhcp xmlns="http://tail-f.com/examples/dhcp/1.0">
   <defaultLeaseTime>PT600S</defaultLeaseTime>
   <maxLeaseTime>PT7200S</maxLeaseTime>
   <logFacility>local7</logFacility>
   <SubNets>
      <subNetworkType>
         <net>10.1.2.0</net>
         <mask>255.255.255.0</mask>
         <range>
            <dynamicBootP>false</dynamicBootP>
            <lowAddr>10.1.2.100</lowAddr>
            <hiAddr>10.1.2.200</hiAddr>
         </range>
         <maxLeaseTime>PT7200S</maxLeaseTime>
      </subNetworkType>
   </SubNets>
   <SharedNetworks></SharedNetworks>
</dhcp>
]]></code> 
      <p>
        We get an entire <em>Dhcp</em> object back from the <em>left</em>
        Device and we can turn
        the entire Object into a String through its inherited
        <em>toXMLString</em> method.
      </p>

      <p>
        The <em>Dhcp</em> object has access methods for its individual
        elements - one for each XML element found in the confspec.
        So to read e.g. the <em>logFacility</em> we have.
      </p>
      <code caption="Accesing fields"><![CDATA[

M3_INCLUDE Main.java getLogLevel

]]></code> 
      <p>
        This code is actually bad, if all we want is to get the
        log level for a device, it is much better to construct 
        a sub filter as in:
      </p>

      <code caption="Accesing fields again"><![CDATA[

M3_INCLUDE Main.java getLogLevel2

]]></code> 

      <p>
        There is no reason to extract the entire dhcp configuration
        just to retrieve the log level. Using the filter we just
        query the device for the components we are interested in.
        Both our variants of methods that read the log level return
        a <em>Loglevel</em> object. This is a generated class that
        corresponds to the <em>simpleType</em> "loglevel" as defined
        in the <em>dhcpd.cs</em> confspec.
      </p>

      <p>
        In order to traverse deeper into the <em>Dhcp</em> structure
        we can use the access functions at the different layers
        in the tree. For example a method which lists and prints
        the <em>network</em> part of all networks the DHCP server
        serves we have:
      </p>
      <code caption="Traversing the Dhcp object"><![CDATA[

M3_INCLUDE Main.java listNetworks

]]></code> 
      <p>
        Note how we conveniently 
        loop through all the <em>/dhcp/subNets/subNet</em>
        structures through a call to 
        <em>config.subNets.subNetIterator</em>
      </p>
      <p>
        The generated Java code contains access functions to 
        traverse the configuration tree.
      </p>
    </body>
  </section>


  <section>
    <title>Manipulating the Configuration</title>
    <body>
      <p>
        To manipulate the configuration of the device we must invoke
        the <em>editConfig</em> method on the NETCONF session. Once a
        Device object has been "connected", we can access the
        session object through the Device object.
        We start off by showing a method to set the 
        <em>/dhcp/logFacility</em> element for all devices.
        If we look at the confspec <em>dhcpd.cs</em> we see that the
        element <em>/dhcp/logFacility</em> is defined as an XML
        <em>simpleType</em>:
      </p>

      <pre> <![CDATA[
  <xs:simpleType name="loglevel">
    <xs:restriction base="xs:string">
      <xs:enumeration value="kern"/>
      <xs:enumeration value="mail"/>
      <xs:enumeration value="local7"/>
    </xs:restriction>
  </xs:simpleType>
  ]]></pre> 
      <p> The simpleType gives rise to a generated Java class called
      <em>Loglevel</em>, thus the signature for our function to set
      the logFacility contains an instance of <em>Loglevel</em> as in:
      </p>

      <code caption="Setting the Loglevel"><![CDATA[

M3_INCLUDE Main.java setLogFacility

      ]]></code> 
      <p>
        The code creates a new <em>Dhcp</em> object. The XML
        representation of that object is retrieved by calling
        the <em>dhcp.toXMLString</em> method and it could look like:
      </p>
      <pre> <![CDATA[
<dhcp:dhcp xmlns:dhcp="http://tail-f.com/examples/dhcp/1.0">
   <dhcp:logFacility>kern</dhcp:logFacility>
</dhcp:dhcp>
      ]]></pre> 

      <p>
        The first method <em>setLogFacility(String lev)</em> is type safe.
        It constructs a new <em>Loglevel</em> object according to the
        XML restrictions associated to the simpleType, thus if we invoke
        <em>setLogFacility("Foobar")</em> it will throw an exception
        since "Foobar" is not an acceptable value according to the 
        restrictions.
      </p>
      <p>
        Note that we issue the editConfig operations directly towards
        the running database at the devices. If the second <em>editConfig</em>
        fails we have an inconsistent state where the operation
        succeeded on the first and failed on the second. This is the
        classical situation that Element Managers face when manipulating
        several devices using CLI screen scraping over SSH or Telnet.
        Since NETCONF supports transactions, we should make use of that
        functionality if it is there. Thus an all-or-nothing version
        version of the previous method is:
      </p>
      

      <code caption="Setting the Loglevel through the candidate"><![CDATA[

M3_INCLUDE Main.java reset
M3_INCLUDE Main.java safeSetLogFacility

      ]]></code> 
      <p>
        The above code has some minor deficiencies, if the first lock
        succeedes but the second fails, we do not release the 
        locks properly.
        Yet another deficiency is that we do not validate our candidates.
        Fixing that we get:
      </p>
      <code caption="Setting the loglevel again"><![CDATA[
M3_INCLUDE Main.java safeSetLogFacility2
]]></code>
        
      <p>
        It is allowed to execute edit-config operations towards the candidate 
        that result in an invalid configuration.  The candidate can be
        seen as a scratch pad towards which we can execute a series of
        edit-config operations. The candidate configuration must be valid when we commit, but not necessarily
        prior to that. This is in contrast to edit-config operations that
        are executed towards the running storage, they must always result
        in a valid configuration and typically an agent validates 
        edit-config operations towards running automatically. This is the
        reason why we want to validate both our candidate stores prior
        to commiting.
      </p>
      <p>
        Finally here we show an example whereby a new <em>Subnet</em>
        is added to both DHCP servers.
      </p>


      <code caption="Adding a network"><![CDATA[

M3_INCLUDE Main.java addNetWork

      ]]></code> 

      <p>
        The code utilizes the various generated Java classes to
        construct a partial configuration tree to send to the
        NETCONF agents.
      </p>
        
    </body>
  </section>


  <section>
    <title>Accumulating Changes</title>
    <body>
      <p>
        The Device class contains a public variable
        called <em>configTree</em>. The type of that element 
        is <em>Element</em> which is the base class for all the
        generated java classes. 
      </p>
      <p>
        The purpose of that variable is to use it as an accumulator
        variable where we during a session can add several configuration
        updates. Our function from above which adds a Network to all
        devices could more easily have been reused had it been written as:
      </p>

      <code caption="Adding a network"><![CDATA[

M3_INCLUDE Main.java addNetWork2

      ]]></code> 
      <p>
        This function just adds our new <em>Network</em> to the
        <em>configTree</em> associated to the Device. We can now
        invoke several different methods that manipulate the 
        configuration tree in various ways. Once all manipulations are 
        done we need a general commit method that could look like:
      </p>

      <code caption="Committing all devices"><![CDATA[

M3_INCLUDE Main.java reset

M3_INCLUDE Main.java commitAll

      ]]></code> 

      <p>
        The example code constructs an <em>ArrayList</em> out of 
        our two example devices and then the general purpose function
        traverses the list and:
      </p>
      <ol>
        <li><p>Resets all devices. This means that we have a situation 
        where all the candidates contains the same as running
      </p></li>
      <li><p>
        Executes <em>editConfig</em> of our accumulated changes towards
        the candidate on all devices.
      </p>
      </li>
      <li>
        <p>
          If the previous step was successful, we finally commit
          on all devices.
        </p>
      </li>
      </ol>

      <p>
        If anything goes wrong - we are thrown out from our loops and we
        close the sockets to all devices. This is how a transaction
        is aborted over NETCONF. There is no explicit abort RPC in the
        NETCONF protocol, but rather to abort, we close the SSH session.
        This will automatically make the device revert the changes.
      </p>
      <p>
        It is also worth to note that the above code is not
        perfect. The possibility of something going wrong still exists.
        Once we go into our loop to <em>commit()</em>, things can go
        wrong and we will end up in an inconsistant state. The remedy
        is to use confirmed commit. 
        The following code utilizes the confirmed commit capability
        of the NETCONF protocol - thus making the code rock solid.
        Either the configuration change takes effect on all devices or
        none. 
      </p>

      <code caption="Committing all devices - but safe"><![CDATA[

M3_INCLUDE Main.java reset

M3_INCLUDE Main.java commitAll2

      ]]></code> 

      <p>
        In this code we utilize the confirmed commit capability in
        the NETCONF protocol. Confirmed commit commits the configuration
        changes from the candidate to the running data store. We also
        pass in a timeout parameter. 
        If a participant does not see the confirmation within the 
        designated timeout, it will automatically rollback the changes.
      </p>
    </body>
  </section>
</chapter>
