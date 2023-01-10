package com.tailf.jnc;

import java.util.List;
import java.util.ArrayList;

public class Capabilities {

    /**
     * The NETCONF namespace "urn:ietf:params:xml:ns:netconf:base:1.0"
     */
    public static final String NS_NETCONF = "urn:ietf:params:xml:ns:netconf:base:1.0";

    public static final String URN_IETF_PARAMS_XML_NS_NETCONF = "urn:ietf:params:xml:ns:netconf:";
    /**
     * The NETCONF notifications namespace.
     * "urn:ietf:params:xml:ns:netconf:notification:1.0"
     */
    public static final String NS_NOTIFICATION = URN_IETF_PARAMS_XML_NS_NETCONF +
    		"notification:1.0";

    /**
     * The NETCONF partial lock namespace.
     * "urn:ietf:params:xml:ns:netconf:partial-lock:1.0"
     */
    public static final String NS_PARTIAL_LOCK = URN_IETF_PARAMS_XML_NS_NETCONF +
    		"partial-lock:1.0";

    public static final String URN_IETF_PARAMS = "urn:ietf:params:";
    /**
     * String constant for the NETCONF base capability.
     * "urn:ietf:params:netconf:base:1.0".
     */
    public static final String NETCONF_BASE_CAPABILITY = URN_IETF_PARAMS +
    		"netconf:base:1.0";
    /**
     * String constant for the NETCONF base capability 1.1
     */
    public static final String NETCONF_BASE_CAPABILITY_1_1 = URN_IETF_PARAMS +
    		"netconf:base:1.1";

    /**
     * String constant for the <code>:writable-running</code> capability.
     * "urn:ietf:params:netconf:capability:writable-running:1.0".
     */
    public static final String WRITABLE_RUNNING_CAPABILITY = URN_IETF_PARAMS +
    		"netconf:capability:writable-running:1.0";

    public static final String URN_IETF_PARAMS_NETCONF = "urn:ietf:params:netconf:";
    /**
     * String constant for the <code>:candidate</code> capability.
     * "urn:ietf:params:netconf:capability:candidate:1.0".
     */
    public static final String CANDIDATE_CAPABILITY = URN_IETF_PARAMS_NETCONF +
    		"capability:candidate:1.0";

    /**
     * String constant for the <code>:confirmed-commit</code> capability.
     * "urn:ietf:params:netconf:capability:confirmed-commit:1.0".
     */
    public static final String CONFIRMED_COMMIT_CAPABILITY = URN_IETF_PARAMS +
    		"netconf:capability:confirmed-commit:1.0";

    /**
     * String constant for the <code>:rollback-on-error</code> capability.
     * "urn:ietf:params:netconf:capability:rollback-on-error:1.0".
     */
    public static final String ROLLBACK_ON_ERROR_CAPABILITY = URN_IETF_PARAMS +
    		"netconf:capability:rollback-on-error:1.0";

    /**
     * String constant for the <code>:validate</code> capability.
     * "urn:ietf:params:netconf:capability:validate:1.0".
     */
    public static final String VALIDATE_CAPABILITY = URN_IETF_PARAMS_NETCONF +
    		"capability:validate:1.0";

    /**
     * String constant for the <code>:startup</code> capability.
     * "urn:ietf:params:netconf:capability:startup:1.0".
     */
    public static final String STARTUP_CAPABILITY = URN_IETF_PARAMS_NETCONF +
    		"capability:startup:1.0";

    /**
     * String constant for the <code>:url</code> capability.
     * "urn:ietf:params:netconf:capability:url:1.0".
     */
    public static final String URL_CAPABILITY = URN_IETF_PARAMS_NETCONF +
    		"capability:url:1.0";

    public static final String URL_CAPABILITY_SCHEME = URN_IETF_PARAMS_NETCONF +
    		"capability:url:1.0?scheme=";

    /**
     * String constant for the <code>:xpath</code> capability.
     * "urn:ietf:params:netconf:capability:xpath:1.0".
     */
    public static final String XPATH_CAPABILITY = URN_IETF_PARAMS_NETCONF +
    		"capability:xpath:1.0";

    /**
     * String constant for the <code>:notification</code> capability.
     * "urn:ietf:params:netconf:capability:notification:1.0".
     */
    public static final String NOTIFICATION_CAPABILITY = URN_IETF_PARAMS_NETCONF +
    		"capability:notification:1.0";

    /**
     * String constant for the <code>:interleave</code> capability.
     * "urn:ietf:params:netconf:capability:interleave:1.0".
     */
    public static final String INTERLEAVE_CAPABILITY = URN_IETF_PARAMS_NETCONF +
    		"capability:interleave:1.0";

    /**
     * String constant for the <code>:partial-lock</code> capability.
     * "urn:ietf:params:netconf:capability:partial-lock:1.0"
     */
    public static final String PARTIAL_LOCK_CAPABILITY = URN_IETF_PARAMS_NETCONF +
    		"capability:partial-lock:1.0";

