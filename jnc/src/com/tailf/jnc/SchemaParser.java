package com.tailf.jnc;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        protected Map<Tagpath, SchemaNode> h;
        protected SchemaNode node;
        protected RevisionInfo ri;
        protected List<RevisionInfo> riArrayList;
        protected String value = null;

        SchemaHandler(Map<Tagpath, SchemaNode> h2) {
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
                value = "";
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
                        node.tagpath.p[i - 1] = splittedTagpath[i];
                    }
                }
            } else if (localName.equals("namespace")) {
                node.namespace = value;
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
                node.desc = value;
            } else if (localName.equals("type")) {
                ri.type = Integer.parseInt(value);
            } else if (localName.equals("idata")) {
                ri.idata = Integer.parseInt(value);
            } else if (localName.equals("data")) {
                ri.data = value;
            } else if (localName.equals("introduced")) {
                ri.introduced = value;
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
    public void readFile(String filename, Map<Tagpath, SchemaNode> h)
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
    public void readFile(URL schemaUrl, Map<Tagpath, SchemaNode> h)
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
            Map<Tagpath, SchemaNode> h) throws JNCException {
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

    /**
     * Scans the classpath for the XML schema file and populates the hashtable with 
     * SchemaNode objects. Class is passed in so that in the case of multiple {@link ClassLoader}s 
     * the correct one can be used to locate the schema.
     * 
     * @param filename
     * @param h
     * @param clazz
     * @throws JNCException if the file is not found or cannot be parsed.
     */
    public void findAndReadFile(final String filename, final Map<Tagpath, SchemaNode> h, final Class clazz)
            throws JNCException {
        final URL url = clazz.getResource(filename);
        if (url == null){
            throw new JNCException(JNCException.PARSER_ERROR, "Cannot find file: " + filename + " on the classpath.");
        }
        readFile(url, h);
    }
}
