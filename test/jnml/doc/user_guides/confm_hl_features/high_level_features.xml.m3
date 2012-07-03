<!-- -*- nxml -*- -->

M3_SET_DIR ../../../examples/dhcp/src/app/

<chapter>
  <title>ConfM High Level Features</title>
  <section>
    <title>Introduction</title>
    <body>
      <p>
        In this chapter we will introduce a series of ConfM high level
        features. We will show:
      </p>
      <ul>
        <li><p>IoSubscribers - how to capture the XML data that goes
        between the manager and the agent.</p>
        </li>
        <li><p>Synchronization - how to construct a minimal
        diff which makes the agent have the same configuration
        as a master copy</p></li>

        <li><p>Backlogging - how to store configuration updates
        destined for a device that was non-operational.
        </p></li>
      </ul>
    </body>
  </section>

  <section>
    <title>IO Subscribers</title>
    <body>
      <p>
        If we want to capture a copy of the XML that is sent
        from the manager and received by the manager we can create
        an IOSubscriber. The reason for capturing the sent data
        can be for debugging, logging or auditing purposes.
      </p>
      <p>
        To install an IOSubscriber we must create a class that
        extends the IOSubscriber class in the INM package.
        For example:
      </p>



      <code caption="An IO Subscriber"><![CDATA[
public class Subscriber extends IOSubscriber {
    
    String devName;

    public DefaultIOSubscriber(String devName) {
        super( false );
        this.devName = devName;
    }
    
    public void input(String s) {
        System.out.println("RECV " + devName);
        System.out.println(s);
    }

    public void output(String s) {
        System.out.println("SEND " + devName);
        System.out.println(s);
    }
}

     ]]></code>        
      

