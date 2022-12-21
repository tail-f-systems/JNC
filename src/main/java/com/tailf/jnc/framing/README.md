Classes in this package implement core parts of both NETCONF-1.0 and
NETCONF-1.1 framing mechanisms.  Many of the design decisions stem from the
following facts or requirements:

* It is desirable that the implementations of the two mechanisms are
  transparent to users; this is further complicated by the fact that it needs
  to be possible to "hook" into `read` calls (e.g. to check for timeouts).

* The 1.1 framing works best with byte data - the chunk length field indicates
  number of bytes of the chunk data (not number of characters; this makes a
  difference for non-latin characters).  So as to avoid repeated encoding of
  the String data, it is best to work with bytes directly, including using
  `InputStream`/`OutputStream` instances instead of `Reader` instances.

* The 1.0 framing mechanism, on the other hand works best with String
  instances, in particular because it is necessary to search the endmarker
  string when receiving; with bytes, this is tricky to do correctly and not
  natively supported by Java.  This also implies using `Reader`/`Writer`
  instances.
  
The components are these, described top-down:

* Enumeration `Framing` with two variants (for 1.0 and 1.1 framing mechanisms)
  that serve as a factory for `Framer` instances.
  
* Interface `Framer` whose instances are responsible for actual receiving and
  sending of frame data.  An instance life scope is supposed to be one
  session. To create an instance it is obviously necessary to provide input and
  output streams, but also a `DataReader` instance, if read hooks are required.
  
* Interface `DataReader` instance can be used to wrap `read` calls.  Its
  methods are invoked whenever actual reading of data from a stream/stream
  reader need to be performed.

* Interface `BaseReader` that serves as a common facade over `InputStream` and
  `Reader` instances.  Instances of this interface are provided to the
  `DataReader` methods.
