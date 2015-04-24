package com.tailf.jnc;

public class RevisionInfo {

    public static final int R_ENUM_ADDED = 1;
    public static final int R_BITS_ADDED = 2;
    public static final int R_RANGE_EXPANDED = 3;
    public static final int R_DEFAULT_ADDED = 4;
    public static final int R_MANDATORY_TRUE_TO_FALSE = 5;
    public static final int R_MIN_ELEM_LOWERED = 6;
    public static final int R_MAX_ELEM_RAISED = 7;
    public static final int R_NODE_ADDED = 8;
    public static final int R_CASE_ADDED = 9;
    public static final int R_STATE_TO_CONFIG = 10;

    public int type; // R_*
    public int idata;
    public String data;
    public String introduced; // YYYY-MM-DD

    protected static boolean olderRevisionSupportEnabled = true;
    protected static boolean newerRevisionSupportEnabled = true;

    /**
     * Enables revision support for newer data. This means that if the device
     * sends us data where we don't understand the data because the device has
     * a newer revision of the YANG model than we have, we accept that data
     * anyway, represented as Element objects (not YangElement).
     */
    public static void enableOlderRevisionSupport() {
        olderRevisionSupportEnabled = true;
    }

    public static void disableOlderRevisionSupport() {
        olderRevisionSupportEnabled = false;
    }

    /**
     * Enables revision support for older schema at the device. Enabling this
     * feature means that we can safely talk NETCONF to devices with an older
     * revision of the YANG data model than we have. This means that we during
     * the encode() process checks each element and makes sure that the device
     * understands it. If not, the element is skipped.
     */
    public static void enableNewerRevisionSupport() {
        newerRevisionSupportEnabled = true;
    }

    public static void disableNewerRevisionSupport() {
        newerRevisionSupportEnabled = false;
    }

    public RevisionInfo() {
    }

    public RevisionInfo(int type, String data, String introduced) {
        this.type = type;
        this.data = data;
        this.introduced = introduced;
    }
}
