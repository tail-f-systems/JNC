/**   
 *  Copyright 2007 Tail-F Systems AB. All rights reserved. 
 *
 *  This software is the confidential and proprietary 
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */
package com.tailf.confm;

import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.util.*;
import com.tailf.inm.*;

/**
 * A SAX parser, for parsing for example NETCONF messages,
 * into a simple {@link Element Element} tree.
 * <p>
 * This parser is data model aware and will try to construct
 * classes that are generated with the ConfM compiler.
 * <p>
 */
public class XMLParser extends com.tailf.inm.XMLParser {

    /** 
     * Constructor.
     * Initializes the parser instance.
     */
    public XMLParser() throws INMException {
	super();
    }
    


    /**
     * Read in an XML file and parse it and return an
     * element tree.
     */
    public Element readFile(String filename) throws INMException {
	try {
	    ConfHandler handler = new ConfHandler();
	    parser.setContentHandler( handler );
	    parser.parse(filename);
	    return (Container) handler.top;
	} catch (Exception e) {
	    throw new INMException(
                INMException.PARSER_ERROR,"parse file: "+filename+" error: "+e);
	}
    }

    
    /**
     * Parses an XML string returning a configuration tree from it.
     * @param is Inputsource (byte stream) where the XML text is read from
     */
    public Element parse(InputSource is) throws INMException {
	try {
	    ConfHandler handler = new ConfHandler();
	    parser.setContentHandler( handler );
	    parser.parse(is);
	    return handler.top;
	} catch (Exception e) {
            e.printStackTrace();
	    throw new INMException(INMException.PARSER_ERROR,"parse error: "+e);

	}
    }


}
