/*    -*- Java -*-
 * 
 *  Copyright 2007 Tail-F Systems AB. All rights reserved. 
 *
 *  This software is the confidential and proprietary 
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import com.tailf.confm.*;

// import com.tailf.inm.xs.*;

public class test_types {
    public static int testnr = 0;
    public static int pass = 0;
    public static int fail = 0;
    public static int skip = 0;
    public static boolean processed = false;

    static public void main(String args[]) {
	int i, th, usid;

	System.err.println("---------------------------------------");
	System.err.println("    ConfM data type tests");
	System.err.println("---------------------------------------");

	TestCase[] tests = new TestCase[] {


	    /************************************************************
	     * Test 0
	     * Xs.Byte type
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.Byte b = new Xs.Byte(45);
		    print("Xs.Byte: "+ b);

		    // rewrite
		    b.setValue(b.getValue());
		    
		    b = new Xs.Byte(127);
		    print("Xs.Byte: "+ b);
	
		    b = new Xs.Byte(-127);
		    print("Xs.Byte: "+ b);
		    
		    try {
			b = new Xs.Byte(256);
			failed("byte 256 should not pass");
		    } catch (ConfMException e) {
		    }
		    
		    try {
			b = new Xs.Byte(-142);
			failed("byte -142 should not pass");
		    } catch (ConfMException e) {
		    }
		    // equals
		    Xs.Byte n1 = new Xs.Byte(-111);
		    Xs.Byte n2 = new Xs.Byte(-111);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.Byte");
		}
	    },
	    
	    /************************************************************
	     * Test 1
	     * Xs.Short type
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.Short b = new Xs.Short(45);
		    print("Xs.Short: "+ b);
		    
		    // rewrite
		    b.setValue(b.getValue());

		    b = new Xs.Short(32767);
		    print("Xs.Short: "+ b);
		    
		    b = new Xs.Short(-32768);
		    print("Xs.Short: "+ b);
		    
		    try {
			b = new Xs.Short(32768);
			failed("Xs.Short 32768 should not pass");
		    } catch (ConfMException e) {
		    }
		    
		    try {
			b = new Xs.Short(-6565655);
			failed("Xs.Short -6565655 should not pass");
		    } catch (ConfMException e) {
		    }
		    // equals
		    Xs.Short n1 = new Xs.Short(32766);
		    Xs.Short n2 = new Xs.Short(32766);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.Short");
		}
	    },
	    
	    /************************************************************
	     * Test 2
	     * Xs.Int type
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.Int s = new Xs.Int(45);
		    print("Xs.Int: "+ s);

		    // rewrite
		    s.setValue(s.getValue());

		    // equals
		    Xs.Int n1 = new Xs.Int(666667);
		    Xs.Int n2 = new Xs.Int(666667);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.Int");

		    s = new Xs.Int(" \n\n\n\t    123  \n \n");
		    print("Xs.Int: "+s);
		}
	    },
	    
	    /************************************************************
	     * Test 3
	     * Xs.Integer type
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.Integer i = new Xs.Integer(232323232345L);
		    print("Xs.Integer: "+ i);

		    // rewrite
		    i.setValue(i.getValue());

		    // equals
		    Xs.Integer n1 = new Xs.Integer(666667);
		    Xs.Integer n2 = new Xs.Integer(666667);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.Integer");
		}
	    },
	    
	    
	    /************************************************************
	     * Test 4
	     * Xs.Float type
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.Float i = new Xs.Float(3.1415);
		    print("Xs.Float: "+ i);

		    // rewrite
		    i.setValue(i.getValue());

		    // equals
		    Xs.Float n1 = new Xs.Float(5555.44);
		    Xs.Float n2 = new Xs.Float(5555.44);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.Float");
		}
	    },
	    
	    
	    /************************************************************
	     * Test 5
	     * Xs.Double type
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.Double i = new Xs.Double(3.1415);
		    print("Xs.Double: "+ i);

		    // rewrite
		    i.setValue(i.getValue());

		    // equals
		    Xs.Double n1 = new Xs.Double(5555.44);
		    Xs.Double n2 = new Xs.Double(5555.44);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.Double");
		}
	    },
	    
	    
	    /************************************************************
	     * Test 6
	     * Xs.Decimal type
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.Decimal i = new Xs.Decimal(3.1415);
		    print("Xs.Decimal: "+ i);

		    // rewrite
		    i.setValue(i.getValue());

		    // equals
		    Xs.Decimal n1 = new Xs.Decimal(5555);
		    Xs.Decimal n2 = new Xs.Decimal(5555);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.Decimal");
		}		   
	    },
	    
	    /************************************************************
	     * Test 7
	     * Derived type: PortType
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    simple.PortType p = new simple.PortType(2048);
		    print("simple.PortType: "+ p);
		   
		    // rewrite
		    p.setValue(p.getValue());
 
		    p = new simple.PortType(1024);
		    print("simple.PortType: "+ p);
		    
		    try {
			p = new simple.PortType(1023);
			failed("simple.PortType must be >=1024. 10234 should not pass");
		    } catch (ConfMException e) {
		    }
		    // equals
		    simple.PortType n1 = new simple.PortType(5555);
		    simple.PortType n2 = new simple.PortType(5555);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for simple.PortType");
		}		   
	    },
	    
	    /************************************************************
	     * Test 8
	     * Xs.Token
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.Token t = new Xs.Token(" kalle kaka\t     baka smaka  \n\n\t ");
		    print("Xs.Token: "+ t);
		 
		    // rewrite
		    t.setValue(t.getValue());
   
		    Xs.Token n = 
			new Xs.Token("    kalle \t\nkaka baka smaka\n  \nx   ");
		    print("Xs.Token: "+ n);
		    // equals
		    Xs.Token n1 = new Xs.Token("fnutt prutt");
		    Xs.Token n2 = new Xs.Token("fnutt\tprutt");
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Token");
		}		   
	    },
	    
	    /************************************************************
	     * Test 9
	     * Xs.NegativeInteger
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.NegativeInteger n = new Xs.NegativeInteger(-45);
		    print("Xs.NegativeInteger: "+ n);
		    
		    // rewrite
		    n.setValue(n.getValue());
   
		    n = new Xs.NegativeInteger(-1);
		    print("Xs.NegativeInteger: "+ n);
		    
		    n = new Xs.NegativeInteger(-32768);
		    print("Xs.NegativeInteger: "+ n);
		    
		    try {
			n = new Xs.NegativeInteger(0);
			failed("negativeInteger '0' should not pass");
		    } catch (ConfMException e) {
		    }
		    
		    try {
			n = new Xs.NegativeInteger(1);
			failed("negativeInteger '1' should not pass");
		    } catch (ConfMException e) {
		    }
		    // equals
		    Xs.NegativeInteger n1 = new Xs.NegativeInteger(-123);
		    Xs.NegativeInteger n2 = new Xs.NegativeInteger(-123);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for NegativeInteger");
		}		   
	    },
	    
	    /************************************************************
	     * Test 10
	     * Xs.NonNegativeInteger
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.NonNegativeInteger n = new Xs.NonNegativeInteger(42);
		    print("Xs.NonNegativeInteger: "+ n);
		    
		    // rewrite
		    n.setValue(n.getValue());
    
		    n = new Xs.NonNegativeInteger(-0);
		    print("Xs.NonNegativeInteger: "+ n);
		    
		    n = new Xs.NonNegativeInteger(26126172);
		    print("Xs.NonNegativeInteger: "+ n);
		    
		    try {
			n = new Xs.NonNegativeInteger(-1);
			failed("nonNegativeInteger '-1' should not pass");
		    } catch (ConfMException e) {
		    }
		    
		    try {
			n = new Xs.NonNegativeInteger(-32323232);
			failed("nonNegativeInteger '-32323232' should not pass");
		    } catch (ConfMException e) {
		    }
		    // equals
		    Xs.NonNegativeInteger n1 = new Xs.NonNegativeInteger(123);
		    Xs.NonNegativeInteger n2 = new Xs.NonNegativeInteger(123);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for nonNegativeInteger");
		}		   
	    },
	    
	    /************************************************************
	     * Test 11
	     * Xs.PositiveInteger
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.PositiveInteger n = new Xs.PositiveInteger(42);
		    print("Xs.PositiveInteger: "+ n);
		    
		    // rewrite
		    n.setValue(n.getValue());
		    
		    n = new Xs.PositiveInteger(+1);
		    print("Xs.PositiveInteger: "+ n);
		    
		    n = new Xs.PositiveInteger(26126172);
		    print("Xs.PositiveInteger: "+ n);
		    
		    try {
			n = new Xs.PositiveInteger(-1);
			failed("positiveInteger '-1' should not pass");
		    } catch (ConfMException e) {
		    }
		    
		    try {
			n = new Xs.PositiveInteger(0);
			failed("positiveInteger '0' should not pass");
		    } catch (ConfMException e) {
		    }
		    // equals
		    Xs.PositiveInteger n1 = new Xs.PositiveInteger(123);
		    Xs.PositiveInteger n2 = new Xs.PositiveInteger(123);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for PositiveInteger");
		}		   
	    },
	    
	    /************************************************************
	     * Test 12
	     * Xs.NonPositiveInteger
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.NonPositiveInteger n = new Xs.NonPositiveInteger(-45);
		    print("Xs.NonPositiveInteger: "+ n);
		    		    
		    // rewrite
		    n.setValue(n.getValue());

		    n = new Xs.NonPositiveInteger(-1);
		    print("Xs.NonPositiveInteger: "+ n);
		    
		    n = new Xs.NonPositiveInteger(0);
		    print("Xs.NonPositiveInteger: "+ n);
		    
		    try {
			n = new Xs.NonPositiveInteger(+1);
			failed("nonPositiveInteger '+1' should not pass");
		    } catch (ConfMException e) {
		    }
		    
		    try {
			n = new Xs.NonPositiveInteger(+1212121);
			failed("nonPositiveInteger '+1212121' should not pass");
		    } catch (ConfMException e) {
		    }		    
		    // equals
		    Xs.NonPositiveInteger n1 = new Xs.NonPositiveInteger(-123);
		    Xs.NonPositiveInteger n2 = new Xs.NonPositiveInteger(-123);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for nonPositiveInteger");
		}
	    },
	    
	    /************************************************************
	     * Test 13
	     * Xs.UnsignedByte type
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.UnsignedByte b = new Xs.UnsignedByte(45);
		    print("Xs.UnsignedByte: "+ b);
		    		    
		    // rewrite
		    b.setValue(b.getValue());

		    b = new Xs.UnsignedByte(127);
		    print("Xs.UnsignedByte: "+ b);
		    
		    b = new Xs.UnsignedByte(255);
		    print("Xs.UnsignedByte: "+ b);
		    
		    try {
			b = new Xs.UnsignedByte(256);
			failed("byte 256 should not pass");
		    } catch (ConfMException e) {
		    }
		    
		    try {
			b = new Xs.UnsignedByte(-1);
			failed("unsignedByte -1 should not pass");
		    } catch (ConfMException e) {
		    }
		    // equals
		    Xs.UnsignedByte n1 = new Xs.UnsignedByte(111);
		    Xs.UnsignedByte n2 = new Xs.UnsignedByte(111);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.UnsignedByte");
		}
	    },

	    /************************************************************
	     * Test 14
	     * Xs.UnsignedShort type
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.UnsignedShort b = new Xs.UnsignedShort(45);
		    print("Xs.UnsignedShort: "+ b);

		    // rewrite
		    b.setValue(b.getValue());
	    
		    b = new Xs.UnsignedShort(127);
		    print("Xs.UnsignedShort: "+ b);
		    
		    b = new Xs.UnsignedShort(65535);
		    print("Xs.UnsignedShort: "+ b);
		    
		    try {
			b = new Xs.UnsignedShort(65536);
			failed("unsignedShort 65536 should not pass");
		    } catch (ConfMException e) {
		    }
		    
		    try {
			b = new Xs.UnsignedShort(-1);
			failed("unsignedShort -1 should not pass");
		    } catch (ConfMException e) {
		    }
		    // equals
		    Xs.UnsignedShort n1 = new Xs.UnsignedShort(111);
		    Xs.UnsignedShort n2 = new Xs.UnsignedShort(111);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.UnsignedShort");
		}
	    },

	    /************************************************************
	     * Test 15
	     * Xs.UnsignedInt type
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.UnsignedInt b = new Xs.UnsignedInt(45);
		    print("Xs.UnsignedInt: "+ b);
		    
		    // rewrite
		    b.setValue(b.getValue());

		    b = new Xs.UnsignedInt(127);
		    print("Xs.UnsignedInt: "+ b);
		    
		    b = new Xs.UnsignedInt(65535);
		    print("Xs.UnsignedInt: "+ b);
		    
		    try {
			b = new Xs.UnsignedInt(0x100000000L);
			failed("unsignedInt 0x100000000L should not pass");
		    } catch (ConfMException e) {
		    }
		    
		    try {
			b = new Xs.UnsignedInt(-1);
			failed("unsignedInt -1 should not pass");
		    } catch (ConfMException e) {
		    }
		    // equals
		    Xs.UnsignedInt n1 = new Xs.UnsignedInt(111);
		    Xs.UnsignedInt n2 = new Xs.UnsignedInt(111);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.UnsignedInt");
		}
	    },

	    /************************************************************
	     * Test 16
	     * Xs.UnsignedLong type
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.UnsignedLong b = new Xs.UnsignedLong(45);
		    print("Xs.UnsignedLong: "+ b);

		    // rewrite
		    b.setValue(b.getValue());
		    
		    b = new Xs.UnsignedLong(127);
		    print("Xs.UnsignedLong: "+ b);
		    
		    b = new Xs.UnsignedLong(65535);
		    print("Xs.UnsignedLong: "+ b);
		    
		    try {
			b = new Xs.UnsignedLong(-1);
			failed("unsignedLong -1 should not pass");
		    } catch (ConfMException e) {
		    }
		    // equals
		    Xs.UnsignedLong n1 = new Xs.UnsignedLong(111);
		    Xs.UnsignedLong n2 = new Xs.UnsignedLong(111);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.UnsignedLong");
		}
	    },

	    /************************************************************
	     * Test 17
	     * simple.MyBitsType (BitsType32)
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    simple.MyBitsType b1 = new simple.MyBitsType(0);		    
		    print("simple.MyBitsType: "+ b1);
		  
		    // rewrite
		    b1.setValue(b1.getValue());
  
		    b1 = new simple.MyBitsType( b1.getValue() | 
					       simple.MyBitsType.DIRTY | 
					       simple.MyBitsType.COMPRESSED);
		    
		    print("Updated with DIRTY | COMPRESSED: "+ b1);
		    
		    simple.MyBitsType b2 = 
			new simple.MyBitsType( simple.MyBitsType.DIRTY | 
					       simple.MyBitsType.COMPRESSED);
		    
		    // equals
		    if ( ! b1.equals(b2))
			failed("equals comparison failed for simple.MyBitsType. b1="+b1+", b2="+b2);

		    // 'or'
		    b1 = new simple.MyBitsType( simple.MyBitsType.DIRTY );
		    b2 = new simple.MyBitsType( simple.MyBitsType.COMPRESSED );
		    print("b1= "+b1+", b2= "+b2);
		    print("b1.or(b2)");
		    b1.OR(b2);
		    print("b1= "+b1+", b2= "+b2);
		    
		}
	    },

	    /************************************************************
	     * Test 18
	     * simple.GzzType (Union)
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    simple.GzzType b1 = new simple.GzzType("45");		    
		    print("simple.GzzType: "+ b1);
		    
		    // rewrite
		    b1.setValue(b1.getValue());

		    simple.GzzType b2 = new simple.GzzType(new simple.SecPortType(2049));
		    print(" made GzzType(SecPortType): "+b2);
		    
		    simple.GzzType b3 = new simple.GzzType(new simple.HashAlgo("sha1"));
		    print(" made GzzType(HashAlgo): "+b3);
		    
		    simple.GzzType b4= new simple.GzzType(new Xs.UnsignedInt(11));
		    print(" made GzzType(UnignedInt): "+b4);
		    
		    b1= new simple.GzzType("2049");
		    if (! b1.equals(b2)) 
			failed("equals comparison failed for simple.GzzType b1="+b1+", b2="+b2);
		    if ( b2.equals(b3))
			failed("equals comparison failed for simple.GzzType b2="+b2+", b3="+b3);
		    
		}
	    },
	
	    /************************************************************
	     * Test 19
	     * simple.Alist (Xs.List type)
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    simple.AlistType b1 = new simple.AlistType("45 46 2 1");
		    print("simple.AlistType b1= "+ b1);
		
		    // rewrite
		    b1.setValue(b1.getValue());
    
		    simple.AlistType b2 = new simple.AlistType( new Xs.Integer[] {
			    new Xs.Integer(45),
			    new Xs.Integer(46),
			    new Xs.Integer(2),
			    new Xs.Integer(1)
			});
		    print("simple.AlistType b2= "+b2);

		    if ( ! b1.equals(b2)) 
			failed("equals comparison failed for b1= "+b1+", b2="+b2);
		    
		    try {
			simple.AlistType b3 = new simple.AlistType("   fooo ");
			failed("tried to make list of wrong type should have failed");
		    } catch (Exception e) {
		    }
		    
		    simple.AlistType b4 = new simple.AlistType("    12  23    5 5 ");
		    print("b4= "+b4);
		    if ( b1.equals(b4)) 
			failed("equals should have failed. b1= "+b1+", b4= "+b4);
		    
		}
	    },

	    
	    /************************************************************
	     * Test 20
	     * Xs.Long type
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.Long i = new Xs.Long(-9223372036854775808L);
		    print("Xs.Long: "+ i);
	
		    // rewrite
		    i.setValue(i.getValue());
    
		    i = new Xs.Long(9223372036854775807L);
		    print("Xs.Long: "+ i);		    
		    // equals
		    Xs.Long n1 = new Xs.Long(666667);
		    Xs.Long n2 = new Xs.Long(666667);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.Long");
		}
	    },
	    
	    
	    /************************************************************
	     * Test 21
	     * Xs.Name
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.Name t = new Xs.Name("kalle");
		    print("Xs.Name: "+ t);

		    // rewrite
		    t.setValue(t.getValue());

		    t = new Xs.Name(":kalle");
		    print("Xs.Name: "+ t);
		    
		    t = new Xs.Name("-kalle");
		    print("Xs.Name: "+ t);
		    
		    // equals
		    Xs.Name n1 = new Xs.Name("Fnutt");
		    Xs.Name n2 = new Xs.Name("Fnutt");
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.Name");
		    
		    // illegal values
		    try {
			Xs.Name t1= new Xs.Name("123ola");
			failed("create of Xs.Name= \"123ola\", should have failed");
		    } catch (Exception e) {
			
		    }
		}
	    },

	    /************************************************************
	     * Test 22
	     * Xs.NCName
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.NCName t = new Xs.NCName("kalle");
		    print("Xs.NCName: "+ t);

		    // rewrite
		    t.setValue(t.getValue());

		    t = new Xs.NCName("-kalle");
		    print("Xs.NCName: "+ t);
		    
		    // equals
		    Xs.NCName n1 = new Xs.NCName("Fnutt");
		    Xs.NCName n2 = new Xs.NCName("Fnutt");
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.NCName");
		    
		    // illegal values
		    try {
			Xs.NCName t1= new Xs.NCName("123:ola");
			failed("create of Xs.NCName= \"123:ola\", should have failed");
		    } catch (Exception e) {
			
		    }
		}
	    },

	    /************************************************************
	     * Test 23
	     * Xs.NMTOKEN
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.NMTOKEN t = new Xs.NMTOKEN("kalle");
		    print("Xs.NMTOKEN: "+ t);

		    // rewrite
		    t.setValue(t.getValue());

		    t = new Xs.NMTOKEN("-kalle");
		    print("Xs.NMTOKEN: "+ t);
		    
		    // equals
		    Xs.NMTOKEN n1 = new Xs.NMTOKEN("Fnutt");
		    Xs.NMTOKEN n2 = new Xs.NMTOKEN("Fnutt");
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.NMTOKEN");
		    
		    // illegal values
		    try {
			Xs.NMTOKEN t1= new Xs.NMTOKEN("123,ola");
			failed("create of Xs.NMTOKEN= \"123,ola\", should have failed");
		    } catch (Exception e) {
			
		    }
		    try {
			Xs.NMTOKEN t1= new Xs.NMTOKEN("123 ola");
			failed("create of Xs.NMTOKEN= \"123 ola\", should have failed");
		    } catch (Exception e) {
		    }
		}
	    },


	    /************************************************************
	     * Test 24
	     * Xs.Language
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.Language t = new Xs.Language("englisch");
		    print("Xs.Language: "+ t);
		
		    // rewrite
		    t.setValue(t.getValue());
    
		    t = new Xs.Language("i-sami-no");
		    print("Xs.Language: "+ t);
		    
		    // equals
		    // compare uppcase toward lowercase
		    Xs.Language n1 = new Xs.Language("x-klingon");
		    Xs.Language n2 = new Xs.Language("x-KLINGON");
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.Language");
		    
		    // illegal values
		    try {
			Xs.Language t1= new Xs.Language("en1-z");
			failed("create of Xs.Language= \"en1-z\", should have failed");
		    } catch (Exception e) {
			
		    }
		    try {
			Xs.Language t1= new Xs.Language("abcdefghi-US");
			failed("create of Xs.Language= \"abcdefghi-US\", should have failed");
		    } catch (Exception e) {
		    }
		}
	    },

	    /************************************************************
	     * Test 25
	     * Confd.Size
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Confd.Size t =  new Confd.Size("S1G8M7K956B");
		    print("Confd.size: "+ t);
		
		    // rewrite
		    t.setValue(t.getValue());
    
		    String s1 = t.getValue();
		    t.setValue(t.longValue());
		    if (! s1.equals( t.getValue()))
			failed("rewriting with long value did not work");
		    
		    t = new Confd.Size("S8M1B");
		    print("Confd.Size: "+ t);
		    // toString
		    if (! t.toString().equals("S8M1B")) 
			failed("toString should have given S81M1B but gave: "+t.toString());
		    
		    // equals
		    // compare uppcase toward lowercase
		    Confd.Size n1 = new Confd.Size("S1K1B");
		    Confd.Size n2 = new Confd.Size("S1025B");
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Confd.Size");
		    
		    // illegal values
		    try {
			Confd.Size t1= new Confd.Size("S1M1G");
			failed("create of Confd.Size= \"S1M1G\", should have failed");
		    } catch (Exception e) {
			
		    }
		    try {
			Confd.Size t1= new Confd.Size("S1G1K1M1B");
			failed("create of Confd.Size= \"abcdefghi-US\", should have failed");
		    } catch (Exception e) {
		    }		   		    
		}
	    },


	    /************************************************************
	     * Test 26
	     * Xs.ID
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.ID t = new Xs.ID("kalle");
		    print("Xs.ID: "+ t);

		    // rewrite
		    t.setValue(t.getValue());

		    t = new Xs.ID("-kalle");
		    print("Xs.ID: "+ t);
		    
		    // equals
		    Xs.ID n1 = new Xs.ID("Fnutt");
		    Xs.ID n2 = new Xs.ID("Fnutt");
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.ID");
		    
		    // illegal values
		    try {
			Xs.ID t1= new Xs.ID("123:ola");
			failed("create of Xs.ID= \"123:ola\", should have failed");
		    } catch (Exception e) {			
		    }
		}
	    },



	    /************************************************************
	     * Test 27
	     * Xs.Duration
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.Duration t = new Xs.Duration("PT1004199059S");
		    print("Xs.Duration: "+ t);
		    
		    // rewrite
		    t.setValue(t.getValue());

		    t = new Xs.Duration("PT130S");
		    print("Xs.Duration: "+ t);

		    t = new Xs.Duration("PT2M10S");
		    print("Xs.Duration: "+ t);

		    t = new Xs.Duration("P1DT2S");
		    print("Xs.Duration: "+ t);

		    t = new Xs.Duration("-P1Y");
		    print("Xs.Duration: "+ t);
	
		    t = new Xs.Duration("P1Y2M3DT5H20M30.123S");
		    print("Xs.Duration: "+ t);
		    if ( ! t.toString().equals("P1Y2M3DT5H20M30.123S"))
			failed("toString is not correct for: "+ t);
		    
		    t = new Xs.Duration("PT0.1234567S");
		    print("Xs.Duration: "+ t);
		    if ( ! t.toString().equals("PT0.123456S"))
			failed("toString is not correct for: "+ t);
		    
		    // equals
		    Xs.Duration n1 = new Xs.Duration("P1Y2M3DT5H20M30.125S");
		    Xs.Duration n2 = new Xs.Duration("P1Y2M3DT5H20M30.125S");
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.Duration");
		    
		    // illegal values
		    try {
			Xs.Duration t1= new Xs.Duration("1Y");
			failed("create of Xs.Duration= \"1Y\", should have failed");
		    } catch (Exception e) {  }
		    try {
			Xs.Duration t1= new Xs.Duration("P1S");
			failed("create of Xs.Duration= \"P1S\", should have failed");
		    } catch (Exception e) {  }
		    try {
			Xs.Duration t1= new Xs.Duration("P-1Y");
			failed("create of Xs.Duration= \"P-1Y\", should have failed");
		    } catch (Exception e) {  }
		    try {
			Xs.Duration t1= new Xs.Duration("P1M2Y");
			failed("create of Xs.Duration= \"P1M2Y\", should have failed");
		    } catch (Exception e) {  }
		    try {
			Xs.Duration t1= new Xs.Duration("P1Y-1M");
			failed("create of Xs.Duration= \"P1Y-1M\", should have failed");
		    } catch (Exception e) {  }
		}
	    },



	    /************************************************************
	     * Test 28
	     * simple.Dzzt structured types
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    simple.Dzzt t = new simple.Dzzt( new Xs.Token("kalle"));
		    print("simple.Dzzt: "+ t);

		    // rewrite
		    t.setValue(t.getValue());

		    t = new simple.Dzzt( new Xs.Token("foo"));
		    print("simple.Dzzt: "+ t);
		    
		    // equals
		    simple.Dzzt n1 = new simple.Dzzt(new Xs.Token("Fnutt"));
		    simple.Dzzt n2 = new simple.Dzzt(new Xs.Token("Fnutt"));

		    if ( ! n1.equals(n2))
			failed("equals comparison failed for simple.Dzzt");

		    int res= n1.compare(n2);
		    if (res != 0) 
			failed("compare of simple.Dzzt= "+n1+" and " +n2+" failed");
		}
	    },



	    /************************************************************
	     * Test 29
	     * Confd.InetAddress
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Confd.InetAddress t = new Confd.InetAddress("192.168.0.1");
                    print("Confd.InetAddress: "+ t);

		    // rewrite
		    t.setValue(t.getValue());

		    t = new Confd.InetAddress("0.0.0.0");
		    print("Confd.InetAddress: "+ t);
		    
		    // equals
		    Confd.InetAddress n1 = new Confd.InetAddress("255.255.0.1");
		    Confd.InetAddress n2 = new Confd.InetAddress("255.255.0.1");

		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Confd.InetAddress");
		}
	    },
	 
	    /************************************************************
	     * Test 30
	     * Confd.InetAddressIP
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Confd.InetAddressIP t = new Confd.InetAddressIP("192.168.0.1");
                    print("Confd.InetAddressIP: "+ t);

		    // rewrite
		    t.setValue(t.getValue());

		    t = new Confd.InetAddressIP("0.0.0.0");
		    print("Confd.InetAddressIP: "+ t);
		    
		    // equals
		    Confd.InetAddressIP n1 = new Confd.InetAddressIP("255.255.0.1");
		    Confd.InetAddressIP n2 = new Confd.InetAddressIP("255.255.0.1");

		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Confd.InetAddressIP");
		}
	    },
	
	 
	    /************************************************************
	     * Test 31
	     * Confd.InetAddressIPv4
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Confd.InetAddressIPv4 t = new Confd.InetAddressIPv4("192.168.0.1");
                    print("Confd.InetAddressIPv4: "+ t);

		    // rewrite
		    t.setValue(t.getValue());

		    t = new Confd.InetAddressIPv4("0.0.0.0");
		    print("Confd.InetAddressIPv4: "+ t);
		    
		    // equals
		    Confd.InetAddressIPv4 n1 = new Confd.InetAddressIPv4("255.255.0.1");
		    Confd.InetAddressIPv4 n2 = new Confd.InetAddressIPv4("255.255.0.1");

		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Confd.InetAddressIPv4");
		}
	    },

	 
	    /************************************************************
	     * Test 32
	     * Confd.InetAddressIPv6
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    
		    Confd.InetAddressIPv6 t = new Confd.InetAddressIPv6("2001:0db8:0000:0000:0000:0000:1428:57ab");
                    print("Confd.InetAddressIPv6: "+ t);

		    t = new Confd.InetAddressIPv6("2001:0db8:0000:0000:0000::1428:57ab");
                    print("Confd.InetAddressIPv6: "+ t);

		    t = new Confd.InetAddressIPv6("2001:0db8:0:0:0:0:1428:57ab");
                    print("Confd.InetAddressIPv6: "+ t);

		    t = new Confd.InetAddressIPv6("2001:0db8:0:0::1428:57ab");
                    print("Confd.InetAddressIPv6: "+ t);

		    t = new Confd.InetAddressIPv6("2001:0db8::1428:57ab");
                    print("Confd.InetAddressIPv6: "+ t);

		    t = new Confd.InetAddressIPv6("2001:db8::1428:57ab");
                    print("Confd.InetAddressIPv6: "+ t);

		    // rewrite
		    t.setValue(t.getValue());

		    // equals
		    Confd.InetAddressIPv6 n1 = new Confd.InetAddressIPv6("2001:db8::1428:57ab");
		    Confd.InetAddressIPv6 n2 = new Confd.InetAddressIPv6("2001:0db8:0:0:0:0:1428:57ab");

		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Confd.InetAddressIPv6");
		}
	    },
	 
	    /************************************************************
	     * Test 33
	     * Confd.InetAddressDNS
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Confd.InetAddressDNS t = new Confd.InetAddressDNS("saturn.tail-f.com");
                    print("Confd.InetAddressDNS: "+ t);

		    // rewrite
		    t.setValue(t.getValue());

		    t = new Confd.InetAddressDNS("localhost");
		    print("Confd.InetAddressDNS: "+ t);
		    
		    // equals
		    Confd.InetAddressDNS n1 = new Confd.InetAddressDNS("vega.google.com");
		    Confd.InetAddressDNS n2 = new Confd.InetAddressDNS("vega.google.com");

		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Confd.InetAddressDNS");
		}
	    },

	    /************************************************************
	     * Test 34
	     * Confd.InetPortNumber
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Confd.InetPortNumber t = new Confd.InetPortNumber(5001);
                    print("Confd.InetPortNumber: "+ t);

		    // rewrite
		    t.setValue(t.getValue());

		    t = new Confd.InetPortNumber(25);
		    print("Confd.InetPortNumber: "+ t);
		    
		    // equals
		    Confd.InetPortNumber n1 = new Confd.InetPortNumber(65535);
		    Confd.InetPortNumber n2 = new Confd.InetPortNumber("65535");

		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Confd.InetPortNumber");
		}
	    },

	    
	    /************************************************************
	     * Test 35
	     * Xs.NormalizedString
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.NormalizedString t = 
			new Xs.NormalizedString("fubb kalle kaka\t     baka smaka  \n\n\t ");
		    print("Xs.NormalizedString: "+ t);

		    // rewrite
		    t.setValue(t.getValue());
		    
		    Xs.NormalizedString n = 
			new Xs.NormalizedString("    kalle \t\nkaka baka smaka\n  \nx   ");
		    print("Xs.NormalizedString: "+ n);
		    // equals
		    Xs.NormalizedString n1 = new Xs.NormalizedString("fnutt prutt");
		    Xs.NormalizedString n2 = new Xs.NormalizedString("fnutt\tprutt");
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for NormalizedString");
		}		   
	    },
	
	    /************************************************************
	     * Test 36
	     * Xs.String
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.String t = 
			new Xs.String("fubb kalle kaka\t     baka smaka  \n\n\t ");
		    print("Xs.String: "+ t);
		  
		    // rewrite
		    t.setValue(t.getValue());
  
		    Xs.String n = 
			new Xs.String("    kalle \t\nkaka baka smaka\n  \nx   ");
		    print("Xs.String: "+ n);
		    // equals
		    Xs.String n1 = new Xs.String("fnutt\tprutt");
		    Xs.String n2 = new Xs.String("fnutt\tprutt");
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.String");

		    // equals
		    Object o1 = new Xs.String("kalle");
		    Object o2 = new Xs.String("kalle");
		    if ( ! o1.equals(o2))
			failed("equals comparison failed for Xs.String");
		}		   
	    },

	    /************************************************************
	     * Test 37
	     * Xs.DateTime
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.DateTime t = 
			new Xs.DateTime("2000-04-01T21:32:52");
		    print("Xs.DateTime: "+ t);
                    print("Time --> "+ t.getTime());

		    // rewrite
		    t.setValue(t.getValue());
		    
		    Xs.DateTime n = 
			new Xs.DateTime("2001-10-26T21:32:52+02:00");
		    print("Xs.DateTime: "+ n);
                    print("Time --> "+ n.getTime());
                    
		    // equals
		    Xs.DateTime n1 = new Xs.DateTime("2001-10-26T19:32:52Z");
		    Xs.DateTime n2 = new Xs.DateTime("2001-10-26T19:32:52+00:00");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());
                    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.DateTime");

                    // equals
		    n1 = new Xs.DateTime("2002-01-18T12:00:00+00:00");
		    n2 = new Xs.DateTime("2002-01-18T11:00:00-01:00");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());                    
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.DateTime");
		    
		    // equals
		    Object o1 = new Xs.DateTime("2001-10-12T00:00:01.01");
		    Object o2 = new Xs.DateTime("2001-10-12T00:00:01.0100");
		    if ( ! o1.equals(o2))
			failed("equals comparison failed for Xs.DateTime");

                    // not equal
		    n1 = new Xs.DateTime("2002-01-18T12:00:00.23+00:00");
		    n2 = new Xs.DateTime("2002-01-18T11:00:00-01:00");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());                    
		    if ( n1.equals(n2))
			failed("equals comparison failed for Xs.DateTime");

                    // bad
                    try {
                        n = new Xs.DateTime("2000-1-13");
                        failed("should have failed: 2000-1-13");
                    } catch (ConfMException e) {
                    }
                    
		}		   
                
	    },


	    /************************************************************
	     * Test 38
	     * Xs.Date
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.Date t = 
			new Xs.Date("2000-04-01");
		    print("Xs.Date: "+ t);
                    print("Time --> "+ t.getTime());
	
		    // rewrite
		    t.setValue(t.getValue());
	    
		    Xs.Date n = new Xs.Date("2001-10-26+02:00");
		    print("Xs.Date: "+ n + " ---> "+ n.getTime());

                    n = new Xs.Date("-2001-10-26");
		    print("Xs.Date: "+ n + " ---> "+ n.getTime());
                    
                    n = new Xs.Date("-20000-04-01");
		    print("Xs.Date: "+ n + " ---> "+ n.getTime());
                    
                    // bad
                    try {
                        n = new Xs.Date("2000-10");
                        failed("should have failed: all parts must be specified");
                    } catch (ConfMException e) {
                    }
                    
                    // bad
                    try {
                        n = new Xs.Date("2000-10-32");
                        failed("should have failed: day 32 should be out of range");
                    } catch (ConfMException e) {
                    }

                    // bad
                    try {
                        n = new Xs.Date("2000-13-26-02:00");
                        failed("should have failed: month 13 should be out of range");
                    } catch (ConfMException e) {
                    }
                    
                    // bad
                    try {
                        n = new Xs.Date("01-01-02");
                        failed("should have failed: the century part is missing");
                    } catch (ConfMException e) {
                    }                    
                        
		    // equals
		    Xs.Date n1 = new Xs.Date("2001-10-26Z");
		    Xs.Date n2 = new Xs.Date("2001-10-26+00:00");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());
                    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.Date");

                    // equals
		    n1 = new Xs.Date("2002-01-18+00:00");
		    n2 = new Xs.Date("2002-01-18Z");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());                    
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.Date");
		    
		    // equals
		    Object o1 = new Xs.Date("2001-10-12");
		    Object o2 = new Xs.Date("2001-10-12");
		    if ( ! o1.equals(o2))
			failed("equals comparison failed for Xs.Date");

                    // not equal
		    n1 = new Xs.Date("2002-01-18+00:00");
		    n2 = new Xs.Date("2002-01-18-01:00");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());                    
		    if ( n1.equals(n2))
			failed("equals comparison failed for Xs.Date");
		    
		}		   
                
	    },
	 			    

	    /************************************************************
	     * Test 39
	     * Xs.GYearMonth
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.GYearMonth t = 
			new Xs.GYearMonth("2000-04");
		    print("Xs.GYearMonth: "+ t);
                    print("Time --> "+ t.getTime());

		    // rewrite
		    t.setValue(t.getValue());
		    
		    Xs.GYearMonth n = new Xs.GYearMonth("2001-10+02:00");
		    print("Xs.GYearMonth: "+ n + " ---> "+ n.getTime());

                    n = new Xs.GYearMonth("-2001-10");
		    print("Xs.GYearMonth: "+ n + " ---> "+ n.getTime());
                    
                    n = new Xs.GYearMonth("-20000-04");
		    print("Xs.GYearMonth: "+ n + " ---> "+ n.getTime());
                    
                    // bad
                    try {
                        n = new Xs.GYearMonth("2000");
                        failed("should have failed: all parts must be specified");
                    } catch (ConfMException e) {
                    }
                    
                    // bad
                    try {
                        n = new Xs.GYearMonth("2000-00");
                        failed("should have failed: month 00 should be out of range");
                    } catch (ConfMException e) {
                    }

                    // bad
                    try {
                        n = new Xs.GYearMonth("2000-13");
                        failed("should have failed: month 13 should be out of range");
                    } catch (ConfMException e) {
                    }
                    
                    // bad
                    try {
                        n = new Xs.GYearMonth("01-01");
                        failed("should have failed: the century part is missing");
                    } catch (ConfMException e) {
                    }                    
                        
		    // equals
		    Xs.GYearMonth n1 = new Xs.GYearMonth("2001-10Z");
		    Xs.GYearMonth n2 = new Xs.GYearMonth("2001-10+00:00");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());
                    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.GYearMonth");

                    // equals
		    n1 = new Xs.GYearMonth("2002-01+00:00");
		    n2 = new Xs.GYearMonth("2002-01Z");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());                    
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.GYearMonth");
		    
		    // equals
		    Object o1 = new Xs.GYearMonth("2001-10");
		    Object o2 = new Xs.GYearMonth("2001-10");
		    if ( ! o1.equals(o2))
			failed("equals comparison failed for Xs.GYearMonth");

                    // not equal
		    n1 = new Xs.GYearMonth("2002-01+00:00");
		    n2 = new Xs.GYearMonth("2002-01-01:00");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());                    
		    if ( n1.equals(n2))
			failed("equals comparison failed for Xs.GYearMonth");
		    
		}		   
                
	    },
	 			    

	    /************************************************************
	     * Test 40
	     * Xs.GYear
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.GYear t = 
			new Xs.GYear("2000");
		    print("Xs.GYear: "+ t);
                    print("Time --> "+ t.getTime());

		    // rewrite
		    t.setValue(t.getValue());
		    
		    Xs.GYear n = new Xs.GYear("2001+02:00");
		    print("Xs.GYear: "+ n + " ---> "+ n.getTime());

                    n = new Xs.GYear("-2001");
		    print("Xs.GYear: "+ n + " ---> "+ n.getTime());
                    
                    n = new Xs.GYear("-20000");
		    print("Xs.GYear: "+ n + " ---> "+ n.getTime());
                    
                    // bad
                    try {
                        n = new Xs.GYear("200");
                        failed("should have failed: all parts must be specified");
                    } catch (ConfMException e) {
                    }

                    try {
                        n = new Xs.GYear("01");
                        failed("should have failed: the century part is missing");
                    } catch (ConfMException e) {
                    }                    
                        
		    // equals
		    Xs.GYear n1 = new Xs.GYear("2001Z");
		    Xs.GYear n2 = new Xs.GYear("2001+00:00");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());
                    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.GYear");

                    // equals
		    n1 = new Xs.GYear("2002+00:00");
		    n2 = new Xs.GYear("2002Z");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());                    
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.GYear");
		    
		    // equals
		    Object o1 = new Xs.GYear("2001");
		    Object o2 = new Xs.GYear("2001");
		    if ( ! o1.equals(o2))
			failed("equals comparison failed for Xs.GYear");

                    // not equal
		    n1 = new Xs.GYear("2002+00:00");
		    n2 = new Xs.GYear("2002-01:00");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());                    
		    if ( n1.equals(n2))
			failed("equals comparison failed for Xs.GYear");
		    
		}		   
                
	    },
	 			    

	    /************************************************************
	     * Test 41
	     * Xs.Time
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.Time t = 
			new Xs.Time("21:32:52");
		    print("Xs.Time: "+ t);
                    print("Time --> "+ t.getTime());

		    // rewrite
		    t.setValue(t.getValue());
		    
		    Xs.Time n = new Xs.Time("21:32:52+02:00");
		    print("Xs.Time: "+ n + " ---> "+ n.getTime());

                    n = new Xs.Time("19:32:52Z");
		    print("Xs.Time: "+ n + " ---> "+ n.getTime());
                    
                    n = new Xs.Time("19:32:52+00:00");
		    print("Xs.Time: "+ n + " ---> "+ n.getTime());

                    n = new Xs.Time("19:32:52.12679");
		    print("Xs.Time: "+ n + " ---> "+ n.getTime());
                    
                    // bad
                    try {
                        n = new Xs.Time("21:32");
                        failed("should have failed: all parts must be specified");
                    } catch (ConfMException e) {
                    }
                    
                    // bad
                    try {
                        n = new Xs.Time("25:25:10");
                        failed("should have failed: the hour part is out of range");
                    } catch (ConfMException e) {
                    }                    
                    // bad
                    try {
                        n = new Xs.Time("-10:00:00");
                        failed("should have failed: the hour part is out of range");
                    } catch (ConfMException e) {
                    }                    
                    // bad
                    try {
                        n = new Xs.Time("1:20:10");
                        failed("should have failed: all the digits must be supplied");
                    } catch (ConfMException e) {
                    }                    
                        
		    // equals
		    Xs.Time n1 = new Xs.Time("20:00:00Z");
		    Xs.Time n2 = new Xs.Time("20:00:00+00:00");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());
                    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.Time");

                    // equals
		    n1 = new Xs.Time("19:32:52+00:00");
		    n2 = new Xs.Time("19:32:52Z");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());                    
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.Time");
		    
		    // equals
		    Object o1 = new Xs.Time("19:00:01");
		    Object o2 = new Xs.Time("19:00:01");
		    if ( ! o1.equals(o2))
			failed("equals comparison failed for Xs.Time");

                    // not equal
		    n1 = new Xs.Time("01:00:01+00:00");
		    n2 = new Xs.Time("01:00:01-01:00");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());                    
		    if ( n1.equals(n2))
			failed("equals comparison failed for Xs.Time");
		    
		}		   
                
	    },

	    /************************************************************
	     * Test 42
	     * Xs.GMonth
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.GMonth n = new Xs.GMonth("--11Z");
		    print("Xs.GMonth: "+ n + " ---> "+ n.getTime());

		    // rewrite
		    n.setValue(n.getValue());

                    n = new Xs.GMonth("--11+02:00");
		    print("Xs.GMonth: "+ n + " ---> "+ n.getTime());
                    
                    n = new Xs.GMonth("--11-04:00");
		    print("Xs.GMonth: "+ n + " ---> "+ n.getTime());

                    n = new Xs.GMonth("--02");
		    print("Xs.GMonth: "+ n + " ---> "+ n.getTime());
                    
                    // bad
                    try {
                        n = new Xs.GMonth("-01-");
                        failed("should have failed: format must be '--MM'");
                    } catch (ConfMException e) {
                    }
                    
                    // bad
                    try {
                        n = new Xs.GMonth("--13");
                        failed("should have failed: month is 13");
                    } catch (ConfMException e) {
                    }                    
                    // bad
                    try {
                        n = new Xs.GMonth("--1");
                        failed("should have failed: both digits must be provided");
                    } catch (ConfMException e) {
                    }                    
                    // bad
                    try {
                        n = new Xs.GMonth("01");
                        failed("should have failed: no leading '--'");
                    } catch (ConfMException e) {
                    }                    
                        
		    // equals
		    Xs.GMonth n1 = new Xs.GMonth("--10Z");
		    Xs.GMonth n2 = new Xs.GMonth("--10+00:00");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());
                    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.GMonth");

		    // equals
		    Object o1 = new Xs.GMonth("--11");
		    Object o2 = new Xs.GMonth("--11");
		    if ( ! o1.equals(o2))
			failed("equals comparison failed for Xs.GMonth");

                    // not equal
		    n1 = new Xs.GMonth("--01+00:00");
		    n2 = new Xs.GMonth("--01-01:00");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());                    
		    if ( n1.equals(n2))
			failed("equals comparison failed for Xs.GMonth");
		    
		}		   
                
	    },

	    /************************************************************
	     * Test 43
	     * Xs.GDay
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.GDay t = new Xs.GDay("---05");
		    print("Xs.GDay: "+ t);
                    print("GDay --> "+ t.getTime());
		    
		    // rewrite
		    t.setValue(t.getValue());
	    
		    Xs.GDay n = new Xs.GDay("---11Z");
		    print("Xs.GDay: "+ n + " ---> "+ n.getTime());

                    n = new Xs.GDay("---11+02:00");
		    print("Xs.GDay: "+ n + " ---> "+ n.getTime());
                    
                    n = new Xs.GDay("---11-04:00");
		    print("Xs.GDay: "+ n + " ---> "+ n.getTime());

                    n = new Xs.GDay("---02");
		    print("Xs.GDay: "+ n + " ---> "+ n.getTime());
                    
                    // bad
                    try {
                        n = new Xs.GDay("--30-");
                        failed("should have failed: format must be '---DD'");
                    } catch (ConfMException e) {
                    }
                    
                    // bad
                    try {
                        n = new Xs.GDay("---35");
                        failed("should have failed: day 35");
                    } catch (ConfMException e) {
                    }                    
                    // bad
                    try {
                        n = new Xs.GDay("---5");
                        failed("should have failed: both digits must be provided");
                    } catch (ConfMException e) {
                    }                    
                    // bad
                    try {
                        n = new Xs.GDay("01");
                        failed("should have failed: no leading '--'");
                    } catch (ConfMException e) {
                    }                    
                        
		    // equals
		    Xs.GDay n1 = new Xs.GDay("---10Z");
		    Xs.GDay n2 = new Xs.GDay("---10+00:00");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());
                    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.GDay");

		    // equals
		    Object o1 = new Xs.GDay("---11");
		    Object o2 = new Xs.GDay("---11");
		    if ( ! o1.equals(o2))
			failed("equals comparison failed for Xs.GDay");

                    // not equal
		    n1 = new Xs.GDay("---01+00:00");
		    n2 = new Xs.GDay("---01-01:00");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());                    
		    if ( n1.equals(n2))
			failed("equals comparison failed for Xs.GDay");
		    
		}		   
                
	    },


	    /************************************************************
	     * Test 43
	     * Xs.GMonthDay
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.GMonthDay t = new Xs.GMonthDay("--01-05");
		    print("Xs.GMonthDay: "+ t);
                    print("GMonthDay --> "+ t.getTime());
		    
		    // rewrite
		    t.setValue(t.getValue());
	    
		    Xs.GMonthDay n = new Xs.GMonthDay("--12-11Z");
		    print("Xs.GMonthDay: "+ n + " ---> "+ n.getTime());

                    n = new Xs.GMonthDay("--02-29+02:00");
		    print("Xs.GMonthDay: "+ n + " ---> "+ n.getTime());
                    
                    n = new Xs.GMonthDay("--11-11-04:00");
		    print("Xs.GMonthDay: "+ n + " ---> "+ n.getTime());

                    n = new Xs.GMonthDay("--02-02");
		    print("Xs.GMonthDay: "+ n + " ---> "+ n.getTime());
                    
                    // bad
                    try {
                        n = new Xs.GMonthDay("-01-30-");
                        failed("should have failed: format must be '--MM-DD'");
                    } catch (ConfMException e) {
                    }
                    
                    // bad
                    try {
                        n = new Xs.GMonthDay("--01-35");
                        failed("should have failed: day 35");
                    } catch (ConfMException e) {
                    }                    
                    // bad
                    try {
                        n = new Xs.GMonthDay("--1-15");
                        failed("should have failed: both digits must be provided");
                    } catch (ConfMException e) {
                    }                    
                    // bad
                    try {
                        n = new Xs.GMonthDay("01-15");
                        failed("should have failed: no leading '--'");
                    } catch (ConfMException e) {
                    }                    
                        
		    // equals
		    Xs.GMonthDay n1 = new Xs.GMonthDay("--01-10Z");
		    Xs.GMonthDay n2 = new Xs.GMonthDay("--01-10+00:00");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());
                    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.GMonthDay");

		    // equals
		    Object o1 = new Xs.GMonthDay("--12-11");
		    Object o2 = new Xs.GMonthDay("--12-11");
		    if ( ! o1.equals(o2))
			failed("equals comparison failed for Xs.GMonthDay");

                    // not equal
		    n1 = new Xs.GMonthDay("--08-01+00:00");
		    n2 = new Xs.GMonthDay("--08-01-01:00");
                    print("n1: "+n1+" --> "+n1.getTime());
                    print("n2: "+n2+" --> "+n2.getTime());                    
		    if ( n1.equals(n2))
			failed("equals comparison failed for Xs.GMonthDay");
		    
		}		   
                
	    },

	    /************************************************************
	     * Test 44
	     * Xs.QName
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.QName t = new Xs.QName("xs:QName");
		    print("Xs.QName: "+ t);
		    
		    // rewrite
		    t.setValue(t.getValue());

		    Xs.QName n = new Xs.QName("preifx:host-xhost");
		    print("Xs.QName: "+ n);

                    // bad
                    try {
                        n = new Xs.QName("ola:kalle:kaka");
                        failed("should have failed: multiple colons");
                    } catch (ConfMException e) {
                    }
                    
                    // bad
                    try {
                        n = new Xs.QName("ola");
                        failed("should have failed: missing colon");
                    } catch (ConfMException e) {
                    }                    
                    // bad
                    try {
                        n = new Xs.QName("ola ola: ola ola");
                        failed("should have failed: contains spaces");
                    } catch (ConfMException e) {
                    }                    
		    // equals
		    Xs.QName n1 = new Xs.QName("xs:hunger");
		    Xs.QName n2 = new Xs.QName("xs:hunger");
                    print("n1: "+n1);
                    print("n2: "+n2);
                    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.QName");

		    // equals
		    Object o1 = new Xs.QName("nc:host");
		    Object o2 = new Xs.QName("nc:host  ");
                    print("n1: "+o1);
                    print("n2: "+o2);
		    if ( ! o1.equals(o2))
			failed("equals comparison failed for Xs.QName");

		}		   
                
	    },

	    /************************************************************
	     * Test 45
	     * Confd.Counter32 type
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Confd.Counter32 b = new Confd.Counter32(45);
		    print("Confd.Counter32: "+ b);

		    // rewrite
		    b.setValue(b.getValue());
		    
		    b = new Confd.Counter32(127);
		    print("Confd.Counter32: "+ b);
		    
		    b = new Confd.Counter32(65535);
		    print("Confd.Counter32: "+ b);
		    
		    try {
			b = new Confd.Counter32(0x100000000L);
			failed("Counter32 0x100000000L should not pass");
		    } catch (ConfMException e) {
		    }
		    
		    try {
			b = new Confd.Counter32(-1);
			failed("Counter32 -1 should not pass");
		    } catch (ConfMException e) {
		    }
		    // equals
		    Confd.Counter32 n1 = new Confd.Counter32(111);
		    Confd.Counter32 n2 = new Confd.Counter32(111);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Confd.Counter32");
		}
	    },

	    /************************************************************
	     * Test 46
	     * Confd.Counter64 type
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Confd.Counter64 b = new Confd.Counter64(45);
		    print("Confd.Counter64: "+ b);

		    // rewrite
		    b.setValue(b.getValue());
		    
		    b = new Confd.Counter64(127);
		    print("Confd.Counter64: "+ b);
		    
		    b = new Confd.Counter64(65535);
		    print("Confd.Counter64: "+ b);
		    
		    b = new Confd.Counter64(0x100000000L);
		    print("Confd.Counter64: "+ b);
		    
		    try {
			b = new Confd.Counter64(-1);
			failed("Counter64 -1 should not pass");
		    } catch (ConfMException e) {
		    }
		    // equals
		    Confd.Counter64 n1 = new Confd.Counter64(111);
		    Confd.Counter64 n2 = new Confd.Counter64(111);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Confd.Counter64");
		}
	    },


	    /************************************************************
	     * Test 47
	     * Confd.Gauge32 type
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Confd.Gauge32 b = new Confd.Gauge32(45);
		    print("Confd.Gauge32: "+ b);

		    // rewrite
		    b.setValue(b.getValue());
		    
		    b = new Confd.Gauge32(127);
		    print("Confd.Gauge32: "+ b);
		    
		    b = new Confd.Gauge32(65535);
		    print("Confd.Gauge32: "+ b);
		    
		    try {
			b = new Confd.Gauge32(0x100000000L);
			failed("Gauge32 0x100000000L should not pass");
		    } catch (ConfMException e) {
		    }
		    
		    try {
			b = new Confd.Gauge32(-1);
			failed("Gauge32 -1 should not pass");
		    } catch (ConfMException e) {
		    }
		    // equals
		    Confd.Gauge32 n1 = new Confd.Gauge32(111);
		    Confd.Gauge32 n2 = new Confd.Gauge32(111);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Confd.Gauge32");
		}
	    },

	    /************************************************************
	     * Test 48
	     * Xs.AnyURI
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.AnyURI b = new Xs.AnyURI("45xxx\n\t");
		    print("Xs.AnyURI: "+ b);

		    // rewrite
		    b.setValue(b.getValue());
		    
		    b = new Xs.AnyURI("dfdkl");
		    print("Xs.AnyURI: "+ b);
		    
		    b = new Xs.AnyURI("!\"&%&/&()=?");
		    print("Xs.AnyURI: "+ b);
		    
		    // equals
		    Xs.AnyURI n1 = new Xs.AnyURI("111");
		    Xs.AnyURI n2 = new Xs.AnyURI("111");
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.AnyURI");
		}
	    },

	    /************************************************************
	     * Test 49
	     * Xs.HexBinary
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Xs.HexBinary b = new Xs.HexBinary("4f4c41\n\t");
		    print("Xs.HexBinary: "+ b);
		    
		    // rewrite
		    b.setValue(b.getValue());
		    
		    b = new Xs.HexBinary(0xfffff);
		    print("Xs.HexBinary: "+ b);
		    
		    b = new Xs.HexBinary(0x121345235fcL);
		    print("Xs.HexBinary: "+ b);
		    
		    b = new Xs.HexBinary("  0102030405060708090a0b0c0d0e0f  ");
		    print("Xs.HexBinary: "+ b);
		    
		    // equals
		    Xs.HexBinary n1 = new Xs.HexBinary("1111");
		    Xs.HexBinary n2 = new Xs.HexBinary("1111");
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Xs.HexBinary");
		}
	    },
	 
	    /************************************************************
	     * Test 50
	     * Confd.HexList
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Confd.HexList b = new Confd.HexList("4f:4c:41\n\t");
		    print("Confd.HexList: "+ b);
		    
		    // rewrite
		    b.setValue(b.getValue());
		    
		    b = new Confd.HexList(" 0f:ff:ff ");
		    print("Confd.HexList: "+ b);
		    
		    b = new Confd.HexList("  01:02:03:04:05:06:07:08:09:0a:0b:0c:0d:0e:0f  ");
		    print("Confd.HexList: "+ b);

		    b = new Confd.HexList( 0xff0102 );
		    print("Confd.HexList: "+ b);

		    b = new Confd.HexList( 0xff0102111111L );
		    print("Confd.HexList: "+ b);
		    
		    // equals
		    Confd.HexList n1 = new Confd.HexList("11:11");
		    Confd.HexList n2 = new Confd.HexList("  11:11  ");
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Confd.HexList");
		}
	    },
	 
	    /************************************************************
	     * Test 51
	     * Confd.OID
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Confd.OID b = new Confd.OID("1.2.196.22412\n\t");
		    print("Confd.OID: "+ b);
		    
		    // rewrite
		    b.setValue(b.getValue());
		    
		    b = new Confd.OID(" 1.412.1.2.3.5.677.0.1 ");
		    print("Confd.OID: "+ b);
		    
		    b = new Confd.OID("  1111.2222.3333  ");
		    print("Confd.OID: "+ b);
		    
		    // equals
		    Confd.OID n1 = new Confd.OID("11.11");
		    Confd.OID n2 = new Confd.OID("  11.11  ");
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Confd.OID");
		}
	    },

	    /************************************************************
	     * Test 52
	     * Confd.OctetList
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Confd.OctetList b = new Confd.OctetList("1.2.196.224.12\n\t");
		    print("Confd.OctetList: "+ b);
		    
		    // rewrite
		    b.setValue(b.getValue());
		    
		    b = new Confd.OctetList(" 1.255.1.2.3.5.67.7.0.1 ");
		    print("Confd.OctetList: "+ b);
		    
		    b = new Confd.OctetList("  111.222.255.0  ");
		    print("Confd.OctetList: "+ b);
		    
		    // equals
		    Confd.OctetList n1 = new Confd.OctetList("11.11");
		    Confd.OctetList n2 = new Confd.OctetList("  11.11  ");
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Confd.OctetList");
		}
	    },

	    /************************************************************
	     * Test 53
	     * Confd.Ipv4Prefix
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Confd.Ipv4Prefix b = new Confd.Ipv4Prefix("192.168.0.1/8\n\t");
		    print("Confd.Ipv4Prefix: "+ b);
		    
		    // rewrite
		    b.setValue(b.getValue());
		    
		    b = new Confd.Ipv4Prefix(" 10.0.0.1/32 ");
		    print("Confd.Ipv4Prefix: "+ b);
		    
		    b = new Confd.Ipv4Prefix("  0.0.0.0/0  ");
		    print("Confd.Ipv4Prefix: "+ b);
		    
		    // equals
		    Confd.Ipv4Prefix n1 = new Confd.Ipv4Prefix("255.255.0.0/11");
		    Confd.InetAddressIPv4 ipaddr = new Confd.InetAddressIPv4("  255.255.0.0 ");
		    Confd.Ipv4Prefix n2 = new Confd.Ipv4Prefix( ipaddr, 11);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Confd.Ipv4Prefix");
		}
	    },

	    /************************************************************
	     * Test 54
	     * Confd.Ipv6Prefix
	     */
	    new TestCase() {
		// public boolean skip() { return true; }
		public void test() throws Exception {
		    Confd.Ipv6Prefix b = new Confd.Ipv6Prefix("  2001:DB8::1428:57AB/125\n\t");
		    print("Confd.Ipv6Prefix: "+ b);
		    
		    // rewrite
		    b.setValue(b.getValue());
		    
		    b = new Confd.Ipv6Prefix(" 10aa::1221/32 ");
		    print("Confd.Ipv6Prefix: "+ b);
		    
		    b = new Confd.Ipv6Prefix("  ffff:ffff::1234/0  ");
		    print("Confd.Ipv6Prefix: "+ b);
		    
		    // equals
		    Confd.Ipv6Prefix n1 = new Confd.Ipv6Prefix("2001:db8::1428:57ab/16");
		    Confd.InetAddressIPv6 ipaddr = new Confd.InetAddressIPv6("2001:0db8:0:0:0:0:1428:57ab");
		    Confd.Ipv6Prefix n2 = new Confd.Ipv6Prefix(ipaddr, 16);
		    if ( ! n1.equals(n2))
			failed("equals comparison failed for Confd.Ipv6Prefix");
		}
	    },
				    
	    
	};
	
	
	/************************************************************
	 * Run the tests
	 */

	int from = 0;
	int to   = tests.length;

	// from = 1;
	// to = 2;

	test.testnr = from;

	for(i = from ; i < to ; i++) 
	    tests[i].doit();

	/************************************************************
	 * Summary 
	 */
	System.err.println("---------------------------------------");
	System.err.println("  Passed:  " + test.pass);
	System.err.println("  Failed:  " + test.fail);
	System.err.println("  Skipped: " + test.skip);
	System.err.println("---------------------------------------");
	if (test.fail == 0)
	    System.exit(0);
	else
	    System.exit(1);
    }


    private static void testStart() {
	test.testnr++;
	test.processed=false;
	System.err.print("Test "+test.testnr+": ");
    }

    private static void passed() {
	if (!test.processed) {
	    test.processed = true;
	    test.pass++;
	    System.err.println("passed");
	}
    }

    private static void skipped() {
	if (!test.processed) {
	    test.processed = true;
	    test.skip++;
	    System.err.println("skipped");
	}
    }


    private static void failed(Exception e) {
	if (!test.processed) {
	    test.fail++;
	    test.processed = true;
	    System.err.println("failed");
	    System.err.println("    '"+e.toString()+"'");
	    e.printStackTrace();
	}
    }

    private static void failed(String reason) {
	if (!test.processed) {
	    test.fail++;
	    test.processed = true;
	    System.err.println("failed");
	    System.err.println("    '"+reason+"'");
	}
    }
}

