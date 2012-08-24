/**
 *  Copyright 2012 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */
package com.tailf.jnc;

import org.xml.sax.InputSource;

/**
 * A SAX parser, for parsing for example NETCONF messages, into a simple
 * {@link Element Element} tree.
 * <p>
 * This parser is data model aware and will try to construct classes that are
 * generated with the ConfM compiler.
 * <p>
 */
public class YangXMLParser extends com.tailf.jnc.XMLParser {

    /**
     * Constructor. Initializes the parser instance.
     */
    public YangXMLParser() throws JNCException {
        super();
    }

    /**
     * Read in an XML file and parse it and return an element tree.
     */
    @Override
    public YangElement readFile(String filename) throws JNCException {
        try {
            final ElementHandler handler = new ElementHandler();
            parser.setContentHandler(handler);
            parser.parse(filename);
            return (YangElement) handler.top;
        } catch (final Exception e) {
            throw new JNCException(JNCException.PARSER_ERROR, "parse file: "
                    + filename + " error: " + e);
        }
    }

    /**
     * Parses an XML string returning a configuration tree from it.
     * 
     * @param is Inputsource (byte stream) where the XML text is read from
     */
    @Override
    public Element parse(InputSource is) throws JNCException {
        try {
            final ElementHandler handler = new ElementHandler();
            parser.setContentHandler(handler);
            parser.parse(is);
            return handler.top;
        } catch (final Exception e) {
            e.printStackTrace();
            throw new JNCException(JNCException.PARSER_ERROR, "parse error: "
                    + e);

        }
    }

}
