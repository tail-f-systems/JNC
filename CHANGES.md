## Release History

2022-06-22
- Ganymed SSH-2 was replaced with SSHJ. Build system updated to Gradle. 

2012-10-17
- JNC is finally made open source! There are some changes remaining
before this will be publicly announced however.

2012-09-20
- Support for notifications added, and there is only one thing
remaining until it is possible to generate fully compliant junos.yang classes!

2012-08-29
- The GitHub repository is renamed to JNC and its readme is updated.

2012-08-28
- JPyang is renamed to JNC. So now there is both a JNC library and a
JNC pyang plugin.

2012-08-23
- Meeting at Tail-f about the future of the project. The marketing and
product management VP seems inclined to release as open source once the most
basic functionality has been tested and the documentation is complete.

2012-08-20
- The JPyang project repository is cleaned up, removing all non-comprehensible
files and adding a new README and ant build files.

2012-08-16
- JPyang is now fully object oriented, with method generator classes for all
relevant statements. This means that the generated classes should import all
JNC, java.util, java.math and generated classes that they use, and no other.

2012-08-03
- The INM and ConfM libraries are merged into the new JNC library, with better
support for YANG.

2012-07-27
- The ConfM.xs classes are replaced by new internal representations of the YANG
built in classes.

2012-07-16
- Unit tests for JPyang functions and class methods are added to pyang.

2012-07-13
- Work on a new structure for the JPyang code begins, using classes to represent
methods and organize functionality.

2012-07-06
- New tests for the INM and ConfM library classes are written.

2012-06-20
- Empty initial commit of the repository. It will contain the source code for
JPyang once it has been decided that it will be open source rather than
proprietary to tail-f. The plugin itself is just the single jpyang.py script.

2012-06-12
- JPyang is born as a few lines of python code that integrates with pyang are
written.

2012-06-04
- Emil starts working at Tail-f and reads up on the NETCONF RFC 6241 and YANG RFC
6020. You should too if you intend to contribute to this project!
