# JNC

Java NETCONF Client.

## Overview

JNC (Java NETCONF Client) consists of two parts:

* Java library for NETCONF client (JNC library)
* pyang plugin for Java class hierarchies generation.

Together with the JNC library, these generated Java classes may be used as the foundation for a NETCONF client (AKA manager) written in Java.

JNC's Java library code uses [SSHJ](https://github.com/hierynomus/sshj) to communicate with NETCONF servers/agents.


## Getting started

Typical use-case of JNC consists of three "standalone" areas - **Java library** for NETCONF clients, **pyang plugin** for YANG hierarchy class generation, and finally complete **Java application** connecting everything into combined client application.

### JNC Java library

JNC's Java library uses [Gradle](https://gradle.org/) as its build system.

If your `gradle` is outdated or is not installed at all, you can use Gradle Wrapper.
To use Gradle Wrapper you replace `gradle` command with `./gradlew` in the following steps/commands to be executed.
Wrapper automatically downloads & caches the correct Gradle version, and uses it to invoke Gradle commands.

You build the JNC Java library with following command:
```
gradle build
```
resp.
```
gradlew build
```
when using the wrapper...

This will download dependencies (if needed), build JNC library, run tests and create corresponding JAR files (library, Javadoc and sources) in the `build/libs` directory.

Alternative to building the JNC library locally is to utilize online Maven based repository.
See codebase of JNC `examples/` for details on the inclusion of Java lib for building your NETCONF client application.


### Installing JNC Pyang plugin

The easiest way to use the pyang plugin is to install it into your environment
using `pip` (or `pip3`) like `pip install jnc`.  This makes sure that `pyang`
is installed too and that `jnc` is registered as a Pyang plugin.

Another option is to use the plugin source that is part of this repository.
For that, you need an installation of *pyang* - get it
[here](https://github.com/mbj4668/pyang) or use `pip3 install pyang`. Then, add
the JNC plugin to your existing pyang installation. This may be done in one of
the following three ways:

1. Add jnc.py to `pyang/plugins` in your pyang installation directory,
2. Add the location of jnc.py to the `$PYANG_PLUGINPATH` environment variable, or
3. Use the `--plugindir` option of pyang each time you want to use JNC

**Note!**
If more than one of these approaches is used, you will end up with *optparse*
library conflicts, so please choose one and stick with it. From here on, we
will assume that you went for (1), but using (2) or (3) should be analogous.


### Generating Java classes

JNC plugin can be used to generate Java classes from a YANG file of your choice.

There is a collection of yang files in the `examples/yang` directory.

To generate classes for a YANG file, open a terminal, change directory to where you want the classes to be generated, launch pyang with the `"jnc"` format, specifying the output folder and the yang model file.

For example, to generate the classes for `simple.yang` file included in our `examples` directory with base package name `gen.simple`, type:

     $ pyang -f jnc --jnc-output src/gen/simple yang/simple.yang

There should now be a newly generated `"src"` folder in the current directory, containing a directory structure with the generated classes. Note that `"src"` is special treated so that it does not become part of the package names of the generated classes.

To get more detailed information on how the generation proceeds the `--jnc-debug` or `--jnc-verbose` options can be used. Re-running JNC silently overwrites any old classes in the output directory.

### JNC Java application

To actually use the generated classes, you need to compile Java client code with the JNC library (JAR file). The examples of JNC application can be found in the `examples` directory.

Gradle is used to build application. It also shows how to use JNC pyang plugin with Gradle task.

See the example's `README` file for details.


## pyang generated files - high level description

There are different types of filed generated by pyang plugin:


| Class name | Description |
| --- | --- |
| Root class | This class has the name of the prefix of the YANG module, and contains fields with the prefix and namespace as well as methods that enables the JNC library to use the other generated classes when interacting with a NETCONF server.|
| YangElement | Each YangElement corresponds to a container or a list in the YANG model.<br/>They represent tree nodes of a configuration and provides methods to modify the configuration in accordance with the YANG model that they were generated from.<br/>The top-level containers or lists in the YANG model will have their corresponding YangElement classes generated in the output directory together with the root class.<br/>Their respective sub-containers and sub-lists are generated in sub-packages with names corresponding to the name of the parent container or list. |
| YangTypes | For each derived type in the YANG model, a class is generated to the root of the output directory. The derived type may either extend another derived type class, or the JNC type class corresponding to a built-in YANG type. |
| PackageInfo | For each package in the generated Java class hierarchy, a `package-info.java` file is generated, which can be useful when generating Javadoc for the hierarchy. |
| Schema file | If enabled, an XML file containing structured information about the generated Java classes is generated. It contains tag-paths, namespace, primitive-type and other useful meta-information. |

The typical use case for these classes is as part of a JAVA network management system, to enable retrieval and/or storing of configurations on NETCONF agents/servers with specific capabilities.


## License
Copyright 2012-2022 Tail-f Systems AB

See [License File](LICENSE).
