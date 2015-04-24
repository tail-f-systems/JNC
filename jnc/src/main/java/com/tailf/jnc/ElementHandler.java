package com.tailf.jnc;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A SAX parser, for parsing for example NETCONF messages,
 * into a simple {@link Element Element} tree.
 * <p>
 * This parser is data model aware and will try to construct
 * classes that are generated from the JNC pyang plugin.
 * <p>
 */

/**
 * The handler with hooks for startElement etc. The SAX parser will build up
 * the parse tree, by calling these hooks.
 */
class ElementHandler extends DefaultHandler {

    // pointer to current element (node)
    public Element current;
    public Element top;
    public PrefixMap prefixes = null;
    public int unknownLevel = 0;
    
    private boolean leaf = false;
    private String leafNs;
    private String leafName;
    private String leafValue;

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {

        if (unknownLevel > 0) {
            unkownStartElement(uri, localName, attributes);
            return;
        }
        final Element parent = current;
        Element child;

        try {
            child = YangElement.createInstance(this, parent, uri, localName);
        } catch (final JNCException e) {
            e.printStackTrace();
            throw new SAXException(e.toString());
        }

        if (top == null) {
            top = child;
        }

        if (child == null && unknownLevel == 1) {
            // we're entering XML data that's not in the schema
            unkownStartElement(uri, localName, attributes);
            return;
        }

        if (child == null) {
            // it's a known leaf
            // it'll be handled in the endElement method
            leaf = true;
            leafNs = uri;
            leafName = localName;
            leafValue = "";
            return;
        }
        child.prefixes = prefixes;
        prefixes = null;
        addOtherAttributes(attributes, child);
        current = child; // step down
    }

    private void unkownStartElement(String uri, String localName, Attributes attributes) throws SAXException {
        final Element child = new Element(uri, localName);
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
            final String attrName = attributes.getLocalName(i);
            final String attrUri = attributes.getURI(i);
            final String attrValue = attributes.getValue(i);
            final Attribute attr = new Attribute(attrUri, attrName, attrValue);
            child.addAttr(attr);
        }
    }

    private void unknownEndElement() {
        // check that we don't have mixed content
        if (current.hasChildren() && current.value != null) {
            // MIXED content not allowed
            current.value = null;
        }
        // step up
        current = current.getParent();
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (unknownLevel > 0) {
            unknownEndElement();
            unknownLevel--;
            return;
        }

        if (leaf) {
            // If it's a Leaf - we need to set value properly using
            // the setLeafValue method which will check restrictions
            try {
            ((YangElement) current).setLeafValue(leafNs, leafName, leafValue);
            } catch (final JNCException e) {
                e.printStackTrace();
                throw new SAXException(e.toString());
            }
        } else {
            // check that we don't have mixed content
            if (current.hasChildren() && current.value != null) {
                // MIXED content not allowed
                current.value = null;
            }
        }

        // step up
        if (!leaf) {
            current = current.getParent();
        } else {
            leaf = false;
        }
    }

    private void unknownCharacters(char[] ch, int start, int length) {
        if (current.value == null) {
            current.value = "";
        }
        current.value = current.value + new String(ch, start, length);
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        if (unknownLevel > 0) {
            unknownCharacters(ch, start, length);
            return;
        }

        if (leaf) {
            leafValue = leafValue + new String(ch, start, length);
        } else {
            if (current.value == null) {
                current.value = "";
            }
            current.value = current.value + new String(ch, start, length);
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) {
        if (prefixes == null) {
            prefixes = new PrefixMap();
        }
        prefixes.add(new Prefix(prefix, uri));
    }
}