M3_SET_DIR  ../../../examples/dhcp/src/app/
      <p>
        We pass in the <em>IOSubscriber</em> when we connect our
        <em>Device</em> object as in:
      </p>
      <pre><![CDATA[
      String devname = "left";
      Subscriber subscriber = new Subscriber(devName);
      DeviceUser duser = new DeviceUser("joe", "admin", "secret");
      Device left = new Device(devName, duser, ip, port);
      left.connect("joe");
      left.newSession(subscriber, "config");
     ]]></pre>        

      <p>
        If we now run an example from the previous chapter
        with the IO Subscriber installed we get printouts of
        all the XML data that flows over the SSH socket.
      </p>
      <p>
        For example the example where we get the log level
        with a filter from the "left" device:
      </p>
      <pre><![CDATA[
M3_INCLUDE Main.java getLogLevel2
     ]]></pre>        

      <p>
        produces the
        following output with an IOSubscriber installed.
      </p>

      <code caption="NETCONF outout"><![CDATA[
SEND left
<hello xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
   <capabilities>
      <capability>urn:ietf:params:netconf:base:1.0</capability>
   </capabilities>
</hello>

RECV left
<hello xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
   <capabilities>
      <capability>urn:ietf:params:netconf:base:1.0</capability>
      <capability>urn:ietf:params:netconf:capability:writable-running:1.0</capability>
      <capability>urn:ietf:params:netconf:capability:candidate:1.0</capability>
      <capability>urn:ietf:params:netconf:capability:confirmed-commit:1.0</capability>
      <capability>urn:ietf:params:netconf:capability:xpath:1.0</capability>
      <capability>urn:ietf:params:netconf:capability:validate:1.0</capability>
      <capability>urn:ietf:params:netconf:capability:rollback-on-error:1.0</capability>
      <capability>http://tail-f.com/ns/netconf/actions/1.0</capability>
      <capability>http://tail-f.com/examples/dhcp/1.0</capability>
      <capability>http://tail-f.com/ns/aaa/1.1</capability>
   </capabilities>
   <session-id>14</session-id>
</hello>

SEND left
<nc:rpc xmlns:nc="urn:ietf:params:xml:ns:netconf:base:1.0" nc:message-id="1">
   <nc:get-config>
      <nc:source>
         <nc:running></nc:running>
      </nc:source>
      <nc:filter nc:type="subtree">
         <dhcp:dhcp xmlns:dhcp="http://tail-f.com/examples/dhcp/1.0">
            <dhcp:logFacility></dhcp:logFacility>
         </dhcp:dhcp>
      </nc:filter>
   </nc:get-config>
</nc:rpc>

RECV left
<rpc-reply xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" 
           xmlns:nc="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1">
   <data>
      <dhcp xmlns="http://tail-f.com/examples/dhcp/1.0">
         <logFacility>mail</logFacility>
      </dhcp>
   </data>
</rpc-reply>


      ]]></code>

      <p>
        A similar class to the above <em>Subcriber</em> is the 
        <em>DefaultIOSubscriber</em> class. It can be
        used without modifications to just get simple printouts of the
        NETCONF traffic.
      </p>
        


    </body>
  </section>

  <section>
    <title>Synchronization</title>
    <body>

      <p>
        Many network management applications are written in such a
        way that the manager software has the master copies of
        the configuration of all managed devices. This data is
        typically stored in persistent storage at the manager.
        With master copies stored at the manager, the typical mode
        of operation at the manager is as follows.
      </p>
      <ol>
        <li><p>Read the configuration from persistent storage</p></li>
        <li><p>Manipulate the configuration in RAM at the manager</p></li>
        <li><p>Push out the changes to all concerned devices</p></li>
        <li><p>If configuration change was successful, write new 
        master copy to persistent storage at the manager.</p></li>
      </ol>

      <p>
        With the above mode of operation the managed network becomes
        inconsistent if a managed device does not have the configuration
        that the manager has.
      </p>

      <p>
        The ConfM Java classes include high level support for synchronizing
        a device. There are several reasons as to why a managed
        device could get "out of sync". Maybe the device was manually
        reconfigured through its CLI or maybe the device was offline for
        some time and missed some configuration updates. 
        This latter case can only occur if the management software is written
        is such a way so that configuration 
        changes are pushed out although maybe all devices that should 
        participate are not operational. 
      </p>
      <p>
        Regardless of how the situation with unsynchronized devices occurred,
        the job of manager software is to remedy the situation.
        ConfM has two different methods to be used here.
      </p>
      <ol>
        <li><p>We have a method to check if device is in sync.</p></li>
        <li><p>We have a method to produce a minimal "diff" for 
        a device, given the master configuration. That is a diff 
        which should be sent to the device in order to make it 
        have the configuration which is stored at the master. 
      </p>
      </li>
      </ol>

      <p>
        Let us start with checking if a device is in sync. 
        Assuming that we have our  master copy of the configuration
        for a device we can simply get the configuration from the
        device and compare it to our master copy as in:
      </p>

