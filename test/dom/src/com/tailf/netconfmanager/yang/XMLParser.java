/**
 *  Copyright 2012 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */
package com.tailf.netconfmanager.yang;

import org.xml.sax.*;

import com.tailf.netconfmanager.*;

/**
 * A SAX parser, for parsing for example NETCONF messages, into a simple
 * {@link Element Element} tree.
 * <p>
 * This parser is data model aware and will try to construct classes that are
 * generated with the ConfM compiler.
 * <p>
 */
public class XMLParser extends com.tailf.netconfmanager.XMLParser {

    /**
     * Constructor. Initializes the parser instance.
     */
    public XMLParser() throws NetconfException {
        super();
    }

    /**
     * Read in an XML file and parse it and return an element tree.
     */
    public Container readFile(String filename) throws NetconfException {
        try {
            ConfHandler handler = new ConfHandler();
            parser.setContentHandler(handler);
            parser.parse(filename);
            return (Container) handler.top;
        } catch (Exception e) {
            throw new NetconfException(NetconfException.PARSER_ERROR, "parse file: "
                    + filename + " error: " + e);
        }
    }

    /**
     * Parses an XML string returning a configuration tree from it.
     * 
     * @param is
     *            Inputsource (byte stream) where the XML text is read from
     */
    public Element parse(InputSource is) throws NetconfException {
        try {
            ConfHandler handler = new ConfHandler();
            parser.setContentHandler(handler);
            parser.parse(is);
            return handler.top;
        } catch (Exception e) {
            e.printStackTrace();
            throw new NetconfException(NetconfException.PARSER_ERROR, "parse error: "
                    + e);

        }
    }

}