    public static final String HTTP_TAIL_F_COM = "http://tail-f.com/";
    /**
     * The NETCONF actions namespace.
     * "http://tail-f.com/ns/netconf/actions/1.0"
     */
    public static final String NS_ACTIONS = HTTP_TAIL_F_COM +
    		"ns/netconf/actions/1.0";

    /**
     * The NETCONF transactions namespace.
     * "http://tail-f.com/ns/netconf/transactions/1.0"
     */
    public static final String NS_TRANSACTIONS = HTTP_TAIL_F_COM +
    		"ns/netconf/transactions/1.0";

    // A set of Tail-f proprietary capabilities
    // that we also check and code for

    /**
     * String constant for the <code>:actions</code> capability.
     * "http://tail-f.com/ns/netconf/actions/1.0".
     */
    public static final String ACTIONS_CAPABILITY = HTTP_TAIL_F_COM +
    		"ns/netconf/actions/1.0";

    /**
     * String constant for the <code>:transactions</code> capability.
     * "http://tail-f.com/ns/netconf/transactions/1.0".
     */
    public static final String TRANSACTIONS_CAPABILITY = HTTP_TAIL_F_COM +
    		"ns/netconf/transactions/1.0";

    /**
     * String constant for the <code>:with-defaults</code> capability.
     * "http://tail-f.com/ns/netconf/with-defaults/1.0".
     */
    public static final String WITH_DEFAULTS_CAPABILITY = HTTP_TAIL_F_COM +
    		"ns/netconf/with-defaults/1.0";

    /**
     * Capabilites
     */
    protected boolean baseCapability;
    protected boolean baseCapability_v1_1;
    protected boolean writableRunningCapability;
    protected boolean candidateCapability;
    protected boolean confirmedCommitCapability;
    protected boolean rollbackOnErrorCapability;
    protected boolean validateCapability;
    protected boolean startupCapability;
    protected boolean notificationCapability;
    protected boolean interleaveCapability;
    protected boolean urlCapability;
    protected boolean xpathCapability;
    protected boolean partialLockCapability;

    protected boolean actionsCapability;
    protected boolean transactionsCapability;
    protected boolean withDefaultsCapability;

    /**
     * Predicate for the <code>:writable-running</code> capability.
     */
    public boolean hasWritableRunning() {
        return writableRunningCapability;
    }

    /**
     * Predicate for the <code>:candidate</code> capability.
     */
    public boolean hasCandidate() {
        return candidateCapability;
    }

    /**
     * Predicate for the <code>:confirmed-commit</code> capability.
     */
    public boolean hasConfirmedCommit() {
        return confirmedCommitCapability;
    }

    /**
     * Predicate for the <code>:rollback-on-error</code> capability.
     */
    public boolean hasRollbackOnError() {
        return rollbackOnErrorCapability;
    }

    /**
     * Predicate for the <code>:validate</code> capability.
     */
    public boolean hasValidate() {
        return validateCapability;
    }

    /**
     * Predicate for the <code>:startup</code> capablity
     */
    public boolean hasStartup() {
        return startupCapability;
    }

    /**
     * Predicate for the <code>:notification</code> capablity
     */
    public boolean hasNotification() {
        return notificationCapability;
    }

    /**
     * Predicate for the <code>:interleave</code> capablity
     */
    public boolean hasInterleave() {
        return interleaveCapability;
    }

    /**
     * Predicate for the <code>:url</code> capability.
     * <p>
     * The <code>:url</code> capability URI must contain a "scheme" argument
     * assigned a comma-separated list of scheme names indicating which schemes
     * the NETCONF peer supports. If the <code>:url</code> capability is set
     * the supported schemas will listed in {@link #urlSchemes}.
     * <p>
     * If the <code>:url</code> capability is supported an url argument may be
     * given to the following methods:
     * <ul>
     * <li>editConfig - the &lt;edit-config&gt; operation can accept an URL as
     * an alternative to the config tree. The URL should identify a local
     * configuration file.
     * <li>copyConfig - the &lt;copy-config&gt; operation can accept an URL as
     * either the source or target parameter.
     * <li>deleteConfig - the &lt;delete-config&gt; operation can accept an
     * URL, then it should identify a local configuration file.
     * <li>validate - the &lt;validate&gt; operation can accept an URL as the
     * source parameter.
     * </ul>
     */
    public boolean hasUrl() {
        return urlCapability;
    }

    /**
     * If the <code>:url</code> capability is set the supported schemes are
     * listed here.
     * <p>
     * For example: In
     * "urn:ietf:params:netconf:capability:url:1.0?scheme=http,ftp,file" the
     * urlSchemes are: "http","ftp","file"
     */
    public String[] urlSchemes() {
        return urlSchemes;
    }

    protected String[] urlSchemes;

    /**
     * Predicate for the <code>:xpath</code> capability.
     */
    public boolean hasXPath() {
        //new Integer(23523421);
        return xpathCapability;
    }

    /**
     * Predicate for the <code>:partial-lock</code> capability.
     */
    public boolean hasPartialLock() {
        return partialLockCapability;
    }

