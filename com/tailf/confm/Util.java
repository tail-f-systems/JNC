/*    -*- Java -*-
 *
 *  Copyright 2008 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.confm;

import java.net.Socket;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Util {

    public static String makeMask (int n) {
        int i;

        n = 32 - n;
        int iVal = ((1 << n) - 1);

        int[] arr = new int[4];
        arr[0] = ((iVal >> 24) & 0xff);
        arr[1] = ((iVal >> 16) & 0xff);
        arr[2] = ((iVal >> 8)  & 0xff);
        arr[3] = ((iVal >> 0)  & 0xff);

        for (int j=0; j<4; j++)
            arr[j] = (~(arr[j])) & 0xff;

        String ret = new String();
        for (int k=0; k<4; k++) {
            ret += arr[k];
            if ( k!= 3)
                ret += ".";
        }
        return ret;
    }


    public static String[] parseTokens(String source, char delimiter) {

        int numtoken = 1;

        for (int i = 0; i < source.length(); i++) {
            if (source.charAt(i) == delimiter)
                numtoken++;
        }

        String list[] = new String[numtoken];
        int nextfield = 0;

        for (int i = 0; i < numtoken; i++) {
            if (nextfield >= source.length()) {
                list[i] = "";
            }
            else {
                int idx = source.indexOf(delimiter, nextfield);
                if (idx == -1)
                    idx = source.length();
                list[i] = source.substring(nextfield, idx);
                nextfield = idx + 1;
            }
        }
        return list;
    }


    public static InetAddress parseIPv4Address(String host)
        throws UnknownHostException {
        if (host == null)
            return null;
        String[] parts = parseTokens(host, '.');
        if ((parts == null) || (parts.length != 4))
            return null;
        byte[] addr = new byte[4];

        for (int i = 0; i < 4; i++) {
            int part = 0;
            if ((parts[i].length() == 0) || (parts[i].length() > 3))
                return null;
            for (int k = 0; k < parts[i].length(); k++) {
                char c = parts[i].charAt(k);
                if ((c < '0') || (c > '9'))
                    return null;
                part = part * 10 + (c - '0');
            }
            if (part > 255)
                return null;
            addr[i] = (byte) part;
        }
        return InetAddress.getByAddress(host, addr);
    }
}
