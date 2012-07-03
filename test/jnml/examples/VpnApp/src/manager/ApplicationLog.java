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
public class ApplicationLog implements MouseListener, ActionListener {
    JTextPane textPane;
    JScrollPane scrollPane;
    JTabbedPane tabbedPane;
    SimpleAttributeSet attrs = new SimpleAttributeSet();
    ImageIcon warningIcon;

    ApplicationLog(JTextPane textPane, JScrollPane scrollPane,
                   JTabbedPane tabbedPane) {
        this.textPane = textPane;
        this.scrollPane = scrollPane;
        this.tabbedPane = tabbedPane;
        warningIcon =
            new ImageIcon(getClass().getResource("/manager/warning.png"));
        textPane.setEditable(false);
        textPane.addMouseListener(this);
        StyleConstants.setFontFamily(attrs, "monospaced");
    }
    
    public void showOnlyMessage(String message) {
        showMessage(message, false);
    }
    
    public void showMessage(String message) {
        showMessage(message, true);
    }
    
    public void showMessage(String message, boolean showDialogBox) {
        Document document = textPane.getDocument();

        Date timestamp = new Date();
        showHeader(document, "**** (" + timestamp + ")\n");
	
        try {
            document.insertString(document.getLength(),
				  message + "\n\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
	
        int index = tabbedPane.indexOfComponent(scrollPane);
        tabbedPane.setIconAt(index, warningIcon);
        tabbedPane.setSelectedIndex(index);

        if (showDialogBox)
            if (message.length() < 72)
                JOptionPane.showMessageDialog(textPane, message);
	    else
                JOptionPane.showMessageDialog(textPane,
		    "A log entry has been written to the application log!");
        
	/*
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                scrollPane.getViewport().setViewPosition(new Point(0, 0));
            }
        });
	*/
    }
    
    void showHeader(Document document, String message) {
        SimpleAttributeSet headerAttrs = new SimpleAttributeSet();
        StyleConstants.setForeground(headerAttrs, Color.blue);
	
        try {
            document.insertString(document.getLength(),
				  message, headerAttrs);
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
            JMenuItem item = popupMenu.add("Clear Application Log");
            item.addActionListener(this);
            popupMenu.show(event.getComponent(), event.getX(), event.getY());
        }
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }
    
    public void actionPerformed(ActionEvent e) {
        try {
            Document document = textPane.getDocument();
	    int index = tabbedPane.indexOfComponent(scrollPane);
	    tabbedPane.setIconAt(index, null);
            document.remove(0, document.getLength());
        } catch (BadLocationException ex) {
        }
    }
}
