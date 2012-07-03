package manager;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
 *
 * @author jocke
 */
abstract public class CustomNode {    
    String type;
    String label;
    String prevLabel = null;
    DefaultMutableTreeNode node;
    public boolean firstTime = true;
    public boolean isCommited = false;

    CustomNode(String type, String label) {
        System.err.println("NEW CUSTOM NODE: "+label); 
        this.type = type;
        this.label = label;
    }

    CustomNode(String type, String label, boolean isCommited) {
        System.err.println("NEW CUSTOM NODE: "+label); 
        this.type = type;
        this.label = label;
        this.isCommited = isCommited;
    }
    
    JPopupMenu createPopup(DefaultMutableTreeNode node) {
        return null;
    }
        
    @Override
    public String toString() {
        return label;
    }

    public void actionPerformed(ActionEvent event) {
    }
}
