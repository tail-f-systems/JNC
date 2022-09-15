# JNC

Java NETCONF Client.

## Overview

JNC (Java NETCONF Client) consists of two parts: 

* Java library for NETCONF client
* Pyang  plugin. You need an installation of pyang to use the JNC
plugin. Get it here: https://github.com/mbj4668/pyang 
(or use `pip3 install pyang`).

The JNC Pyang plugin can be used to generate Java class hierarchies from YANG data
models. Together with the JNC library, these generated Java classes may be used
as the foundation for a NETCONF client (AKA manager) written in Java. 

The JNC library is distributed along with the JNC plugin script.

JNC uses SSHJ (https://github.com/hierynomus/sshj) 
to communicate with NETCONF servers/agents.


## Getting started

### JNC Java library

To build JNC Java library, use Gradle (https://gradle.org/). 
If you have Gradle installed (use https://sdkman.io/), build JNC Java library with
command:

```
gradle build
```

This command will download dependencies (if needed), 
build JNC library, run tests and create corresponding JAR files 
(library, javadoc and sources) in the `build/libs` directory.

If your `gradle` is outdated or is not installed at all, you can use Gradle Wrapper. 
To use Gradle Wrapper you replace `gradle` command with `./gradlew`.

Wrapper takes care of downloading correct gradle version (only once) 
and calling it.  

E.g.:

```
./gradlew build
```
  
TODO: 
_It is planned to upload created artefacts (JAR files) to Maven repository, 
so the JNC Java library can be directly used in the Java applications using
Maven repostory for building (Gradle, Maven)._
                                        
### JNC Pyang plugin

Make sure `pyang` is installed in your system and is on the `PATH` 
(check it with `pyang --version`).

Add the JNC plugin to your existing pyang installation. This may be done in one
of the following three ways:

1. Add jnc.py to pyang/plugins in your pyang installation,
2. Add the location of jnc.py to the `$PYANG_PLUGINPATH` environment variable, or
3. Use the `--plugindir` option of pyang each time you want to use JNC

If more than one of these approaches is used, you will end up with optparse
conflicts so please choose one and stick with it. From here on, we will assume
that you went for (1), but using (2) or (3) should be analogous.

JNC plugin can be used to generate Java classes from a YANG file of your choice.
There are a collection of yang files in the 'examples/yang' directory. To
generate classes for a YANG file, open a terminal, change directory to where
you want the classes to be generated, launch pyang with the jnc format,
specifying the output folder and the yang model file.

For example, to generate the classes for simple.yang to the 'examples'
directory with base package gen.simple, type:

     $ pyang -f jnc --jnc-output src/gen/simple yang/simple.yang

There should now be a newly generated 'src' folder in the current directory,
containing a directory structure with the generated classes. Note that 'src' is
special treated so that it does not become part of the package names of the
generated classes.

To get more detailed information on how the generation proceeds the --jnc-debug
or --jnc-verbose options can be used. Rerunning JNC silently overwrites any old
classes in the output directory.
            
### JNC Java application

To actually use the generated classes, you need to compile Java client code
using the JNC library (JAR file). The examples of JNC application can be 
found in the `examples` directory.

Gradle is used to build application. It is also shown how to use JNC
pyang plugin with Gradle task. 

See `README` for the examples. 


## High level description

The different types of generated files are:

Root class  -- This class has the name of the prefix of the YANG module, and
               contains fields with the prefix and namespace as well as methods
               that enables the JNC library to use the other generated classes
               when interacting with a NETCONF server.

YangElement -- Each YangElement corresponds to a container or a list in the
               YANG model. They represent tree nodes of a configuration and
               provides methods to modify the configuration in accordance with
               the YANG model that they were generated from.
               The top-level containers or lists in the YANG model will have
               their corresponding YangElement classes generated in the output
               directory together with the root class. Their respective
               subcontainers and sublists are generated in subpackages with
               names corresponding to the name of the parent container or list.

YangTypes   -- For each derived type in the YANG model, a class is generated to
               the root of the output directory. The derived type may either
               extend another derived type class, or the JNC type class
               corresponding to a built-in YANG type.

Packageinfo -- For each package in the generated Java class hierarchy, a
               package-info.java file is generated, which can be useful when
               generating javadoc for the hierarchy.

Schema file -- If enabled, an XML file containing structured information about
               the generated Java classes is generated. It contains tagpaths,
               namespace, primitive-type and other useful meta-information.

The typical use case for these classes is as part of a JAVA network management
system (EMS), to enable retrieval and/or storing of configurations on NETCONF
agents/servers with specific capabilities.




## License
Copyright 2012 Tail-f Systems AB

See [License File](LICENSE).
