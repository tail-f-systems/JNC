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
 * An extended NETCONF session class, with capabilities that ConfD supports.
 * <p>
 * ConfD supports the following optional, non-standard capabilities:
 * <ul>
 * <li>actions
 * <li>transactions
 * <li>with-defaults
 * </ul>
 * 
 * The <code>:with-defaults</code> capability introduces an attribute
 * 'with-defaults' which can be used in the 'rpc' element when the operation is
 * 'get', 'get-config', or 'copy-config', to control if default values are
 * returned by the NETCONF agent or not. If 'with-defaults' is "true", default
 * values are included. If 'with-defaults' is "false", default values are not
 * included.
 * <p>
 * The <code>:action</code> capability introduces one new rpc method which is
 * used to invoke actions (methods) defined in the data model. When an action is
 * invoked, the instance on which the action is invoked is explicitly identified
 * by an hierarchy of configuration or state data.
 * <p>
 * Here's a simple example which resets an interface.
 * 
 * <pre>
 * &lt;rpc message-id="101"
 *      xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
 *   &lt;action xmlns="http://tail-f.com/ns/netconf/actions/1.0">
 *      &lt;data>
 *          &lt;interfaces xmlns="http://example.com/interfaces/1.0">
 *            &lt;interface>
 *              &lt;name>eth0&lt;/name>
 *              &lt;reset/>
 *            &lt;/interface>
 *          &lt;/interfaces>
 *        &lt;/data>
 *      &lt;/action>
 *    &lt;/rpc>
 * 
 * &lt;rpc-reply message-id="101"
 *             xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
 *      &lt;ok/>
 * &lt;/rpc-reply>
 * </pre>
 * <p>
 * The <code>:transactions</code> capability introduces four new rpc methods
 * which are used to control a two-phase commit transaction on the NETCONF
 * server. The normal <code>edit-config</code> operation is used to write data
 * in the transaction, but the modifications are not applied until an explicit
 * <code>commit-transaction</code> is sent.
 * <p>
 * A typical sequence of operations looks like this:
 * 
 * <pre>
 * C                           S
 * |                           |
 * |  capability exchange      |
 * |-------------------------->|
 * |&lt;------------------------->|
 * |                           |
 * |   &lt;start-transaction>     |
 * |-------------------------->|
 * |&lt;--------------------------|
 * |         &lt;ok/>             |
 * |                           |
 * |     &lt;edit-config>         |
 * |-------------------------->|
 * |&lt;--------------------------|
 * |         &lt;ok/>             |
 * |                           |
 * |  &lt;prepare-transaction>    |
 * |-------------------------->|
 * |&lt;--------------------------|
 * |         &lt;ok/>             |
 * |                           |
 * |   &lt;commit-transaction>    |
 * |-------------------------->|
 * |&lt;--------------------------|
 * |         &lt;ok/>             |
 * |                           |
 * </pre>
 * 
 * @see NetconfSession
 * 
 **/

public class ConfDSession extends NetconfSession {

    /**
     * Constructor. Creates a new session object using the given transport
     * object.
     * 
     * @see SSHSession
     * 
     * @param transport
     *            Transport object
     */
    public ConfDSession(Transport transport) throws INMException, IOException {
        super();
        setTransport(transport);
        mkSession();
    }

    /**
     * Constructor. Creates a new session object using the given transport
     * object.
     * 
     * @see SSHSession
     * 
     * @param transport
     *            Transport object
     * @param parse
     *            XML parser object
     */

    public ConfDSession(Transport transport, XMLParser parser)
            throws INMException, IOException {
        super();
        setTransport(transport);
        this.parser = parser;
        mkSession();
    }

    private void mkSession() throws INMException, IOException {
        setCapability(Capabilities.WITH_DEFAULTS_CAPABILITY);
        setCapability(Capabilities.ACTIONS_CAPABILITY);
        setCapability(Capabilities.TRANSACTIONS_CAPABILITY);
        hello();

        /**
         * set defaultPrefixes for NS_ACTIONS NS_TRANSACTIONS
         */
        if (Element.defaultPrefixes == null)
            Element.defaultPrefixes = new PrefixMap();
        Element.defaultPrefixes.set(new Prefix("nca", Capabilities.NS_ACTIONS));
        Element.defaultPrefixes.set(new Prefix("nctr",
                Capabilities.NS_TRANSACTIONS));
    }

