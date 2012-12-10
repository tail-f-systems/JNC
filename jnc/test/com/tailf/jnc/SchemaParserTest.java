package com.tailf.jnc;

import java.util.HashMap;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;



public class SchemaParserTest {
    
    private static SchemaParser parser;
    private HashMap<Tagpath, SchemaNode> h = new HashMap<Tagpath, SchemaNode>();      
    
    public SchemaParserTest() {
    }
    
    @Before
    public void setUp() throws JNCException {
        parser = new SchemaParser();
    }
    
    @After
    public void tearDown() {
    }

    @Test (expected=JNCException.class)
    public void testFileNotFound() throws JNCException {
        parser.findAndReadFile("File Not Found.schema", h, SchemaParser.class);
    }
    
    @Test
    public void testLoadSchemaFromClasspath() throws JNCException {
        parser.findAndReadFile("Yang.schema", h, SchemaParser.class);
        Assert.assertEquals(1, h.size());
        Assert.assertEquals("urn:ietf:params:xml:ns:yang:ietf-yang-types", h.values().iterator().next().namespace);
    }

}
