/*
 * VpnFrame.java
 *
 * Created on December 17, 2007, 5:01 PM (gazonk)
 */

package manager;
import javax.swing.*;
import javax.swing.tree.*;

/**
 *
 * @author  jocke
 */
public class VpnFrame extends javax.swing.JFrame {
    VpnSetup vpnSetup;
    ApplicationLog log = null;
    NetconfLog consoleWest = null;
    NetconfLog consoleNorth = null;
    NetconfLog consoleEast = null;
    NetconfLog consoleInterior = null;
    ConfirmCommitCountDownThread confirmCommitThread; 
    private int timeout = -1;

    Vapp vapp;
    Db db;
    /** Creates new form testframe */
    public VpnFrame() {
        initComponents();
        toolbarProbe.setVisible(false); // not used for now
        toolbarStatistics.setVisible(false); // not used for now
        ((NetworkCanvas)networkCanvas).setTree(jTree1); // hack
        hideConfirmCommitButtons();
        vpnSetup = new VpnSetup(jTree1, (NetworkCanvas)networkCanvas);
        mainSplitPane.setOneTouchExpandable(true);
        mainSplitPane.setResizeWeight(0);
        mainSplitPane.setContinuousLayout(true);
        
        networkSplitPane.setOneTouchExpandable(true);
        networkSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        networkSplitPane.setResizeWeight(0.8);
        networkSplitPane.setContinuousLayout(true); 

        log = new ApplicationLog(applicationLogTextPane,
                                 applicationLogScrollPane, logTabbedPane);
	db = new Db(log);
	vapp = new Vapp(db, log);
	vapp.vpnSetup = vpnSetup;
	vapp.vpnFrame = this;
	((NetworkCanvas)networkCanvas).db = db;     // hack
	((NetworkCanvas)networkCanvas).vapp = vapp; // hack
	vpnSetup.init(db, vapp);
	vapp.startTrans();

	VDevice d;
        consoleWest =
            new NetconfLog(routerWestLogTextPane, routerWestLogScrollPane,
                           logTabbedPane);

	d = db.getVDevice("Router-West");
	d.setConsole(consoleWest);

	if (d.createSession((NetworkCanvas)networkCanvas))
	    log.showOnlyMessage(d.name+": Established SSH session");
	else {
	    ((NetworkCanvas)networkCanvas).disableRouter(d.name);
	    log.showOnlyMessage
		(d.name+ ": failed to establish init session with ");
	}
        consoleNorth =
            new NetconfLog(routerNorthLogTextPane, routerNorthLogScrollPane,
                           logTabbedPane);

	d = db.getVDevice("Router-North");
	d.setConsole(consoleNorth);

	if (d.createSession((NetworkCanvas)networkCanvas))
	    log.showOnlyMessage(d.name+": Established SSH session");
	else  {
	    ((NetworkCanvas)networkCanvas).disableRouter(d.name);
	    log.showOnlyMessage
		(d.name+ ": failed to establish init session with ");
	}

        consoleEast =
            new NetconfLog(routerEastLogTextPane, routerEastLogScrollPane,
                           logTabbedPane);

	d = db.getVDevice("Router-East");
	d.setConsole(consoleEast);

	if (d.createSession((NetworkCanvas)networkCanvas))
	    log.showOnlyMessage(d.name+": Established SSH session");
	else {
	    ((NetworkCanvas)networkCanvas).disableRouter(d.name);
	    log.showOnlyMessage
		(d.name+ ": failed to establish init session with ");
	}
    }

    NetworkCanvas getNetworkCanvas() {
	return (NetworkCanvas)networkCanvas;
    }
    
    void showConfirmCommitButtons() {
        toolbarConfirmCommit.setVisible(true);
        toolbarRejectCommit.setVisible(true);
        toolbarCommit.setVisible(false);
    }
    
    void hideConfirmCommitButtons() {
        toolbarConfirmCommit.setVisible(false);
        toolbarRejectCommit.setVisible(false);
        toolbarCommit.setVisible(true);       
    }