    /**
     * Set the 'with-defaults' to 'true' or 'false'. This capability is valid
     * for <code>get</code>, <code>get-config</code>, and
     * <code>copy-config</code>, to control if defualt values are returned by
     * the NETCONF agent or not. If this value is 'true', default values are
     * included. If this value is 'false', default valus are not included.
     * 
     * @param value
     *            Value for with-defaults.
     */
    public void setWithDefaults(boolean value) throws INMException {
        if (!capabilities.hasWithDefaults())
            throw new INMException(INMException.SESSION_ERROR,
                    "server does not support the :with-defaults capability");
        withDefaultsAttr = new Attribute(Capabilities.WITH_DEFAULTS_CAPABILITY,
                "with-defaults", new Boolean(value).toString());
    }

    /**
     * Action capability. An action that does not return any result value,
     * replies with the standard 'ok' element. If a result value is returned, it
     * is encapsulated within a returned 'data' element.
     * 
     * @param data
     *            element tree with action-data
     */
    public Element action(Element data) throws INMException, IOException {
        trace("action: " + data.toXMLString());
        encode_action(out, data);
        out.flush();
        return recv_rpc_reply();
    }

    /**
     * Starts a transaction towards a configuration datastore. There can be a
     * single ongoing transaction at any time.
     * <p>
     * When a transaction has been started, the client can send any NETCONF
     * operation, but any <code>edit-config</code> operation sent from the
     * client must speify the same <code>target</code> as the
     * <code>start-transaction</code>. If the server receives and
     * <code>edit-config</code> with another <code>target</code>, an error must
     * be returned with an <code>error-tag</code> set to "invalid-value".
     * <p>
     * The modifications sent in the <code>edit-config</code> operations are not
     * immediately applied to the configuration datastore. Instead they are kept
     * in the transaction state of the server. The transaction state is only
     * applied when a <code>commit-transaction</code> is received.
     * <p>
     * The client sends a <code>prepare-transaction</code> when all
     * modifications have been sent.
     * 
     * @param datastore
     *            The datastore. One of {@link #RUNNING}, {@link #CANDIDATE},
     *            {@link #STARTUP}
     * 
     * @see #startTransaction(int)
     * @see #commitTransaction()
     * @see #abortTransaction()
     */
    public void startTransaction(int datastore) throws INMException,
            IOException {
        trace("startTransaction: " + datastoreToString(datastore));
        encode_startTransaction(out, encode_datastore(datastore));
        out.flush();
        recv_rpc_reply_ok();
    }

    /**
     * Prepares the transaction state for commit. The server may reject the
     * prepare request for any reason, for example due to lack of resources or
     * if the combined changes would result in an invalid configuration
     * datastore.
     * <p>
     * After a successful <code>prepare-transaction</code>, the next transaction
     * related rpc operation must be <code>commit-transaction</code> or
     * <code>abort-transaction</code>. Note that an <code>edit-config</code>
     * cannot be sent before the transaction is either committed or aborted.
     * <p>
     * Care must be taken by the server to make sure that if
     * <code>prepare-transaction</code> succeeds then the
     * <code>commit-transaction</code> should not fail, since this might result
     * in an inconsistent distributed state. Thus,
     * <code>prepare-transaction</code> should allocate any resources needed to
     * make sure the <code>commit-transaction</code> will succeed.
     * 
     * 
     * @see #startTransaction(int)
     * @see #commitTransaction()
     * @see #abortTransaction()
     */
    public void prepareTransaction() throws INMException, IOException {
        trace("prepareTransaction");
        encode_prepareTransaction(out);
        out.flush();
        recv_rpc_reply_ok();
    }

    /**
     * Applies the changes made in the transaction to the configuration
     * datatore. The transaction is closed after a
     * <code>commit-transaction</code>.
     * 
     * @see #startTransaction(int)
     * @see #prepareTransaction()
     * @see #abortTransaction()
     */
    public void commitTransaction() throws INMException, IOException {
        trace("commitTransaction");
        encode_commitTransaction(out);
        out.flush();
        recv_rpc_reply_ok();
    }

    /**
     * Aborts the ongoing transaction, and all pending changes are discarded.
     * <code>abort-transaction</code> can be given at any time during an ongoing
     * transaction.
     * 
     * 
     * @see #startTransaction(int)
     * @see #prepareTransaction()
     * @see #commitTransaction()
     */
    public void abortTransaction() throws INMException, IOException {
        trace("abortTransaction");
        encode_abortTransaction(out);
        out.flush();
        recv_rpc_reply_ok();
    }

    /**
     * ------------------------------------------------------------ Receive from
     * session
     */

