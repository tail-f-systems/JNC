package com.tailf.jnc;

import com.tailf.jnc.framing.Framing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A NETCONF session class. It makes it possible to connect to a NETCONF agent
 * using a preferred transport mechanism. After a successful connect all
 * operations defined by the NETCONF configuration protocol [&lt;a target="_top"
 * href="ftp://ftp.rfc-editor.org/in-notes/rfc4741.txt"&gt;RFC 4741&lt;/a&gt;] can be
 * performed, e.g. <code>get-config</code>, <code>edit-config</code> and
 * <code>commit</code>.
 * <p>
 * The NETCONF session class is data model agnostic. Manipulation of data is
 * done via the {@link Element} class.
 * <p>
 * Example 1: Read and manipulate data locally
 * <p>
 * A get-config operation with an XPATH filter is sent to the agent (the agent
 * must implement the <code>:xpath</code> capability). When the configuration
 * tree is received it is edited locally and written back with an
 * <code>edit-config</code> operation. The read and write is done towards the
 * RUNNING datastore.
 *
 * <pre>
 * // Start NETCONF session towards devices
 * SSHConnection c = new SSHConnection(&quot;127.0.0.1&quot;, 5678);
 * c.authenticateWithPassword(&quot;admin&quot;, &quot;pass&quot;);
 * SSHSession ssh = new SSHSession(c);
 * NetconfSession dev1 = new NetconfSession(ssh);
 *
 * // Get system configuration from dev1 (RUNNING datastore)
 * // Read is done using Xpath expression
 * Element sys1 = dev1.getConfig(&quot;/system&quot;).first();
 *
 * // Manipulate the config tree locally
 * sys1.setValue(&quot;dns&quot;, &quot;83.100.1.1&quot;);
 * sys1.setValue(&quot;gateway&quot;, &quot;10.0.0.1&quot;);
 *
 * // Write back the updated element tree to the device
 * dev1.editConfig(sys1);
 * </pre>
 * <p>
 * Example 2: Read config using subtree filtering
 * <p>
 * A subtree filter is built using the <code>Element.create</code> method. The
 * matching configuration is received and one of the elements is deleted. It is
 * "marked" for deletion and an <code>edit-config</code> operation is used to
 * actually delete the element on the agent.
 *
 * <pre>
 * // Start NETCONF session toward our device
 * SSHConnection c = new SSHConnection(&quot;127.0.0.1&quot;, 5678);
 * c.authenticateWithPassword(&quot;admin&quot;, &quot;pass&quot;);
 * SSHSession ssh = new SSHSession(c);
 * NetconfSession dev1 = new NetconfSession(ssh);
 *
 * // Create subtree filter
 * Element filter = Element.create(&quot;/hosts/host[name='kalle']&quot;);
 * // Read from RUNNING datastore
 * Element hosts = dev1.getConfig(filter).first();
 *
 * // delete host 'kalle'
 * // that is... mark the node for deletion
 * sys1.markDelete(&quot;hosts/host[name='kalle']&quot;);
 *
 * // edit-config. The deleted node has the operation=&quot;delete&quot;
 * // marked on it.
 * dev1.editConfig(sys1);
 * </pre>
 * <p>
 * Example 3: Transaction using candidates
 * <p>
 *
 * <pre>
 * try {
 *     // Start (NETCONF) sessions towards devices
 *     SSHConnection c1 = new SSHConnection(&quot;127.0.0.1&quot;, 3456);
 *     SSHConnection c2 = new SSHConnection(&quot;10.4.5.6&quot;, 3456);
 *     c1.authenticateWithPassword(&quot;admin&quot;, &quot;pass&quot;);
 *     c2.authenticateWithPassword(&quot;admin&quot;, &quot;pass&quot;);
 *     SSHSession ssh1 = new SSHSession(c1);
 *     SSHSession ssh2 = new SSHSession(c2);
 *     NetconfSession dev1 = new NetconfSession(ssh1);
 *     NetconfSession dev2 = new NetconfSession(ssh2);
 *
 *     // take locks on CANDIDATE datastore so that we are not interrupted
 *     dev1.lock(NetconfSession.CANDIDATE);
 *     dev2.lock(NetconfSession.CANDIDATE);
 *
 *     // reset candidates so that CANDIDATE is an exact copy of running
 *     dev1.copyConfig(NetconfSession.RUNNING, NetconfSession.CANDIDATE);
 *     dev2.copyConfig(NetconfSession.RUNNING, NetconfSession.CANDIDATE);
 *
 *     // Get system configuration from dev1
 *     Element sys1 = dev1.getConfig(&quot;/system&quot;).first();
 *
 *     // Manipulate element trees locally
 *     sys1.setValue(&quot;dns&quot;, &quot;83.100.1.1&quot;);
 *     sys1.setValue(&quot;gateway&quot;, &quot;10.0.0.1&quot;);
 *
 *     // Write back the updated element tree to both devices
 *     dev1.editConfig(NetconfSession.CANDIDATE, sys1);
 *     dev2.editConfig(NetconfSession.CANDIDATE, sys1);
 *
 *     // candidates are now updated
 *     dev1.confirmedCommit(60);
 *     dev2.confirmedCommit(60);
 *
 *     // now commit them
 *     dev1.commit();
 *     dev2.commit();
 *
 *     dev1.unlock(NetconfSession.CANDIDATE);
 *     dev2.unlock(NetconfSession.CANDIDATE);
 *
 * } catch (Exception e) {
 *     // Devices will rollback within 1 min
 *
 * }
 * </pre>
 *
 * @see Element
 *
 **/

public class NetconfSession {

    public static final String GET_CONFIG_GT = "get-config>";
    public static final String SOURCE_GT = "source>";
    public static final String FILTER_GT = "filter>";
    public static final String FILTER = "filter ";
    public static final String GET_GT = "get>";
    public static final String EDIT_CONFIG_GT = "edit-config>";
    public static final String TARGET_GT = "target>";
    public static final String CONFIG_GT = "config>";
    public static final String COPY_CONFIG_GT = "copy-config>";
    public static final String VALIDATE_GT = "validate>";
    public static final String STREAM_GT = "stream>";
    public static final String START_TIME_GT = "startTime>";
    public static final String STOP_TIME_GT = "stopTime>";
    /**
     * Monotonically increased message identifier for this session.
     */
    int message_id = 1;

    /**
     * The <code>RUNNING</code> datastore.
     */
    public static final int RUNNING = 0;
    /**
     * The <code>STARTUP</code> datastore.
     */
    public static final int STARTUP = 1;
    /**
     * The <code>CANDIDATE</code> datastore.
     */
    public static final int CANDIDATE = 2;

    /**
     * Session identifier. Received from the initial <code>hello</code>
     * message.
     */
    public long sessionId;

    /**
     * The capability elements for the session. Retrieved from
     * <code>hello</code> message upon connect.
     */
    protected Capabilities capabilities;

    /**
     * Return a Capabilities object with the NETCONF capabilities for this
     * session. The capabilities are received from the <code>hello</code>
     * message from the server.
     */
    public Capabilities getCapabilities() {
        return capabilities;
    }

    /**
     * Check if a specific named capability is supported.
     *
     * @param uri Name of capability to check
     */
    public boolean hasCapability(String uri) {
        return capabilities.hasCapability(uri);
    }

    /**
     * The XML parser instance.
     */
    XMLParser parser;

    /**
     * The outgoing transport for this Session
     */
    Transport out;

    /**
     * The incoming transport for this Session
     */
    Transport in;

    /**
     * netconf client version: default 1.0
     */

     boolean use11;

    /**
     * Creates a new session object using the given transport object. This will
     * initialize the transport and send out an initial hello message to the
     * server.
     *
     * @see SSHSession
     *
     * @param transport Transport object
     */

    public NetconfSession(Transport transport) throws JNCException,
            IOException {
        out = transport;
        in = transport; // same
        parser = new XMLParser();
        hello();
    }

    /**
     * Creates a new session object using the given transport object. This will
     * initialize the transport and send out an initial hello message to the
     * server.
     *
     * @see SSHSession
     *
     * @param transport Transport object
     * @param parser XML parser object
     *
     *            If we are using the confm package to create and manipulate
     *            objects we must instantiate the parser as an
     *            com.tailf.confm.XMLParser() If we fail to do that, we get the
     *            XMLParser class from the inm package which always only
     *            returns Element objects as opposed to the XMLParser from the
     *            confm package which can return objects matching the confm
     *            generated classes. If we use the confm Device class to create
     *            our netconf sessions, this is all done automatically, whereas
     *            if we create our netconf sessions ourselves, _and_ also want
     *            to retrieve real confm objects we must:
     *
     *            <pre>
     * SSHConnection ssh = new SSHConnection(&quot;127.0.0.1&quot;, 2022);
     * ssh.authenticateWithPassword(&quot;admin&quot;, &quot;admin&quot;);
     * Transport tr = new SSHSession(ssh);
     * NetconfSession sess = new NetconfSession(tr, new com.tailf.confm.XMLParser());
     * </pre>
     **/

    public NetconfSession(Transport transport, XMLParser parser)
            throws JNCException, IOException {
        this(transport, parser, true);
    }

    @SuppressWarnings("PMD.ClassNamingConventions")
    public NetconfSession(Transport transport, XMLParser parser, boolean use11)
            throws JNCException, IOException {
        out = transport;
        in = transport; // same
        this.parser = parser;
        this.use11 = use11;
        hello();
    }

    /**
     * Creates a new session object. The session need to be given a transport
     * object with {@link #setTransport(Transport)} and an initial hello needs
     * to be sent to advertise capabilities.
     */
    public NetconfSession() throws JNCException {
        parser = new XMLParser();
    }

    /**
     * Sets the transport used by this session.
     *
     * @param transport Transport object, for example {@link SSHSession}
     */
    public void setTransport(Transport transport) {
        out = transport;
        in = transport; // same
    }

    /**
     * Returns the transport object used by this session.
     *
     * @see SSHSession
     */
    public Transport getTransport() {
        return in;
    }

