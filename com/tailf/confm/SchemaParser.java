/**
 *  Copyright 2010 Tail-F Systems AB. All rights reserved.
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
import java.net.*;
import com.tailf.inm.*;

/**
 * A simple SAX parser, for parsing ConfD schema files with
 * the following syntax:
 *
 * <schema>
 *   <node>
 *     <tagapth>string</tagpath>
 *     <namespace>string</namespace>
 *     <primitive_type>string</primitive_type>
 *     <min_occurs>int</min_occurs>
 *     <max_occurs>int</max_occurs>
 *     <children>space-separated strings</children>
 *     <flags>integer</flags>
 *     <desc>string</desc>
 *     <rev>
 *       <info>
 *         <type>7</type>
 *         <idata>4711</idata>
 *         <data>foo</data>
 *         <introduced>2007-10-11</introduced>
 *       </info>
 *     </rev>
 *   </node>
 * <schema>
 *
 * into a hashtable with {@link CsNode} elements.
 *
 */
public class SchemaParser {
    protected XMLReader parser;

    public SchemaParser() throws INMException {
        try {
            String javaVersion = System.getProperty("java.version");

            if (javaVersion.startsWith("1.4"))
                parser =
                    XMLReaderFactory.createXMLReader(
                        "org.apache.crimson.parser.XMLReaderImpl");
            else
                parser = XMLReaderFactory.createXMLReader();
        } catch (Exception e) {
            System.exit(-1);
            throw new INMException(INMException.PARSER_ERROR,
                                   "failed to initialize parser: "+e);
        }
    }

    private class SchemaHandler extends DefaultHandler {
        protected Hashtable h;
        protected CsNode node;
        protected RevisionInfo ri;
        protected ArrayList riArrayList;
        protected String value = null;

        SchemaHandler(Hashtable h2) {
            super();
            h = h2;
        }

        public void startElement(String uri, String localName,
                                 String qName, Attributes attributes)
          throws SAXException {
            if (localName.equals("node")) {
                node = new CsNode();
                value = null;
            } else if (localName.equals("rev")) {
                riArrayList = new ArrayList();
                value = null;
            } else if (localName.equals("info")) {
                ri = new RevisionInfo();
                value = null;
            } else if (localName.equals("schema") || localName.equals("node"))
                value = null;
            else
                value = new String();
        }

        public void endElement(String uri, String localName, String qName) {
            if (localName.equals("node")) {
                h.put(node.tagpath, node);
            } else if (localName.equals("tagpath")) {
                String[] splittedTagpath = value.split("/");

                if (splittedTagpath.length == 0)
                    node.tagpath = new Tagpath(0);
                else {
                    node.tagpath = new Tagpath(splittedTagpath.length-1);

                    for (int i = 1; i < splittedTagpath.length; i++)
                        node.tagpath.p[i-1] = new String(splittedTagpath[i]);
                }
            } else if (localName.equals("namespace")) {
                node.namespace = new String(value);
            } else if (localName.equals("primitive_type")) {
                node.primitive_type = Integer.parseInt(value);
            } else if (localName.equals("min_occurs")) {
                node.min_occurs = Integer.parseInt(value);
            } else if (localName.equals("max_occurs")) {
                node.max_occurs = Integer.parseInt(value);
            } else if (localName.equals("children")) {
                String[] child = value.split(" ");
                if (child.length == 0)
                    node.children = null;
                else {
                    node.children = new String[child.length];
                    for (int i = 0; i < child.length; i++)
                        node.children[i] = new String(child[i]);
                }
            } else if (localName.equals("flags")) {
                node.flags = Integer.parseInt(value);
            } else if (localName.equals("desc")) {
                node.desc = new String(value);
            } else if (localName.equals("type")) {
                ri.type = Integer.parseInt(value);
            } else if (localName.equals("idata")) {
                ri.idata = Integer.parseInt(value);
            } else if (localName.equals("data")) {
                ri.data = new String(value);
            } else if (localName.equals("introduced")) {
                ri.introduced = new String(value);
            } else if (localName.equals("info")) {
                riArrayList.add(ri);
            } else if (localName.equals("rev")) {
                RevisionInfo[] riArray =
                    (RevisionInfo[])(riArrayList.toArray(new RevisionInfo[riArrayList.size()]));
                node.revInfo = riArray;
            }

            value = null;
        }

        public void characters(char[] ch, int start, int length) {
            if (value == null)
                return;
            value += new String(ch, start, length);
        }
    }

    /**
     * Read in an XML file and parse it and return a hashtable
     * with CsNode objects.
     */
    public void readFile(String filename, Hashtable h) throws INMException {
        try {
            SchemaHandler handler = new SchemaHandler(h);
            parser.setContentHandler(handler);
            parser.parse(filename);
        } catch (Exception e) {
            e.printStackTrace();
            throw new INMException(INMException.PARSER_ERROR,
                                   "parse file: "+filename+" error: "+e);
        }
    }

    public void readFile(URL schemaUrl, Hashtable h) throws INMException {
        try {
            SchemaHandler handler = new SchemaHandler(h);
            parser.setContentHandler(handler);
            parser.parse(new InputSource(schemaUrl.openStream()));
        } catch (Exception e) {
            throw new INMException(INMException.PARSER_ERROR,
                                   "parse file: "+schemaUrl+" error: "+e);
        }
    }
}
