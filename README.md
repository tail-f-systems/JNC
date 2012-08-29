JNC
===

JNC (Java NETCONF Client) is the name of both a Java library for NETCONF client
code and a Java output format plugin for pyang, an extensible YANG validator
and converter in python. You need an installation of pyang to use the JNC
plugin. Get it here: http://code.google.com/p/pyang/

The JNC plugin can be used to generate Java class hierarchies from YANG data
models. Together with the JNC library, these generated Java classes may be used
as the foundation for a NETCONF client (AKA manager) written in Java. The JNC
library is distributed along with the JNC plugin script.

JNC uses Ganymed SSH-2 to communicate with NETCONF servers/agents, you may get
it from here: http://www.cleondris.ch/opensource/ssh2/ or by using aptitude
(http://manpages.ubuntu.com/manpages/lucid/man8/aptitude.8.html):
sudo aptitude install libganymed-ssh2-java

JNC has not yet been released as open source, but stay tuned!


----- High level description

The different types of generated files are:

Root class  -- This class has the name of the prefix of the YANG module, and
-              contains fields with the prefix and namespace as well as methods
-              that enables the JNC library to use the other generated classes
-              when interacting with a NETCONF server.

YangElement -- Each YangElement corresponds to a container or a list in the
-              YANG model. They represent tree nodes of a configuration and
-              provides methods to modify the configuration in accordance with
-              the YANG model that they were generated from.
-
-              The top-level containers or lists in the YANG model will have
-              their corresponding YangElement classes generated in the output
-              directory together with the root class. Their respective
-              subcontainers and sublists are generated in subpackages with
-              names corresponding to the name of the parent container or list.

YangTypes   -- For each derived type in the YANG model, a class is generated to
-              the root of the output directory. The derived type may either
-              extend another derived type class, or the JNC type class
-              corresponding to a built-in YANG type.

Packageinfo -- For each package in the generated Java class hierarchy, a
-              package-info.java file is generated, which can be useful when
-              generating javadoc for the hierarchy.

Schema file -- If enabled, an XML file containing structured information about
-              the generated Java classes is generated. It contains tagpaths,
-              namespace, primitive-type and other useful meta-information.

The typical use case for these classes is as part of a JAVA network management
system (EMS), to enable retrieval and/or storing of configurations on NETCONF
agents/servers with specific capabilities.


----- Milestones

2012-06-04
Emil starts working at Tail-f and reads up on the NETCONF RFC 6241 and YANG RFC
6020. You should too if you intend to contribute to this project!

2012-06-12
JPyang is born as a few lines of python code that integrates with pyang are
written.

2012-06-20
Empty initial commit of the repository. It will contain the source code for
JPyang once it has been decided that it will be open source rather than
proprietary to tail-f. The plugin itself is just the single jpyang.py script.

2012-07-06
New tests for the INM and ConfM library classes are written.

2012-07-13
Work on a new structure for the JPyang code begins, using classes to represent
methods and organize functionality.

2012-07-16
Unit tests for JPyang functions and class methods are added to pyang.

1012-07-27
The ConfM.xs classes are replaced by new internal representations of the YANG
built in classes.

2012-08-03
The INM and ConfM libraries are merged into the new JNC library, with better
support for YANG.

2012-08-16
JPyang is now fully object oriented, with method generator classes for all
relevant statements. This means that the generated classes should import all
JNC, java.util, java.math and generated classes that they use, and no other.

2012-08-20
The JPyang project repository is cleaned up, removing all non-comprehensible
files and adding a new README and ant build files.

2012-08-23
Meeting at Tail-f about the future of the project. The marketing and product
management VP seems inclined to release as open source once the most basic
functionality has been tested and the documentation is complete.

2012-08-28
JPyang is renamed to JNC. So now there is both a JNC library and a JNC pyang
plugin.

2012-08-29
The GitHub repository is renamed to JNC and this readme is updated.