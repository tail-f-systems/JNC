/*    -*- Java -*-
 *
 *  Copyright 2012 Tail-F Systems AB. All rights reserved.
 *
 *  This software is the confidential and proprietary
 *  information of Tail-F Systems AB.
 *
 *  $Id$
 *
 */

package com.tailf.confm.yang;

import java.util.Arrays;

/**
 * A YANG statement. Can be used to represent a base identity for an
 * identityref leaf type, an instance node for an instance-identifier, etc.
 * 
 * @author emil@tail-f.com
 */
final class Statement {

    public static final String[] keywords = { "anyxml", "augment", "choice",
            "contact", "container", "description", "deviation", "extension",
            "feature", "grouping", "identity", "import", "include", "leaf",
            "leaf-list", "list", "namespace", "notification", "organization",
            "prefix", "reference", "revision", "rpc", "typedef", "uses",
            "yang-version" };

    /**
     * Statement keyword. One of the strings in the keywords array, unless this
     * statement was created as a yang extension.
     */
    private String keyword;

    /**
     * Statement argument. Identifier of a YANG statement.
     */
    private String arg;

    /**
     * The top level statement, if any.
     */
    private Statement top = null;

    /**
     * The statement that this object is a sub-statement of, if any.
     */
    private Statement parent = null;

    /**
     * The statement's sub-statements
     */
    private Statement[] substmts = {};

    /**
     * Main contructor.
     * 
     * @param keyword
     * @param arg
     * @param top
     * @param parent
     * @param substmts
     */
    public Statement(String keyword, String arg, Statement top,
            Statement parent, Statement[] substmts) {
        if (keyword == null || arg == null) {
            System.err.println("Error: keyword or arg null in Statement " +
            		"constructor");
            System.exit(-1);
        }
        if ((keyword != "module" && keyword != "submodule")
                && (top == null || parent == null)) {
            System.err.println("Warning: Parent or top statement not " +
            		"specified for statement: " + keyword + " " + arg);
        }
        
        // Assign statement keyword
        int keywordIndex = Arrays.binarySearch(keywords, keyword);
        if (keywordIndex < 0) {
            System.err.println("Warning: Keyword not found. This is an " +
                    "error unless the statement is an extension");
        }
        this.keyword = keyword;

        // Assign statement argument/identifier
        this.arg = arg;

        // Assign top level statement.
        if (top == null) {
            top = this;
        }
        this.top = top;

        // Assign parent statement.
        if (parent == null) {
            parent = this;
        }
        this.parent = parent;

        // Assign sub-statements
        if (substmts == null) {
            substmts = new Statement[0];
        }
        if ((keyword == "module" || keyword == "submodule" || keyword == "leaf"
                || keyword == "leaf-list" || keyword == "typedef")
                && substmts.length == 0) {
            System.err.println("Warning: Mandatory child statements missing" +
            		"from statement: " + keyword + " " + arg);
        }
        this.substmts = new Statement[substmts.length];
        System.arraycopy(substmts, 0, this.substmts, 0, substmts.length);
    }

    /**
     * Constructor with only keyword and arg, provided as a convenience. Note
     * that the resulting statement is not fully defined since it lacks
     * information about parent and children statements.
     * 
     * @param keyword
     * @param arg
     */
    public Statement(String keyword, String arg) {
        this(keyword, arg, null, null, new Statement[0]);
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public String getArg() {
        return arg;
    }
    
    public Statement getTop() {
        return top;
    }
    
    public Statement getParent() {
        return parent;
    }
    
    public Statement[] getSubstmts() {
        return substmts;
    }

}