    /**
     * Predicate for the <code>:actions</code> capability.
     */
    public boolean hasActions() {
        return actionsCapability;
    }

    /**
     * Predicate for the <code>:transactions</code> capability.
     */
    public boolean hasTransactions() {
        return transactionsCapability;
    }

    /**
     * Predicate for the <code>:with-defaults</code> capability.
     */
    public boolean hasWithDefaults() {
        return withDefaultsCapability;
    }

    public String[] getUrlSchemes() {
        return urlSchemes;
    }

    private final List<Capa> capas;
    private final List<Capa> data_capas;

    static private class Capa {
        String uri;
        String revision;

        Capa(String uri, String revision) {
            this.uri = uri;
            this.revision = revision;
        }
    }

    protected Capabilities(Element e) throws JNCException {
        final NodeSet caps = e.get("capability");
        capas = new ArrayList<Capa>(caps.size());
        data_capas = new ArrayList<Capa>(caps.size());

        for (int i = 0; i < caps.size(); i++) {
            final Element cap = caps.getElement(i);

            // Do we have a query part
            final String parts[] = cap.value.toString().split("\\?");
            String rev = null;
            final String uri = parts[0];
            if (parts.length == 2) {
                // we have some query part data
                final String q = parts[1];
                final String pairs[] = q.split("&");

                for (final String pair : pairs) {
                    final String kv[] = pair.split("=");
                    if ("revision".equals(kv[0])) {
                        rev = kv[1];
                    }
                }
            }
            capas.add(new Capa(uri, rev));
            if (NETCONF_BASE_CAPABILITY.equals(uri)) {
                baseCapability = true;
            } else if (NETCONF_BASE_CAPABILITY_1_1.equals(uri)) {
            	baseCapability_v1_1 = true;
            } else if (WRITABLE_RUNNING_CAPABILITY.equals(uri)) {
                writableRunningCapability = true;
            } else if (CANDIDATE_CAPABILITY.equals(uri)) {
                candidateCapability = true;
            } else if (CONFIRMED_COMMIT_CAPABILITY.equals(uri)) {
                confirmedCommitCapability = true;
            } else if (ROLLBACK_ON_ERROR_CAPABILITY.equals(uri)) {
                rollbackOnErrorCapability = true;
            } else if (VALIDATE_CAPABILITY.equals(uri)) {
                validateCapability = true;
            } else if (NOTIFICATION_CAPABILITY.equals(uri)) {
                notificationCapability = true;
            } else if (INTERLEAVE_CAPABILITY.equals(uri)) {
                interleaveCapability = true;
            } else if (STARTUP_CAPABILITY.equals(uri)) {
                startupCapability = true;
            } else if (cap.value.toString().startsWith(URL_CAPABILITY_SCHEME)) {
                urlCapability = true;
                final String schemes = cap.value.toString().substring(
                        URL_CAPABILITY_SCHEME.length());
                urlSchemes = schemes.split(",");
            } else if (XPATH_CAPABILITY.equals(uri)) {
                xpathCapability = true;
            } else if (PARTIAL_LOCK_CAPABILITY.equals(uri)) {
                partialLockCapability = true;
            } else if ("urn:ietf:params:xml:ns:netconf:base:1.0".equals(uri)) {
                baseCapability = true;
            } else if (uri.equals(URN_IETF_PARAMS_XML_NS_NETCONF +
            		"capability:candidate:1.0")) {
                candidateCapability = true;
            } else if (uri.equals(URN_IETF_PARAMS_XML_NS_NETCONF +
            		"capability:confirmed-commit:1.0")) {
                confirmedCommitCapability = true;
            } else if (uri.equals(URN_IETF_PARAMS_XML_NS_NETCONF +
            		"capability:validate:1.0")) {
                validateCapability = true;
            } else if (uri.equals(URN_IETF_PARAMS_XML_NS_NETCONF +
            		"capability:url:1.0")) {
                urlCapability = true;
                if (parts[1] != null) {
                    urlSchemes = parts[1].split(",");
                }
            }

            // tail-f proprietary capas
            else if (WITH_DEFAULTS_CAPABILITY.equals(uri)) {
                withDefaultsCapability = true;
            } else if (ACTIONS_CAPABILITY.equals(uri)) {
                actionsCapability = true;
            } else if (TRANSACTIONS_CAPABILITY.equals(uri)) {
                transactionsCapability = true;
            } else {
                // It's either a proper data schema capability or some
                // homegrown agent capability
                data_capas.add(new Capa(uri, rev));
            }
        }
    }

    /** Checks all capabilities including the rfc 4711 ones */
    public boolean hasCapability(String uri) {
        for (final Capa c : capas) {
            if (c.uri.equals(uri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the revision for a specific uri, return null if no revision
     * found. Only check the user data capabilities.
     */
    public String getRevision(String uri) {
        for (Capa capa : data_capas) {
            if (capa.uri.equals(uri)) {
                return capa.revision;
            }
        }
        return null;
    }

}
