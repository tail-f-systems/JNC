package com.tailf.jnc;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * A simple SAX parser, for parsing schema files with the following syntax:
 * <pre>
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
 * </pre>
 * into a hashtable with {@link SchemaNode} elements.
 */
public class SchemaParser {
    protected XMLReader parser;

    public SchemaParser() throws JNCException {
        try {
            final String javaVersion = System.getProperty("java.version");

            if (javaVersion.startsWith("1.4")) {
                parser = XMLReaderFactory
                        .createXMLReader("org.apache.crimson.parser.XMLReaderImpl");
            } else {
                parser = XMLReaderFactory.createXMLReader();
            }
        } catch (final Exception e) {
            System.exit(-1);
            throw new JNCException(JNCException.PARSER_ERROR,
                    "failed to initialize parser: " + e);
        }
    }

    private class SchemaHandler extends DefaultHandler {
        protected HashMap<Tagpath, SchemaNode> h;
        protected SchemaNode node;
        protected RevisionInfo ri;
        protected ArrayList<RevisionInfo> riArrayList;
        protected String value = null;

        SchemaHandler(HashMap<Tagpath, SchemaNode> h2) {
            super();
            h = h2;
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            if (localName.equals("node")) {
                node = new SchemaNode();
                value = null;
            } else if (localName.equals("rev")) {
                riArrayList = new ArrayList<RevisionInfo>();
                value = null;
            } else if (localName.equals("info")) {
                ri = new RevisionInfo();
                value = null;
            } else if (localName.equals("schema") || localName.equals("node")) {
                value = null;
            } else {
                value = new String();
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (localName.equals("node")) {
                h.put(node.tagpath, node);
            } else if (localName.equals("tagpath")) {
                final String[] splittedTagpath = value.split("/");

                if (splittedTagpath.length == 0) {
                    node.tagpath = new Tagpath(0);
                } else {
                    node.tagpath = new Tagpath(splittedTagpath.length - 1);
                    for (int i = 1; i < splittedTagpath.length; i++) {
                        node.tagpath.p[i - 1] = new String(splittedTagpath[i]);
                    }
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
                final String[] child = value.split(" ");
                if (child.length == 0) {
                    node.children = null;
                } else {
                    node.children = new String[child.length];
                    for (int i = 0; i < child.length; i++) {
                        node.children[i] = new String(child[i]);
                    }
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
                final RevisionInfo[] riArray = new RevisionInfo[riArrayList
                        .size()];
                node.revInfo = riArrayList.toArray(riArray);
            }

            value = null;
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (value == null) {
                return;
            }
            value += new String(ch, start, length);
        }
    }

    /**
     * Read in and parse an XML file, and populate a hashtable with SchemaNode
     * objects.
     *
     * @param filename name of file containing the schema
     * @param h The hashtable to populate.
     * @throws JNCException If there is an IO or SAX parse problem.
     */
    public void readFile(String filename, HashMap<Tagpath, SchemaNode> h)
            throws JNCException {
        readFile(new InputSource(filename), h);
    }

    /**
     * Read in and parse an XML file, and populate a hashtable with SchemaNode
     * objects.
     *
     * @param schemaUrl URL of the schema to parse
     * @param h The hashtable to populate.
     * @throws JNCException If there is an IO or SAX parse problem.
     */
    public void readFile(URL schemaUrl, HashMap<Tagpath, SchemaNode> h)
            throws JNCException {
        try {
            readFile(new InputSource(schemaUrl.openStream()), h);
        } catch (IOException e) {
            e.printStackTrace();
            throw new JNCException(JNCException.PARSER_ERROR, "Unable to open" +
            		" file: " + schemaUrl + ": " + e);
        }
    }

    private void readFile(InputSource inputSource,
            HashMap<Tagpath, SchemaNode> h) throws JNCException {
        try {
            final SchemaHandler handler = new SchemaHandler(h);
            parser.setContentHandler(handler);
            parser.parse(inputSource);
        } catch (final Exception e) {
            e.printStackTrace();
            throw new JNCException(JNCException.PARSER_ERROR, "parse file: "
                    + inputSource + " error: " + e);
        }
    }
}
