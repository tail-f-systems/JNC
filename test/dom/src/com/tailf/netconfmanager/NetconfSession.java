/*    -*- Java -*-
 *
 *  Copyright 2007 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.netconfmanager;

import java.util.ArrayList;
import java.io.IOException;

/**
 * A NETCONF session class. It makes it possible to connect to a NETCONF agent
 * using a preferred transport mechanism. After a successful connect all
 * operations defined by the NETCONF configuration protocol [<a target="_top"
 * href="ftp://ftp.rfc-editor.org/in-notes/rfc4741.txt">RFC 4741</a>] can be
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
 *     dev1.lock(NetconfSession.CANDIDATE);
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
     * Session identifier. Received from the initial <code>hello</code> message.
     */
    public int sessionId;

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
     * @param uri
     *            Name of capability to check
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
     * Creates a new session object using the given transport object. This will
     * initialize the transport and send out an initial hello message to the
     * server.
     * 
     * @see SSHSession
     * 
     * @param transport
     *            Transport object
     */

    public NetconfSession(Transport transport) throws NetconfException, IOException {
        this.out = transport;
        this.in = transport; // same
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
     * @param transport
     *            Transport object
     * @param parser
     *            XML parser object
     * 
     *            If we are using the confm package to create and manipulate
     *            objects we must instantiate the parser as an
     *            com.tailf.confm.XMLParser() If we fail to do that, we get the
     *            XMLParser class from the inm package which always only returns
     *            Element objects as opposed to the XMLParser from the confm
     *            package which can return objects matching the confm generated
     *            classes. If we use the confm Device class to create our
     *            netconf sessions, this is all done automatically, whereas if
     *            we create our netconf sessions ourselves, _and_ also want to
     *            retrieve real confm objects we must:
     * 
     *            <pre>
     * SSHConnection ssh = new SSHConnection(&quot;127.0.0.1&quot;, 2022);
     * ssh.authenticateWithPassword(&quot;admin&quot;, &quot;admin&quot;);
     * Transport tr = new SSHSession(ssh);
     * NetconfSession sess = new NetconfSession(tr, new com.tailf.confm.XMLParser());
     * </pre>
     **/

    public NetconfSession(Transport transport, XMLParser parser)
            throws NetconfException, IOException {
        this.out = transport;
        this.in = transport; // same
        this.parser = parser;
        hello();
    }

    /**
     * Creates a new session object. The session need to be given a transport
     * object with {@link #setTransport(Transport)} and an initial hello needs
     * to be sent to advertise capabilities.
     * 
     */
    public NetconfSession() throws NetconfException {
        parser = new XMLParser();
    }

    /**
     * Sets the transport used by this session.
     * 
     * @param transport
     *            Transport object, for example {@link SSHSession}
     */
    public void setTransport(Transport transport) {
        this.out = transport;
        this.in = transport; // same
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
     * establishment. When the NETCONF session is opened, each peer (both client
     * and server) must send a <code>hello</code> element containing a list of
     * that peer's capabilities. Each peer must send at least the base NETCONF
     * capability, "urn:ietf:params:netconf:base:1.0".
     * <p>
     * This method will send an initial <code>hello</code> to the output stream
     * and await the <code>hello</code> from the server.
     * <p>
     * Used from the constructor {@link #NetconfSession(Transport)}.
     */
    protected void hello() throws NetconfException, IOException {
        trace("hello: ");
        encode_hello(out);
        out.flush();
        StringBuffer reply = in.readOne();
        // System.out.println("reply= "+ reply);
        Element t = parser.parse(reply.toString());
        Element capatree = t.getFirst("self::hello/capabilities");
        if (capatree == null)
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "hello contains no capabilities");
        trace("capabilities: \n" + capatree.toXMLString());

        capabilities = new Capabilities(capatree);
        if (!capabilities.baseCapability)
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "server does not support NETCONF base capability: "
                            + Capabilities.NETCONF_BASE_CAPABILITY);
        // lookup session id
        Element sess = t.getFirst("self::hello/session-id");
        if (sess == null)
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "hello contains no session identifier");
        sessionId = Integer.parseInt((String) sess.value);
        trace("sessionId = " + sessionId);
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
     * @param request
     *            XML encoded NETCONF request
     */
    public Element rpc(String request) throws IOException, NetconfException {
        out.print(request);
        out.flush();
        StringBuffer reply = in.readOne();
        return parser.parse(reply.toString());
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
     * @param request
     *            XML element tree
     */
    public Element rpc(Element request) throws IOException, NetconfException {
        // print, but no newline at the end
        request.encode(out, false, capabilities);
        out.flush();
        StringBuffer reply = in.readOne();
        return parser.parse(reply.toString());
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
     * @param request
     *            XML encoded NETCONF request
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
     * @param request
     *            Element tree
     */
    public int sendRequest(Element request) throws IOException, NetconfException {
        // print, but no newline at the end
        request.encode(out, false, capabilities);
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
    public Element readReply() throws IOException, NetconfException {
        StringBuffer reply = in.readOne();
        return parser.parse(reply.toString());
    }

    /**
     * Gets the device configuration data specified by subtree filtering.
     * 
     * @param subtreeFilter
     *            A subtree filter
     */
    public NodeSet getConfig(Element subtreeFilter) throws NetconfException,
            IOException {
        return getConfig(RUNNING, subtreeFilter);
    }

    /**
     * Gets the device configuration data.
     * 
     */
    public NodeSet getConfig() throws NetconfException, IOException {
        return getConfig(RUNNING);
    }

    /**
     * Gets the device configuration data.
     * 
     */
    public NodeSet getConfig(int datastore) throws NetconfException, IOException {
        trace("getConfig: " + datastoreToString(datastore));
        int mid = encode_getConfig(out, encode_datastore(datastore));
        out.flush();
        return recv_rpc_reply_data(mid);
    }

    /**
     * Calls rpc method.
     * 
     */
    public NodeSet callRpc(Element data) throws NetconfException, IOException {
        trace("call: " + data.toXMLString());
        int mid = encode_rpc(out, data);
        out.flush();
        return recv_call_rpc_reply(data, mid);
    }

    /**
     * Gets the device configuration data specified by an xpath expression. The
     * <code>:xpath</code> capability must be supported by the server.
     * 
     * @param xpath
     *            XPath expression
     */
    public NodeSet getConfig(String xpath) throws NetconfException, IOException {
        return getConfig(RUNNING, xpath);
    }

    /**
     * Gets the device configuration data specified by subtree filtering.
     * 
     * @param datastore
     *            The datastore. One of {@link #RUNNING}, {@link #CANDIDATE},
     *            {@link #STARTUP}
     * @param subtreeFilter
     *            A subtree filter
     */
    public NodeSet getConfig(int datastore, Element subtreeFilter)
            throws NetconfException, IOException {
        trace("getConfig: " + datastoreToString(datastore) + "\n"
                + subtreeFilter.toXMLString());
        int mid = encode_getConfig(out, encode_datastore(datastore),
                subtreeFilter);
        out.flush();
        return recv_rpc_reply_data(mid);
    }

    /**
     * Gets the device configuration data specified by an xpath filter.
     * 
     * @param datastore
     *            The datastore. One of {@link #RUNNING}, {@link #CANDIDATE},
     *            {@link #STARTUP}
     * @param xpath
     *            XPath expression
     */
    public NodeSet getConfig(int datastore, String xpath) throws NetconfException,
            IOException {
        trace("getConfig: " + datastoreToString(datastore) + " \"" + xpath
                + "\"");
        if (!capabilities.xpathCapability)
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "the :xpath capability is not supported by server");
        int mid = encode_getConfig(out, encode_datastore(datastore), xpath);
        out.flush();
        return recv_rpc_reply_data(mid);
    }

    /**
     * Retrieves running configuration and device state information.
     */
    public NodeSet get() throws NetconfException, IOException {
        trace("get: \"\"");
        int mid = encode_get(out, "");
        out.flush();
        return recv_rpc_reply_data(mid);
    }

    /**
     * Retrieves running configuration and device state information.
     * 
     * @param subtreeFilter
     *            A subtree filter
     */
    public NodeSet get(Element subtreeFilter) throws NetconfException, IOException {
        trace("get: " + subtreeFilter.toXMLString());
        int mid = encode_get(out, subtreeFilter);
        out.flush();
        return recv_rpc_reply_data(mid);
    }

    /**
     * Retrieves running configuration and device state information. The
     * <code>:xpath</code> capability must be supported by the server.
     * 
     * @param xpath
     *            An xpath epxression.
     */
    public NodeSet get(String xpath) throws NetconfException, IOException {
        trace("get: \"" + xpath + "\"");
        if (!capabilities.hasXPath())
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "the :xpath capability is not supported by server");
        int mid = encode_get(out, xpath);
        out.flush();
        return recv_rpc_reply_data(mid);
    }

    /**
     * Edits the configuration. The <code>edit-config</code> operation loads all
     * or part of a specified configuration to the {@link #RUNNING} target
     * datatore.
     * 
     * @param configTree
     *            Configuration tree
     */
    public void editConfig(Element configTree) throws NetconfException, IOException {
        editConfig(RUNNING, configTree);
    }

    /**
     * Edits the configuration. If we have multiple top elements in our
     * configuration schema (YANG model) we must send a NodeSet as opposed to an
     * Element to the device
     */

    public void editConfig(NodeSet configTrees) throws NetconfException,
            IOException {
        editConfig(RUNNING, configTrees);
    }

    /**
     * Edits the configuration. The <code>edit-config</code> operation loads all
     * or part of a specified configuration to the specified target
     * configuration.
     * 
     * @param datastore
     *            The target datastore. One of {@link #RUNNING},
     *            {@link #CANDIDATE}, {@link #STARTUP}
     * @param configTree
     *            The config tree to edit.
     */
    public void editConfig(int datastore, Element configTree)
            throws NetconfException, IOException {
        trace("editConfig: target=" + datastoreToString(datastore) + "\n"
                + configTree.toXMLString());
        int mid = encode_editConfig(out, encode_datastore(datastore),
                configTree);
        out.flush();
        recv_rpc_reply_ok(mid);
    }

    public void editConfig(int datastore, NodeSet configTrees)
            throws NetconfException, IOException {
        trace("editConfig: target=" + datastoreToString(datastore) + "\n"
                + configTrees.toXMLString());
        int mid = encode_editConfig(out, encode_datastore(datastore),
                configTrees);
        out.flush();
        recv_rpc_reply_ok(mid);
    }

    /**
     * Edits the configuration. The <edit-config> operation loads all or part of
     * a specified configuration to the specified target configuration.
     * 
     * @param datastore
     *            The target datastore. One of {@link #RUNNING},
     *            {@link #CANDIDATE}, {@link #STARTUP}
     * @param url
     *            The source url.
     */
    public void editConfig(int datastore, String url) throws NetconfException,
            IOException {
        trace("editConfig: target=" + datastoreToString(datastore) + " source="
                + url);
        int mid = encode_editConfig(out, encode_datastore(datastore),
                encode_url(url));
        out.flush();
        recv_rpc_reply_ok(mid);
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
     * with an "error-tag" value of data-missing. Using "none" allows operations
     * like "delete" to avoid unintentionally creating the parent hierarchy of
     * the element to be deleted.
     * </ul>
     * 
     * @param op
     *            One of {@link #MERGE}, {@link #REPLACE}, {@link #NONE}.
     * 
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
     * {@link #TEST_ONLY}: Only test. (Option is not supported in the standard.)
     * </ul>
     * 
     * @param testoption
     *            One of {@link #SET}, {@link #TEST_THEN_SET},
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
     * @param erroroption
     *            One of {@link #STOP_ON_ERROR}, {@link #CONTINUE_ON_ERROR},
     *            {@link #ROLLBACK_ON_ERROR}
     * 
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
     * Creates or replace an entire configuration datastore with the contents of
     * another complete configuration datastore. If the target datastore exists,
     * it is overwritten. Otherwise, a new one is created, if allowed.
     * 
     * @param sourceTree
     *            A config tree
     * @param target
     *            The target datastore
     */
    public void copyConfig(Element sourceTree, int target) throws NetconfException,
            IOException {
        copyConfig(new NodeSet(sourceTree), target);
    }

    /**
     * variant of copyConfig() that takes a NodeSet as param
     */

    public void copyConfig(NodeSet sourceTrees, int target)
            throws NetconfException, IOException {

        trace("copyConfig: target=" + datastoreToString(target) + "\n"
                + sourceTrees.toXMLString());
        encode_copyConfig(out, sourceTrees, encode_datastore(target));
        out.flush();
        recv_rpc_reply_ok();
    }

    /**
     * Same as {@link #copyConfig(Element,int)} but uses an url as target. Only
     * possible if <code>:url</code> capability is supported.
     * 
     * @param sourceTree
     *            A config tree
     * @param targetUrl
     *            The target URL.
     */
    public void copyConfig(Element sourceTree, String targetUrl)
            throws NetconfException, IOException {
        copyConfig(new NodeSet(sourceTree), targetUrl);
    }

    public void copyConfig(NodeSet sourceTrees, String targetUrl)
            throws NetconfException, IOException {

        trace("copyConfig: target=" + targetUrl + "\n"
                + sourceTrees.toXMLString());
        encode_copyConfig(out, sourceTrees, encode_url(targetUrl));
        out.flush();
        recv_rpc_reply_ok();
    }

    /**
     * Creates or replace an entire configuration datastore with the contents of
     * another complete configuration datastore. If the target datastore exists,
     * it is overwritten. Otherwise, a new one is created, if allowed.
     * 
     * @param source
     *            The source datastore
     * @param target
     *            The target datastore
     */
    public void copyConfig(int source, int target) throws NetconfException,
            IOException {
        trace("copyConfig: " + datastoreToString(source) + " "
                + datastoreToString(target));
        encode_copyConfig(out, encode_datastore(source),
                encode_datastore(target));
        out.flush();
        recv_rpc_reply_ok();
    }

    /**
     * Same as {@link #copyConfig(int,int)} but uses an url as target. Only
     * possible if <code>:url</code> capability is supported.
     * 
     * @param source
     *            The datastore to be used as source
     * @param targetUrl
     *            The target URL.
     */
    public void copyConfig(int source, String targetUrl) throws NetconfException,
            IOException {
        trace("copyConfig: source=" + datastoreToString(source) + " target="
                + targetUrl);
        encode_copyConfig(out, encode_datastore(source), encode_url(targetUrl));
        out.flush();
        recv_rpc_reply_ok();
    }

    /**
     * Same as {@link #copyConfig(int,int)} but uses an url as target and an url
     * as a source. Only possible if <code>:url</code> capability is supported.
     * 
     * @param sourceUrl
     *            The source URL.
     * @param targetUrl
     *            The target URL.
     */
    public void copyConfig(String sourceUrl, String targetUrl)
            throws NetconfException, IOException {
        trace("copyConfig: source=" + sourceUrl + " target=" + targetUrl);
        encode_copyConfig(out, encode_url(sourceUrl), encode_url(targetUrl));
        out.flush();
        recv_rpc_reply_ok();
    }

    /**
     * Same as {@link #copyConfig(int,int)} but uses an url as the source. Only
     * possible if <code>:url</code> capability is supported.
     * 
     * @param sourceUrl
     *            The source URL.
     * @param target
     *            The target datastore
     */
    public void copyConfig(String sourceUrl, int target) throws NetconfException,
            IOException {
        trace("copyConfig: source=" + sourceUrl + " target="
                + datastoreToString(target));
        encode_copyConfig(out, encode_url(sourceUrl), encode_datastore(target));
        out.flush();
        recv_rpc_reply_ok();
    }

    /**
     * Deletes a configuration datastore. The <running> configuration datastore
     * cannot be deleted.
     * 
     * @param datastore
     *            Datastore to be deleted
     */
    public void deleteConfig(int datastore) throws NetconfException, IOException {
        trace("deleteConfig: " + datastoreToString(datastore));
        encode_deleteConfig(out, encode_datastore(datastore));
        out.flush();
        recv_rpc_reply_ok();
    }

    /**
     * Deletes a configuration target url.
     * 
     * @param targetUrl
     *            Name of configuration target url to be deleted.
     */
    public void deleteConfig(String targetUrl) throws NetconfException, IOException {
        trace("deleteConfig: " + targetUrl);
        encode_deleteConfig(out, encode_url(targetUrl));
        out.flush();
        recv_rpc_reply_ok();
    }

    /**
     * The lock operation allows the client to lock the configuration system of
     * a device. Such locks are intended to be short-lived and allow a client to
     * make a change without fear of interaction with other NETCONF clients,
     * non-NETCONF clients (e.g., SNMP and command line interface (CLI)
     * scripts), and human users.
     * <p>
     * An attempt to lock the configuration must fail if an existing session or
     * other entity holds a lock on any portion of the lock target.
     * 
     * @param datastore
     *            The datastore to lock
     */
    public void lock(int datastore) throws NetconfException, IOException {
        trace("lock: " + datastoreToString(datastore));
        encode_lock(out, encode_datastore(datastore));
        out.flush();
        recv_rpc_reply_ok();
    }

    /**
     * The unlock operation is used to release a configuration lock, previously
     * obtained with the {@link #lock} operation.
     * 
     * @param datastore
     *            The target datastore to unlock
     */
    public void unlock(int datastore) throws NetconfException, IOException {
        trace("unlock: " + datastoreToString(datastore));
        encode_unlock(out, encode_datastore(datastore));
        out.flush();
        recv_rpc_reply_ok();
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
     * is only used for the creation of the partial lock. Conceptually the scope
     * of the lock is defined by the returned nodeset and not by the XPath
     * expression.
     * <p>
     * If a node is locked by a session, only that same session will be able to
     * modify that node or any node in the subtree underneath it.
     * <p>
     * If a top level node of a locked subtree is deleted, any other session can
     * recreate it, as it is not covered by the lock anymore. The lock operation
     * allows the client to lock the configuration system of a device. Such
     * locks are intended to be short-lived and allow a client to make a change
     * without fear of interaction with other NETCONF clients, non-NETCONF
     * clients (e.g., SNMP and command line interface (CLI) scripts), and human
     * users.
     * <p>
     * An attempt to lock the configuration must fail if an existing session or
     * other entity holds a lock on any portion of the lock target.
     * 
     * @param datastore
     *            datastore of which a part will be locked
     * @param select
     *            An array of selection filters
     * @return A unique lock reference which should be used to unlockPartial()
     * 
     */
    public int lockPartial(String[] select) throws NetconfException, IOException {
        trace("lockPartial");
        if (!capabilities.hasPartialLock())
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "capability :partial-lock is not supported by server");
        // Allow simple paths to instance identifiers also
        // if (!xpathCapability)
        // throw new INMException(INMException.SESSION_ERROR,
        // "capability :xpath is not supported by server");
        int mid = encode_lockPartial(out, select);
        out.flush();
        NodeSet reply = recv_rpc_reply_lockPartial(mid);
        try {
            Element t = reply.first().getFirst("self::lock-id");
            return Integer.parseInt((String) t.value);
        } catch (Exception e) {
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "bad lock-id returned from partial-lock: "
                            + reply.toXMLString());
        }
    }

    /**
     * Same as {@link #lockPartial(int,String[])} except it only takes one
     * selection as argument.
     * 
     * @see #lockPartial(int,String[])
     */
    public int lockPartial(String select) throws NetconfException, IOException {
        return lockPartial(new String[] { select });
    }

    /**
     * The unlock operation is used to release a configuration lock, previously
     * obtained with the {@link #lock} operation.
     * 
     * @param lockId
     *            Previously received lock identifier from
     *            {@link #lockPartial(int,String[])}
     */
    public void unlockPartial(int lockId) throws NetconfException, IOException {
        trace("partialUnlock: " + lockId);
        if (!capabilities.hasPartialLock())
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "capability :partial-lock is not supported by server");
        if (!capabilities.hasXPath())
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "capability :xpath is not supported by server");
        int mid = encode_unlockPartial(out, lockId);
        out.flush();
        recv_rpc_reply_ok(mid);
    }

    /**
     * When a candidate configuration's content is complete, the configuration
     * data can be committed, publishing the data set to the rest of the device
     * and requesting the device to conform to the behavior described in the new
     * configuration.
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
     * 
     */
    public void commit() throws NetconfException, IOException {
        trace("commit");
        if (!capabilities.hasCandidate())
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "the :candidate capability is not supported by server");
        int mid = encode_commit(out);
        out.flush();
        recv_rpc_reply_ok(mid);
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
     * configuration to the state prior to the issuance of the confirmed commit.
     * Note that any commit operation, including a commit which introduces
     * additional changes to the configuration, will serve as a confirming
     * commit. Thus to cancel a confirmed commit and revert changes without
     * waiting for the confirm timeout to expire, the manager can explicitly
     * restore the configuration to its state before the confirmed commit was
     * issued.
     * <p>
     * For shared configurations, this feature can cause other configuration
     * changes (for example, via other NETCONF sessions) to be inadvertently
     * altered or removed, unless the configuration locking feature is used (in
     * other words, the lock is obtained before the edit-config operation is
     * started). Therefore, it is strongly suggested that in order to use this
     * feature with shared configuration databases, configuration locking should
     * also be used.
     * 
     * @param timeout
     *            Time that server will wait for confirming commit before
     *            reverting config
     * 
     */
    public void confirmedCommit(int timeout) throws NetconfException, IOException {
        trace("confirmedCommit: " + timeout);
        if (!capabilities.hasCandidate())
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "the :candidate capability is not supported by server");
        if (!capabilities.hasConfirmedCommit())
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "the :confirmed-commit capability is not supported by server");
        int mid = encode_confirmedCommit(out, timeout);
        out.flush();
        recv_rpc_reply_ok(mid);
    }

    /**
     * If the client decides that the candidate configuration should not be
     * committed, the <discard-changes> operation can be used to revert the
     * candidate configuration to the current running configuration.
     * 
     */
    public void discardChanges() throws NetconfException, IOException {
        trace("discardChanges");
        if (!capabilities.hasCandidate())
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "the :candidate capability is not supported by server");
        int mid = encode_discardChanges(out);
        out.flush();
        recv_rpc_reply_ok(mid);
    }

    /**
     * Requests graceful termination of a NETCONF session.
     * <p>
     * When a NETCONF server receives a <code>close-session</code> request, it
     * will gracefully close the session. The server will release any locks and
     * resources associated with the session and gracefully close any associated
     * connections.
     */
    public void closeSession() throws NetconfException, IOException {
        trace("closeSession");
        int mid = encode_closeSession(out);
        out.flush();
        recv_rpc_reply_ok(mid);
    }

    /**
     * Force the termination of a NETCONF session.
     * <p>
     * When a NETCONF entity receives a <code>kill-session</code> request for an
     * open session, it will abort any operations currently in process, release
     * any locks and resources associated with the session, and close any
     * associated connections.
     * <p>
     * Session identifier of the NETCONF session to be terminated, if this value
     * is the current session ID a INMException will be thrown.
     * 
     * @param sessionId
     *            The id of the session to terminate
     */
    public void killSession(int sessionId) throws NetconfException, IOException {
        trace("killSession: " + sessionId);
        if (sessionId == this.sessionId)
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "illegal to use kill-session on own session id");
        int mid = encode_killSession(out, sessionId);
        out.flush();
        recv_rpc_reply_ok(mid);
    }

    /**
     * This protocol operation validates the contents of the specified
     * configuration.
     * 
     * @param configTree
     *            configuration tree to validate
     */
    public void validate(Element configTree) throws NetconfException, IOException {
        trace("validate: " + configTree.toXMLString());
        if (!capabilities.hasValidate())
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "capability :validate is not supported by server");
        int mid = encode_validate(out, configTree);
        out.flush();
        recv_rpc_reply_ok(mid);
    }

    /**
     * This protocol operation validates the given datastore. For example the
     * {@link #CANDIDATE} datastore.
     * 
     * @param datastore
     *            The datastore to validate
     */
    public void validate(int datastore) throws IOException, NetconfException {
        trace("validate: " + datastoreToString(datastore));
        if (!capabilities.hasValidate())
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "capability :validate is not supported by server");
        int mid = encode_validate(out, encode_datastore(datastore));
        out.flush();
        recv_rpc_reply_ok(mid);
    }

    /**
     * This protocol operation validates the given URL. The url may for example
     * be a file <code>"file://incoming.conf"</code> then file must be supported
     * by the <code>:url</code> capability.
     * 
     * @param url
     *            The source url to validate
     */
    public void validate(String url) throws IOException, NetconfException {
        trace("validate: " + url);
        if (!capabilities.hasValidate())
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "capability :validate is not supported by server");
        int mid = encode_validate(out, encode_url(url));
        out.flush();
        recv_rpc_reply_ok(mid);
    }

    /**
     * The notification capability makes it possible to receive notifications
     * specified in a subscription. The <code>:notification</code> capability
     * must be supported by the server.
     * 
     * @see #createSubscription(String,String,String,String)
     * 
     */
    public void createSubscription() throws IOException, NetconfException {
        createSubscription(null, (String) null, null, null);
    }

    /**
     * The notification capability makes it possible to receive notifications
     * specified in a subscription. The <code>:notification</code> capability
     * must be supported by the server.
     * <p>
     * Subscribe on speciied stream name.
     * 
     * 
     * @param stream
     *            The name of the stream or 'null'
     * @see #createSubscription(String,String,String,String)
     * 
     */
    public void createSubscription(String stream) throws IOException,
            NetconfException {
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
     * <code>startTime</code> is not present, this is not a replay subscription.
     * It is not valid to specify start times that are later than the current
     * time. If the <code>startTime</code> specified is earlier than the log can
     * support, the replay will begin with the earliest available notification.
     * The time is specified in dateTime format.
     * <p>
     * An optional parameter, <code>stopTime</code>, used with the optional
     * replay feature to indicate the newest notifications of interest. If stop
     * time is not present, the notifications will continue until the
     * subscription is terminated. Must be used with and be later than
     * <code>startTime</code>. Values of <code>stopTime</code> in the future are
     * valid. The time is specified in dateTime format.
     * <p>
     * 
     * @param streamName
     *            The name of the stream or 'null'
     * @param eventFilter
     *            a subtree filter - list of events, or 'null'
     * @param startTime
     *            a dateTime string specifying the replay start, or 'null'
     * @param stopTime
     *            a dateTime string specifying the stop of replay, or 'null'
     * @see #receiveNotification()
     */
    public void createSubscription(String streamName, NodeSet eventFilter,
            String startTime, String stopTime) throws IOException, NetconfException {
        trace("createSubscription: stream=" + streamName + " filter="
                + eventFilter.toXMLString() + " from=" + startTime + " to="
                + stopTime);
        if (!capabilities.hasNotification())
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "capability :notification is not supported by server");
        int mid = encode_createSubscription(out, streamName, eventFilter,
                startTime, stopTime);
        out.flush();
        recv_rpc_reply_ok(mid);
    }

    /**
     * Same as {@link #createSubscription(String,NodeSet,String,String)} except
     * a filter in xpath format can be given instead of a subtree filter.
     * 
     * @param streamName
     *            The name of the stream or 'null'
     * @param eventFilter
     *            a filter xpath expression, or 'null'
     * @param startTime
     *            a dateTime string specifying the replay start, or 'null'
     * @param stopTime
     *            a dateTime string specifying the stop of replay, or 'null'
     * @see #receiveNotification()
     */
    public void createSubscription(String streamName, String eventFilter,
            String startTime, String stopTime) throws IOException, NetconfException {
        trace("createSubscription: stream=" + streamName + " filter="
                + eventFilter + " from=" + startTime + " to=" + stopTime);
        if (!capabilities.hasNotification())
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "capability :notification is not supported by server");
        if (!capabilities.hasXPath())
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "capability :xpath is not supported by server");
        int mid = encode_createSubscription(out, streamName, eventFilter,
                startTime, stopTime);
        out.flush();
        recv_rpc_reply_ok(mid);
    }

    /**
     * Method to get the available streams from the agent. This will do:
     * 
     * <pre>
     * Element subtree = Element.create(&quot;urn:ietf:params:xml:ns:netmod:notification&quot;,
     *         &quot;netconf/streams&quot;);
     * return session.get(subtree);
     * </pre>
     * 
     * The available streams are returned.
     */
    public NodeSet getStreams() throws NetconfException, IOException {
        Element filter = Element
                .create("urn:ietf:params:xml:ns:netmod:notification",
                        "netconf/streams");
        return get(filter);
    }

    /**
     * Receive one notification. This is a blocking call - it blocks the caller
     * until an entire notifications messages has been received. It's possible
     * to check if there is data to be read ahead using the ready() method on
     * the SSHSession object.
     * 
     */

    public Element receiveNotification() throws IOException, NetconfException {
        StringBuffer notification = in.readOne();
        trace("notification= " + notification);
        if (notification.length() == 0)
            throw new NetconfException(NetconfException.PARSER_ERROR, "empty input");
        Element t = parser.parse(notification.toString());
        Element test = t.getFirst("self::notification");
        if (test != null)
            return t;
        /* rpc-error */
        throw new NetconfException(NetconfException.NOTIFICATION_ERROR, t);
    }

    /**
     * ------------------------------------------------------------ Receive from
     * session
     */

    /**
     * Reads one rpc-reply from session and parse an <ok/>. If not ok then throw
     * RCP_REPLY_ERROR exception.
     */
    void recv_rpc_reply_ok() throws NetconfException, IOException {
        recv_rpc_reply_ok(null);
    }

    void recv_rpc_reply_ok(int mid) throws NetconfException, IOException {
        recv_rpc_reply_ok(Integer.toString(mid));
    }

    void recv_rpc_reply_ok(String mid) throws NetconfException, IOException {
        StringBuffer reply = in.readOne();
        trace("reply= " + reply);
        if (reply.length() == 0)
            throw new NetconfException(NetconfException.PARSER_ERROR, "empty input");
        Element t = parser.parse(reply.toString());
        Element rep = t.getFirst("self::rpc-reply");
        if (rep != null) {
            check_mid(rep, mid);
        }
        Element ok = rep.getFirst("self::rpc-reply/ok");
        if (ok != null) {
            return;
        }

        /* rpc-error */
        throw new NetconfException(NetconfException.RPC_REPLY_ERROR, t);
    }

    /**
     * Reads one rpc-reply from session and parse the <data>. Returns the
     * NodeSet contained in the data tag.
     */
    NodeSet recv_rpc_reply_data(int mid) throws NetconfException, IOException {
        return recv_rpc_reply("/data", parser, Integer.toString(mid));
    }

    NodeSet recv_rpc_reply_lockPartial(int mid) throws NetconfException,
            IOException {
        return recv_rpc_reply("", parser, Integer.toString(mid));
    }

    NodeSet recv_call_rpc_reply(Element e, int mid) throws NetconfException,
            IOException {
        XMLParser parser = new XMLParser(); 
        // XXX Why new parser?
        return recv_rpc_reply("", parser, Integer.toString(mid));
    }

    NodeSet recv_rpc_reply(String path) throws NetconfException, IOException {
        return recv_rpc_reply(path, parser, null);
    }

    NodeSet recv_rpc_reply(String path, XMLParser parser, String mid)
            throws NetconfException, IOException {
        StringBuffer reply = in.readOne();
        trace("reply= " + reply);

        Element t = parser.parse(reply.toString());
        Element rep = t.getFirst("self::rpc-reply");
        if (rep != null) {
            check_mid(rep, mid);
        }

        Element data = t.getFirst("self::rpc-reply" + path);

        if (data != null) {
            PrefixMap ctxtPrefix = data.prefixes;
            if (ctxtPrefix == null)
                ctxtPrefix = t.prefixes;
            else
                ctxtPrefix.merge(t.prefixes);
            if (ctxtPrefix == null)
                ctxtPrefix = new PrefixMap();
            /*
             * need to set parent of each data entry to null don't want
             * rpc-reply to be part of returned tree
             */
            if (data.children != null) {
                for (int i = 0; i < data.children.size(); i++) {
                    Element child = data.children.getElement(i);
                    child.parent = null;
                    if (child.prefixes != null)
                        // merge in prefix mapping from rpc header
                        child.prefixes.merge(ctxtPrefix);
                    else
                        child.prefixes = (PrefixMap) ctxtPrefix.clone();
                }
                return data.children;
            }
            // return empty node set rather than null
            return new NodeSet();
        }
        /* rpc-error */
        throw new NetconfException(NetconfException.RPC_REPLY_ERROR, t);
    }

    /**
     * ------------------------------------------------------------ Extending
     * the session with new capabilities.
     */

    /**
     * Set a proprietary capability. This capability will be advertised in the
     * initial hello message so this method need to invoked before the
     * {@link #hello()} method to have any effect.
     * 
     * @param capability
     *            Add a capablity string for this client session
     */
    protected void setCapability(String capability) {
        if (proprietaryClientCaps == null)
            proprietaryClientCaps = new ArrayList<String>();
        for (int i = 0; i < proprietaryClientCaps.size(); i++) {
            String cap = (String) proprietaryClientCaps.get(i);
            if (cap.equals(capability))
                return; // already member
        }
        proprietaryClientCaps.add(capability);
    }

    private ArrayList<String> proprietaryClientCaps;

    /**
     * Used by ConfDSession to set the withDefaults Attribute. Will be included
     * in the RPC header, if set
     */
    Attribute withDefaultsAttr = null;

    /**
     * ------------------------------------------------------------ Encoding
     */

    /**
     * Encodes the hello message. The capabilities advertised from the client
     * side are the base NETCONF capability.
     * 
     */
    void encode_hello(Transport out) {
        out.print("<hello xmlns=\"" + Capabilities.NS_NETCONF + "\">");
        out.print("<capabilities>");
        out.println("<capability>" + Capabilities.NETCONF_BASE_CAPABILITY
                + "</capability>");
        /* List proprietary client capabilities */
        if (proprietaryClientCaps != null) {
            for (int i = 0; i < proprietaryClientCaps.size(); i++) {
                out.print("<capability>");
                out.print((String) proprietaryClientCaps.get(i));
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
     * @param out
     *            Transport output stream
     */
    protected int encode_rpc_begin(Transport out) {
        return encode_rpc_begin(out, null);
    }

    /**
     * Encodes the RPC header and writes it to the provided output transport.
     * This method is provided to be able to extend this class with proprietary
     * capabilities. The extra attribute arguement makes it possible to add an
     * extra attribute to the RPC header.
     * 
     * @param out
     *            Transport output stream
     * @param attr
     *            Extra attribute to be added to rpc header
     */
    protected int encode_rpc_begin(Transport out, Attribute attr) {
        String prefix = Element.defaultPrefixes
                .nsToPrefix(Element.NETCONF_NAMESPACE);
        nc = mk_prefix_colon(prefix);
        String xmlnsAttr = mk_xmlns_attr(prefix, Element.NETCONF_NAMESPACE);

        out.print("<" + nc + "rpc " + xmlnsAttr + " " + nc + "message-id=\"");
        int mid = message_id++;
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
     * Temporary holder for the encode functions. Hold the prefix to be appended
     * on NETCONF NAMESPACE operations. It is achieved through: String prefix =
     * Element.defaultPrefixes.nsToPrefix(Element.NETCONF_NAMESPACE); nc =
     * mk_prefix_colon(prefix);
     */
    private String nc;

    /**
     * Closes the rpc tag.
     * 
     * @param out
     *            Transport output stream
     */
    protected void encode_rpc_end(Transport out) {
        out.print("</" + nc + "rpc>");
        // do not end with newline
    }

    /**
     * Encode the <getConfig>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <get-config>
     * <source><running/></source> <filter type="subtree"> <top
     * xmlns="http://example.com/schema/1.2/config"> <users/> </top> </filter>
     * </get-config> </rpc>
     */
    int encode_getConfig(Transport out, String source, Element subtreeFilter)
            throws NetconfException {
        int mid = encode_rpc_begin(out, withDefaultsAttr);
        out.println("<" + nc + "get-config>");
        out.print("<" + nc + "source>");
        out.print(source);
        out.println("</" + nc + "source>");
        out.println("<" + nc + "filter " + nc + "type=\"subtree\">");
        subtreeFilter.encode(out, true, capabilities);
        out.println("</" + nc + "filter>");
        out.println("</" + nc + "get-config>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <rpc>. Example: <rpc
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0" message-id="1"> <math
     * xmlns="http://example.com/math"> <add> <operand>2</operand>
     * <operand>3</operand> </add> </math> </rpc>
     */
    int encode_rpc(Transport out, Element data) throws NetconfException {
        int mid = encode_rpc_begin(out);
        data.encode(out);
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <getConfig>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <get-config>
     * <source><running/></source> <filter type="xpath"
     * select="top/users/user[name='fred']"/> </get-config> </rpc>
     */
    int encode_getConfig(Transport out, String source, String xpath) {
        int mid = encode_rpc_begin(out, withDefaultsAttr);
        out.println("<" + nc + "get-config>");
        out.print("<" + nc + "source>");
        out.print(source);
        out.println("</" + nc + "source>");
        if (xpath != null && xpath.length() > 0) {
            out.print("<" + nc + "filter " + nc + "type=\"xpath\" " + nc
                    + "select=\"");
            out.print(xpath);
            out.println("\"/>");
        }
        out.println("</" + nc + "get-config>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <getConfig>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <get-config>
     * <source><running/></source> </get-config> </rpc>
     */
    int encode_getConfig(Transport out, String source) {
        int mid = encode_rpc_begin(out, withDefaultsAttr);
        out.println("<" + nc + "get-config>");
        out.print("<" + nc + "source>");
        out.print(source);
        out.println("</" + nc + "source>");
        out.println("</" + nc + "get-config>");
        encode_rpc_end(out);
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
        }
        return "UNKNOWN_DATASTORE(" + datastore + ")";
    }

    /**
     * Encode datastore
     */
    String encode_datastore(int datastore) throws NetconfException {
        String prefix = Element.defaultPrefixes
                .nsToPrefix(Element.NETCONF_NAMESPACE);
        nc = mk_prefix_colon(prefix);

        switch (datastore) {
        case RUNNING:
            return "<" + nc + "running/>";
        case CANDIDATE:
            return "<" + nc + "candidate/>";
        case STARTUP:
            return "<" + nc + "startup/>";
        }
        throw new NetconfException(NetconfException.SESSION_ERROR,
                "unknown datastore: " + datastore);
    }

    /**
     * Encode URL
     */
    String encode_url(String url) throws NetconfException {
        if (!isUrlOK(url))
            throw new NetconfException(NetconfException.SESSION_ERROR, "the url: \""
                    + url + "\" is not a supported :url scheme");
        return "<url>" + url + "</url>";
    }

    /**
     * Check if given url is supported by the <code>:url</code> capabililty for
     * the session.
     */
    private boolean isUrlOK(String url) {
        String urlSchemes[] = capabilities.getUrlSchemes();
        if (urlSchemes == null)
            return false;
        for (int i = 0; i < urlSchemes.length; i++) {
            /*
             * check if given url starts with any of the scheme urls given for
             * the :url capability. for example "file:"
             */
            if (url.startsWith(urlSchemes[i] + ":"))
                return true;
        }
        return false;
    }

    /**
     * Encode the <get>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <get> <filter
     * type="subtree"> <top xmlns="http://example.com/schema/1.2/config">
     * <users/> </top> </filter> </get> </rpc>
     */
    int encode_get(Transport out, Element subtreeFilter) throws NetconfException {
        int mid = encode_rpc_begin(out, withDefaultsAttr);
        out.println("<" + nc + "get>");
        out.println("<" + nc + "filter " + nc + "type=\"subtree\">");
        subtreeFilter.encode(out, true, capabilities);
        out.println("</" + nc + "filter>");
        out.println("</" + nc + "get>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <get>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <get> <filter
     * type="xpath" select="top/users/user[name='fred']"/> </get> </rpc>
     */
    int encode_get(Transport out, String xpath) {
        int mid = encode_rpc_begin(out, withDefaultsAttr);
        out.println("<" + nc + "get>");
        if (xpath != null && xpath.length() > 0) {
            out.print("<" + nc + "filter " + nc + "type=\"xpath\" " + nc
                    + "select=\"");
            out.print(xpath);
            out.println("\"/>");
        }
        out.println("</" + nc + "get>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <edit-Config>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <edit-config>
     * <target><running/></target> <config> <top
     * xmlns="http://example.com/schema/1.2/config"> <interface>
     * <name>Ethernet0/0</name> <mtu>1500</mtu> </interface> </top> </config>
     * </edit-config> </rpc>
     * 
     */
    int encode_editConfig(Transport out, String target, Element configTree)
            throws NetconfException {
        return encode_editConfig(out, target, new NodeSet(configTree));
    }

    int encode_editConfig(Transport out, String target, NodeSet configTrees)
            throws NetconfException {

        int mid = encode_rpc_begin(out);
        out.println("<" + nc + "edit-config>");
        out.print("<" + nc + "target>");
        out.print(target);
        out.println("</" + nc + "target>");
        encode_defaultOperation(out);
        encode_testOption(out);
        encode_errorOption(out);
        out.println("<" + nc + "config>");
        configTrees.encode(out, capabilities);
        out.println("</" + nc + "config>");
        out.println("</" + nc + "edit-config>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <edit-Config>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <edit-config>
     * <target><running/></target> <url>file://incoming.conf"</url>
     * </edit-config> </rpc>
     * 
     */
    int encode_editConfig(Transport out, String target, String url)
            throws NetconfException {
        int mid = encode_rpc_begin(out);
        out.println("<" + nc + "edit-config>");
        out.print("<" + nc + "target>");
        out.print(target);
        out.println("</" + nc + "target>");
        encode_defaultOperation(out);
        encode_testOption(out);
        encode_errorOption(out);
        out.println(url);
        out.println("</" + nc + "edit-config>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode default-operation for editConfig.
     */
    void encode_defaultOperation(Transport out) throws NetconfException {
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
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "unknown default-operation value: " + defaultOperation);
        }
    }

    /**
     * Encode test-option for editConfig
     */
    void encode_testOption(Transport out) throws NetconfException {
        switch (testOption) {
        case NOT_SET:
            return;
        case SET:
            if (!capabilities.hasValidate())
                throw new NetconfException(NetconfException.SESSION_ERROR,
                        "test-option is given but the :validate "
                                + "capability is not supported by server");
            out.println("<" + nc + "test-option>set</" + nc + "test-option>");
            return;
        case TEST_THEN_SET:
            if (!capabilities.hasValidate())
                throw new NetconfException(NetconfException.SESSION_ERROR,
                        "test-option is given but the :validate "
                                + "capability is not supported by server");
            out.println("<" + nc + "test-option>test-then-set</" + nc
                    + "test-option>");
            return;
        case TEST_ONLY:
            if (!capabilities.hasValidate())
                throw new NetconfException(NetconfException.SESSION_ERROR,
                        "test-option is given but the :validate "
                                + "capability is not supported by server");
            out.println("<" + nc + "test-option>test-only</" + nc
                    + "test-option>");
            return;
        default:
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "unknown test-option value: " + testOption);
        }
    }

    /**
     * Encode error-option for editConfig
     */
    void encode_errorOption(Transport out) throws NetconfException {
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
            if (!capabilities.hasRollbackOnError())
                throw new NetconfException(NetconfException.SESSION_ERROR,
                        "the :rollback-on-error capability "
                                + "is used but not supported by server");
            out.println("<" + nc + "error-option>rollback-on-error</" + nc
                    + "error-option>");
            return;
        default:
            throw new NetconfException(NetconfException.SESSION_ERROR,
                    "unknown error-option value: " + errorOption);
        }
    }

    /**
     * Encode the <copy-config>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <copy-config>
     * <target><running/></target> <default-operation>none</default-operation>
     * <config> <top xmlns="http://example.com/schema/1.2/config"> <interface>
     * <name>Ethernet0/0</name> <mtu>1500</mtu> </interface> </top> </config>
     * </copy-config> </rpc>
     * 
     */
    int encode_copyConfig(Transport out, Element sourceTree, String target)
            throws NetconfException {
        return encode_copyConfig(out, new NodeSet(sourceTree), target);
    }

    /**
     * If we have multiple top nodes in our schema, we must pass a NodeSet to
     * the copyConfig oeration
     */

    int encode_copyConfig(Transport out, NodeSet sourceTrees, String target)
            throws NetconfException {
        int mid = encode_rpc_begin(out);
        out.println("<" + nc + "copy-config>");
        out.print("<" + nc + "target>");
        out.print(target);
        out.println("</" + nc + "target>");
        out.println("<" + nc + "source>");
        out.println("<" + nc + "config>");
        sourceTrees.encode(out, capabilities);
        out.println("</" + nc + "config>");
        out.println("</" + nc + "source>");
        out.println("</" + nc + "copy-config>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <copy-config>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <copy-config>
     * <target><running/></target> <default-operation>none</default-operation>
     * <> <top xmlns="http://example.com/schema/1.2/config"> <interface>
     * <name>Ethernet0/0</name> <mtu>1500</mtu> </interface> </top> </config>
     * </copy-config> </rpc>
     * 
     */
    int encode_copyConfig(Transport out, String source, String target)
            throws NetconfException {
        int mid = encode_rpc_begin(out, withDefaultsAttr);
        out.println("<" + nc + "copy-config>");
        out.print("<" + nc + "target>");
        out.print(target);
        out.println("</" + nc + "target>");
        out.print("<" + nc + "source>");
        out.print(source);
        out.println("</" + nc + "source>");
        out.println("</" + nc + "copy-config>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <delete-config>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <delete-config>
     * <target><startup/></target> </delete-config> </rpc>
     * 
     */
    int encode_deleteConfig(Transport out, String target) {
        int mid = encode_rpc_begin(out);
        out.println("<" + nc + "delete-config>");
        out.print("<" + nc + "target>");
        out.print(target);
        out.println("</" + nc + "target>");
        out.println("</" + nc + "delete-config>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <lock>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <lock>
     * <target><running/></target> </lock> </rpc>
     * 
     */
    int encode_lock(Transport out, String target) {
        int mid = encode_rpc_begin(out);
        out.println("<" + nc + "lock>");
        out.print("<" + nc + "target>");
        out.print(target);
        out.println("</" + nc + "target>");
        out.println("</" + nc + "lock>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <unlock>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <unlock>
     * <target><running/></target> </unlock> </rpc>
     * 
     */
    int encode_unlock(Transport out, String target) {
        int mid = encode_rpc_begin(out);
        out.println("<" + nc + "unlock>");
        out.print("<" + nc + "target>");
        out.print(target);
        out.println("</" + nc + "target>");
        out.println("</" + nc + "unlock>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <partial-lock>. Example: <nc:rpc
     * xmlns:nc="urn:ietf:params:xml:ns:netconf:base:1.0"
     * xmlns="urn:ietf:params:xml:ns:netconf:partial-lock:1.0"
     * xmlns:rte="http://example.com/ns/route">
     * xmlns:if="http://example.com/ns/interface"> nc:message-id="135">
     * <partial-lock> <target> <running/> </target>
     * <select>/routing/virtualRouter['routerName=router1']</select>
     * <select>/interfaces/['interfaceId=eth1']</select> </partial-lock> </rpc>
     * 
     */
    int encode_lockPartial(Transport out, String[] select) {

        String prefix = Element.defaultPrefixes
                .nsToPrefix(Capabilities.NS_PARTIAL_LOCK);
        String pl = mk_prefix_colon(prefix);
        String xmlnsAttr = mk_xmlns_attr(prefix, Capabilities.NS_PARTIAL_LOCK);
        int mid = encode_rpc_begin(out);
        out.println("<" + pl + "partial-lock " + xmlnsAttr + ">");
        for (int i = 0; i < select.length; i++) {
            out.print("<" + pl + "select>");
            out.print(select[i]);
            out.println("</" + pl + "select>");
        }
        out.println("</" + pl + "partial-lock>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <partial-unlock>. Example: <nc:rpc
     * xmlns:nc="urn:ietf:params:xml:ns:netconf:base:1.0"
     * xmlns="urn:ietf:params:xml:ns:netconf:partial-lock:1.0"
     * nc:message-id="136"> <partial-unlock> <lock-id>127</lock-id>
     * </partial-unlock>
     * 
     */
    int encode_unlockPartial(Transport out, int lockId) {

        String prefix = Element.defaultPrefixes
                .nsToPrefix(Capabilities.NS_PARTIAL_LOCK);
        String pl = mk_prefix_colon(prefix);
        String xmlnsAttr = mk_xmlns_attr(prefix, Capabilities.NS_PARTIAL_LOCK);

        int mid = encode_rpc_begin(out);
        out.println("<" + pl + "partial-unlock " + xmlnsAttr + ">");
        out.print("<" + pl + "lock-id>");
        out.print(lockId);
        out.println("</" + pl + "lock-id>");
        out.println("</" + pl + "partial-unlock>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <commit>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <commit/> </rpc>
     * 
     */
    int encode_commit(Transport out) {
        int mid = encode_rpc_begin(out);
        out.println("<" + nc + "commit/>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <commit>. (confirmed) Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <commit> <confirmed/>
     * <confirm-timeout>120</confirm-timeout> </commit> </rpc>
     * 
     */
    int encode_confirmedCommit(Transport out, int timeout) {
        int mid = encode_rpc_begin(out);
        out.println("<" + nc + "commit>");
        out.println("<" + nc + "confirmed/>");
        out.print("<" + nc + "confirm-timeout>");
        out.print(new Integer(timeout).toString());
        out.println("</" + nc + "confirm-timeout>");
        out.println("</" + nc + "commit>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <discard-changes>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <discard-changes/>
     * </rpc>
     * 
     */
    int encode_discardChanges(Transport out) {
        int mid = encode_rpc_begin(out);
        out.println("<" + nc + "discard-changes/>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <close-session>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <close-session/> </rpc>
     * 
     */
    int encode_closeSession(Transport out) {
        int mid = encode_rpc_begin(out);
        out.println("<" + nc + "close-session/>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <kill-session>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <kill-session>
     * <session-id>4</session-id> </kill-session> </rpc>
     * 
     */
    int encode_killSession(Transport out, int sessionId) {
        int mid = encode_rpc_begin(out);
        out.println("<" + nc + "kill-session>");
        out.print("<" + nc + "session-id>");
        out.print(sessionId);
        out.println("</" + nc + "session-id>");
        out.println("</" + nc + "kill-session>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <validate>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <validate> <source>
     * <config> <top xmlns="http://example.com/schema/1.2/config"> <interface>
     * <name>Ethernet0/0</name> <mtu>1500</mtu> </interface> </top> </config>
     * </source> </validate> </rpc>
     * 
     */
    int encode_validate(Transport out, Element configTree) throws NetconfException {
        int mid = encode_rpc_begin(out);
        out.println("<" + nc + "validate>");
        out.println("<" + nc + "source>");
        out.println("<" + nc + "config>");
        configTree.encode(out, true, capabilities);
        out.println("</" + nc + "config>");
        out.println("</" + nc + "source>");
        out.println("</" + nc + "validate>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <validate>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <validate>
     * <source><candidate/></source> </validate> </rpc>
     * 
     */
    int encode_validate(Transport out, String source) {
        int mid = encode_rpc_begin(out);
        out.println("<" + nc + "validate>");
        out.print("<" + nc + "source>");
        out.print(source);
        out.println("</" + nc + "source>");
        out.println("</" + nc + "validate>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <create-subscription>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <create-subscription
     * xmlns='urn:ietf:params:xml:ns:netconf:notification:1.0'>
     * </create-subscription> </rpc>
     * 
     */
    int encode_createSubscription(Transport out, String stream, String filter,
            String startTime, String stopTime) {
        String prefix = Element.defaultPrefixes
                .nsToPrefix(Capabilities.NS_NOTIFICATION);
        String ncn = mk_prefix_colon(prefix);
        String xmlnsAttr = mk_xmlns_attr(prefix, Capabilities.NS_NOTIFICATION);

        int mid = encode_rpc_begin(out);
        out.println("<" + ncn + "create-subscription " + xmlnsAttr + ">");
        if (stream != null) {
            out.print("<" + ncn + "stream>");
            out.print(stream);
            out.println("</" + ncn + "stream>");
        }
        if (filter != null) {
            out.print("<" + ncn + "filter " + ncn + "type='xpath'>");
            out.print(filter);
            out.println("</" + ncn + "filter>");
        }
        if (startTime != null) {
            out.print("<" + ncn + "startTime>");
            out.print(startTime);
            out.println("</" + ncn + "startTime>");
        }
        if (stopTime != null) {
            out.print("<" + ncn + "stopTime>");
            out.print(stopTime);
            out.println("</" + ncn + "stopTime>");
        }
        out.println("</" + ncn + "create-subscription>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * Encode the <create-subscription>. Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <create-subscription
     * xmlns='urn:ietf:params:xml:ns:netconf:notification:1.0'>
     * </create-subscription> </rpc>
     * 
     */
    int encode_createSubscription(Transport out, String stream,
            NodeSet eventFilter, String startTime, String stopTime)
            throws NetconfException {
        String noNotification = Capabilities.NS_NOTIFICATION;
        String prefix = Element.defaultPrefixes.nsToPrefix(noNotification);
        String ncn = mk_prefix_colon(prefix);

        int mid = encode_rpc_begin(out);
        out.println("<" + ncn + "create-subscription>");
        if (stream != null) {
            out.print("<" + ncn + "stream>");
            out.print(stream);
            out.println("</" + ncn + "stream>");
        }
        if (eventFilter != null) {
            out.print("<" + ncn + "filter " + ncn + "type='subtree'>");
            eventFilter.encode(out, capabilities);
            out.println("</" + ncn + "filter>");
        }
        if (startTime != null) {
            out.print("<" + ncn + "startTime>");
            out.print(startTime);
            out.println("</" + ncn + "startTime>");
        }
        if (stopTime != null) {
            out.print("<" + ncn + "stopTime>");
            out.print(stopTime);
            out.println("</" + ncn + "stopTime>");
        }
        out.println("</" + ncn + "create-subscription>");
        encode_rpc_end(out);
        return mid;
    }

    /**
     * ------------------------------------------------------------ help
     * functions
     */

    /**
     * Help function to make prefix. Returns either: "PREFIX:" or "".
     */
    String mk_prefix_colon(String prefix) {
        if (prefix == null)
            return "unknown:";
        if (prefix.equals(""))
            return "";
        else
            return prefix + ":";
    }

    /**
     * Help function to make xmlns attr from prefix and namespace. Returns
     * either: "xmlns=NAMESPACE" or "xmlns:PREFIX=NAMESPACE".
     * 
     */
    String mk_xmlns_attr(String prefix, String ns) {
        if (prefix == null)
            return "xmlns:unknown=\"" + ns + "\"";
        if (prefix.equals(""))
            return "xmlns=\"" + ns + "\"";
        else
            return "xmlns:" + prefix + "=\"" + ns + "\"";
    }

    /**
     * Printout trace if 'debug'-flag is enabled.
     */
    private static void trace(String s) {
        if (Element.debugLevel >= Element.DEBUG_LEVEL_SESSION)
            System.err.println("*NetconfSession: " + s);
    }

    void check_mid(Element t, String mid) throws NetconfException {
        if (mid == null) {
            return;
        }
        String returned_id = t.getAttrValue("message-id");
        if (returned_id == null || (!returned_id.equals(mid))) {
            throw new NetconfException(NetconfException.MESSAGE_ID_MISMATCH,
                    "After sending rpc with message-id=" + mid
                            + ", received rpc-reply with message-id="
                            + returned_id);
        }
    }
}
