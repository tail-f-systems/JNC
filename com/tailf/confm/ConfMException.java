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

package com.tailf.confm;
import com.tailf.inm.*;

/**
 * This is the class of exceptions that are thrown
 * from the ConfM classes.
 *
 *
 */
public class ConfMException extends INMException {
    public ConfMException() {
        super(NOT_SET, null);
    }

    public ConfMException(int errorCode) {
        super(errorCode, null);
    }

    public ConfMException(int errorCode, Object opaqueData) {
        super(errorCode, opaqueData);
    }

    public static final int ELEMENT_ALREADY_IN_USE = -1024;
    public static final int ELEMENT_MISSING = -1025;
    public static final int BAD_VALUE = -1026;
    public static final int BAD_USER = -1027;
    public static final int NOT_COMMON_ROOT_ELEMENT = -1028;
    public static final int DEVICE_CONNECT = -1029;
    public static final int DEVICE_AUTH = -1030;
    public static final int NOT_CONTAINER = -1031;
    public static final int BAD_SESSION_NAME = -1032;

    public String toString() {
        switch (errorCode) {
        case ELEMENT_ALREADY_IN_USE:
            return "Element has already been used: "+((Element)opaqueData).name;
        case ELEMENT_MISSING:
            return "Element does not exist: "+opaqueData;
        case BAD_VALUE:
            return "Bad value: "+opaqueData;
        case NOT_CONTAINER:
            return "Element is not a container. (Namespace not "+
                "enabled in XML parser?) "+opaqueData;
        case NOT_COMMON_ROOT_ELEMENT:
            return "Elements does not share common root: "+opaqueData;
        case BAD_SESSION_NAME:
            return "Session name doesn't exist (or already exists)";
        default:
            return super.toString();
        }
    }
}
