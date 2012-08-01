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

package com.tailf.netconfmanager.yang.type;

import java.util.ArrayList;
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
     * The top level statement, this if module.
     */
    private Statement top;

    /**
     * The statement that this object is a sub-statement of, this if module.
     */
    private Statement parent;

    /**
     * The statement's sub-statements
     */
    private ArrayList<Statement> substmts;

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
            Statement parent, ArrayList<Statement> substmts) {
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
            substmts = new ArrayList<Statement>();
        }
        if ((keyword == "module" || keyword == "submodule" || keyword == "leaf"
                || keyword == "leaf-list" || keyword == "typedef")
                && substmts.size() == 0) {
            System.err.println("Warning: Mandatory child statements missing" +
            		"from statement: " + keyword + " " + arg);
        }
        this.substmts = new ArrayList<Statement>(substmts);
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
        this(keyword, arg, null, null, null);
    }
    
    /**
     * @return Statement keyword
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * @return Statement argument/identifier.
     */
    public String getArg() {
        return arg;
    }
    
    /**
     * @return The top level statement.
     */
    public Statement getTop() {
        return top;
    }
    
    /**
     * @return The statement that this object is a sub-statement of, or this
     *         statement if module.
     */
    public Statement getParent() {
        return parent;
    }
    
    /**
     * @return List of sub-statements.
     */
    public ArrayList<Statement> getSubstmts() {
        return substmts;
    }
    
    /**
     * Adds a statement as a sub-statement. The parent and top of stmt are
     * changed to this statement, and the top of this statement, respectively.
     * 
     * @param stmt The statement to add as child to this statement.
     */
    public void addChild(Statement stmt) {
        stmt.top = this.top;
        stmt.parent = this;
        substmts.add(stmt);
    }
    
    /**
     * Searches non-recursively among the sub-statements of stmt after a
     * statement with specified keyword.
     * 
     * @param keyword To search for.
     * @param stmt To search in.
     * @return The argument/identifier of the first sub-statement of stmt that
     *         has the specified keyword, null if no such statement was found.
     */
    private static String searchOne(String keyword, Statement stmt) {
        for (Statement substmt : stmt.substmts) {
            if (substmt.keyword.equalsIgnoreCase(keyword))
                return substmt.arg;
        }
        return null;
    }
    
    /**
     * @return The prefix of the module containing this statement, null if no
     *         prefix can be found.
     */
    public String getPrefix() {
        return searchOne("prefix", this.top);
    }
    
    /**
     * @return The namespace of the module containing this statement, null if
     *         no namespace can be found.
     */
    public String getNamespace() {
        return searchOne("namespace", this.top);
    }

}
