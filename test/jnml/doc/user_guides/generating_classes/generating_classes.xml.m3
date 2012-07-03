<!-- -*- nxml -*- -->
<chapter>
  <title>Generating a Class Hierarchy</title>
  <section>
    <title>Introduction</title>
    <body>
      <p>
        If a device's configuration has been formally described using
        one or several confspecs, then ConfM can use these
        to generate a Java class hierarchy. The generated set of
        classes makes it easy for a Java based NMS/EMS application
        developer to programatically build a valid configuration or to
        extract a configuration from a specific NETCONF device.
      </p>

      <p>
        To generate a class hierarchy ConfM provides a compiler
        (confdc) which takes a compiled and linked confspec (from now
        on called <em>fxs</em> file) as input and generates a Java
        package, e.g.
      </p>

      <pre><![CDATA[
$ confdc -c dhcp.cs
$ confdc -l -o dhcp.fxs dhcp.xso
$ confdc --confm-emit-java src/dhcp dhcp.fxs
Generating ConfM Java classes from the namespace: 'http://tail-f.com/examples/dhcp/1.0'
Java package: dhcp
Generating container element class: src/dhcp/Dhcp.java
Generating container element class: src/dhcp/SubNets.java
Generating dynamic element class: src/dhcp/SubNet.java
Generating container element class: src/dhcp/Range.java
Generating simpleType class: src/dhcp/Loglevel.java
Generating Javadoc package documentation: src/dhcp/package.html
]]></pre>

      <p>
        The Java package name is extracted from the namespace
        <em>URI</em> identifying the fxs file. If the URI is on the
        recommended <em>URL</em> format, i.e.
        <em>http://&lt;org-name&gt;/.../&lt;module-name&gt;/&lt;module-version&gt;</em>,
        the package name is the same as
        <em>&lt;module-name&gt;</em>. In the example above the URI is
        <c>http://tail-f.com/examples/dhcp/1.0</c> and hence the
        package name is <c>dhcp</c>.
      </p>
      
      <p>
        An alternative package name can be specified using the confdc
        <em>--java-package</em> option.
      </p>

      <p>
        The rest of this chapter describes how constructs in a
        confspec are mapped into a Java class hierarchy, i.e. how
        container elements, leaf elements, simpleTypes and dynamic
        elements are converted to Java classes and methods. The last 
        two sections explains how to avoid class name conflicts and
        how to generate Javadoc documentation from the generated class
        hierarchy.
      </p>
    </body>
  </section>

  <section>
    <title>Class Mapping - An Overview</title>
    <body>
      <p>
        The mapping from a confspec to a Java class hierarchy is
        straight forward. Unique classes are generated for all
        <em>container elements</em>, <em>simpleTypes</em> and
        <em>dynamic elements</em>.
      </p>

      <p>
        These classes inherits from the generic INM <em>Element</em>
        class. The Element class can indeed be used as-is to interact
        with NETCONF enabled devices not using a confspec for its
        configuration data model. Using generated ConfM classes
        greatly simplifies the management of devices though.
      </p>

      <p>
        The upcoming sections each delve into the anatomy behind the
        three types of generated classes and refers to constructs
        in the following confspec (if nothing else is mentioned):
      </p>

      <code caption="An example confspec"><![CDATA[
M3_INCLUDE dhcp.cs ALL
]]></code>

      <p>
        The above example is by design kept terse and focus is instead
        put on the actual confspec Java mapping rules. The <em>Using
        ConfM</em> chapter introduces a fully blown application which
        exemplifies how to benefit from generated classes in a real
        world application.
      </p>

    </body>
  </section>
  
  <section>
    <title>Class Mapping - Container Elements</title>
    <body>
      <p>
        A container class contains other leaf and/or container
        elements.
      </p>
        
      <p>
        The top-level container defines an <em>enable</em> method to
        be called to inform ConfM that incoming NETCONF
        XML streams should be converted to confspec aware objects
        (instantiated from the generated classes) and not generic INM
        Element objects.
      </p>

      <p>
        A child container can be added to a parent container using a
        specifically generated <em>add</em> method. For example, in
        order to add a new 'subNets' container to the 'dhcp' parent
        container we call the generated <em>Dhcp.addSubNets()</em>
        method. As a side effect the field <em>Dhcp.subNets</em> is
        set to point to the new child container. Note: Child container
        fields are read-only, i.e. add methods <strong>must</strong>
        be used to add child containers.
      </p>
      
      <p>
        A container object must be cloned before it can be added to
        several different parents. To add a child container object to
        several parents <strong>never</strong> makes sense and ConfM will throw an exception if this is done. Each container class is
        generated with a clone method such as
        <em>SubNets.clone()</em> for this purpose.
      </p>

      <p>
        A generated container class inherits from the generic
        <em>Container</em> class (which comes with ConfM)
        and contains important methods such as the <em>mark</em>
        methods. Mark methods can be used to signal how a container
        and its children should affect the current datastore during a
        NETCONF &lt;edit-config&gt; operation. 
      </p>

      <dl termtype="em">
        <dt>markMerge</dt>
        <dd>
          This method signals that the container and its children should be
          merged with corresponding configuration in the datastore.
        </dd>
        <dt>markReplace</dt>
        <dd>
          This method signals that the container and its children
          should replace corresponding and existing configuration in
          the datastore. Unlike a NETCONF &lt;copy-config&gt;
          operation, which replaces the entire target configuration,
          only the configuration actually present in the config
          parameter is affected.
        </dd>
        <dt>markCreate</dt>
        <dd>
          This method signals that the container and its children
          should be added to the configuration if and only if the
          configuration data does not already exist in the datastore.
        </dd>
        <dt>markDelete</dt>
        <dd>
          This method signals that the container and its children
          should be deleted in the datastore.
        </dd>
      </dl>

      <p>
        The exact semantics behind &lt;edit-config&gt; can be found in
        the <em>RFC 4741 NETCONF Configuration Protocol</em> standard.
      </p>

      <p>
        The generic Container class also provides <em>inspect</em>,
        <em>checkSync</em> and <em>sync</em> methods.
      </p>

      <dl termtype="em">
        <dt>inspect</dt>
        <dd>
          The inspect method performs a deep inspect and returns the
          difference  between two containers A and B. Returns three
          different sets of elements, i.e. elements that are unique in
          A, elements that are unique in B and dynamic elements (see
          below) that differ but have the same keys.
        </dd>
        <dt>checkSync</dt>
        <dd>
          The checkSync method checks if two containers A and B are
          equal down to each individual leaf.
        </dd>
        <dt>sync</dt>
        <dd>
          The sync method takes two containers A and B and calculates
          how to transmute A into B. The result of the calculation is
          a new (possibly deeply nested) container object C whereas
          mark methods have been called at appropriate positions. The
          result is intended to be used as input to the
          <em>NetconfSession.editConfig</em> method.
        </dd>
      </dl>

      <p>
        The generic INM Element and Container classes (and more) are
        described in detail in the ConfM Javadoc documentation.
      </p>
    </body>

    <subsection>
      <title>Mandatory and Optional Container Elements</title>
      <body>
        <p>
          Container elements are either <em>mandatory</em> or
          <em>optional</em>. A mandatory container such as 'subNets'
          has its <em>minOccurs</em> and <em>maxOccurs</em>
          attributes set to 1. An optional container such
          as 'range' has minOccurs set to 0 and maxOccurs to 1.
        </p>
        
        <p>
          A parent to an optional container is generated with an
          additional <em>delete</em> method. For example, the 'subNets'
          container class is generated with a <em>deleteRange</em>
          method.
        </p>
      </body>
    </subsection>
  </section>

  <section>
    <title>Class Mapping - Leaf Elements</title>
    <body>
      <p>
        Container elements also contain leaf elements. Leaf
        elements are not represented as unique classes but rather as a
        set of methods implemented by the parent container. For
        example, the leaf element 'leaseTime' can be accessed using
        the methods <em>getLeaseTime</em> and <em>setLeaseTime</em> in
        the 'dhcp' parent container class.
      </p>

      <p>
        <em>Get</em> methods return a simpleType object representinga
        the <em>PCDATA</em> value attached to the leaf element:
      </p>

      <pre><![CDATA[
Xs.UnsignedLong leaseTime = dhcp.getLeaseTime();
]]></pre>

      <p>
        <em>Set</em> methods take a simpleType object value as input
        and attach it as a PCDATA value to the leaf element. String
        based set methods are also generated:
      </p>

      <pre><![CDATA[
Xs.UnsignedLong leaseTime = new Xs.UnsignedLog(18977);
dhcp.setLeaseTime(leaseTime);
dhcp.setLeaseTime("2390");
]]></pre>

      <p>
        The mapping between confspec types and Java types is described
        in the simpleType section below.
      </p>
    </body>

    <subsection>
      <title>Mandatory, Default and Optional Leaf Elements</title>
      <body>
        <p>
          Leaf elements are either <em>mandatory</em>, <em>default</em> or <em>optional</em>. A
          mandatory leaf such as 'logFacilty' has its minOccurs and
          maxOccurs attributes set to 1. An optional leaf element
          such as 'maxLeaseTime' has minOccurs set to 0 and
          maxOccurs to 1 and a default leaf element such as
          'leaseTime' has a <em>default</em> attribute defined.
        </p>
        
        <p>
          Optional and default leafs are generated with an
          additional <em>unset</em> method to make it possible to
          delete the container. For example, the 'dhcp' container is
          generated with an <em>unsetMaxLeaseTime</em> method.
        </p>
        
        <p>
          Default leafs are generated with an additional <em>is</em> method
          to query if a specific leaf element value has been set
          explicitly or if the default value is used. For example,
          the default leaf element 'leaseTime' can be queried using
          the generated <em>isLeaseTime</em> method in the 'dhcp'
          parent container.
        </p>
        
        <p>
          Mark methods, as introduced for containers, are also
          generated for optional and default leaf elements but
          <strong>not</strong> for mandatory leaf elements.
        </p>
      </body>
    </subsection>
  </section>
  
  <section>
    <title>Class Mapping - simpleTypes</title>
    <body>
      <p>
        The type of PCDATA values attached to leaf elements are
        defined using <em>primitive</em> and/or <em>derived</em>
        simpleTypes. A simpleType is a confspec construct which
        defines the lexical and value space for leaf element
        values. simpleTypes are described in the <em>Confspec Data Modeling</em>
        chapter and they make it possible to define your own custom
        data types.
      </p>

      <p>
        Each simpleType (primitive or derived) is represented as a
        unique Java class. Primitive types are built-in classes
        bundled with ConfM and derived types are generated
        as unique classes.
      </p>

      <p>
        The built-in <em>Xs</em> class contains easy access to all
        built-in types defined by <em>XML schema</em>
        (<c>http://www.w3.org/TR/2001/REC-xmlschema-2-20010502/#built-in-datatypes</c>)
        such as <em>xs:integer</em> and <em>xs:dateTime</em>.
      </p>

      <p>
        The built-in <em>ConfD</em> class contains easy access to all
        built-in types defined by the ConfD utility namespace
        (<c>http://tail-f.com/ns/confd/1.0</c>) such as
        <em>confd:inetAddressIPv4</em> and
        <em>confd:MD5DigestString</em>.
      </p>

      <p>
        Read more about the Xs and Confd classes in the ConfM Javadoc
        documentation. For example, the generated access methods for
        'leaseTime' and 'net' elements can be used like: 
      </p>
      
      <pre><![CDATA[
// Get the lease time PCDATA value
Xs.PositiveInteger leaseTime = dhcp.getLeaseTime();
// Increase the lease time
leaseTime = leaseTime.setValue(leaseTime.getValue()+1);
// Get the net PCDATA value
Xs.InetAddressIP net = dhcp.getNet();
// Make sure that the network IP is OK
assert(net.equals(new Xs.InetAddressIP("255.255.255.0"));
]]></pre>      

      <p>
        Each built-in type class typically defines two constructors,
        i.e. either taking a string or a native Java type value as
        input. The built-in classes also define the methods
        <em>getValue</em>, <em>setValue</em>, <em>toString</em> and
        <em>equals</em>.
      </p>

      <p>
        The built-in type classes make sure that a value of a certain
        type always follows the data type definition. The same is true
        for derived simpleTypes, i.e. all custom restrictions such as
        <em>xs:pattern</em> and <em>xs:enumeration</em> etc are
        strictly checked or else an exception is thrown. For example,
        the type class generated for the derived 'loglevel' simpleType
        can be used like:
      </p>

      <pre><![CDATA[
// Invalid enumeration. An exception is thrown.
Dhcp.Loglevel lvl = Dhcp.Loglevel("crock");
// OK
Dhcp.Loglevel lvl2 = Dhcp.Loglevel("local");
// Throws an exception
lvl2 = lvl2.setValue("crock");
// Equality?
assert(lvl2.equals(dhcp.getLoglevel()));
// Exercise the toString() method
System.out.println(lvl2);
]]></pre>
    </body>
  </section>

  <section>
    <title>Class Mapping - Dynamic Elements</title>
    <body>
      <p>
        A dynamic element has a maxOccurs attribute > 1 and can exist
        in many instances. A dynamic element has at least one child
        key element which uniquely identifies dynamic element
        instances. Unique classes are generated for each dynamic
        element such as 'subNet' in the example confspec.
      </p>

      <p>
        A dynamic element can be added using a specifc <em>add</em>
        method in its parent container. For example, a call to
        <c>subNets.addSubNet("192.2.33.1","255.255.255.0")</c> adds a
        dynamic 'subNet' element to the 'subNets' container and set
        the value for the leaf element <em>net</em> to <c>192.2.33.1</c>
        and the value for <em>mask</em> to <c>255.255.255.0</c>. 
      </p>

      <p>
        The generated add method takes as many parameters as the
        number of key elements in the confspec. The add method
        either takes object parameters of correct type as input or
        their corresponding string representation.
      </p>

      <p>
        A dynamic element can be extracted and deleted from the parent
        container using specific <em>get</em> and <em>delete</em>
        methods. For example, a call to
        <c>SubNets.getSubNet("192.2.33.1", "255.255.255.0")</c>
        extacts and a call to <c>SubNets.deleteSubNet("192.2.33.1",
        "255.255.255.0")</c> deletes a specific dynamic 'subNet'
        element.
      </p>

      <p>
        The parent 'subNets' container also makes an <em>iterator</em>
        available making it possible to iterate over all dynamic
        'subNet' elements. This iterator is returned by a method named
        <em>subNetIterator</em> in the 'subNets' parent container. The
        iterator is typically used like:
      </p>

      <pre><![CDATA[
ElementChildrenIterator iter = dhcp.subNets.subNetIterator();

while (iter.hasNext()) {
    SubNet s = (SubNet)iter.next();
    System.out.println("SubNet: "+s.getNetValue()+"/"+s.getMaskValue());
}
]]></pre>
    </body>
  </section>

  <section>
    <title>Class Name Conflicts</title>
    <body>
      <p>
        The mapping from a confspec to a Java class hierarchy may
        lead to Java class name conflicts. The reason for this is that
        each container element results in a Java class with the same
        name. If a child container with the same name occurs in
        several locations there is a Java class name conflict.
      </p>

      <p>
        The following confspec introduces a name conflict for the
        'baz' container element:
      </p>

      <code caption="A confspec with conflicts"><![CDATA[
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
      <elem name="baz">
        <elem name="bingo" type="xs:string"/>
      </elem>
    </elem>
  </elem>
</confspec>
]]></code>

      <p>
        The confdc compiler complains:
      </p>

      <pre><![CDATA[
$ confdc --confm-emit-java src/clash clash.fxs
Generating ConfM Java classes from the namespace: 'http://tail-f.com/examples/dhcp/1.0'
Java package: dhcp
Generating container element class: src/clash/Foo.java
Generating container element class: src/clash/Bar.java
Generating container element class: src/clash/Baz.java
Generating container element class: src/clash/Zip.java
"clash.fxs": cannot generate unique classname for path foo/zip/baz
]]></pre>

      <p>
        To fix this we use the confdc <em>--java-class-naming</em>
        option. This option adds the name of the parent container to
        any child container that has the same name that another child
        container. Like this:
      </p>

      <pre>
$ confdc --java-class-naming 1 --confm-emit-java src/clash clash.fxs
Generating ConfM Java classes from the namespace: 'http://tail-f.com/examples/dhcp/1.0'
Java package: dhcp
Generating container element class: src/clash/Foo.java
Generating container element class: src/clash/Bar.java
Generating container element class: src/clash/Baz.java
Generating container element class: src/clash/Zip.java
Generating container element class: src/clash/ZipBaz.java
                                              ^^^^^^^^^^^
Generating root class: src/clash/Dhcp.java
Generating Javadoc package documentation: src/clash/package.html
done
</pre>

      <p>
        This way the conflicting class get the name of the parent
        container prepended to make it unique. The numerical value
        specified for --java-class-naming specifies the maximum number
        of ancestors to prepend. If the conflict can not be avoided
        with this number of prepended ancestor names confdc fails. 
      </p>

      <p>
        A <em>generatedName</em> attribute can be used in the confspec
        as an alternative to the --java-class-naming option. The
        generatedName attribute can be added to container elements
        which are in conflict. In the confspec above we could do the
        following:
      </p>

      <code caption="A confspec with a generatedName attribute"><![CDATA[
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
      <elem generatedName="Fingal" name="baz">
        <elem name="bingo" type="xs:string"/>
      </elem>
    </elem>
  </elem>
</confspec>
]]></code>

      <p>
        confdc then happily generates the class hierarchy without a
        --java-class-naming option:
      </p>

      <pre>
$ confdc --confm-emit-java src/clash clash.fxs
Generating ConfM Java classes from the namespace: 'http://tail-f.com/examples/dhcp/1.0'
Java package: dhcp
Generating container element class: src/clash/Foo.java
Generating container element class: src/clash/Bar.java
Generating container element class: src/clash/Baz.java
Generating container element class: src/clash/Zip.java
Generating container element class: src/clash/Fingal.java
                                              ^^^^^^^^^^^
Generating root class: src/clash/Dhcp.java
Generating Javadoc package documentation: src/clash/package.html
done
</pre>

      <p>
        Feel free to use any combination of --java-class-naming option
        and generatedName attributes.
      </p>
    </body>
  </section>

  <section>
    <title>Javadoc</title>
    <body>
      <p>
        Special care has been taken to adorn the generated classes
        with Javadoc comments. A clickable class hierarchy is also
        generated in the package summary. The class hierarchy shows a
        nested list with all parent and child dependencies including
        easy access to all generated classes.
      </p>

      <img src="../pics/javadoc.jpg"
           width="14.5cm"
           caption="Generated Javadoc documentation"/>
      
      <p>
        The above was generated using a build.xml fragment like:
      </p>
      
      <pre><![CDATA[      
<property name="doc.dir" value="build/javadoc"/>
<property name="confm-jar.dir" value="../../../build/jar"/>
<property name="ganymed.dir" value="../../../ganymed"/>

<target name="javadoc">
  <mkdir dir="${doc.dir}"/>
  <javadoc public="true" destdir="${doc.dir}" windowtitle="DHCP API">
    <classpath location="${confm-jar.dir}/ConfM.jar"/>
    <classpath location="${confm-jar.dir}/INM.jar"/>
    <classpath location="${ganymed.dir}/ganymed-ssh2-build251beta1.jar" />
    <fileset dir="src/dhcp" defaultexcludes="yes">
      <include name="*.java"/>
    </fileset>
  </javadoc>
</target>
]]></pre>
    </body>
  </section>
</chapter>
