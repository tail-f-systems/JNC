package manager;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.text.*;

/**
 *
 * @author jocke
 */

public class NetconfLog implements MouseListener, ActionListener {
    JTextPane textPane;
    JScrollPane scrollPane;
    SimpleAttributeSet attrs = new SimpleAttributeSet();
    JTabbedPane tabbedPane;
    ImageIcon warningIcon;
    static ArrayList allLogs = new ArrayList();
    
    NetconfLog(JTextPane textPane, JScrollPane scrollPane,
               JTabbedPane tabbedPane) {
        this.textPane = textPane;
        this.scrollPane = scrollPane;
        this.tabbedPane = tabbedPane;
        warningIcon =
            new ImageIcon(getClass().getResource("/manager/warning.png"));
        textPane.setEditable(false);
        textPane.addMouseListener(this);
        StyleConstants.setFontFamily(attrs, "monospaced");
	NetconfLog.allLogs.add(this);
    }
    
    public void sentXML(String payload, String from) {
	sentXML(payload, from, true);
    }

    public void sentXML(String payload, String from, boolean addHeader) {
        Document document = textPane.getDocument();
	String nl = "";
	if (addHeader) {
	    nl = "\n\n";
	    Date timestamp = new Date();
	    showHeader(document, ">>>>> "+from+" ("+timestamp+")\n" , Color.green);	
	}
	else {
	    payload = "OUT: " + payload;
	}
	if (!addHeader) {System.out.print(payload); System.out.flush();}
        try {
            document.insertString(document.getLength(), payload + nl, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        
        int index = tabbedPane.indexOfComponent(scrollPane);
        tabbedPane.setIconAt(index, warningIcon);
        tabbedPane.setSelectedIndex(index);        
        
	/*
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                scrollPane.getViewport().setViewPosition(new Point(0,0));
            }
        });
	*/
    }
    
    public void receivedXML(String payload, String from) {
	receivedXML(payload, from , true);
    }
    public void receivedXML(String payload, String from, boolean  addHeader) {
        Document document = textPane.getDocument();
	String nl = "";
	if (addHeader) {
	    nl = "\n\n";
	    Date timestamp = new Date();
	    showHeader(document, "<<<< "+from+" ("+timestamp+")\n", Color.red);
	}
	else {
	    payload = "IN:  " + payload;
	}
	if (!addHeader) {System.out.print(payload); System.out.flush();}
        try {
            document.insertString(document.getLength(), payload + nl, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        
	/*
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                scrollPane.getViewport().setViewPosition(new Point(0,0));
            }
        });
	*/
    }
    
    void showHeader(Document document, String message, Color color) {
	SimpleAttributeSet headerAttrs = new SimpleAttributeSet();
	StyleConstants.setForeground(headerAttrs, color);
	
	try {
	    document.insertString(document.getLength(), message, headerAttrs);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }    
    
    public void mouseClicked(MouseEvent e) {
        int index = tabbedPane.indexOfComponent(scrollPane);
        tabbedPane.setIconAt(index, null);
    }
    
    public void mousePressed(MouseEvent event) {
        handlePopup(event);
    }

    public void mouseReleased(MouseEvent event) {
        handlePopup(event);
    }

    private void handlePopup(MouseEvent event) {
        if (event.isPopupTrigger()) {
            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem clearItem = popupMenu.add("Clear Router Log");
            clearItem.addActionListener(this);
            JMenuItem clearAllItem = popupMenu.add("Clear All Router Logs");
            clearAllItem.addActionListener(this);
            JMenuItem unmarkAllItem = popupMenu.add("Unmark All Router Logs");
            unmarkAllItem.addActionListener(this);
            popupMenu.show(event.getComponent(), event.getX(), event.getY());
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
    
    public void actionPerformed(ActionEvent e) {
	String command = e.getActionCommand();
	
	if (command.equals("Clear Router Log")) {
	    clearLog();
	    unmarkLog();
	} else
	    if (command.equals("Clear All Router Logs")) {
		NetconfLog.clearAllLogs();
		NetconfLog.unmarkAllLogs();
	    } else
		NetconfLog.unmarkAllLogs();
    }
    
    void clearLog() {
	try {
	    Document document = textPane.getDocument();
	    document.remove(0, document.getLength());
        } catch (BadLocationException ex) {
        }
    }
    
    void unmarkLog() {
	int index = tabbedPane.indexOfComponent(scrollPane);
	tabbedPane.setIconAt(index, null);
    }
    
    static void clearAllLogs() {
	int len = allLogs.size();
	
	for (int i = 0; i < len; i++)
	    ((NetconfLog)allLogs.get(i)).clearLog();
    }
    
    static void unmarkAllLogs() {
	int len = allLogs.size();
	
	for (int i = 0; i < len; i++)
	    ((NetconfLog)allLogs.get(i)).unmarkLog();
    }
}