    Element recv_rpc_reply() throws INMException, IOException {
        StringBuffer reply = in.readOne();
        trace("reply= " + reply);
        Element t = parser.parse(reply.toString());
        Element ok = t.getFirst("self::rpc-reply/ok");
        if (ok != null)
            return ok;
        Element data = t.getFirst("self::rpc-reply/data");
        if (data != null)
            return data;
        /* rpc-error */
        throw new INMException(INMException.RPC_REPLY_ERROR, t);
    }

    /**
     * ------------------------------------------------------------ Encoding
     */

    /**
     * Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <action
     * xmlns="http://tail-f.com/ns/netconf/actions/1.0"> <data> <interfaces
     * xmlns="http://example.com/interfaces/1.0"> <interface> <name>eth0</name>
     * <reset/> </interface> </interfaces> </data> </action> </rpc>
     */
    void encode_action(Transport out, Element data) throws INMException {
        String prefix = Element.defaultPrefixes
                .nsToPrefix(Capabilities.NS_ACTIONS);
        String act = mk_prefix_colon(prefix);
        String xmlnsAttr = mk_xmlns_attr(prefix, Capabilities.NS_ACTIONS);
        encode_rpc_begin(out);
        out.println("<" + act + "action " + xmlnsAttr + ">");
        out.print("<" + act + "data>");
        data.encode(out);
        out.println("</" + act + "data>");
        out.println("</" + act + "action>");
        encode_rpc_end(out);
    }

    /**
     * Example: <rpc message-id="101"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <start-transaction
     * xmlns="http://tail-f.com/ns/netconf/transactions/1.0">
     * <target><running/></target> </start-transaction> </rpc>
     */
    void encode_startTransaction(Transport out, String target) {
        String prefix = Element.defaultPrefixes
                .nsToPrefix(Capabilities.NS_TRANSACTIONS);
        String tr = mk_prefix_colon(prefix);
        String xmlnsAttr = mk_xmlns_attr(prefix, Capabilities.NS_TRANSACTIONS);

        encode_rpc_begin(out);
        out.println("<" + tr + "start-transaction " + xmlnsAttr + ">");
        out.print("<" + tr + "target>");
        out.print(target);
        out.println("</" + tr + "target>");
        out.println("</" + tr + "start-transaction>");
        encode_rpc_end(out);
    }

    /**
     * Example: <rpc message-id="103"
     * xmlns="urn:ietf:params:xml:ns:netconf:base:1.0"> <prepare-transaction
     * xmlns="http://tail-f.com/ns/netconf/transactions/1.0"/> </rpc>
     */
    void encode_prepareTransaction(Transport out) {
        String prefix = Element.defaultPrefixes
                .nsToPrefix(Capabilities.NS_TRANSACTIONS);
        String tr = mk_prefix_colon(prefix);
        String xmlnsAttr = mk_xmlns_attr(prefix, Capabilities.NS_TRANSACTIONS);

        encode_rpc_begin(out);
        out.println("<" + tr + "prepare-transaction " + xmlnsAttr + "/>");
        encode_rpc_end(out);
    }

    /**
     * <rpc message-id="104" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
     * <commit-transaction
     * xmlns="http://tail-f.com/ns/netconf/transactions/1.0"/> </rpc>
     * 
     */
    void encode_commitTransaction(Transport out) {
        String prefix = Element.defaultPrefixes
                .nsToPrefix(Capabilities.NS_TRANSACTIONS);
        String tr = mk_prefix_colon(prefix);
        String xmlnsAttr = mk_xmlns_attr(prefix, Capabilities.NS_TRANSACTIONS);

        encode_rpc_begin(out);
        out.println("<" + tr + "commit-transaction " + xmlnsAttr + "/>");
        encode_rpc_end(out);
    }

    /**
     * <rpc message-id="104" xmlns="urn:ietf:params:xml:ns:netconf:base:1.0">
     * <abort-transaction
     * xmlns="http://tail-f.com/ns/netconf/transactions/1.0"/> </rpc>
     */
    void encode_abortTransaction(Transport out) {
        String prefix = Element.defaultPrefixes
                .nsToPrefix(Capabilities.NS_TRANSACTIONS);
        String tr = mk_prefix_colon(prefix);
        String xmlnsAttr = mk_xmlns_attr(prefix, Capabilities.NS_TRANSACTIONS);

        encode_rpc_begin(out);
        out.println("<" + tr + "abort-transaction " + xmlnsAttr + "/>");
        encode_rpc_end(out);
    }

    /**
     * ------------------------------------------------------------ help
     * functions
     */

    /**
     * Printout trace if 'debug'-flag is enabled.
     */
    private static void trace(String s) {
        if (Element.debugLevel >= Element.DEBUG_LEVEL_SESSION)
            System.err.println("*ConfDSession: " + s);
    }

}