    class MyJTree extends JTree {
	public boolean isPathEditable(TreePath path) {
	    DefaultMutableTreeNode node;
	    String type;

            switch (path.getPathCount()) {
                case 1: // root
                    return false;
	        case 2: // Visible top-level entries
		    node =
			(DefaultMutableTreeNode)(path.getLastPathComponent());
		    type = ((CustomNode)node.getUserObject()).type;
		    
		    if (type.equals("default-encryption-algo"))
			return true;
		    
		    if (type.equals("default-hash-algo"))
			return true;
		    
		    return false;
                case 3: // Vpn
                    return true;
                case 4: // Router
		    node =
			(DefaultMutableTreeNode)(path.getLastPathComponent());
		    type = ((CustomNode)node.getUserObject()).type;
		    
		    if (type.equals("shared-secret"))
			return true;

		    if (type.equals("encryption-algo"))
			return true;
		    
		    if (type.equals("hash-algo"))
			return true;
		    
		    return false;
                case 5: // Endpoint
                    return false;
                case 6: // Network
                    return true;
                default:
                    return false;
            }
        }    
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        toolbarValidate = new javax.swing.JButton();
        toolbarInspect = new javax.swing.JButton();
        toolbarSync = new javax.swing.JButton();
        toolbarProbe = new javax.swing.JButton();
        toolbarStatistics = new javax.swing.JButton();
        toolbarCommit = new javax.swing.JButton();
        toolbarConfirmCommit = new javax.swing.JButton();
        toolbarRejectCommit = new javax.swing.JButton();
        toolbarAbort = new javax.swing.JButton();
        toolbarExit = new javax.swing.JButton();
        mainSplitPane = new javax.swing.JSplitPane();
        networkSplitPane = new javax.swing.JSplitPane();
        networkCanvas = new NetworkCanvas();
        logTabbedPane = new javax.swing.JTabbedPane();
        routerWestLogScrollPane = new javax.swing.JScrollPane();
        routerWestLogTextPane = new javax.swing.JTextPane();
        routerNorthLogScrollPane = new javax.swing.JScrollPane();
        routerNorthLogTextPane = new javax.swing.JTextPane();
        routerEastLogScrollPane = new javax.swing.JScrollPane();
        routerEastLogTextPane = new javax.swing.JTextPane();
        applicationLogScrollPane = new javax.swing.JScrollPane();
        applicationLogTextPane = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new MyJTree();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Vpn Management");

        jToolBar1.setRollover(true);

