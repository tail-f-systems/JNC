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

import com.tailf.netconfmanager.*;

/**
 * A SAX parser, for parsing for example NETCONF messages,
 * into a simple {@link Element Element} tree.
 * <p>
 * This parser is data model aware and will try to construct
 * classes that are generated with the ConfM compiler.
 * <p>
 */

/**
 * The handler with hooks for startElement etc. The SAX parser will build up the
 * parse tree, by calling these hooks.
 */
class ConfHandler extends DefaultHandler {

    // pointer to current element (node)
    public Element current;
    public Element top;
    public PrefixMap prefixes = null;
    boolean leaf = false;
    String leafNs;
    String leafName;
    String leafValue;
    int unknownLevel = 0;

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {

        if (unknownLevel > 0) {
            unkownStartElement(uri, localName, qName, attributes);
            return;
        }

        try {
            Element parent = current;
            Element child = null;

            child = Container.createInstance(this, parent, uri, localName);
            if (top == null)
                top = child;

            if (child == null && unknownLevel == 1) {
                // we're entering XML data that's not
                // in the schema
                unkownStartElement(uri, localName, qName, attributes);
                return;
            }

            if (child == null) {
                // it's a known leaf
                // it'll be handled in the endElement method
                leaf = true;
                leafNs = uri;
                leafName = localName;
                leafValue = new String();
                return;
            }
            child.prefixes = prefixes;
            prefixes = null;
            addOtherAttributes(attributes, child);
            current = child; // step down
        } catch (NetconfException e) {
            e.printStackTrace();
            throw new SAXException(e.toString());
        }
    }

    private void unkownStartElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        Element parent = current;
        Element child = new Element(uri, localName);
        child.prefixes = prefixes;
        prefixes = null;
        addOtherAttributes(attributes, child);
        if (current == null) {
            top = child;
        } else {
            current.addChild(child);
        }
        current = child; // step down
    }

    private void addOtherAttributes(Attributes attributes, Element child) {
        // add other attributes
        for (int i = 0; i < attributes.getLength(); i++) {
            String attrName = attributes.getLocalName(i);
            // String attrType= attributes.getType(i);
            String attrUri = attributes.getURI(i);
            String attrValue = attributes.getValue(i);
            Attribute attr = new Attribute(attrUri, attrName, attrValue);
            child.addAttr(attr);
        }
    }

    private void unknownEndElement(String uri, String localName, String qName) {
        // check that we don't have mixed content
        if (current.hasChildren() && current.value != null) {
            // MIXED content not allowed
            current.value = null;
        }
        // step up
        current = current.getParent();
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (unknownLevel > 0) {
            unknownEndElement(uri, localName, qName);
            unknownLevel--;
            return;
        }

        try {
            if (leaf) {
                // If it's a Leaf - we need to set value properly using
                // the setLeafValue method which will check
                // restrictions
                ((Container) current).setLeafValue(leafNs, leafName, leafValue);
            } else {
                // check that we don't have mixed content
                // System.out.println("XXXXXXXXXXXX" + current);
                if (current.hasChildren() && current.value != null) {
                    // MIXED content not allowed
                    current.value = null;
                }
            }
        } catch (NetconfException e) {
            e.printStackTrace();
            throw new SAXException(e.toString());
        }
        // step up
        if (!leaf)
            current = current.getParent();
        else
            leaf = false;
    }

    private void unknownCharacters(char[] ch, int start, int length) {
        if (current.value == null)
            current.value = new String();
        current.value = current.value + new String(ch, start, length);
    }

    public void characters(char[] ch, int start, int length) {
        if (unknownLevel > 0) {
            unknownCharacters(ch, start, length);
            return;
        }

        if (leaf) {
            leafValue = leafValue + new String(ch, start, length);
        } else {
            if (current.value == null)
                current.value = new String();
            current.value = current.value + new String(ch, start, length);
        }
    }

    public void startPrefixMapping(String prefix, String uri) {
        if (prefixes == null)
            prefixes = new PrefixMap();
        prefixes.add(new Prefix(prefix, uri));
    }
}
