/*    -*- Java -*-
 *
 *  Copyright 2009 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.inm;

import java.util.ArrayList;

public class Capabilities {

    /**
     * The NETCONF namespace "urn:ietf:params:xml:ns:netconf:base:1.0"
     */
    public static String NS_NETCONF = "urn:ietf:params:xml:ns:netconf:base:1.0";

    /**
     * The NETCONF notifications namespace.
     * "urn:ietf:params:xml:ns:netconf:notification:1.0"
     */
    public static String NS_NOTIFICATION = "urn:ietf:params:xml:ns:netconf:notification:1.0";

    /**
     * The NETCONF partial lock namespace.
     * "urn:ietf:params:xml:ns:netconf:partial-lock:1.0"
     */
    public static String NS_PARTIAL_LOCK = "urn:ietf:params:xml:ns:netconf:partial-lock:1.0";

    /**
     * String constant for the NETCONF base capability.
     * "urn:ietf:params:netconf:base:1.0".
     */
    public static String NETCONF_BASE_CAPABILITY = "urn:ietf:params:netconf:base:1.0";

    /**
     * String constant for the <code>:writable-running</code> capability.
     * "urn:ietf:params:netconf:capability:writable-running:1.0".
     */
    public static String WRITABLE_RUNNING_CAPABILITY = "urn:ietf:params:netconf:capability:writable-running:1.0";

    /**
     * String constant for the <code>:candidate</code> capability.
     * "urn:ietf:params:netconf:capability:candidate:1.0".
     */
    public static String CANDIDATE_CAPABILITY = "urn:ietf:params:netconf:capability:candidate:1.0";

    /**
     * String constant for the <code>:confirmed-commit</code> capability.
     * "urn:ietf:params:netconf:capability:confirmed-commit:1.0".
     */
    public static String CONFIRMED_COMMIT_CAPABILITY = "urn:ietf:params:netconf:capability:confirmed-commit:1.0";

    /**
     * String constant for the <code>:rollback-on-error</code> capability.
     * "urn:ietf:params:netconf:capability:rollback-on-error:1.0".
     */
    public static String ROLLBACK_ON_ERROR_CAPABILITY = "urn:ietf:params:netconf:capability:rollback-on-error:1.0";

    /**
     * String constant for the <code>:validate</code> capability.
     * "urn:ietf:params:netconf:capability:validate:1.0".
     */
    public static String VALIDATE_CAPABILITY = "urn:ietf:params:netconf:capability:validate:1.0";

    /**
     * String constant for the <code>:startup</code> capability.
     * "urn:ietf:params:netconf:capability:startup:1.0".
     */
    public static String STARTUP_CAPABILITY = "urn:ietf:params:netconf:capability:startup:1.0";

    /**
     * String constant for the <code>:url</code> capability.
     * "urn:ietf:params:netconf:capability:url:1.0".
     */
    public static String URL_CAPABILITY = "urn:ietf:params:netconf:capability:url:1.0";

    public static String URL_CAPABILITY_SCHEME = "urn:ietf:params:netconf:capability:url:1.0?scheme=";

    /**
     * String constant for the <code>:xpath</code> capability.
     * "urn:ietf:params:netconf:capability:xpath:1.0".
     */
    public static String XPATH_CAPABILITY = "urn:ietf:params:netconf:capability:xpath:1.0";

    /**
     * String constant for the <code>:notification</code> capability.
     * "urn:ietf:params:netconf:capability:notification:1.0".
     */
    public static String NOTIFICATION_CAPABILITY = "urn:ietf:params:netconf:capability:notification:1.0";

    /**
     * String constant for the <code>:interleave</code> capability.
     * "urn:ietf:params:netconf:capability:interleave:1.0".
     */
    public static String INTERLEAVE_CAPABILITY = "urn:ietf:params:netconf:capability:interleave:1.0";

    /**
     * String constant for the <code>:partial-lock</code> capability.
     * "urn:ietf:params:netconf:capability:partial-lock:1.0"
     */
    public static String PARTIAL_LOCK_CAPABILITY = "urn:ietf:params:netconf:capability:partial-lock:1.0";

    /**
     * The NETCONF actions namespace. "http://tail-f.com/ns/netconf/actions/1.0"
     */
    public static final String NS_ACTIONS = "http://tail-f.com/ns/netconf/actions/1.0";

    /**
     * The NETCONF transactions namespace.
     * "http://tail-f.com/ns/netconf/transactions/1.0"
     */
    public static final String NS_TRANSACTIONS = "http://tail-f.com/ns/netconf/transactions/1.0";

    // A set of Tail-f proprietary capabilities
    // that we also check and code for

    /**
     * String constant for the <code>:actions</code> capability.
     * "http://tail-f.com/ns/netconf/actions/1.0".
     */
    public static final String ACTIONS_CAPABILITY = "http://tail-f.com/ns/netconf/actions/1.0";

    /**
     * String constant for the <code>:transactions</code> capability.
     * "http://tail-f.com/ns/netconf/transactions/1.0".
     */
    public static final String TRANSACTIONS_CAPABILITY = "http://tail-f.com/ns/netconf/transactions/1.0";

    /**
     * String constant for the <code>:with-defaults</code> capability.
     * "http://tail-f.com/ns/netconf/with-defaults/1.0".
     */
    public static final String WITH_DEFAULTS_CAPABILITY = "http://tail-f.com/ns/netconf/with-defaults/1.0";

    /**
     * Capabilites
     */
    protected boolean baseCapability = false;
    protected boolean writableRunningCapability = false;
    protected boolean candidateCapability = false;
    protected boolean confirmedCommitCapability = false;
    protected boolean rollbackOnErrorCapability = false;
    protected boolean validateCapability = false;
    protected boolean startupCapability = false;
    protected boolean notificationCapability = false;
    protected boolean interleaveCapability = false;
    protected boolean urlCapability = false;
    protected boolean xpathCapability = false;
    protected boolean partialLockCapability = false;

    protected boolean actionsCapability = false;
    protected boolean transactionsCapability = false;
    protected boolean withDefaultsCapability = false;

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
     * the NETCONF peer supports. If the <code>:url</code> capability is set the
     * supported schemas will listed in {@link #urlSchemes}.
     * <p>
     * If the <code>:url</code> capability is supported an url argument may be
     * given to the follwing methods:
     * <ul>
     * <li> {@link #editConfig(int,String)} - the <edit-config> operation can
     * accept an URL as an alternative to the config tree. The URL should
     * identify a local configuration file.
     * <li> {@link #copyConfig(String,String)} - the <copy-config> operation can
     * accept an URL as either the source or target parameter.
     * <li> {@link #deleteConfig(String)} - the <delete-config> operation can
     * accept an URL, then it should identify a local configuration file.
     * <li> {@link #validate(String)} - the <validate> operation can accept an
     * URL as the source parameter.
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

    private ArrayList capas;
    private ArrayList data_capas;

    static private class Capa {
        String uri;
        String revision;

        Capa(String uri, String revision) {
            this.uri = uri;
            this.revision = revision;
        }
    }

    protected Capabilities(Element e) throws INMException {
        NodeSet caps = e.get("capability");
        capas = new ArrayList(caps.size());
        data_capas = new ArrayList(caps.size());

        for (int i = 0; i < caps.size(); i++) {
            Element cap = caps.getElement(i);

            // Do we have a query part
            String parts[] = cap.value.toString().split("\\?");
            String rev = null;
            String uri = parts[0];
            if (parts.length == 2) {
                // we have a some query part data

                String q = parts[1];
                String pairs[] = q.split("&");

                for (int j = 0; j < pairs.length; j++) {
                    String pair = pairs[j];
                    String kv[] = pair.split("=");
                    if (kv[0].equals("revision"))
                        rev = kv[1];
                }
            }
            Capa c = new Capa(uri, rev);
            capas.add(c);
            if (uri.equals(NETCONF_BASE_CAPABILITY))
                baseCapability = true;
            else if (uri.equals(WRITABLE_RUNNING_CAPABILITY))
                writableRunningCapability = true;
            else if (uri.equals(CANDIDATE_CAPABILITY))
                candidateCapability = true;
            else if (uri.equals(CONFIRMED_COMMIT_CAPABILITY))
                confirmedCommitCapability = true;
            else if (uri.equals(ROLLBACK_ON_ERROR_CAPABILITY))
                rollbackOnErrorCapability = true;
            else if (uri.equals(VALIDATE_CAPABILITY))
                validateCapability = true;
            else if (uri.equals(NOTIFICATION_CAPABILITY))
                notificationCapability = true;
            else if (uri.equals(INTERLEAVE_CAPABILITY))
                interleaveCapability = true;
            else if (uri.equals(STARTUP_CAPABILITY))
                startupCapability = true;
            else if (cap.value.toString().startsWith(URL_CAPABILITY_SCHEME)) {
                urlCapability = true;
                String schemes = cap.value.toString().substring(
                        URL_CAPABILITY_SCHEME.length());
                urlSchemes = schemes.split(",");
            } else if (uri.equals(XPATH_CAPABILITY))
                xpathCapability = true;
            else if (uri.equals(PARTIAL_LOCK_CAPABILITY))
                partialLockCapability = true;

            /**
             * JUNIPER has an erroneous naming of the capabilities let's accept
             * their capabilities also.
             */
            else if (uri.equals("urn:ietf:params:xml:ns:netconf:base:1.0"))
                baseCapability = true;
            else if (uri
                    .equals("urn:ietf:params:xml:ns:netconf:capability:candidate:1.0"))
                candidateCapability = true;
            else if (uri
                    .equals("urn:ietf:params:xml:ns:netconf:capability:confirmed-commit:1.0"))
                confirmedCommitCapability = true;
            else if (uri
                    .equals("urn:ietf:params:xml:ns:netconf:capability:validate:1.0"))
                validateCapability = true;
            else if (uri
                    .equals("urn:ietf:params:xml:ns:netconf:capability:url:1.0")) {
                urlCapability = true;
                if (parts[1] != null)
                    urlSchemes = parts[1].split(",");
            }

            // / tail-f proprietary capas
            else if (uri.equals(WITH_DEFAULTS_CAPABILITY))
                withDefaultsCapability = true;
            else if (uri.equals(ACTIONS_CAPABILITY))
                actionsCapability = true;
            else if (uri.equals(TRANSACTIONS_CAPABILITY))
                transactionsCapability = true;

            else {
                // It's either a proper data schema capability or some
                // homegrown agent capability
                data_capas.add(new Capa(uri, rev));

            }

        }
    }

    /* Check all capabilities including the rfc 4711 ones */
    public boolean hasCapability(String uri) {
        for (int i = 0; i < capas.size(); i++) {
            Capa c = (Capa) capas.get(i);
            if (c.uri.equals(uri))
                return true;
        }
        return false;
    }

    /*
     * Return the revision for a specific uri, return null if no revision found.
     * Only check the user data capabilities.
     */

    public String getRevision(String uri) {
        for (int i = 0; i < data_capas.size(); i++) {
            if (((Capa) data_capas.get(i)).uri.equals(uri)) {
                return ((Capa) data_capas.get(i)).revision;
            }
        }
        return null;
    }

}