        toolbarValidate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/manager/validate.png"))); // NOI18N
        toolbarValidate.setText("Validate");
        toolbarValidate.setToolTipText("Validate Network Configuration");
        toolbarValidate.setFocusable(false);
        toolbarValidate.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolbarValidate.setMaximumSize(new java.awt.Dimension(70, 52));
        toolbarValidate.setMinimumSize(new java.awt.Dimension(70, 52));
        toolbarValidate.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbarValidate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolbarValidateActionPerformed(evt);
            }
        });
        jToolBar1.add(toolbarValidate);

        toolbarInspect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/manager/inspect.png"))); // NOI18N
        toolbarInspect.setText("Inspect");
        toolbarInspect.setToolTipText("Is synchronization needed?");
        toolbarInspect.setFocusable(false);
        toolbarInspect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolbarInspect.setMaximumSize(new java.awt.Dimension(70, 52));
        toolbarInspect.setMinimumSize(new java.awt.Dimension(70, 52));
        toolbarInspect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbarInspect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolbarInspectActionPerformed(evt);
            }
        });
        jToolBar1.add(toolbarInspect);

        toolbarSync.setIcon(new javax.swing.ImageIcon(getClass().getResource("/manager/sync.png"))); // NOI18N
        toolbarSync.setText("Sync");
        toolbarSync.setToolTipText("Synchronize Network");
        toolbarSync.setFocusable(false);
        toolbarSync.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolbarSync.setMaximumSize(new java.awt.Dimension(70, 52));
        toolbarSync.setMinimumSize(new java.awt.Dimension(70, 52));
        toolbarSync.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbarSync.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolbarSyncActionPerformed(evt);
            }
        });
        jToolBar1.add(toolbarSync);

        toolbarProbe.setIcon(new javax.swing.ImageIcon(getClass().getResource("/manager/probe.png"))); // NOI18N
        toolbarProbe.setText("Probe");
        toolbarProbe.setToolTipText("Probe Network");
        toolbarProbe.setEnabled(false);
        toolbarProbe.setFocusable(false);
        toolbarProbe.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolbarProbe.setMaximumSize(new java.awt.Dimension(70, 52));
        toolbarProbe.setMinimumSize(new java.awt.Dimension(70, 52));
        toolbarProbe.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbarProbe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolbarProbeActionPerformed(evt);
            }
        });
        jToolBar1.add(toolbarProbe);

        toolbarStatistics.setIcon(new javax.swing.ImageIcon(getClass().getResource("/manager/statistics.png"))); // NOI18N
        toolbarStatistics.setText("Statistics");
        toolbarStatistics.setToolTipText("Show Network Statistics");
        toolbarStatistics.setEnabled(false);
        toolbarStatistics.setFocusable(false);
        toolbarStatistics.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolbarStatistics.setMaximumSize(new java.awt.Dimension(70, 52));
        toolbarStatistics.setMinimumSize(new java.awt.Dimension(70, 52));
        toolbarStatistics.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbarStatistics.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolbarStatisticsActionPerformed(evt);
            }
        });
        jToolBar1.add(toolbarStatistics);

        toolbarCommit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/manager/commit.png"))); // NOI18N
        toolbarCommit.setText("Commit");
        toolbarCommit.setToolTipText("Commit Changes to Network");
        toolbarCommit.setFocusable(false);
        toolbarCommit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolbarCommit.setMaximumSize(new java.awt.Dimension(70, 52));
        toolbarCommit.setMinimumSize(new java.awt.Dimension(70, 52));
        toolbarCommit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbarCommit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolbarCommitActionPerformed(evt);
            }
        });
        jToolBar1.add(toolbarCommit);

        toolbarConfirmCommit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/manager/confirm_commit.png"))); // NOI18N
        toolbarConfirmCommit.setText("Confirm");
        toolbarConfirmCommit.setFocusable(false);
        toolbarConfirmCommit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolbarConfirmCommit.setMaximumSize(new java.awt.Dimension(70, 52));
        toolbarConfirmCommit.setMinimumSize(new java.awt.Dimension(70, 52));
        toolbarConfirmCommit.setPreferredSize(new java.awt.Dimension(54, 52));
        toolbarConfirmCommit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbarConfirmCommit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolbarConfirmCommitActionPerformed(evt);
            }
        });
        jToolBar1.add(toolbarConfirmCommit);

        toolbarRejectCommit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/manager/reject_commit.png"))); // NOI18N
        toolbarRejectCommit.setText("Reject");
        toolbarRejectCommit.setFocusable(false);
        toolbarRejectCommit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolbarRejectCommit.setMaximumSize(new java.awt.Dimension(70, 52));
        toolbarRejectCommit.setMinimumSize(new java.awt.Dimension(70, 52));
        toolbarRejectCommit.setPreferredSize(new java.awt.Dimension(54, 52));
        toolbarRejectCommit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbarRejectCommit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolbarRejectCommitActionPerformed(evt);
            }
        });
        jToolBar1.add(toolbarRejectCommit);

        toolbarAbort.setIcon(new javax.swing.ImageIcon(getClass().getResource("/manager/abort.png"))); // NOI18N
        toolbarAbort.setText("Abort");
        toolbarAbort.setFocusable(false);
        toolbarAbort.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolbarAbort.setMaximumSize(new java.awt.Dimension(70, 52));
        toolbarAbort.setMinimumSize(new java.awt.Dimension(70, 52));
        toolbarAbort.setPreferredSize(new java.awt.Dimension(54, 52));
        toolbarAbort.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbarAbort.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolbarAbortActionPerformed(evt);
            }
        });
        jToolBar1.add(toolbarAbort);

        toolbarExit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/manager/exit.png"))); // NOI18N
        toolbarExit.setText("Exit");
        toolbarExit.setToolTipText("Exit Configuration Session");
        toolbarExit.setFocusable(false);
        toolbarExit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        toolbarExit.setMaximumSize(new java.awt.Dimension(70, 52));
        toolbarExit.setMinimumSize(new java.awt.Dimension(70, 52));
        toolbarExit.setPreferredSize(new java.awt.Dimension(34, 52));
        toolbarExit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolbarExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toolbarExitActionPerformed(evt);
            }
        });
        jToolBar1.add(toolbarExit);

        networkCanvas.setBorder(javax.swing.BorderFactory.createTitledBorder("Network Topology"));

        org.jdesktop.layout.GroupLayout networkCanvasLayout = new org.jdesktop.layout.GroupLayout(networkCanvas);
        networkCanvas.setLayout(networkCanvasLayout);
        networkCanvasLayout.setHorizontalGroup(
            networkCanvasLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 90, Short.MAX_VALUE)
        );
        networkCanvasLayout.setVerticalGroup(
            networkCanvasLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 716, Short.MAX_VALUE)
        );

        networkSplitPane.setLeftComponent(networkCanvas);

        logTabbedPane.setBorder(javax.swing.BorderFactory.createTitledBorder("Logs"));

        routerWestLogScrollPane.setViewportView(routerWestLogTextPane);

        logTabbedPane.addTab("Router West Log", routerWestLogScrollPane);

        routerNorthLogScrollPane.setViewportView(routerNorthLogTextPane);

        logTabbedPane.addTab("Router North Log", routerNorthLogScrollPane);

        routerEastLogScrollPane.setViewportView(routerEastLogTextPane);

        logTabbedPane.addTab("Router East Log", routerEastLogScrollPane);

        applicationLogScrollPane.setViewportView(applicationLogTextPane);

        logTabbedPane.addTab("Application Log", applicationLogScrollPane);

        networkSplitPane.setRightComponent(logTabbedPane);

        mainSplitPane.setRightComponent(networkSplitPane);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("VPN Setup"));

        jTree1.setFocusable(false);
        jTree1.setRootVisible(false);
        jTree1.setShowsRootHandles(true);
        jScrollPane1.setViewportView(jTree1);

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE)
                .addContainerGap())
        );

        mainSplitPane.setLeftComponent(jPanel1);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jToolBar1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1293, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, mainSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1293, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jToolBar1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(mainSplitPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 752, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void toolbarValidateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolbarValidateActionPerformed
        // Keep all logs on shift-click else clear
        if ((evt.getModifiers() & java.awt.event.ActionEvent.SHIFT_MASK) == 0) {
            NetconfLog.clearAllLogs();
            NetconfLog.unmarkAllLogs();            
        }

	vapp.validateButton();
    }//GEN-LAST:event_toolbarValidateActionPerformed

    private void toolbarInspectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolbarInspectActionPerformed
        // Keep all logs on shift-click else clear
        if ((evt.getModifiers() & java.awt.event.ActionEvent.SHIFT_MASK) == 0) {
            NetconfLog.clearAllLogs();
            NetconfLog.unmarkAllLogs();            
        }

	vapp.inspectButton();
}//GEN-LAST:event_toolbarInspectActionPerformed

    private void toolbarSyncActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolbarSyncActionPerformed
        // Keep all logs on shift-click else clear
        if ((evt.getModifiers() & java.awt.event.ActionEvent.SHIFT_MASK) == 0) {
            NetconfLog.clearAllLogs();
            NetconfLog.unmarkAllLogs();            
        }

	vapp.syncButton();
    }//GEN-LAST:event_toolbarSyncActionPerformed

    private void toolbarProbeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolbarProbeActionPerformed
        // Keep all logs on shift-click else clear
        if ((evt.getModifiers() & java.awt.event.ActionEvent.SHIFT_MASK) == 0) {
            NetconfLog.clearAllLogs();
            NetconfLog.unmarkAllLogs();            
        }

	vapp.probeButton();
    }//GEN-LAST:event_toolbarProbeActionPerformed

    private void toolbarStatisticsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolbarStatisticsActionPerformed
        // Keep all logs on shift-click else clear
        if ((evt.getModifiers() & java.awt.event.ActionEvent.SHIFT_MASK) == 0) {
            NetconfLog.clearAllLogs();
            NetconfLog.unmarkAllLogs();            
        }

	vapp.statsButton();
    }//GEN-LAST:event_toolbarStatisticsActionPerformed

    private void toolbarCommitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolbarCommitActionPerformed
        String string;
        timeout = -1;        
        while (true) {
            string =
                (String)JOptionPane.showInputDialog(this,
                    "Timeout [seconds] (0 = no timeout)", "Commit", -1, null,
                    null, "0");
            if (string == null) // Cancel button
                return;;
            try {
                timeout = Integer.parseInt(string);
            } catch (NumberFormatException e) {
                continue;
            }
                      
            if (timeout >= 0 && timeout < 3600)
                break;
        }

        // Keep all logs on shift-click else clear
        if ((evt.getModifiers() & java.awt.event.ActionEvent.SHIFT_MASK) == 0) {
            NetconfLog.clearAllLogs();
            NetconfLog.unmarkAllLogs();            
        }
        
        if (timeout == 0 || timeout == -1)
            vapp.commitButton(0, null);
        else {            
	    confirmCommitThread =
                new ConfirmCommitCountDownThread(timeout, vapp, this);
            if (vapp.commitButton(timeout, confirmCommitThread)) {
		showConfirmCommitButtons();
		confirmCommitThread.start();
	    }
	    else {
		confirmCommitThread.stop();
	    }
	}
    }//GEN-LAST:event_toolbarCommitActionPerformed

    private void toolbarExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolbarExitActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Do you really want to exit?", "Exit now?",
                                          JOptionPane.YES_NO_OPTION) == 0)
            System.exit(0);
    }//GEN-LAST:event_toolbarExitActionPerformed
    
    private void toolbarAbortActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolbarAbortActionPerformed
        // Keep all logs on shift-click else clear
        if ((evt.getModifiers() & java.awt.event.ActionEvent.SHIFT_MASK) == 0) {
            NetconfLog.clearAllLogs();
            NetconfLog.unmarkAllLogs();            
        }

	vapp.abortButton();
}//GEN-LAST:event_toolbarAbortActionPerformed

    private void toolbarConfirmCommitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolbarConfirmCommitActionPerformed
        //vapp.conirmCommitButton(); // FIXME: klacke
        hideConfirmCommitButtons();
        confirmCommitThread.stop();
	vapp.confirmCommitButton(timeout);
    }//GEN-LAST:event_toolbarConfirmCommitActionPerformed

    private void toolbarRejectCommitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toolbarRejectCommitActionPerformed
        hideConfirmCommitButtons();
        confirmCommitThread.stop();
	vapp.rejectCommitButton(timeout);
    }//GEN-LAST:event_toolbarRejectCommitActionPerformed
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
	//    Main.main(args);
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new VpnFrame().setVisible(true);
            }
        });
    }
    
    public javax.swing.JButton getAckButton() {
	return toolbarConfirmCommit;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane applicationLogScrollPane;
    private javax.swing.JTextPane applicationLogTextPane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JTree jTree1;
    private javax.swing.JTabbedPane logTabbedPane;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JPanel networkCanvas;
    private javax.swing.JSplitPane networkSplitPane;
    private javax.swing.JScrollPane routerEastLogScrollPane;
    private javax.swing.JTextPane routerEastLogTextPane;
    private javax.swing.JScrollPane routerNorthLogScrollPane;
    private javax.swing.JTextPane routerNorthLogTextPane;
    private javax.swing.JScrollPane routerWestLogScrollPane;
    private javax.swing.JTextPane routerWestLogTextPane;
    private javax.swing.JButton toolbarAbort;
    private javax.swing.JButton toolbarCommit;
    private javax.swing.JButton toolbarConfirmCommit;
    private javax.swing.JButton toolbarExit;
    private javax.swing.JButton toolbarInspect;
    private javax.swing.JButton toolbarProbe;
    private javax.swing.JButton toolbarRejectCommit;
    private javax.swing.JButton toolbarStatistics;
    private javax.swing.JButton toolbarSync;
    private javax.swing.JButton toolbarValidate;
    // End of variables declaration//GEN-END:variables
    
}