    /**
     * Capabilities are advertised in messages sent by each peer during session
     * establishment. When the NETCONF session is opened, each peer (both
     * client and server) must send a <code>hello</code> element containing a
     * list of that peer's capabilities. Each peer must send at least the base
     * NETCONF capability, "urn:ietf:params:netconf:base:1.0".
     * <p>
     * This method will send an initial <code>hello</code> to the output stream
     * and await the <code>hello</code> from the server.
     * <p>
     * Used from the constructor {@link #NetconfSession(Transport)}.
     */
    protected void hello() throws JNCException, IOException {
        trace("hello: ");
        encodeHello(out);
        out.flush();
        final String reply = in.readOne();
        // System.out.println("reply= "+ reply);
        final Element t = parser.parse(reply);
        final Element capatree = t.getFirst("self::hello/capabilities");
        if (capatree == null) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "hello contains no capabilities");
        }
        trace("capabilities: \n{}", capatree.toXMLString());

        capabilities = new Capabilities(capatree);
        if (!capabilities.baseCapability && !capabilities.baseCapability_v1_1) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "server does not support NETCONF base capability: "
                            + Capabilities.NETCONF_BASE_CAPABILITY);
        }

        if (capabilities.baseCapability_v1_1 && use11){
            out.setFraming(Framing.CHUNKED);
        }
        // lookup session id
        final Element sess = t.getFirst("self::hello/session-id");
        if (sess == null) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "hello contains no session identifier");
        }
        sessionId = Long.parseLong((String) sess.value);
        trace("sessionId = {}", sessionId);
    }

    /**
     * The NETCONF protocol uses a remote procedure call (RPC) paradigm. A
     * client encodes an RPC in XML and sends it to a server using a secure,
     * connection-oriented session. The server responds with a reply encoded in
     * XML.
     * <p>
     * This method may be used for sending an XML string over the connected
     * session and receiving a reply.
     * <p>
     * The reply is parsed into an element tree.
     *
     * @param request XML encoded NETCONF request
     */
    public Element rpc(String request) throws IOException, JNCException {
        out.print(request);
        out.flush();
        return parser.parse(in.readOne());
    }

    /**
     * The NETCONF protocol uses a remote procedure call (RPC) paradigm. A
     * client encodes an RPC in XML and sends it to a server using a secure,
     * connection-oriented session. The server responds with a reply encoded in
     * XML.
     * <p>
     * This method may be used for sending an XML element tree the connected
     * session and receiving a reply.
     * <p>
     * The reply is parsed into an element tree.
     *
     * @param request XML element tree
     */
    public Element rpc(Element request) throws IOException, JNCException {
        // print, but no newline at the end
        out.print(request.encodedXMLString(false));
        out.flush();
        return parser.parse(in.readOne());
    }

    /**
     * Sends rpc request and return. This method may be used for sending an XML
     * string over the connected session. To receive a reply the
     * {@link #readReply()} should be used.
     * <p>
     * Note that the return value reflects the last request-id generated by the
     * NetconfSession methods, and is not checked against the request-id in the
     * request parameter.
     *
     * @param request XML encoded NETCONF request
     */
    public int sendRequest(String request) throws IOException {
        // no newline before flush
        out.print(request);
        out.flush();
        return message_id - 1; // FIXME
    }

    /**
     * Sends rpc request and return. This method may be used for sending an XML
     * element tree over the connected session. To receive a reply the
     * {@link #readReply()} should be used.
     * <p>
     * Note that the return value reflects the last request-id generated by the
     * NetconfSession methods, and is not checked against the request-id in the
     * request parameter.
     *
     * @param request Element tree
     */
    public int sendRequest(Element request) throws IOException, JNCException {
        // print, but no newline at the end
        out.print(request.encodedXMLString(false));
        out.flush();
        return message_id - 1; // FIXME
    }

    /**
     * Reads a rpc reply from NETCONF. Preforms a blocking read.
     * <p>
     * The reply is parsed into an element tree.
     *
     * @see #sendRequest(Element)
     */
    public Element readReply() throws IOException, JNCException {
        return parser.parse(in.readOne());
    }

    /**
     * Gets the device configuration data specified by subtree filtering.
     *
     * @param subtreeFilter A subtree filter
     */
    public NodeSet getConfig(Element subtreeFilter) throws JNCException,
            IOException {
        return getConfig(RUNNING, subtreeFilter);
    }

    /**
     * Gets the device configuration data.
     */
    public NodeSet getConfig() throws JNCException, IOException {
        return getConfig(RUNNING);
    }

    /**
     * Gets the device configuration data.
     */
    public NodeSet getConfig(int datastore) throws JNCException, IOException {
        trace("getConfig: {}", datastoreToString(datastore));
        RPCRequest rpcRequest = prepareGetConfigMessage(encodeDatastore(datastore));
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        return recvRpcReplyData(rpcRequest.getMsgId());
    }

    /**
     * Calls rpc method.
     */
    public NodeSet callRpc(Element data) throws JNCException, IOException {
        trace("call: " + data.toXMLString());
        RPCRequest rpcRequest = prepareRPCMessage(data);
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        return recvCallRpcReply(data, rpcRequest.getMsgId());
    }

    /**
     * Calls rpc method but does not read a response.
     * <p>
     * Returns the request-id used in the message.
     */
    public int sendRpc(Element data) throws JNCException, IOException {
        trace("send rpc: " + data.toXMLString());
        RPCRequest rpcRequest = prepareRPCMessage(data);
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        return rpcRequest.getMsgId();
    }

    /**
     * Gets the device configuration data specified by an xpath expression. The
     * <code>:xpath</code> capability must be supported by the server.
     *
     * @param xpath XPath expression
     */
    public NodeSet getConfig(String xpath) throws JNCException, IOException {
        return getConfig(RUNNING, xpath);
    }

    /**
     * Gets the device configuration data specified by subtree filtering.
     *
     * @param datastore The datastore. One of {@link #RUNNING},
     *            {@link #CANDIDATE}, {@link #STARTUP}
     * @param subtreeFilter A subtree filter
     */
    public NodeSet getConfig(int datastore, Element subtreeFilter)
            throws JNCException, IOException {
        trace("getConfig: {}\n{}", datastoreToString(datastore),
              subtreeFilter.toXMLString());
        RPCRequest rpcRequest = prepareGetConfigMessage(encodeDatastore(datastore), subtreeFilter);
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        return recvRpcReplyData(rpcRequest.getMsgId());
    }

    /**
     * Gets the device configuration data specified by an xpath filter.
     *
     * @param datastore The datastore. One of {@link #RUNNING},
     *            {@link #CANDIDATE}, {@link #STARTUP}
     * @param xpath XPath expression
     */
    public NodeSet getConfig(int datastore, String xpath)
            throws JNCException, IOException {
        trace("getConfig: {} \"{}\"", datastoreToString(datastore), xpath);
        if (!capabilities.xpathCapability) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "the :xpath capability is not supported by server");
        }
        RPCRequest rpcRequest = prepareGetConfigMessage(encodeDatastore(datastore), xpath);
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        return recvRpcReplyData(rpcRequest.getMsgId());
    }

    /**
     * Retrieves running configuration and device state information.
     */
    public NodeSet get() throws JNCException, IOException {
        trace("get: \"\"");
        RPCRequest rpcRequest = prepareGetMessage("");
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        return recvRpcReplyData(rpcRequest.getMsgId());
    }

    /**
     * Retrieves running configuration and device state information.
     *
     * @param subtreeFilter A subtree filter
     */
    public NodeSet get(Element subtreeFilter) throws JNCException,
            IOException {
        trace("get: " + (null!=subtreeFilter?subtreeFilter.toXMLString(): null));
        RPCRequest rpcRequest = prepareGetMessage(subtreeFilter);
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        return recvRpcReplyData(rpcRequest.getMsgId());
    }

    /**
     * Retrieves running configuration and device state information. The
     * <code>:xpath</code> capability must be supported by the server.
     *
     * @param xpath An xpath epxression.
     */
    public NodeSet get(String xpath) throws JNCException, IOException {
        trace("get: \"{}\"", xpath);
        if (!capabilities.hasXPath()) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "the :xpath capability is not supported by server");
        }
        RPCRequest rpcRequest = prepareGetMessage(xpath);
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        return recvRpcReplyData(rpcRequest.getMsgId());
    }

    /**
     * Edits the configuration. The <code>edit-config</code> operation loads
     * all or part of a specified configuration to the {@link #RUNNING} target
     * datatore.
     *
     * @param configTree Configuration tree
     */
    public void editConfig(Element configTree) throws JNCException,
            IOException {
        editConfig(RUNNING, configTree);
    }

    /**
     * Edits the configuration. If we have multiple top elements in our
     * configuration schema (YANG model) we must send a NodeSet as opposed to
     * an Element to the device
     */

    public void editConfig(NodeSet configTrees) throws JNCException,
            IOException {
        editConfig(RUNNING, configTrees);
    }

    /**
     * Edits the configuration. The <code>edit-config</code> operation loads
     * all or part of a specified configuration to the specified target
     * configuration.
     *
     * @param datastore The target datastore. One of {@link #RUNNING},
     *            {@link #CANDIDATE}, {@link #STARTUP}
     * @param configTree The config tree to edit.
     */
    public void editConfig(int datastore, Element configTree)
            throws JNCException, IOException {
        trace("editConfig: target={}\n{}", datastoreToString(datastore), configTree.toXMLString());
        RPCRequest rpcRequest = prepareEditConfigMessage(encodeDatastore(datastore),
                new NodeSet(configTree));
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    public void editConfig(int datastore, NodeSet configTrees)
            throws JNCException, IOException {
        trace("editConfig: target=" + datastoreToString(datastore) + "\n"
                + configTrees.toXMLString());
        RPCRequest rpcRequest = prepareEditConfigMessage(encodeDatastore(datastore), configTrees);
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());

    }

    /**
     * Edits the configuration. The &lt;edit-config&gt; operation loads all or part
     * of a specified configuration to the specified target configuration.
     *
     * @param datastore The target datastore. One of {@link #RUNNING},
     *            {@link #CANDIDATE}, {@link #STARTUP}
     * @param url The source url.
     */
    public void editConfig(int datastore, String url) throws JNCException,
            IOException {
        trace("editConfig: target={} source={}", datastoreToString(datastore), url);
        RPCRequest rpcRequest = prepareEditConfigMessage(encodeDatastore(datastore), url);
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());

    }

    /**
     * Value is not set.
     */
    public static final int NOT_SET = 0;

    /**
     * Value for default operation.
     *
     * @see #setDefaultOperation(int)
     */
    public static final int MERGE = 1;

    /**
     * Value for default operation.
     *
     * @see #setDefaultOperation(int)
     */
    public static final int REPLACE = 2;

    /**
     * Value for default operation.
     *
     * @see #setDefaultOperation(int)
     */
    public static final int NONE = 3;

    /**
     * Specifies the default operation for all edit-config operations for the
     * session. The default value for the default-operation parameter is
     * "merge", unless a value is set by invoking this method.
     * <p>
     * The default-operation parameter is optional, but if provided, it must
     * have one of the following values:
     * <ul>
     * <li>
     * {@link #MERGE}: The configuration data is merged with the configuration
     * at the corresponding level in the target datastore.
     * <li>
     * {@link #REPLACE}: The configuration data completely replaces the
     * configuration in the target datastore. This is useful for loading
     * previously saved configuration data.
     * <li>
     * {@link #NONE}: The target datastore is unaffected by the configuration
     * parameter, unless and until the incoming configuration data uses the
     * "operation" attribute to request a different operation. If the
     * configuration parameter contains data for which there is not a
     * corresponding level in the target datastore, an "rpc-error" is returned
     * with an "error-tag" value of data-missing. Using "none" allows
     * operations like "delete" to avoid unintentionally creating the parent
     * hierarchy of the element to be deleted.
     * </ul>
     *
     * @param op One of {@link #MERGE}, {@link #REPLACE}, {@link #NONE}.
     */
    public void setDefaultOperation(int op) {
        defaultOperation = op;
    }

    /**
     * The default-operaton for edit-config operations.
     */
    private int defaultOperation = NOT_SET;

    /**
     * Value for test option.
     *
     * @see #setTestOption(int)
     */
    public static final int SET = 1;

    /**
     * Value for test option.
     *
     * @see #setTestOption(int)
     */
    public static final int TEST_THEN_SET = 2;

    /**
     * Value for test option.
     *
     * @see #setTestOption(int)
     */
    public static final int TEST_ONLY = 3;

    /**
     * Specifies the test-option parameter for the edit-config operations for
     * the session. The test-option element may be specified only if the device
     * advertises the <code>:validate</code> capability.
     * <p>
     * The test-option element has one of the following values:
     * <ul>
     * <li>
     * {@link #TEST_THEN_SET}: Perform a validation test before attempting to
     * set. If validation errors occur, do not perform the "edit-config"
     * operation. This is the default test-option.
     * <li>
     * {@link #SET}: Perform a set without a validation test first.
     * <li>
     * {@link #TEST_ONLY}: Only test. (Option is not supported in the
     * standard.)
     * </ul>
     *
     * @param testoption One of {@link #SET}, {@link #TEST_THEN_SET},
     *            {@link #TEST_ONLY}
     */
    public void setTestOption(int testoption) {
        testOption = testoption;
    }

    /**
     * The test-option parameter sent in editConfig.
     */
    private int testOption = NOT_SET;

    /**
     * Value for error option.
     *
     * @see #setErrorOption(int)
     */
    public static final int STOP_ON_ERROR = 1;

    /**
     * Value for error option.
     *
     * @see #setErrorOption(int)
     */
    public static final int CONTINUE_ON_ERROR = 2;

    /**
     * Value for error option.
     *
     * @see #setErrorOption(int)
     */
    public static final int ROLLBACK_ON_ERROR = 3;

    /**
     * Specifies the error-option for the edit-config operations for the
     * session. The error-option element has one of the following values:
     * <ul>
     * <li>
     * {@link #STOP_ON_ERROR}: Abort the edit-config operation on first error.
     * This is the default error-option.
     * <li>
     * {@link #CONTINUE_ON_ERROR}: Continue to process configuration data on
     * error; error is recorded, and negative response is generated if any
     * errors occur.
     * <li>
     * {@link #ROLLBACK_ON_ERROR}: If an error condition occurs such that an
     * error severity "rpc-error" element is generated, the server will stop
     * processing the edit-config operation and restore the specified
     * configuration to its complete state at the start of this edit-config
     * operation. This option requires the server to support the
     * <code>:rollback-on-error</code> capability.
     * </ul>
     *
     * @param erroroption One of {@link #STOP_ON_ERROR},
     *            {@link #CONTINUE_ON_ERROR}, {@link #ROLLBACK_ON_ERROR}
     *
     */
    public void setErrorOption(int erroroption) {
        errorOption = erroroption;
    }

    /**
     * The error-option parameter sent in editConfig.
     */
    private int errorOption = NOT_SET;

    /**
     * Creates or replace an entire configuration datastore with the contents
     * of another complete configuration datastore. If the target datastore
     * exists, it is overwritten. Otherwise, a new one is created, if allowed.
     *
     * @param sourceTree A config tree
     * @param target The target datastore
     */
    public void copyConfig(Element sourceTree, int target)
            throws JNCException, IOException {
        copyConfig(new NodeSet(sourceTree), target);
    }

    /**
     * variant of copyConfig() that takes a NodeSet as param
     */

    public void copyConfig(NodeSet sourceTrees, int target)
            throws JNCException, IOException {

        trace("copyConfig: target=" + datastoreToString(target) + "\n"
                + sourceTrees.toXMLString());
        RPCRequest rpcRequest = prepareCopyConfigMessage(sourceTrees, encodeDatastore(target));
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * Same as {@link #copyConfig(Element,int)} but uses an url as target. Only
     * possible if <code>:url</code> capability is supported.
     *
     * @param sourceTree A config tree
     * @param targetUrl The target URL.
     */
    public void copyConfig(Element sourceTree, String targetUrl)
            throws JNCException, IOException {
        copyConfig(new NodeSet(sourceTree), targetUrl);
    }

    public void copyConfig(NodeSet sourceTrees, String targetUrl)
            throws JNCException, IOException {

        trace("copyConfig: target=" + targetUrl + "\n"
                + sourceTrees.toXMLString());
        RPCRequest rpcRequest = prepareCopyConfigMessage(sourceTrees, encodeUrl(targetUrl));
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * Creates or replace an entire configuration datastore with the contents
     * of another complete configuration datastore. If the target datastore
     * exists, it is overwritten. Otherwise, a new one is created, if allowed.
     *
     * @param source The source datastore
     * @param target The target datastore
     */
    public void copyConfig(int source, int target) throws JNCException,
            IOException {
        trace("copyConfig: {} {}", datastoreToString(source), datastoreToString(target));
        RPCRequest rpcRequest = prepareCopyConfigMessage(encodeDatastore(source), encodeDatastore(target));
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * Same as {@link #copyConfig(int,int)} but uses an url as target. Only
     * possible if <code>:url</code> capability is supported.
     *
     * @param source The datastore to be used as source
     * @param targetUrl The target URL.
     */
    public void copyConfig(int source, String targetUrl) throws JNCException,
            IOException {
        trace("copyConfig: source={} target={}", datastoreToString(source), targetUrl);
        RPCRequest rpcRequest = prepareCopyConfigMessage(encodeDatastore(source), encodeUrl(targetUrl));
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * Same as {@link #copyConfig(int,int)} but uses an url as target and an
     * url as a source. Only possible if <code>:url</code> capability is
     * supported.
     *
     * @param sourceUrl The source URL.
     * @param targetUrl The target URL.
     */
    public void copyConfig(String sourceUrl, String targetUrl)
            throws JNCException, IOException {
        trace("copyConfig: source={} target={}", sourceUrl, targetUrl);
        RPCRequest rpcRequest = prepareCopyConfigMessage(encodeUrl(sourceUrl), encodeUrl(targetUrl));
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * Same as {@link #copyConfig(int,int)} but uses an url as the source. Only
     * possible if <code>:url</code> capability is supported.
     *
     * @param sourceUrl The source URL.
     * @param target The target datastore
     */
    public void copyConfig(String sourceUrl, int target) throws JNCException,
            IOException {
        trace("copyConfig: source={} target={}", sourceUrl, datastoreToString(target));
        RPCRequest rpcRequest = prepareCopyConfigMessage(encodeUrl(sourceUrl), encodeDatastore(target));
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * Deletes a configuration datastore. The &lt;running&gt; configuration datastore
     * cannot be deleted.
     *
     * @param datastore Datastore to be deleted
     */
    public void deleteConfig(int datastore) throws JNCException, IOException {
        trace("deleteConfig: {}", datastoreToString(datastore));
        RPCRequest rpcRequest = prepareDeleteConfigMessage(encodeDatastore(datastore));
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * Deletes a configuration target url.
     *
     * @param targetUrl Name of configuration target url to be deleted.
     */
    public void deleteConfig(String targetUrl) throws JNCException,
            IOException {
        trace("deleteConfig: {}", targetUrl);
        RPCRequest rpcRequest = prepareDeleteConfigMessage(encodeUrl(targetUrl));
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * The lock operation allows the client to lock the configuration system of
     * a device. Such locks are intended to be short-lived and allow a client
     * to make a change without fear of interaction with other NETCONF clients,
     * non-NETCONF clients (e.g., SNMP and command line interface (CLI)
     * scripts), and human users.
     * <p>
     * An attempt to lock the configuration must fail if an existing session or
     * other entity holds a lock on any portion of the lock target.
     *
     * @param datastore The datastore to lock
     */
    public void lock(int datastore) throws JNCException, IOException {
        trace("lock: {}", datastoreToString(datastore));
        RPCRequest rpcRequest = prepareLockMessage(encodeDatastore(datastore));
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk();
    }

    /**
     * The unlock operation is used to release a configuration lock, previously
     * obtained with the {@link #lock} operation.
     *
     * @param datastore The target datastore to unlock
     */
    public void unlock(int datastore) throws JNCException, IOException {
        trace("unlock: {}", datastoreToString(datastore));
        RPCRequest rpcRequest = prepareUnlockMessage(encodeDatastore(datastore));
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk();
    }

    /**
     * The partial-lock operation allows the client to lock a portion of a data
     * store. The portion to lock is specified by an array of selection strings
     * which can be XPath filters (if the server supports XPath) or a simple
     * paths to instance idenfifiers.
     * <p>
     * If the selection filters are XPath expressions they are evaluated only
     * once at lock time, thereafter the scope of the lock is maintained as a
     * set of nodes. If the configuration data is later altered in a way that
     * would make the original XPath filter expressions evaluate to a different
     * set of nodes, this does not affect the scope of the partial lock. XPath
     * is only used for the creation of the partial lock. Conceptually the
     * scope of the lock is defined by the returned nodeset and not by the
     * XPath expression.
     * <p>
     * If a node is locked by a session, only that same session will be able to
     * modify that node or any node in the subtree underneath it.
     * <p>
     * If a top level node of a locked subtree is deleted, any other session
     * can recreate it, as it is not covered by the lock anymore. The lock
     * operation allows the client to lock the configuration system of a
     * device. Such locks are intended to be short-lived and allow a client to
     * make a change without fear of interaction with other NETCONF clients,
     * non-NETCONF clients (e.g., SNMP and command line interface (CLI)
     * scripts), and human users.
     * <p>
     * An attempt to lock the configuration must fail if an existing session or
     * other entity holds a lock on any portion of the lock target.
     *
     * @param datastore datastore of which a part will be locked
     * @param select An array of selection filters
     * @return A unique lock reference which should be used to unlockPartial()
     */
    public int lockPartial(String[] select) throws JNCException, IOException {
        trace("lockPartial");
        if (!capabilities.hasPartialLock()) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "capability :partial-lock is not supported by server");
        }
        // Allow simple paths to instance identifiers also
        // if (!xpathCapability)
        // throw new JNCException(JNCException.SESSION_ERROR,
        // "capability :xpath is not supported by server");
        RPCRequest rpcRequest = prepareLockPartialMessage(select);
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        final NodeSet reply = recvRpcReplyLockPartial(rpcRequest.getMsgId());
        try {
            final Element t = reply.first().getFirst("self::lock-id");
            return Integer.parseInt((String) t.value);
        } catch (final Exception e) {
            throw (JNCException) new JNCException(JNCException.SESSION_ERROR,
                    "bad lock-id returned from partial-lock: "
                            + reply.toXMLString()).initCause(e);
        }
    }

    /**
     * Same as {@link #lockPartial(int,String[])} except it only takes one
     * selection as argument.
     *
     * @see #lockPartial(int,String[])
     */
    public int lockPartial(String select) throws JNCException, IOException {
        return lockPartial(new String[] { select });
    }

    /**
     * The unlock operation is used to release a configuration lock, previously
     * obtained with the {@link #lock} operation.
     *
     * @param lockId Previously received lock identifier from
     *            {@link #lockPartial(int,String[])}
     */
    public void unlockPartial(int lockId) throws JNCException, IOException {
        trace("partialUnlock: {}", lockId);
        if (!capabilities.hasPartialLock()) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "capability :partial-lock is not supported by server");
        }
        if (!capabilities.hasXPath()) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "capability :xpath is not supported by server");
        }
        RPCRequest rpcRequest = prepareUnlockPartialMessage(lockId);
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * When a candidate configuration's content is complete, the configuration
     * data can be committed, publishing the data set to the rest of the device
     * and requesting the device to conform to the behavior described in the
     * new configuration.
     * <p>
     * To commit the candidate configuration as the device's new current
     * configuration, use the <code>commit</code> operation.
     * <p>
     * The <code>commit</code> operation instructs the device to implement the
     * configuration data contained in the candidate configuration. If the
     * device is unable to commit all of the changes in the candidate
     * configuration datastore, then the running configuration must remain
     * unchanged. If the device does succeed in committing, the running
     * configuration must be updated with the contents of the candidate
     * configuration.
     * <p>
     * If the system does not have the <code>:candidate</code> capability, the
     * <code>commit</code> operation is not available.
     *
     */
    public void commit() throws JNCException, IOException {
        trace("commit");
        if (!capabilities.hasCandidate()) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "the :candidate capability is not supported by server");
        }
        RPCRequest rpcRequest = prepareCommitMessage();
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * The <code>:confirmed-commit</code> capability indicates that the server
     * supports the confirmed commit.
     * <p>
     * A confirmed commit operation must be reverted if a follow-up commit
     * (called the "confirming commit") is not issued within 600 seconds (10
     * minutes). The timeout period can be adjusted with the timeout parameter.
     * <p>
     * If the session issuing the confirmed commit is terminated for any reason
     * before the confirm timeout expires, the server must restore the
     * configuration to its state before the confirmed commit was issued.
     * <p>
     * If the device reboots for any reason before the confirm timeout expires,
     * the server must restore the configuration to its state before the
     * confirmed commit was issued.
     * <p>
     * If a confirming commit is not issued, the device will revert its
     * configuration to the state prior to the issuance of the confirmed
     * commit. Note that any commit operation, including a commit which
     * introduces additional changes to the configuration, will serve as a
     * confirming commit. Thus to cancel a confirmed commit and revert changes
     * without waiting for the confirm timeout to expire, the manager can
     * explicitly restore the configuration to its state before the confirmed
     * commit was issued.
     * <p>
     * For shared configurations, this feature can cause other configuration
     * changes (for example, via other NETCONF sessions) to be inadvertently
     * altered or removed, unless the configuration locking feature is used (in
     * other words, the lock is obtained before the edit-config operation is
     * started). Therefore, it is strongly suggested that in order to use this
     * feature with shared configuration databases, configuration locking
     * should also be used.
     *
     * @param timeout Time that server will wait for confirming commit before
     *            reverting config
     */
    public void confirmedCommit(int timeout) throws JNCException, IOException {
        trace("confirmedCommit: {}", timeout);
        if (!capabilities.hasCandidate()) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "the :candidate capability is not supported by server");
        }
        if (!capabilities.hasConfirmedCommit()) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "the :confirmed-commit capability is not supported by server");
        }
        RPCRequest rpcRequest = prepareConfirmedCommitMessage(timeout);
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * If the client decides that the candidate configuration should not be
     * committed, the &lt;discard-changes&gt; operation can be used to revert the
     * candidate configuration to the current running configuration.
     */
    public void discardChanges() throws JNCException, IOException {
        trace("discardChanges");
        if (!capabilities.hasCandidate()) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "the :candidate capability is not supported by server");
        }
        RPCRequest rpcRequest = prepareDiscardChangesMessage();
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * Requests graceful termination of a NETCONF session.
     * <p>
     * When a NETCONF server receives a <code>close-session</code> request, it
     * will gracefully close the session. The server will release any locks and
     * resources associated with the session and gracefully close any
     * associated connections.
     */
    public void closeSession() throws JNCException, IOException {
        trace("closeSession");
        RPCRequest rpcRequest = prepareCloseSessionMessage();
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * Force the termination of a NETCONF session.
     * <p>
     * When a NETCONF entity receives a <code>kill-session</code> request for
     * an open session, it will abort any operations currently in process,
     * release any locks and resources associated with the session, and close
     * any associated connections.
     * <p>
     * Session identifier of the NETCONF session to be terminated, if this
     * value is the current session ID a JNCException will be thrown.
     *
     * @param sessionId The id of the session to terminate
     */
    public void killSession(long sessionId) throws JNCException, IOException {
        trace("killSession: {}", sessionId);
        if (sessionId == this.sessionId) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "illegal to use kill-session on own session id");
        }
        RPCRequest rpcRequest = prepareKillSessionMessage(sessionId);
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * This protocol operation validates the contents of the specified
     * configuration.
     *
     * @param configTree configuration tree to validate
     */
    public void validate(Element configTree) throws JNCException, IOException {
        trace("validate: {}", configTree.toXMLString());
        if (!capabilities.hasValidate()) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "capability :validate is not supported by server");
        }
        RPCRequest rpcRequest = prepareValidateMessage(configTree);
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * This protocol operation validates the given datastore. For example the
     * {@link #CANDIDATE} datastore.
     *
     * @param datastore The datastore to validate
     */
    public void validate(int datastore) throws IOException, JNCException {
        trace("validate: {}", datastoreToString(datastore));
        if (!capabilities.hasValidate()) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "capability :validate is not supported by server");
        }
        RPCRequest rpcRequest = prepareValidateMessage(encodeDatastore(datastore));
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * This protocol operation validates the given URL. The url may for example
     * be a file <code>"file://incoming.conf"</code> then file must be
     * supported by the <code>:url</code> capability.
     *
     * @param url The source url to validate
     */
    public void validate(String url) throws IOException, JNCException {
        trace("validate: {}", url);
        if (!capabilities.hasValidate()) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "capability :validate is not supported by server");
        }
        RPCRequest rpcRequest = prepareValidateMessage(encodeUrl(url));
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * The notification capability makes it possible to receive notifications
     * specified in a subscription. The <code>:notification</code> capability
     * must be supported by the server.
     *
     * @see #createSubscription(String,String,String,String)
     */
    public void createSubscription() throws IOException, JNCException {
        createSubscription(null, (String) null, null, null);
    }

    /**
     * The notification capability makes it possible to receive notifications
     * specified in a subscription. The <code>:notification</code> capability
     * must be supported by the server.
     * <p>
     * Subscribe on specified stream name.
     *
     * @param stream The name of the stream or <code>null</code> if all streams
     *         of events are of interest.
     * @see #createSubscription(String,String,String,String)
     */
    public void createSubscription(String stream)
            throws IOException, JNCException {
        createSubscription(stream, (String) null, null, null);
    }

    /**
     * The notification capability makes it possible to receive notifications
     * specified in a subscription. The <code>:notification</code> capability
     * must be supported by the server.
     * <p>
     * An optional parameter, <code>stream</code>, that indicates which stream
     * of events is of interest. If not present, events in the default NETCONF
     * stream will be sent.
     * <p>
     * An optional parameter, <code>filter</code>, that indicates which subset
     * of all possible events is of interest. If not present, all events not
     * precluded by other parameters will be sent.
     * <p>
     * A parameter, <code>startTime</code>, used to trigger the replay feature
     * and indicate that the replay should start at the time specified. If
     * <code>startTime</code> is not present, this is not a replay
     * subscription. It is not valid to specify start times that are later than
     * the current time. If the <code>startTime</code> specified is earlier
     * than the log can support, the replay will begin with the earliest
     * available notification. The time is specified in dateTime format.
     * <p>
     * An optional parameter, <code>stopTime</code>, used with the optional
     * replay feature to indicate the newest notifications of interest. If stop
     * time is not present, the notifications will continue until the
     * subscription is terminated. Must be used with and be later than
     * <code>startTime</code>. Values of <code>stopTime</code> in the future
     * are valid. The time is specified in dateTime format.
     * <p>
     *
     * @param streamName The name of the stream or <code>null</code>
     * @param eventFilter a subtree filter - list of events, or
     *            <code>null</code>
     * @param startTime a dateTime string specifying the replay start, or
     *            <code>null</code> if the subscription is not a replay
     *            subscription
     * @param stopTime a dateTime string specifying the stop of replay, or
     *            <code>null</code> if the subscription is not a replay
     *            subscription or infinite subscription is desired
     * @see #receiveNotification()
     */
    public void createSubscription(String streamName, NodeSet eventFilter,
            String startTime, String stopTime) throws IOException,
            JNCException {
        trace("createSubscription: stream={} filter={} form={} to={}",
            streamName, eventFilter.toXMLString(), startTime, stopTime);
        if (!capabilities.hasNotification()) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "capability :notification is not supported by server");
        }
        RPCRequest rpcRequest = prepareCreateSubscriptionMessage(streamName,
                eventFilter, startTime, stopTime);
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * Same as {@link #createSubscription(String,NodeSet,String,String)} except
     * a filter in xpath format can be given instead of a subtree filter.
     *
     * @param streamName The name of the stream or <code>null</code>
     * @param eventFilter a filter xpath expression, or <code>null</code>
     * @param startTime a dateTime string specifying the replay start, or
     *            <code>null</code>
     * @param stopTime a dateTime string specifying the stop of replay, or
     *            <code>null</code>
     * @see #receiveNotification()
     */
    public void createSubscription(String streamName, String eventFilter,
            String startTime, String stopTime) throws IOException,
            JNCException {
        trace("createSubscription: stream={} filter={} from={} to={}",
             streamName, eventFilter, startTime, stopTime);
        if (!capabilities.hasNotification()) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "capability :notification is not supported by server");
        }
        if (!capabilities.hasXPath()) {
            throw new JNCException(JNCException.SESSION_ERROR,
                    "capability :xpath is not supported by server");
        }
        RPCRequest rpcRequest = prepareCreateSubscriptionMessage(streamName,
                eventFilter, startTime, stopTime);
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        recvRpcReplyOk(rpcRequest.getMsgId());
    }

    /**
     * Method to get the available streams from the agent. This will do:
     *
     * <pre>
     * Element subtree = Element.create(
     *         &quot;urn:ietf:params:xml:ns:netmod:notification&quot;, &quot;netconf/streams&quot;);
     * return session.get(subtree);
     * </pre>
     *
     * The available streams are returned.
     */
    public NodeSet getStreams() throws JNCException, IOException {
        final Element filter = Element.create(
                "urn:ietf:params:xml:ns:netmod:notification",
                "netconf/streams");
        return get(filter);
    }

    /**
     * Receive one notification. This is a blocking call - it blocks the caller
     * until an entire notifications messages has been received. It's possible
     * to check if there is data to be read ahead using the ready() method on
     * the SSHSession object.
     */

    public Element receiveNotification() throws IOException, JNCException {
        final String notification = in.readOne();
        trace("notification= {}", notification);
        if (notification.length() == 0) {
            throw new JNCException(JNCException.PARSER_ERROR, "empty input");
        }
        final Element t = parser.parse(notification);
        final Element test = t.getFirst("self::notification");
        if (test != null) {
            return t;
        }
        /* rpc-error */
        throw new JNCException(JNCException.NOTIFICATION_ERROR, t);
    }

    /**
     * Action capability. An action that does not return any result value,
     * replies with the standard 'ok' element. If a result value is returned,
     * it is encapsulated within a returned 'data' element.
     *
     * @param data element tree with action-data
     */
    public Element action(Element data) throws JNCException, IOException {
        trace("action: {}", data.toXMLString());
        RPCRequest rpcRequest = prepareActionMessage(data);
        out.print(rpcRequest.getMessage().toString());
        out.flush();
        return recvRpcReplyOk(null);

    }

    /* Receive from session */

    /**
     * Reads one rpc-reply from session and parse an &lt;ok/&gt;. If not ok then
     * throw RCP_REPLY_ERROR exception.
     */
    void recvRpcReplyOk() throws JNCException, IOException {
        recvRpcReplyOk(null);
    }

    void recvRpcReplyOk(int mid) throws JNCException, IOException {
        recvRpcReplyOk(Integer.toString(mid));
    }

    /**
     * Receive from session
     *
     * @return the reply element
     * @throws JNCException
     * @throws IOException
     */
    protected Element recvRpcReplyOk(String mid) throws JNCException, IOException {
        final String reply = in.readOne();
        trace("reply= {}", reply);
        if (reply.length() == 0) {
            throw new JNCException(JNCException.PARSER_ERROR, "empty input");
        }
        final Element t = parser.parse(reply);
        final Element ok;

        if (mid != null) {
            final Element rep = t.getFirst("self::rpc-reply");
            if (rep != null) {
                checkMid(rep, mid);
            }
            ok = rep != null ? rep.getFirst("self::rpc-reply/ok") : null;
        } else {
            ok = t.getFirst("self::rpc-reply/ok");
        }

        if (ok != null) {
            return ok;
        }
        final Element data = t.getFirst("self::rpc-reply/data");
        if (data != null) {
            return data;
        }

        /* rpc-error */
        throw new JNCException(JNCException.RPC_REPLY_ERROR, t);
    }

    /**
     * Reads one rpc-reply from session and parse the &lt;data&gt;. Returns the
     * NodeSet contained in the data tag.
     */
    NodeSet recvRpcReplyData(int mid) throws JNCException, IOException {
        return recvRpcReply("/data", parser, Integer.toString(mid));
    }

    NodeSet recvRpcReplyLockPartial(int mid) throws JNCException,
            IOException {
        return recvRpcReply("", parser, Integer.toString(mid));
    }

    NodeSet recvCallRpcReply(Element e, int mid) throws JNCException,
            IOException {
        final XMLParser parser = new XMLParser(); // XXX: Why new parser?
        return recvRpcReply("", parser, Integer.toString(mid));
    }

    NodeSet recvRpcReply(String path) throws JNCException, IOException {
        return recvRpcReply(path, parser, null);
    }

    NodeSet recvRpcReply(String path, XMLParser parser, String mid)
            throws JNCException, IOException {
        final String reply = in.readOne();
        trace("reply= {}", reply);

        final Element t = parser.parse(reply);
        final Element rep = t.getFirst("self::rpc-reply");
        if (rep != null) {
            checkMid(rep, mid);
        }

        final Element data = t.getFirst("self::rpc-reply" + path);

        if (data != null) {
            PrefixMap ctxtPrefix = data.prefixes;
            if (ctxtPrefix == null) {
                ctxtPrefix = t.prefixes;
            } else {
                ctxtPrefix.merge(t.prefixes);
            }
            if (ctxtPrefix == null) {
                ctxtPrefix = new PrefixMap();
            }
            /*
             * need to set parent of each data entry to null don't want
             * rpc-reply to be part of returned tree
             */
            if (data.children != null) {
                for (int i = 0; i < data.children.size(); i++) {
                    final Element child = data.children.getElement(i);
                    child.parent = null;
                    if (child.prefixes != null) {
                        // merge in prefix mapping from rpc header
                        child.prefixes.merge(ctxtPrefix);
                    } else {
                        child.prefixes = (PrefixMap) ctxtPrefix.clone();
                    }
                }
                return data.children;
            }
            // return empty node set rather than null
            return new NodeSet();
        }
        /* rpc-error */
        throw new JNCException(JNCException.RPC_REPLY_ERROR, t);
    }

    /* Extending the session with new capabilities. */

    /**
     * Set a proprietary capability. This capability will be advertised in the
     * initial hello message so this method need to invoked before the
     * {@link #hello()} method to have any effect.
     *
     * @param capability Add a capablity string for this client session
     */
    protected void setCapability(String capability) {
        if (proprietaryClientCaps == null) {
            proprietaryClientCaps = new ArrayList<String>();
        }
        for (final String cap : proprietaryClientCaps) {
            if (cap.equals(capability)) {
                return; // already member
            }
        }
        proprietaryClientCaps.add(capability);
    }

    private List<String> proprietaryClientCaps;

    /**
     * Used by ConfDSession to set the withDefaults Attribute. Will be included
     * in the RPC header, if set
     */
    Attribute withDefaultsAttr = null;

    /* Encoding */

    /**
     * Encodes the hello message. The capabilities advertised from the client
     * side are the base NETCONF capability for versions 1.0 and 1.1.
     */
    void encodeHello(Transport out) {
        out.print("<hello xmlns=\"" + Capabilities.NS_NETCONF + "\">");
        out.print("<capabilities>");
        out.println("<capability>" + Capabilities.NETCONF_BASE_CAPABILITY
                + "</capability>");
        if (use11) {
            out.println("<capability>" + Capabilities.NETCONF_BASE_CAPABILITY_1_1
                    + "</capability>");
        }
        /* List proprietary client capabilities */
        if (proprietaryClientCaps != null) {
            for (String proprietaryClientCap : proprietaryClientCaps) {
                out.print("<capability>");
                out.print(proprietaryClientCap);
                out.println("</capability>");
            }
        }
        out.println("</capabilities>");
        out.print("</hello>");
        // do no end with newline
    }

    /**
     * Encodes the RPC header and writes it to the provided output transport.
     * This method is provided to be able extend this class with proprietary
     * capabilities.
     *
     * @param out Transport output stream
     */
    protected int encodeRpcBegin(Transport out) {
        return encodeRpcBegin(out, null);
    }

    /**
     * Encodes the RPC header and writes it to the provided output transport.
     * This method is provided to be able to extend this class with proprietary
     * capabilities. The extra attribute arguement makes it possible to add an
     * extra attribute to the RPC header.
     *
     * @param out Transport output stream
     * @param attr Extra attribute to be added to rpc header
     */
    protected int encodeRpcBegin(Transport out, Attribute attr) {
        final String prefix = Element.defaultPrefixes
                .nsToPrefix(Element.NETCONF_NAMESPACE);
        nc = mkPrefixColon(prefix);
        final String xmlnsAttr = mkXmlnsAttr(prefix,
                Element.NETCONF_NAMESPACE);

        out.print("<" + nc + "rpc " + xmlnsAttr + " " + nc + "message-id=\"");
        final int mid = message_id++;
        out.print(mid);
        out.print("\"");
        if (attr != null) {
            out.print(" ");
            attr.encode(out);
        }
        out.print(">");
        return mid;
    }

    /**
     * Temporary holder for the encode functions. Hold the prefix to be
     * appended on NETCONF NAMESPACE operations. It is achieved through: String
     * prefix = Element.defaultPrefixes.nsToPrefix(Element.NETCONF_NAMESPACE);
     * nc = mk_prefix_colon(prefix);
     */
    private String nc;

    /**
     * Closes the rpc tag.
     *
     * @param out Transport output stream
     */
    protected void encodeRpcEnd(Transport out) {
        out.print("</" + nc + "rpc>");
        // do not end with newline
    }

    /**
     * Encode the &lt;getConfig&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *        xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *     &lt;get-config&gt;
     *        &lt;source&gt;&lt;running/&gt;&lt;/source&gt;
     *        &lt;filter type="subtree"&gt;
     *              &lt;top xmlns="http://example.com/schema/1.2/config"&gt;
     *                    &lt;users/&gt;
     *              &lt;/top&gt;
     *        &lt;/filter&gt;
     *     &lt;/get-config&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeGetConfig(Transport out, String source, Element subtreeFilter)
            throws JNCException {
        final int mid = encodeRpcBegin(out, withDefaultsAttr);
        out.println("<" + nc + GET_CONFIG_GT);
        out.print("<" + nc + SOURCE_GT);
        out.print(source);
        out.println("</" + nc + SOURCE_GT);
        out.println("<" + nc + FILTER + nc + "type=\"subtree\">");
        subtreeFilter.encode(out, true, capabilities);
        out.println("</" + nc + FILTER_GT);
        out.println("</" + nc + GET_CONFIG_GT);
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;rpc&gt;. Example:
     *
     * <pre>
     * &lt;rpc xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1"&gt;
     *   &lt;math xmlns="http://example.com/math"&gt;
     *     &lt;add&gt;
     *       &lt;operand&gt;2&lt;/operand&gt;
     *       &lt;operand&gt;3&lt;/operand&gt;
     *     &lt;/add&gt;
     *   &lt;/math&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeRpc(Transport out, Element data) throws JNCException {
        final int mid = encodeRpcBegin(out);
        data.encode(out);
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;getConfig&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *        xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *     &lt;get-config&gt;
     *        &lt;source&gt;&lt;running/&gt;&lt;/source&gt;
     *        &lt;filter type="xpath" select="top/users/user[name='fred']"/&gt;
     *     &lt;/get-config&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeGetConfig(Transport out, String source, String xpath) {
        final int mid = encodeRpcBegin(out, withDefaultsAttr);
        out.println("<" + nc + GET_CONFIG_GT);
        out.print("<" + nc + SOURCE_GT);
        out.print(source);
        out.println("</" + nc + SOURCE_GT);
        if (xpath != null && xpath.length() > 0) {
            out.print("<" + nc + FILTER + nc + "type=\"xpath\" " + nc
                    + "select=\"");
            out.print(xpath);
            out.println("\"/>");
        }
        out.println("</" + nc + GET_CONFIG_GT);
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;getConfig&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *        xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *     &lt;get-config&gt;
     *        &lt;source&gt;&lt;running/&gt;&lt;/source&gt;
     *     &lt;/get-config&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeGetConfig(Transport out, String source) {
        final int mid = encodeRpcBegin(out, withDefaultsAttr);
        out.println("<" + nc + GET_CONFIG_GT);
        out.print("<" + nc + SOURCE_GT);
        out.print(source);
        out.println("</" + nc + SOURCE_GT);
        out.println("</" + nc + GET_CONFIG_GT);
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Help function to print datastore in readable format.
     */
    String datastoreToString(int datastore) {
        switch (datastore) {
        case RUNNING:
            return "RUNNING";
        case CANDIDATE:
            return "CANDIDATE";
        case STARTUP:
            return "STARTUP";
        default:
            return "UNKNOWN_DATASTORE(" + datastore + ")";
        }
    }

    /**
     * Encode datastore
     */
    String encodeDatastore(int datastore) throws JNCException {
        final String prefix = Element.defaultPrefixes
                .nsToPrefix(Element.NETCONF_NAMESPACE);
        nc = mkPrefixColon(prefix);

        switch (datastore) {
        case RUNNING:
            return "<" + nc + "running/>";
        case CANDIDATE:
            return "<" + nc + "candidate/>";
        case STARTUP:
            return "<" + nc + "startup/>";
        default:
            throw new JNCException(JNCException.SESSION_ERROR,
                "unknown datastore: " + datastore);
        }
    }

    /**
     * Encode URL
     */
    String encodeUrl(String url) throws JNCException {
        if (!isUrlOK(url)) {
            throw new JNCException(JNCException.SESSION_ERROR, "the url: \""
                    + url + "\" is not a supported :url scheme");
        }
        return "<url>" + url + "</url>";
    }

    /**
     * Check if given url is supported by the <code>:url</code> capabililty for
     * the session.
     */
    private boolean isUrlOK(String url) {
        final String urlSchemes[] = capabilities.getUrlSchemes();
        if (urlSchemes == null) {
            return false;
        }
        for (String urlScheme : urlSchemes) {
            /*
             * check if given url starts with any of the scheme urls given for
             * the :url capability. for example "file:"
             */
            if (url.startsWith(urlScheme + ":")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Encode the &lt;get&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *        xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *     &lt;get&gt;
     *        &lt;filter type="subtree"&gt;
     *              &lt;top xmlns="http://example.com/schema/1.2/config"&gt;
     *                    &lt;users/&gt;
     *              &lt;/top&gt;
     *        &lt;/filter&gt;
     *     &lt;/get&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeGet(Transport out, Element subtreeFilter) throws JNCException {
        final int mid = encodeRpcBegin(out, withDefaultsAttr);
        out.println("<" + nc + GET_GT);
        out.println("<" + nc + FILTER + nc + "type=\"subtree\">");
        subtreeFilter.encode(out, true, capabilities);
        out.println("</" + nc + FILTER_GT);
        out.println("</" + nc + GET_GT);
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;get&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *        xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *     &lt;get&gt;
     *        &lt;filter type="xpath" select="top/users/user[name='fred']"/&gt;
     *     &lt;/get&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeGet(Transport out, String xpath) {
        final int mid = encodeRpcBegin(out, withDefaultsAttr);
        out.println("<" + nc + GET_GT);
        if (xpath != null && xpath.length() > 0) {
            out.print("<" + nc + FILTER + nc + "type=\"xpath\" " + nc
                    + "select=\"");
            out.print(xpath);
            out.println("\"/>");
        }
        out.println("</" + nc + GET_GT);
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;edit-Config&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *    &lt;edit-config&gt;
     *     &lt;target&gt;&lt;running/&gt;&lt;/target&gt;
     *     &lt;config&gt;
     *        &lt;top xmlns="http://example.com/schema/1.2/config"&gt;
     *            &lt;interface&gt;
     *               &lt;name&gt;Ethernet0/0&lt;/name&gt;
     *                   &lt;mtu&gt;1500&lt;/mtu&gt;
     *            &lt;/interface&gt;
     *        &lt;/top&gt;
     *     &lt;/config&gt;
     *    &lt;/edit-config&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeEditConfig(Transport out, String target, Element configTree)
            throws JNCException {
        return encodeEditConfig(out, target, new NodeSet(configTree));
    }

    int encodeEditConfig(Transport out, String target, NodeSet configTrees)
            throws JNCException {

        final int mid = encodeRpcBegin(out);
        out.println("<" + nc + EDIT_CONFIG_GT);
        out.print("<" + nc + TARGET_GT);
        out.print(target);
        out.println("</" + nc + TARGET_GT);
        encodeDefaultOperation(out);
        encodeTestOption(out);
        encodeErrorOption(out);
        out.println("<" + nc + CONFIG_GT);
        configTrees.encode(out, capabilities);
        out.println("</" + nc + CONFIG_GT);
        out.println("</" + nc + EDIT_CONFIG_GT);
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;edit-Config&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *    &lt;edit-config&gt;
     *      &lt;target&gt;&lt;running/&gt;&lt;/target&gt;
     *      &lt;url&gt;file://incoming.conf"&lt;/url&gt;
     *    &lt;/edit-config&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeEditConfig(Transport out, String target, String url)
            throws JNCException {
        final int mid = encodeRpcBegin(out);
        out.println("<" + nc + EDIT_CONFIG_GT);
        out.print("<" + nc + TARGET_GT);
        out.print(target);
        out.println("</" + nc + TARGET_GT);
        encodeDefaultOperation(out);
        encodeTestOption(out);
        encodeErrorOption(out);
        out.println(url);
        out.println("</" + nc + EDIT_CONFIG_GT);
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode default-operation for editConfig.
     */
    void encodeDefaultOperation(Transport out) throws JNCException {
        switch (defaultOperation) {
        case NOT_SET:
            return;
        case MERGE:
            out.println("<" + nc + "default-operation>merge</" + nc
                    + "default-operation>");
            return;
        case REPLACE:
            out.println("<" + nc + "default-operation>replace</" + nc
                    + "default-operation>");
            return;
        case NONE:
            out.println("<" + nc + "default-operation>none</" + nc
                    + "default-operation>");
            return;
        default:
            throw new JNCException(JNCException.SESSION_ERROR,
                    "unknown default-operation value: " + defaultOperation);
        }
    }

    /**
     * Encode test-option for editConfig
     */
    void encodeTestOption(Transport out) throws JNCException {
        switch (testOption) {
        case NOT_SET:
            return;
        case SET:
            if (!capabilities.hasValidate()) {
                throw new JNCException(JNCException.SESSION_ERROR,
                        "test-option is given but the :validate "
                                + "capability is not supported by server");
            }
            out.println("<" + nc + "test-option>set</" + nc + "test-option>");
            return;
        case TEST_THEN_SET:
            if (!capabilities.hasValidate()) {
                throw new JNCException(JNCException.SESSION_ERROR,
                        "test-option is given but the :validate "
                                + "capability is not supported by server");
            }
            out.println("<" + nc + "test-option>test-then-set</" + nc
                    + "test-option>");
            return;
        case TEST_ONLY:
            if (!capabilities.hasValidate()) {
                throw new JNCException(JNCException.SESSION_ERROR,
                        "test-option is given but the :validate "
                                + "capability is not supported by server");
            }
            out.println("<" + nc + "test-option>test-only</" + nc
                    + "test-option>");
            return;
        default:
            throw new JNCException(JNCException.SESSION_ERROR,
                    "unknown test-option value: " + testOption);
        }
    }

    /**
     * Encode error-option for editConfig
     */
    void encodeErrorOption(Transport out) throws JNCException {
        switch (errorOption) {
        case NOT_SET:
            return;
        case STOP_ON_ERROR:
            out.println("<" + nc + "error-option>stop-on-error</" + nc
                    + "error-option>");
            return;
        case CONTINUE_ON_ERROR:
            out.println("<" + nc + "error-option>continue-on-error</" + nc
                    + "error-option>");
            return;
        case ROLLBACK_ON_ERROR:
            if (!capabilities.hasRollbackOnError()) {
                throw new JNCException(JNCException.SESSION_ERROR,
                        "the :rollback-on-error capability "
                                + "is used but not supported by server");
            }
            out.println("<" + nc + "error-option>rollback-on-error</" + nc
                    + "error-option>");
            return;
        default:
            throw new JNCException(JNCException.SESSION_ERROR,
                    "unknown error-option value: " + errorOption);
        }
    }

    /**
     * Encode the &lt;copy-config&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *    &lt;copy-config&gt;
     *     &lt;target&gt;&lt;running/&gt;&lt;/target&gt;
     *     &lt;default-operation&gt;none&lt;/default-operation&gt;
     *     &lt;config&gt;
     *        &lt;top xmlns="http://example.com/schema/1.2/config"&gt;
     *            &lt;interface&gt;
     *               &lt;name&gt;Ethernet0/0&lt;/name&gt;
     *                   &lt;mtu&gt;1500&lt;/mtu&gt;
     *            &lt;/interface&gt;
     *        &lt;/top&gt;
     *     &lt;/config&gt;
     *    &lt;/copy-config&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeCopyConfig(Transport out, Element sourceTree, String target)
            throws JNCException {
        return encodeCopyConfig(out, new NodeSet(sourceTree), target);
    }

    /**
     * If we have multiple top nodes in our schema, we must pass a NodeSet to
     * the copyConfig oeration
     */

    int encodeCopyConfig(Transport out, NodeSet sourceTrees, String target)
            throws JNCException {
        final int mid = encodeRpcBegin(out);
        out.println("<" + nc + COPY_CONFIG_GT);
        out.print("<" + nc + TARGET_GT);
        out.print(target);
        out.println("</" + nc + TARGET_GT);
        out.println("<" + nc + SOURCE_GT);
        out.println("<" + nc + CONFIG_GT);
        sourceTrees.encode(out, capabilities);
        out.println("</" + nc + CONFIG_GT);
        out.println("</" + nc + SOURCE_GT);
        out.println("</" + nc + COPY_CONFIG_GT);
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;copy-config&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *    &lt;copy-config&gt;
     *     &lt;target&gt;&lt;running/&gt;&lt;/target&gt;
     *     &lt;default-operation&gt;none&lt;/default-operation&gt;
     *     &lt;config&gt;
     *        &lt;top xmlns="http://example.com/schema/1.2/config"&gt;
     *            &lt;interface&gt;
     *               &lt;name&gt;Ethernet0/0&lt;/name&gt;
     *                   &lt;mtu&gt;1500&lt;/mtu&gt;
     *            &lt;/interface&gt;
     *        &lt;/top&gt;
     *     &lt;/config&gt;
     *    &lt;/copy-config&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeCopyConfig(Transport out, String source, String target)
            throws JNCException {
        final int mid = encodeRpcBegin(out, withDefaultsAttr);
        out.println("<" + nc + COPY_CONFIG_GT);
        out.print("<" + nc + TARGET_GT);
        out.print(target);
        out.println("</" + nc + TARGET_GT);
        out.print("<" + nc + SOURCE_GT);
        out.print(source);
        out.println("</" + nc + SOURCE_GT);
        out.println("</" + nc + COPY_CONFIG_GT);
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;delete-config&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *    &lt;delete-config&gt;
     *     &lt;target&gt;
     *        &lt;startup/&gt;
     *     &lt;/target&gt;
     *    &lt;/delete-config&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeDeleteConfig(Transport out, String target) {
        final int mid = encodeRpcBegin(out);
        out.println("<" + nc + "delete-config>");
        out.print("<" + nc + TARGET_GT);
        out.print(target);
        out.println("</" + nc + TARGET_GT);
        out.println("</" + nc + "delete-config>");
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;lock&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *    &lt;lock&gt;
     *     &lt;target&gt;&lt;running/&gt;&lt;/target&gt;
     *    &lt;/lock&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeLock(Transport out, String target) {
        final int mid = encodeRpcBegin(out);
        out.println("<" + nc + "lock>");
        out.print("<" + nc + TARGET_GT);
        out.print(target);
        out.println("</" + nc + TARGET_GT);
        out.println("</" + nc + "lock>");
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;unlock&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *    &lt;unlock&gt;
     *     &lt;target&gt;&lt;running/&gt;&lt;/target&gt;
     *    &lt;/unlock&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeUnlock(Transport out, String target) {
        final int mid = encodeRpcBegin(out);
        out.println("<" + nc + "unlock>");
        out.print("<" + nc + TARGET_GT);
        out.print(target);
        out.println("</" + nc + TARGET_GT);
        out.println("</" + nc + "unlock>");
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;partial-lock&gt;. Example:
     *
     * <pre>
     * &lt;nc:rpc
     * xmlns:nc="urn:ietf:params:xml:ns:netconf:base:1.0"
     * xmlns="urn:ietf:params:xml:ns:netconf:partial-lock:1.0"
     * xmlns:rte="http://example.com/ns/route"&gt;
     * xmlns:if="http://example.com/ns/interface"&gt;
     * nc:message-id="135"&gt;
     *    &lt;partial-lock&gt;
     *      &lt;target&gt;
     *        &lt;running/&gt;
     *      &lt;/target&gt;
     *      &lt;select&gt;/routing/virtualRouter['routerName=router1']&lt;/select&gt;
     *      &lt;select&gt;/interfaces/['interfaceId=eth1']&lt;/select&gt;
     *    &lt;/partial-lock&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeLockPartial(Transport out, String[] select) {

        final String prefix = Element.defaultPrefixes
                .nsToPrefix(Capabilities.NS_PARTIAL_LOCK);
        final String pl = mkPrefixColon(prefix);
        final String xmlnsAttr = mkXmlnsAttr(prefix,
                Capabilities.NS_PARTIAL_LOCK);
        final int mid = encodeRpcBegin(out);
        out.println("<" + pl + "partial-lock " + xmlnsAttr + ">");
        for (String element : select) {
            out.print("<" + pl + "select>");
            out.print(element);
            out.println("</" + pl + "select>");
        }
        out.println("</" + pl + "partial-lock>");
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;partial-unlock&gt;. Example:
     *
     * <pre>
     * &lt;nc:rpc xmlns:nc="urn:ietf:params:xml:ns:netconf:base:1.0"
     * xmlns="urn:ietf:params:xml:ns:netconf:partial-lock:1.0"
     *  nc:message-id="136"&gt;
     *  &lt;partial-unlock&gt;
     *      &lt;lock-id&gt;127&lt;/lock-id&gt;
     * &lt;/partial-unlock&gt;
     * </pre>
     */
    int encodeUnlockPartial(Transport out, int lockId) {

        final String prefix = Element.defaultPrefixes
                .nsToPrefix(Capabilities.NS_PARTIAL_LOCK);
        final String pl = mkPrefixColon(prefix);
        final String xmlnsAttr = mkXmlnsAttr(prefix,
                Capabilities.NS_PARTIAL_LOCK);

        final int mid = encodeRpcBegin(out);
        out.println("<" + pl + "partial-unlock " + xmlnsAttr + ">");
        out.print("<" + pl + "lock-id>");
        out.print(lockId);
        out.println("</" + pl + "lock-id>");
        out.println("</" + pl + "partial-unlock>");
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;commit&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *    &lt;commit/&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeCommit(Transport out) {
        final int mid = encodeRpcBegin(out);
        out.println("<" + nc + "commit/>");
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;commit&gt;. (confirmed) Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *    &lt;commit&gt;
     *      &lt;confirmed/&gt;
     *      &lt;confirm-timeout&gt;120&lt;/confirm-timeout&gt;
     *    &lt;/commit&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeConfirmedCommit(Transport out, int timeout) {
        final int mid = encodeRpcBegin(out);
        out.println("<" + nc + "commit>");
        out.println("<" + nc + "confirmed/>");
        out.print("<" + nc + "confirm-timeout>");
        out.print(Integer.valueOf(timeout).toString());
        out.println("</" + nc + "confirm-timeout>");
        out.println("</" + nc + "commit>");
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;discard-changes&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *    &lt;discard-changes/&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeDiscardChanges(Transport out) {
        final int mid = encodeRpcBegin(out);
        out.println("<" + nc + "discard-changes/>");
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;close-session&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *    &lt;close-session/&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeCloseSession(Transport out) {
        final int mid = encodeRpcBegin(out);
        out.println("<" + nc + "close-session/>");
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;kill-session&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *    &lt;kill-session&gt;
     *         &lt;session-id&gt;4&lt;/session-id&gt;
     *    &lt;/kill-session&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeKillSession(Transport out, long sessionId) {
        final int mid = encodeRpcBegin(out);
        out.println("<" + nc + "kill-session>");
        out.print("<" + nc + "session-id>");
        out.print(sessionId);
        out.println("</" + nc + "session-id>");
        out.println("</" + nc + "kill-session>");
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;validate&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *    &lt;validate&gt;
     *     &lt;source&gt;
     *        &lt;config&gt;
     *            &lt;top xmlns="http://example.com/schema/1.2/config"&gt;
     *               &lt;interface&gt;
     *                  &lt;name&gt;Ethernet0/0&lt;/name&gt;
     *                      &lt;mtu&gt;1500&lt;/mtu&gt;
     *               &lt;/interface&gt;
     *           &lt;/top&gt;
     *        &lt;/config&gt;
     *     &lt;/source&gt;
     *   &lt;/validate&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeValidate(Transport out, Element configTree)
            throws JNCException {
        final int mid = encodeRpcBegin(out);
        out.println("<" + nc + VALIDATE_GT);
        out.println("<" + nc + SOURCE_GT);
        out.println("<" + nc + CONFIG_GT);
        configTree.encode(out, true, capabilities);
        out.println("</" + nc + CONFIG_GT);
        out.println("</" + nc + SOURCE_GT);
        out.println("</" + nc + VALIDATE_GT);
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;validate&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *    &lt;validate&gt;
     *     &lt;source&gt;&lt;candidate/&gt;&lt;/source&gt;
     *   &lt;/validate&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeValidate(Transport out, String source) {
        final int mid = encodeRpcBegin(out);
        out.println("<" + nc + VALIDATE_GT);
        out.print("<" + nc + SOURCE_GT);
        out.print(source);
        out.println("</" + nc + SOURCE_GT);
        out.println("</" + nc + VALIDATE_GT);
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;create-subscription&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *    &lt;create-subscription
     *         xmlns='urn:ietf:params:xml:ns:netconf:notification:1.0'&gt;
     *    &lt;/create-subscription&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeCreateSubscription(Transport out, String stream,
            String filter, String startTime, String stopTime) {
        final String prefix = Element.defaultPrefixes
                .nsToPrefix(Capabilities.NS_NOTIFICATION);
        final String ncn = mkPrefixColon(prefix);
        final String xmlnsAttr = mkXmlnsAttr(prefix,
                Capabilities.NS_NOTIFICATION);

        final int mid = encodeRpcBegin(out);
        out.println("<" + ncn + "create-subscription " + xmlnsAttr + ">");
        if (stream != null) {
            out.print("<" + ncn + STREAM_GT);
            out.print(stream);
            out.println("</" + ncn + STREAM_GT);
        }
        if (filter != null) {
            out.print("<" + ncn + FILTER + ncn + "type='xpath'>");
            out.print(filter);
            out.println("</" + ncn + FILTER_GT);
        }
        if (startTime != null) {
            out.print("<" + ncn + START_TIME_GT);
            out.print(startTime);
            out.println("</" + ncn + START_TIME_GT);
        }
        if (stopTime != null) {
            out.print("<" + ncn + STOP_TIME_GT);
            out.print(stopTime);
            out.println("</" + ncn + STOP_TIME_GT);
        }
        out.println("</" + ncn + "create-subscription>");
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encode the &lt;create-subscription&gt;. Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *    &lt;create-subscription
     *         xmlns='urn:ietf:params:xml:ns:netconf:notification:1.0'&gt;
     *    &lt;/create-subscription&gt;
     * &lt;/rpc&gt;
     * </pre>
     */
    int encodeCreateSubscription(Transport out, String stream,
            NodeSet eventFilter, String startTime, String stopTime)
            throws JNCException {
        final String prefix = Element.defaultPrefixes
                .nsToPrefix(Capabilities.NS_NOTIFICATION);
        final String ncn = mkPrefixColon(prefix);
        final String xmlnsAttr = mkXmlnsAttr(prefix,
                Capabilities.NS_NOTIFICATION);

        final int mid = encodeRpcBegin(out);
        out.println("<" + ncn + "create-subscription " + xmlnsAttr + ">");
        if (stream != null) {
            out.print("<" + ncn + STREAM_GT);
            out.print(stream);
            out.println("</" + ncn + STREAM_GT);
        }
        if (eventFilter != null) {
            out.print("<" + ncn + FILTER + ncn + "type='subtree'>");
            eventFilter.encode(out, capabilities);
            out.println("</" + ncn + FILTER_GT);
        }
        if (startTime != null) {
            out.print("<" + ncn + START_TIME_GT);
            out.print(startTime);
            out.println("</" + ncn + START_TIME_GT);
        }
        if (stopTime != null) {
            out.print("<" + ncn + STOP_TIME_GT);
            out.print(stopTime);
            out.println("</" + ncn + STOP_TIME_GT);
        }
        out.println("</" + ncn + "create-subscription>");
        encodeRpcEnd(out);
        return mid;
    }

    /**
     * Encodes an Element tree (data) and sends it to out.
     * <p>
     * Example:
     *
     * <pre>
     * &lt;rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"&gt;
     *  &lt;action xmlns="http://tail-f.com/ns/netconf/actions/1.0"&gt;
     *   &lt;data&gt;
     *    &lt;interfaces xmlns="http://example.com/interfaces/1.0"&gt;
     *     &lt;interface&gt;
     *       &lt;name&gt;eth0&lt;/name&gt;
     *       &lt;reset/&gt;
     *     &lt;/interface&gt;
     *    &lt;/interfaces&gt;
     *   &lt;/data&gt;
     *  &lt;/action&gt;
     * &lt;/rpc&gt;
     * </pre>
     *
     * @param out The transport interface to send the action to
     * @param data Element tree representing the action
     * @throws JNCException if unable to encode data
     */
    void encodeAction(Transport out, Element data) throws JNCException {
        final String prefix = Element.defaultPrefixes.nsToPrefix(Capabilities.NS_ACTIONS);
        final String act = mkPrefixColon(prefix);
        final String xmlnsAttr = mkXmlnsAttr(prefix, Capabilities.NS_ACTIONS);
        encodeRpcBegin(out);
        out.println("<" + act + "action " + xmlnsAttr + ">");
        out.print("<" + act + "data>");
        data.encode(out);
        out.println("</" + act + "data>");
        out.println("</" + act + "action>");
        encodeRpcEnd(out);
    }

    /* help functions */

    /**
     * Help function to make prefix. Returns either: "PREFIX:" or "".
     *
     * @param prefix
     * @return "unknown:" if prefix is null, an empty string if prefix is
     *         empty, otherwise prefix appended with a colon.
     */
    String mkPrefixColon(String prefix) {
        if (prefix == null) {
            return "unknown:";
        }
        return prefix.isEmpty() ? "" : prefix + ":";
    }

    /**
     * Help function to make xmlns attr from prefix and namespace. Returns
     * either: "xmlns=NAMESPACE" or "xmlns:PREFIX=NAMESPACE".
     */
    String mkXmlnsAttr(String prefix, String ns) {
        if (prefix == null) {
            return "xmlns:unknown=\"" + ns + "\"";
        }
        if ("".equals(prefix)) {
            return "xmlns=\"" + ns + "\"";
        } else {
            return "xmlns:" + prefix + "=\"" + ns + "\"";
        }
    }

    /**
     * Printout trace if 'debug'-flag is enabled.
     */
    private static void trace(String format, Object ... args) {
        if (Element.debugLevel >= Element.DEBUG_LEVEL_SESSION) {
            System.err.println(String.format("*NetconfSession: " + format, args));
        }
    }

    /**
     * Checks that message id attribute of t is mid. If mid is null, no check
     * is performed.
     *
     * @param t rpc-reply Element tree
     * @param mid message id to check for
     * @throws JNCException if there is a message id mismatch
     */
    void checkMid(Element t, String mid) throws JNCException {
        if (mid == null) {
            return;
        }
        final String returnedId = t.getAttrValue("message-id");
        if (returnedId == null || !returnedId.equals(mid)) {
            throw new JNCException(JNCException.MESSAGE_ID_MISMATCH,
                    "After sending rpc with message-id=" + mid
                            + ", received rpc-reply with message-id="
                            + returnedId);
        }
    }


    class RPCRequest
    {
        final int msgId;
        @SuppressWarnings("PMD.AvoidStringBufferField")
        StringBuilder message;

        RPCRequest()
        {
            msgId = message_id++ ;
            message = new StringBuilder();
        }

        public int getMsgId()
        {
            return msgId;
        }

        public StringBuilder getMessage()
        {
            return message;
        }

        void addRpcBegin(Attribute attr)
        {
            final String prefix = Element.defaultPrefixes
                    .nsToPrefix(Element.NETCONF_NAMESPACE);
            nc = mkPrefixColon(prefix);
            final String xmlnsAttr = mkXmlnsAttr(prefix,
                    Element.NETCONF_NAMESPACE);

            StringBuilder rpcBegin  = new StringBuilder("<" + nc + "rpc " + xmlnsAttr + " " +
                    nc + "message-id=\"" + msgId + "\"");
            if (attr != null) {
                rpcBegin.append(" ").append(attr.toXMLString(null));
            }
            rpcBegin.append(">");
            message.append(rpcBegin.toString());
        }

        void addRpcEnd()
        {
            message.append("\n</" + nc + "rpc>");
        }

        /**
         * Encode default-operation for editConfig.
         */
        void encodeDefaultOperation() throws JNCException {
            switch (defaultOperation) {
                case NOT_SET:
                    return;
                case MERGE:
                    message.append("\n<" + nc + "default-operation>merge</" + nc
                            + "default-operation>");
                    return;
                case REPLACE:
                    message.append("\n<" + nc + "default-operation>replace</" + nc
                            + "default-operation>");
                    return;
                case NONE:
                    message.append("\n<" + nc + "default-operation>none</" + nc
                            + "default-operation>");
                    return;
                default:
                    throw new JNCException(JNCException.SESSION_ERROR,
                            "unknown default-operation value: " + defaultOperation);
            }
        }

        /**
         * Encode test-option for editConfig
         */
        void encodeTestOption() throws JNCException {
            switch (testOption) {
                case NOT_SET:
                    return;
                case SET:
                    if (!capabilities.hasValidate()) {
                        throw new JNCException(JNCException.SESSION_ERROR,
                                "test-option is given but the :validate "
                                        + "capability is not supported by server");
                    }
                    message.append("\n<" + nc + "test-option>set</" + nc + "test-option>");
                    return;
                case TEST_THEN_SET:
                    if (!capabilities.hasValidate()) {
                        throw new JNCException(JNCException.SESSION_ERROR,
                                "test-option is given but the :validate "
                                        + "capability is not supported by server");
                    }
                    message.append("\n<" + nc + "test-option>test-then-set</" + nc
                            + "test-option>");
                    return;
                case TEST_ONLY:
                    if (!capabilities.hasValidate()) {
                        throw new JNCException(JNCException.SESSION_ERROR,
                                "test-option is given but the :validate "
                                        + "capability is not supported by server");
                    }
                    message.append("\n<" + nc + "test-option>test-only</" + nc
                            + "test-option>");
                    return;
                default:
                    throw new JNCException(JNCException.SESSION_ERROR,
                            "unknown test-option value: " + testOption);
            }
        }

        /**
         * Encode error-option for editConfig
         */
        void encodeErrorOption() throws JNCException {
            switch (errorOption) {
                case NOT_SET:
                    return;
                case STOP_ON_ERROR:
                    message.append("\n<" + nc + "error-option>stop-on-error</" + nc
                            + "error-option>");
                    return;
                case CONTINUE_ON_ERROR:
                    message.append("\n<" + nc + "error-option>continue-on-error</" + nc
                            + "error-option>");
                    return;
                case ROLLBACK_ON_ERROR:
                    if (!capabilities.hasRollbackOnError()) {
                        throw new JNCException(JNCException.SESSION_ERROR,
                                "the :rollback-on-error capability "
                                        + "is used but not supported by server");
                    }
                    message.append("\n<" + nc + "error-option>rollback-on-error</" + nc
                            + "error-option>");
                    return;
                default:
                    throw new JNCException(JNCException.SESSION_ERROR,
                            "unknown error-option value: " + errorOption);
            }
        }

    }

    RPCRequest prepareGetMessage(Element subtreeFilter)
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(withDefaultsAttr);
        rpcMsg.getMessage().append("\n<" + nc + GET_GT);
        rpcMsg.getMessage().append("\n<" + nc + FILTER + nc + "type=\"subtree\">");
        if(null != subtreeFilter)
        {
            rpcMsg.getMessage().append("\n" + subtreeFilter.encodedXMLString(false));
        }
        rpcMsg.getMessage().append("\n</" + nc + FILTER_GT);
        rpcMsg.getMessage().append("\n</" + nc + GET_GT);
        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    RPCRequest prepareGetMessage(String xpath)
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(withDefaultsAttr);

        rpcMsg.getMessage().append("\n<" + nc + GET_GT);
        if (xpath != null && xpath.length() > 0) {
            rpcMsg.getMessage().append("\n<" + nc + FILTER + nc + "type=\"xpath\" " + nc
                    + "select=\"");
            rpcMsg.getMessage().append(xpath);
            rpcMsg.getMessage().append("\"/>");
        }
        rpcMsg.getMessage().append("\n</" + nc + GET_GT);

        rpcMsg.addRpcEnd();
        return rpcMsg;
    }


    private RPCRequest prepareGetConfigMessage(String source, Element subtreeFilter)
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(withDefaultsAttr);

        rpcMsg.getMessage().append("\n<" + nc + GET_CONFIG_GT);
        rpcMsg.getMessage().append("\n<" + nc + SOURCE_GT);
        rpcMsg.getMessage().append("\n" + source);
        rpcMsg.getMessage().append("\n</" + nc + SOURCE_GT);
        rpcMsg.getMessage().append("\n<" + nc + FILTER + nc + "type=\"subtree\">");
        rpcMsg.getMessage().append("\n" + subtreeFilter.encodedXMLString(false));
        rpcMsg.getMessage().append("\n</" + nc + FILTER_GT);
        rpcMsg.getMessage().append("\n</" + nc + GET_CONFIG_GT);

        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareGetConfigMessage(String source, String xpath)
    {

        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(withDefaultsAttr);
        rpcMsg.getMessage().append("\n<" + nc + GET_CONFIG_GT);
        rpcMsg.getMessage().append("\n<" + nc + SOURCE_GT);
        rpcMsg.getMessage().append("\n" + source);
        rpcMsg.getMessage().append("\n</" + nc + SOURCE_GT);
        if (xpath != null && xpath.length() > 0) {
            rpcMsg.getMessage().append("\n<" + nc + FILTER + nc + "type=\"xpath\" " + nc
                    + "select=\"");
            rpcMsg.getMessage().append(xpath);
            rpcMsg.getMessage().append("\"/>");
        }
        rpcMsg.getMessage().append("\n</" + nc + GET_CONFIG_GT);
        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareGetConfigMessage(String source)
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(withDefaultsAttr);
        rpcMsg.getMessage().append("\n<" + nc + GET_CONFIG_GT);
        rpcMsg.getMessage().append("\n<" + nc + SOURCE_GT);
        rpcMsg.getMessage().append("\n" + source);
        rpcMsg.getMessage().append("\n</" + nc + SOURCE_GT);
        rpcMsg.getMessage().append("\n</" + nc + GET_CONFIG_GT);
        rpcMsg.addRpcEnd();
        return rpcMsg;
    }


    private RPCRequest prepareEditConfigMessage(String target, NodeSet configTrees) throws JNCException
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);

        rpcMsg.getMessage().append("\n<" + nc + EDIT_CONFIG_GT);
        rpcMsg.getMessage().append("\n<" + nc + TARGET_GT);
        rpcMsg.getMessage().append("\n" + target);
        rpcMsg.getMessage().append("\n</" + nc + TARGET_GT);
        rpcMsg.encodeDefaultOperation();
        rpcMsg.encodeTestOption();
        rpcMsg.encodeErrorOption();
        rpcMsg.getMessage().append("\n<" + nc + CONFIG_GT);
        rpcMsg.getMessage().append("\n" + configTrees.encodedXMLString());
        rpcMsg.getMessage().append("\n</" + nc + CONFIG_GT);
        rpcMsg.getMessage().append("\n</" + nc + EDIT_CONFIG_GT);

        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareEditConfigMessage(String target, String url) throws JNCException
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);

        rpcMsg.getMessage().append("\n<" + nc + EDIT_CONFIG_GT);
        rpcMsg.getMessage().append("\n<" + nc + TARGET_GT);
        rpcMsg.getMessage().append("\n" + target);
        rpcMsg.getMessage().append("\n</" + nc + TARGET_GT);
        rpcMsg.encodeDefaultOperation();
        rpcMsg.encodeTestOption();
        rpcMsg.encodeErrorOption();
        rpcMsg.getMessage().append("\n" + url);
        rpcMsg.getMessage().append("\n</" + nc + EDIT_CONFIG_GT);

        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareCopyConfigMessage(NodeSet sourceTrees, String target) throws JNCException
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);

        rpcMsg.getMessage().append("\n<" + nc + COPY_CONFIG_GT);
        rpcMsg.getMessage().append("\n<" + nc + TARGET_GT);
        rpcMsg.getMessage().append("\n" + target);
        rpcMsg.getMessage().append("\n</" + nc + TARGET_GT);
        rpcMsg.getMessage().append("\n<" + nc + SOURCE_GT);
        rpcMsg.getMessage().append("\n<" + nc + CONFIG_GT);
        rpcMsg.getMessage().append("\n" + sourceTrees.encodedXMLString());
        rpcMsg.getMessage().append("\n</" + nc + CONFIG_GT);
        rpcMsg.getMessage().append("\n</" + nc + SOURCE_GT);
        rpcMsg.getMessage().append("\n</" + nc + COPY_CONFIG_GT);

        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareCopyConfigMessage(String source, String target) throws JNCException
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(withDefaultsAttr);

        rpcMsg.getMessage().append("\n<" + nc + COPY_CONFIG_GT);
        rpcMsg.getMessage().append("\n<" + nc + TARGET_GT);
        rpcMsg.getMessage().append("\n" + target);
        rpcMsg.getMessage().append("\n</" + nc + TARGET_GT);
        rpcMsg.getMessage().append("\n<" + nc + SOURCE_GT);
        rpcMsg.getMessage().append("\n" + source);
        rpcMsg.getMessage().append("\n</" + nc + SOURCE_GT);
        rpcMsg.getMessage().append("\n</" + nc + COPY_CONFIG_GT);

        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareDeleteConfigMessage(String target)
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);

        rpcMsg.getMessage().append("\n<" + nc + "delete-config>");
        rpcMsg.getMessage().append("\n<" + nc + TARGET_GT);
        rpcMsg.getMessage().append("\n" + target);
        rpcMsg.getMessage().append("\n</" + nc + TARGET_GT);
        rpcMsg.getMessage().append("\n</" + nc + "delete-config>");

        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareRPCMessage(Element data)
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);
        rpcMsg.getMessage().append("\n" + data.encodedXMLString(false));
        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareLockMessage(String target)
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);

        rpcMsg.getMessage().append("\n<" + nc + "lock>");
        rpcMsg.getMessage().append("\n<" + nc + TARGET_GT);
        rpcMsg.getMessage().append("\n" + target);
        rpcMsg.getMessage().append("\n</" + nc + TARGET_GT);
        rpcMsg.getMessage().append("\n</" + nc + "lock>");

        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareUnlockMessage(String target)
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);

        rpcMsg.getMessage().append("\n<" + nc + "unlock>");
        rpcMsg.getMessage().append("\n<" + nc + TARGET_GT);
        rpcMsg.getMessage().append("\n" + target);
        rpcMsg.getMessage().append("\n</" + nc + TARGET_GT);
        rpcMsg.getMessage().append("\n</" + nc + "unlock>");

        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareLockPartialMessage(String[] select)
    {
        final String prefix = Element.defaultPrefixes
                .nsToPrefix(Capabilities.NS_PARTIAL_LOCK);
        final String pl = mkPrefixColon(prefix);
        final String xmlnsAttr = mkXmlnsAttr(prefix,
                Capabilities.NS_PARTIAL_LOCK);

        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);

        rpcMsg.getMessage().append("\n<" + pl + "partial-lock " + xmlnsAttr + ">");
        for (String item: select) {
            rpcMsg.getMessage().append("\n<" + pl + "select>");
            rpcMsg.getMessage().append(item);
            rpcMsg.getMessage().append("\n</" + pl + "select>");
        }
        rpcMsg.getMessage().append("\n</" + pl + "partial-lock>");
        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareUnlockPartialMessage(int lockId)
    {
        final String prefix = Element.defaultPrefixes
                .nsToPrefix(Capabilities.NS_PARTIAL_LOCK);
        final String pl = mkPrefixColon(prefix);
        final String xmlnsAttr = mkXmlnsAttr(prefix,
                Capabilities.NS_PARTIAL_LOCK);

        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);

        rpcMsg.getMessage().append("\n<" + pl + "partial-unlock " + xmlnsAttr + ">");
        rpcMsg.getMessage().append("\n<" + pl + "lock-id>");
        rpcMsg.getMessage().append("\n" + lockId);
        rpcMsg.getMessage().append("\n</" + pl + "lock-id>");
        rpcMsg.getMessage().append("\n</" + pl + "partial-unlock>");

        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareCommitMessage()
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);
        rpcMsg.getMessage().append("\n<" + nc + "commit/>");
        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareConfirmedCommitMessage(int timeout)
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);

        rpcMsg.getMessage().append("\n<" + nc + "commit>");
        rpcMsg.getMessage().append("\n<" + nc + "confirmed/>");
        rpcMsg.getMessage().append("\n<" + nc + "confirm-timeout>");
        rpcMsg.getMessage().append("\n" + Integer.valueOf(timeout).toString());
        rpcMsg.getMessage().append("\n</" + nc + "confirm-timeout>");
        rpcMsg.getMessage().append("\n</" + nc + "commit>");

        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareDiscardChangesMessage()
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);
        rpcMsg.getMessage().append("\n<" + nc + "discard-changes/>");
        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareCloseSessionMessage()
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);
        rpcMsg.getMessage().append("\n<" + nc + "close-session/>");
        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareKillSessionMessage(long sessionId)
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);

        rpcMsg.getMessage().append("\n<" + nc + "kill-session>");
        rpcMsg.getMessage().append("\n<" + nc + "session-id>");
        rpcMsg.getMessage().append("\n" + sessionId);
        rpcMsg.getMessage().append("\n</" + nc + "session-id>");
        rpcMsg.getMessage().append("\n</" + nc + "kill-session>");

        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareValidateMessage(Element configTree)
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);

        rpcMsg.getMessage().append("\n<" + nc + VALIDATE_GT);
        rpcMsg.getMessage().append("\n<" + nc + SOURCE_GT);
        rpcMsg.getMessage().append("\n<" + nc + CONFIG_GT);
        rpcMsg.getMessage().append("\n" + configTree.encodedXMLString(false));
        rpcMsg.getMessage().append("\n</" + nc + CONFIG_GT);
        rpcMsg.getMessage().append("\n</" + nc + SOURCE_GT);
        rpcMsg.getMessage().append("\n</" + nc + VALIDATE_GT);

        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareValidateMessage(String source)
    {
        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);

        rpcMsg.getMessage().append("\n<" + nc + VALIDATE_GT);
        rpcMsg.getMessage().append("\n<" + nc + SOURCE_GT);
        rpcMsg.getMessage().append("\n" + source);
        rpcMsg.getMessage().append("\n</" + nc + SOURCE_GT);
        rpcMsg.getMessage().append("\n</" + nc + VALIDATE_GT);

        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareCreateSubscriptionMessage(String stream,
         String filter, String startTime, String stopTime)
    {
        final String prefix = Element.defaultPrefixes
                .nsToPrefix(Capabilities.NS_NOTIFICATION);
        final String ncn = mkPrefixColon(prefix);
        final String xmlnsAttr = mkXmlnsAttr(prefix,
                Capabilities.NS_NOTIFICATION);

        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);

        rpcMsg.getMessage().append("\n<" + ncn + "create-subscription " + xmlnsAttr + ">");
        if (stream != null) {
            rpcMsg.getMessage().append("\n<" + ncn + STREAM_GT);
            rpcMsg.getMessage().append("\n" + stream);
            rpcMsg.getMessage().append("\n</" + ncn + STREAM_GT);
        }
        if (filter != null) {
            rpcMsg.getMessage().append("\n<" + ncn + FILTER + ncn + "type='xpath'>");
            rpcMsg.getMessage().append("\n" + filter);
            rpcMsg.getMessage().append("\n</" + ncn + FILTER_GT);
        }
        if (startTime != null) {
            rpcMsg.getMessage().append("\n<" + ncn + START_TIME_GT);
            rpcMsg.getMessage().append("\n" + startTime);
            rpcMsg.getMessage().append("\n</" + ncn + START_TIME_GT);
        }
        if (stopTime != null) {
            rpcMsg.getMessage().append("\n<" + ncn + STOP_TIME_GT);
            rpcMsg.getMessage().append("\n" + stopTime);
            rpcMsg.getMessage().append("\n</" + ncn + STOP_TIME_GT);
        }
        rpcMsg.getMessage().append("\n</" + ncn + "create-subscription>");

        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareCreateSubscriptionMessage(String stream,
            NodeSet eventFilter, String startTime, String stopTime) throws JNCException
    {
        final String prefix = Element.defaultPrefixes
                .nsToPrefix(Capabilities.NS_NOTIFICATION);
        final String ncn = mkPrefixColon(prefix);
        final String xmlnsAttr = mkXmlnsAttr(prefix,
                Capabilities.NS_NOTIFICATION);

        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);

        rpcMsg.getMessage().append("\n<" + ncn + "create-subscription " + xmlnsAttr + ">");
        if (stream != null) {
            rpcMsg.getMessage().append("\n<" + ncn + STREAM_GT);
            rpcMsg.getMessage().append("\n" + stream);
            rpcMsg.getMessage().append("\n</" + ncn + STREAM_GT);
        }
        if (eventFilter != null) {
            rpcMsg.getMessage().append("\n<" + ncn + FILTER + ncn + "type='subtree'>");
            rpcMsg.getMessage().append("\n" + eventFilter.encodedXMLString());
            rpcMsg.getMessage().append("\n</" + ncn + FILTER_GT);
        }
        if (startTime != null) {
            rpcMsg.getMessage().append("\n<" + ncn + START_TIME_GT);
            rpcMsg.getMessage().append("\n" + startTime);
            rpcMsg.getMessage().append("\n</" + ncn + START_TIME_GT);
        }
        if (stopTime != null) {
            rpcMsg.getMessage().append("\n<" + ncn + STOP_TIME_GT);
            rpcMsg.getMessage().append("\n" + stopTime);
            rpcMsg.getMessage().append("\n</" + ncn + STOP_TIME_GT);
        }
        rpcMsg.getMessage().append("\n</" + ncn + "create-subscription>");

        rpcMsg.addRpcEnd();
        return rpcMsg;
    }

    private RPCRequest prepareActionMessage(Element data)
    {
        final String prefix = Element.defaultPrefixes.nsToPrefix(Capabilities.NS_ACTIONS);
        final String act = mkPrefixColon(prefix);
        final String xmlnsAttr = mkXmlnsAttr(prefix, Capabilities.NS_ACTIONS);

        RPCRequest rpcMsg = new RPCRequest();
        rpcMsg.addRpcBegin(null);

        rpcMsg.getMessage().append("\n<" + act + "action " + xmlnsAttr + ">");
        rpcMsg.getMessage().append("\n<" + act + "data>");
        rpcMsg.getMessage().append("\n" + data.encodedXMLString(false));
        rpcMsg.getMessage().append("\n</" + act + "data>");
        rpcMsg.getMessage().append("\n</" + act + "action>");

        rpcMsg.addRpcEnd();
        return rpcMsg;
    }
}