M3_SET_DIR ../../../examples/dhcp/src/app/

      <code caption="Checking if a device is in sync"><![CDATA[

M3_INCLUDE Main.java checkSync
     ]]></code>        
      <p> The code retrieves the <em>Dhcp</em> part of the configuration
      of a device and compares it to our master copy. Again, this
      strategy is only viable when we have an architecture where the
      manager contains the master copy of the device configuration.
      We are using a static method in the ConfM <em>Container</em> class
      to perform the check. All Java classes that were generated 
      by the <em>confdc</em> compiler that represent XML container
      elements will be Java classes that extend the ConfM 
      <em>Container</em> class.
      
      </p>
      <p>
        Now assume that the device was out of sync, i.e that our call above
        to <em>Container.checkSync()</em> returned <em>false</em>. Now
        we want to to produce the smallest possible diff and send an
        <em>edit-config</em> to the device to make it up to date.
      </p>
      
      <p>
        The highly useful <em>Container</em> method <em>sync</em>
        can be used to transform one container into another. 
        In our case we have one <em>Dhcp</em> object (which is also
        a Container object) that represents the desired state, and
        another <em>Dhcp</em> object that represent the actual state, 
        i.e. the actual device configuration.
        Our aim is to make the target device enter into the desired state, 
        i.e. we wish the target device to have the same configuration
        as our master copy.
      </p>
      <p>
        The <em>Container.sync()</em> method does precisely this, it
        takes two configuration trees and returns a tree, which
        we can use as input to <em>editConfig()</em> to transform
        the first tree into the second. Thus the following code
        brings a device to the desired state.
      </p>

      <code caption="Synchronizing a device"><![CDATA[

M3_INCLUDE Main.java makeSync
     ]]></code>        

      <p>
        Both the methods <em>checkSync()</em> and <em>Container.sync()</em>
        work equally well on entire configurations as well as on partial
        trees. Thus we can use the methods on a particular sub-tree
        down in the configuration as well as on the entire configuration.
      </p>


    </body>
  </section>


  <section>
    <title>Backlog</title>
    <body>
      <p>
        Backlogging is a simple but useful strategy that is built
        into the <em>Device</em> class. 
        The idea is if we try to perform a configuration change
        towards a device and the device is not operational, we can
        easily store the change in a backlog queue and then later
        when the device becomes operational, we can traverse the
        backlog and thus make device up to date.
      </p>
      <p>
        The <em>Device</em> class has a simple <em>ArrayList</em> 
        associated to it and we can add items to the backlog, 
        query if the backlog is empty and finally we can run the
        backlog.
      </p>
    </body>
    </section>

    <section>
      <title>Persistence</title>
      <body>
        <p>
          When the manager stores the device configuration
          on persistent storage we can choose from several
          different technologies and strategies.
          The following non-exhaustive list indicates some
          possible solutions to the problem of storing the configuration
          data at the manager side.
        </p>
        <ul>
          <li>
            <p>
              A very simple way is to utilize the built in Java
              <em>Serializable</em> interface. All ConfM and INM objects
              are serializable. Thus we can save the configuration 
              of a device on a file with the following code:
            </p>
            <code caption="Storing configuration persistently"><![CDATA[
M3_INCLUDE Main.java saveDevice
            ]]></code>
            <br/> <!-- Hasty crock to make PDF output OK -->
          </li>
        

          <li>
            <p>
              There exists a multitude of XML databases to choose from.
              Since the configuration data of a managed device is
              an XML instance document adhering to an XML schema 
              (we can use the confdc compiler to produce an XML schema
              from a confspec), we can store the XML data in such a database.
            </p>
            <p>
              Alternatively, when we want to go the other way, we
              read data from the XML database. The <em>XMLParser</em> in
              either the ConfM or the INM package can be used to easily
              produce a new ConfM object or an <em>Element</em> instance.
              Thus if we use an XML database at the manager to store
              the configuration data persistently it is a
              straight forward procedure to store ConfM data.
            </p>
          </li>


          <li>
            <p>
              If we aim to store the data at the manager in an SQL
              database we need to do some more work. A number of
              strategies are possible. The best route to choose here 
              depends mainly on local issues such as 
              proficiency with ORM technologies such as Hibernate etc.
            </p>
            <ul>
              <li>
                <p>
                  It is possible to serialize the ConfM objects and
                  store the serialized buffers as BLOBs in 
                  an SQL database.
                </p>
              </li>
              
              <li>
                <p>
                  If the data model at the manager is a proper
                  SQL data model, we typically need to write software
                  that maps the relational data from SQL to ConfM
                  objects back and forth. Depending on the size of the
                  data model this can be more or less tedious.
                </p>
              </li>
              
              <li>
                <p>
                  We cannot easily utilize ORM technology to map
                  a tree of ConfM objects to an equivalent set of
                  SQL tables. A mapping layer is necessary to utilize ORM technology to store ConfM
                  objects persistently.
                  ConfM does not provide such mapping.
                </p>
              </li>
            </ul>
          </li>

M3_SET_DIR ../../../examples/confm_intro/src/app/
          <li>
            <p>
              We can read and write XML in text format back and 
              forth to a file (or any other output stream).
              The follwing code examplifies:
            </p>
            <code caption="Read and writing XML data"><![CDATA[
M3_INCLUDE Main.java writeReadFile
            ]]></code>
          </li>
            
        </ul>
      </body>
    </section>
</chapter>
