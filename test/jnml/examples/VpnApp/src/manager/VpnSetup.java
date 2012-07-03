package manager;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;
import java.net.InetAddress;
import com.tailf.confm.*;

/**
 *
 * @author jocke
 * Beware: Nothing is this file has been generated
 */
public class VpnSetup {
    private DefaultMutableTreeNode rootNode, vpnContainerNode;
    private DefaultTreeModel treeModel;
    private JTree tree;
    NetworkCanvas networkCanvas;
    private Icon rootIcon, vpnIcon, routerIcon, endpointIcon, networkIcon,
            defaultIcon;
    private CustomNode rememberedCustomNode;
    Db db;
    Vapp vapp;

    public VpnSetup(JTree tree, NetworkCanvas networkCanvas) {
        this.tree = tree;
        this.networkCanvas = networkCanvas;
        ToolTipManager.sharedInstance().registerComponent(tree);        

        rootNode = new DefaultMutableTreeNode(new RootNode());
        treeModel = new DefaultTreeModel(rootNode);
        treeModel.addTreeModelListener(new MyTreeModelListener());
        tree.setModel(treeModel);

        tree.setEditable(true);
        tree.setShowsRootHandles(true);
        tree.getSelectionModel().setSelectionMode(
            TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.addMouseListener(mouseListener);
	tree.addTreeExpansionListener(new MyTreeExpansionListener());

        MyTreeCellRenderer treeCellRenderer = new MyTreeCellRenderer();
        tree.setCellRenderer(treeCellRenderer);

        tree.setCellEditor(new MyTreeCellEditor(tree, treeCellRenderer));

        tree.setRowHeight(20);
    }
    
    public void init(Db db, Vapp vapp) {
	this.db = db;
	this.vapp = vapp;
        addObject(rootNode, new DefaultEncryptionAlgoNode(
		      "Encryption: " + db.getDefaultEnc()));
	addObject(rootNode, new DefaultHashAlgoNode(
		      "Hash: " + db.getDefaultHash()));
        vpnContainerNode = addObject(rootNode, new VpnContainerNode("VPNs"));

	for (int i = 0; i < db.vpns.size(); i++) {
	    Vpn v = (Vpn)db.vpns.get(i);
	    DefaultMutableTreeNode vTree =
		addObject(vpnContainerNode, new VpnNode(v.name));
	    addStandardVpnEntries(vTree, v);
	    addVpnNetworks(vTree, v);
	}
	expandAll(tree, rootNode.children());
        allIsCommited();
	networkCanvas.refresh(vpnContainerNode);
    }

    void allIsCommited() {
	if (tree == null || vpnContainerNode == null)
	    return;
        
	Enumeration vpnEnumeration = vpnContainerNode.children();
	int vpnIndex = 0;
	String[] vpnName = new String[vpnContainerNode.getChildCount()];
        
	// VPNs
	while (vpnEnumeration.hasMoreElements()) {
	    // VPN
	    DefaultMutableTreeNode vpnTreeNode =
		(DefaultMutableTreeNode)vpnEnumeration.nextElement();
	    CustomNode vpnNode = (CustomNode)vpnTreeNode.getUserObject();
	    vpnName[vpnIndex] = vpnNode.label;            
                    
            TreePath vpnPath = new TreePath(vpnTreeNode.getPath());
            
            if (tree.isCollapsed(vpnPath)) {
                vpnIndex++;
                continue;
            }
                                        
	    // WEST
	    DefaultMutableTreeNode routerTreeNode =
		(DefaultMutableTreeNode)vpnTreeNode.getChildAt(3);
	    DefaultMutableTreeNode endpointTreeNode =
		(DefaultMutableTreeNode)routerTreeNode.getChildAt(0);
            
	    Enumeration networkEnumeration = endpointTreeNode.children();
            
	    while (networkEnumeration.hasMoreElements()) {
		DefaultMutableTreeNode networkTreeNode =
		    (DefaultMutableTreeNode)networkEnumeration.nextElement();
		NetworkNode networkNode =
		    (NetworkNode)networkTreeNode.getUserObject();
                networkNode.isCommited = true;
                networkTreeNode.setUserObject(networkNode);
	    }
            
     	    // NORTH
	    routerTreeNode =
		(DefaultMutableTreeNode)vpnTreeNode.getChildAt(4);
	    endpointTreeNode =
                (DefaultMutableTreeNode)routerTreeNode.getChildAt(0);
            
	    networkEnumeration = endpointTreeNode.children();
            
	    while (networkEnumeration.hasMoreElements()) {
		DefaultMutableTreeNode networkTreeNode =
		    (DefaultMutableTreeNode)networkEnumeration.nextElement();
		NetworkNode networkNode =
		    (NetworkNode)networkTreeNode.getUserObject();
                networkNode.isCommited = true;
                networkTreeNode.setUserObject(networkNode);
	    }       
            
      	    // EAST
	    routerTreeNode =
		(DefaultMutableTreeNode)vpnTreeNode.getChildAt(5);
	    endpointTreeNode =
                (DefaultMutableTreeNode)routerTreeNode.getChildAt(0);
            
	    networkEnumeration = endpointTreeNode.children();
            
	    while (networkEnumeration.hasMoreElements()) {
		DefaultMutableTreeNode networkTreeNode =
		    (DefaultMutableTreeNode)networkEnumeration.nextElement();
		NetworkNode networkNode =
		    (NetworkNode)networkTreeNode.getUserObject();
                networkNode.isCommited = true;
                networkTreeNode.setUserObject(networkNode);
	    }                 
            
            vpnIndex++;
	}
        
        networkCanvas.refresh(vpnContainerNode);
    }
    
    public void rePaint() {
	// Remove all VPN nodes
	ArrayList arr = new ArrayList();
	Enumeration e0 = vpnContainerNode.children();
	while (e0.hasMoreElements()) {
	    DefaultMutableTreeNode vpnTreeNode0 =
 		(DefaultMutableTreeNode)e0.nextElement();
	    CustomNode c0 = (CustomNode)vpnTreeNode0.getUserObject();
	    arr.add(vpnTreeNode0);
	}
	// Need to accumulate and then remove, 
	for (int i=0; i<arr.size(); i++) {
	    DefaultMutableTreeNode vpnTreeNode =
 		(DefaultMutableTreeNode) arr.get(i);
	    treeModel.removeNodeFromParent(vpnTreeNode);
	}
   
	// Repopulate from DB
	for (int i = 0;  i < db.vpns.size(); i++) {
	    Vpn v = (Vpn)db.vpns.get(i);
	    DefaultMutableTreeNode vTree =
		addObject(vpnContainerNode, new VpnNode(v.name));
	    addStandardVpnEntries(vTree, v);
	    addVpnNetworks(vTree, v);
	}
	
	expandAll(tree, rootNode.children());
	networkCanvas.refresh(vpnContainerNode);
    }
    
    void addStandardVpnEntries(DefaultMutableTreeNode node, Vpn v) {
	String secret, enc, hash;
	if (v != null) {
	    secret = v.sharedSecret; enc = v.encAlgo;
	    hash = v.hashAlgo;
	}
	else {
	    secret = "-"; enc = "default"; hash = "default";
	}

        addObject(node, new SharedSecretNode("Secret: " + secret));
        addObject(node, new EncryptionAlgoNode("Encryption: " + enc));
	addObject(node, new HashAlgoNode("Hash: " + hash));
	
	for (int j = 0; j < db.devices.size(); j++) {
	    VDevice d = (VDevice)db.devices.get(j);
	    DefaultMutableTreeNode rNode =
		addObject(node, new RouterNode(d.name));
	    addObject(rNode, new EndpointNode(d.endpointIp));
	}
    }
    
    void addVpnNetworks(DefaultMutableTreeNode vpn, Vpn v) {
	for (int j = 0; j < db.devices.size(); j++) {
	    VDevice d = (VDevice)db.devices.get(j);
	    int pos = 3;
	    if (d.name.equals("Router-West"))
		pos = 3;
	    else if (d.name.equals("Router-North"))
		pos = 4;
	    else if (d.name.equals("Router-East"))
		pos = 5;

	    DefaultMutableTreeNode edgeRouter =
		(DefaultMutableTreeNode)vpn.getChildAt(pos);
	    DefaultMutableTreeNode edgeRouterEndpoint =
		(DefaultMutableTreeNode)edgeRouter.getChildAt(0);

	    // Now we need to get the Networks for v at d
	    Endpoint ep = v.findEndpoint(d.endpointIp);
	    for (int i = 0; i < ep.networks.size(); i++) {
		Network n = (Network)ep.networks.get(i);
		addObject(edgeRouterEndpoint, 
			  new NetworkNode(n.net+"/"+n.masksize, true));
	    }
	}
    }
    // Validate the Net/mask input
    public static boolean splitValidate(String s, String[] ret) {
	try {
	    String[] a = s.split("/");
	    if (a.length != 2) {
		Vapp.log("Illegal format: use Net/Mask format", true);
		return false;
	    }
            InetAddress ip = Util.parseIPv4Address(a[0]);
            if (ip == null) {
                Vapp.log("Illegal IP Address", true);
                return false;
            }
            int iVal = Integer.parseInt(a[1]);
            if (iVal < 1 || iVal > 32) {
                Vapp.log("Masksize out of bounds", true);
                return false;
            }
            ret[0] = a[0]; 
            ret[1] = a[1];
            return true;
        }                
        catch (Exception e3) {
            Vapp.log("Illegal format: use Net/Mask format", true);
            return false;
        }
    }
    

    class RootNode extends CustomNode {     
        RootNode() {
            super("root", "root");
        }
    }
    
    class VpnContainerNode extends CustomNode implements ActionListener {     
        VpnContainerNode(String label) {
            super("vpn-container", label);
        }
        
        @Override
        JPopupMenu createPopup(DefaultMutableTreeNode node) {
            this.node = node;
            JPopupMenu menu = new JPopupMenu();
            JMenuItem item = menu.add("Add VPN");
            item.addActionListener(this);
            return menu; 
        }
        
        @Override
        public void actionPerformed(ActionEvent event) {
            DefaultMutableTreeNode vpnNode =
                addObject(node, new VpnNode("<New VPN>"));
            addStandardVpnEntries(vpnNode, null);
            TreePath vpnPath = new TreePath(vpnNode.getPath());
            tree.expandPath(vpnPath);
            tree.setSelectionPath(vpnPath);
            tree.startEditingAtPath(vpnPath);
        }
    }
    
    private void expandAll(JTree tree, Enumeration e) {
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node =
                (DefaultMutableTreeNode)e.nextElement();
            TreePath path = new TreePath(node.getPath());
            tree.collapsePath(path);
            expandAll(tree, node.children());
        }
    }
    
    class DefaultEncryptionAlgoNode extends CustomNode {
        DefaultEncryptionAlgoNode(String label) {
            super("default-encryption-algo", label);
        }
    }
    
    class DefaultHashAlgoNode extends CustomNode {
        DefaultHashAlgoNode(String label) {
            super("default-hash-algo", label);
        }
    }

    class SharedSecretNode extends CustomNode {
        SharedSecretNode(String label) {
            super("shared-secret", label);
        }
    }
    
    class EncryptionAlgoNode extends CustomNode {
        EncryptionAlgoNode(String label) {
            super("encryption-algo", label);
        }
    }
    
    class HashAlgoNode extends CustomNode {
        HashAlgoNode(String label) {
            super("hash-algo", label);
        }
    }
    
    class VpnNode extends CustomNode implements ActionListener {     
        VpnNode(String label) {
            super("vpn", label);
        }
        
        @Override
        JPopupMenu createPopup(DefaultMutableTreeNode node) {
            this.node = node;
            JPopupMenu menu = new JPopupMenu();
            JMenuItem item = menu.add("Delete VPN");
            item.addActionListener(this);
            return menu; 
        }
        
        @Override
        public void actionPerformed(ActionEvent event) {
            // The only action we receive here is a remove event!@!
            System.out.println("vpn node action performed: "+event);
            removeCurrentVpnNode();
        }
    }
    
    class RouterNode extends CustomNode {     
        RouterNode(String label) {
            super("router", label);
        }
    }
    
    class EndpointNode extends CustomNode implements ActionListener {     
        EndpointNode(String label) {
            super("endpoint", label);
        }
        
        @Override
        JPopupMenu createPopup(DefaultMutableTreeNode node) {
            this.node = node;
            JPopupMenu menu = new JPopupMenu();
            JMenuItem item = menu.add("Add Network");
            item.addActionListener(this);
            return menu; 
        }
        
        @Override
        public void actionPerformed(ActionEvent event) {
            DefaultMutableTreeNode networkNode =
                addObject(node, new NetworkNode("<New Network>"));
            tree.expandPath(tree.getSelectionPath());
            TreePath networkPath = new TreePath(networkNode.getPath());
            tree.setSelectionPath(networkPath);
            tree.startEditingAtPath(networkPath);
        }
    }

    class NetworkNode extends CustomNode implements ActionListener {     
        NetworkNode(String label) {
            super("network", label);
            System.err.println("NEW NETWORK NODE: "+label); 
        }

        NetworkNode(String label, boolean isCommited) {
            super("network", label, isCommited);
            System.err.println("NEW NETWORK NODE: "+label); 
        }
        
        @Override
        JPopupMenu createPopup(DefaultMutableTreeNode node) {
            this.node = node;
            JPopupMenu menu = new JPopupMenu();
            JMenuItem item = menu.add("Delete Network");
            item.addActionListener(this);            
            return menu; 
        }
        
        @Override
        public void actionPerformed(ActionEvent event) {
            removeCurrentNetworkNode();
        }
    }
        
    public void removeCurrentVpnNode() {
        TreePath currentSelection = tree.getSelectionPath();
         if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
                         (currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
            
            if (parent != null) {     
		Object[] arr = currentNode.getPath();
		String vpnName = ((DefaultMutableTreeNode)arr[2]).toString(); 
		try {
		    System.out.println("Delete " + vpnName);
		    vapp.delVpn(vpnName);
		    treeModel.removeNodeFromParent(currentNode);
		} catch (Exception e3) {
		    e3.printStackTrace();
		}
                return;
            }
        } 
    }

    public void removeCurrentNetworkNode() {
        TreePath currentSelection = tree.getSelectionPath();
 
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
                         (currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
            
            if (parent != null) {     
		 String[] sarr = new String[2];
		 CustomNode cust = (CustomNode)currentNode.getUserObject();
		 System.out.println("Delete the Network node " + cust);
		 if (!VpnSetup.splitValidate(cust.label, sarr)) {
		     treeModel.removeNodeFromParent(currentNode);
		     return;
		 }
		 try {
		     Object[] arr = currentNode.getPath();
		     String vpnName =
			 ((DefaultMutableTreeNode)arr[2]).toString(); 
		     String deviceName =
			 ((DefaultMutableTreeNode)arr[3]).toString();
		     String endpointIp =
			 ((DefaultMutableTreeNode)arr[4]).toString();
		     vapp.delNetworkFromVpn(vpnName, endpointIp,
					    sarr[0], Integer.parseInt(sarr[1]));
		 } catch (Exception e2) {
		     e2.printStackTrace();
		 }
		 treeModel.removeNodeFromParent(currentNode);
		 return;
            }
        } 
    } 
    
    public DefaultMutableTreeNode addObject(Object child) {
        DefaultMutableTreeNode parentNode = null;
        TreePath parentPath = tree.getSelectionPath();

        if (parentPath == null)
            parentNode = vpnContainerNode;
        else
            parentNode =
                (DefaultMutableTreeNode)(parentPath.getLastPathComponent());

        return addObject(parentNode, child, true);
    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child) {
        return addObject(parent, child, false);
    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child, 
                                            boolean shouldBeVisible) {
        DefaultMutableTreeNode childNode = 
                new DefaultMutableTreeNode(child);

        if (parent == null)
            parent = vpnContainerNode;
	
        treeModel.insertNodeInto(childNode, parent, 
                                 parent.getChildCount());

        if (shouldBeVisible)
            tree.scrollPathToVisible(new TreePath(childNode.getPath()));
        
        return childNode;
    }
    
    private MouseListener mouseListener = 
        new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent event) {
                handlePopup(event);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                handlePopup(event);
            }

            private void handlePopup(MouseEvent event) {
                if (event.isPopupTrigger()) {
                    TreePath path =
                        tree.getPathForLocation(event.getX(), event.getY());

                    if (path != null) {
                        tree.getSelectionModel().setSelectionPath(path);
                        DefaultMutableTreeNode node =
                            (DefaultMutableTreeNode)path.getLastPathComponent();
                        CustomNode customNode =
                            (CustomNode)node.getUserObject();
                        
                        if (customNode.type != null) {
                            JPopupMenu popup = customNode.createPopup(node);

                            if (popup != null)
                                popup.show(event.getComponent(), event.getX(),
                                           event.getY());
                        }
                    }
                }
            }
        };   
        
    class MyTreeModelListener implements TreeModelListener {
        public void treeNodesChanged(TreeModelEvent e) {
             DefaultMutableTreeNode node =
                 (DefaultMutableTreeNode)
 		(e.getTreePath().getLastPathComponent());
             /*
              * If the event lists children, then the changed node is the child
              * of the node we've already gotten. Otherwise, the changed node
              * and the specified node are the same.
              */
             int index = e.getChildIndices()[0];
             node = (DefaultMutableTreeNode)(node.getChildAt(index));
	     
	     CustomNode cust = (CustomNode) node.getUserObject();
	     if (cust instanceof NetworkNode) {
		 System.out.println("Created the Network node " + cust);
		 System.out.println("Prev label = " + cust.prevLabel);
		 // Start by splitting the label into a net/bits 
		 String[] sarr = new String[2];
		 if (!VpnSetup.splitValidate(cust.label, sarr)) {
		     cust.label = new String(cust.prevLabel);
		     networkCanvas.refresh(vpnContainerNode);
		     return;
		 }
		 
		 TreePath tp = e.getTreePath();
		 Object[] arr = tp.getPath();
		 String vpnName = ((DefaultMutableTreeNode)arr[2]).toString(); 
		 String deviceName =
		     ((DefaultMutableTreeNode)arr[3]).toString();
		 String endpointIp =
		     ((DefaultMutableTreeNode)arr[4]).toString();
		 try {
		     if (cust.firstTime ) {
			 cust.firstTime = false;
			 vapp.addNetworkToVpn(vpnName, deviceName, endpointIp,
					      sarr[0],
					      Integer.parseInt(sarr[1]));
		     }
		     else {
			 // Old node is edited with a new network
			 String[] sarr2 = new String[2];
			 VpnSetup.splitValidate(cust.prevLabel, sarr2);
			 vapp.changeNetworkInVpn(vpnName, deviceName,
						 endpointIp, sarr[0],
						 Integer.parseInt(sarr[1]),
						 sarr2[0],
						 Integer.parseInt(sarr2[1])
			     );
		     }
		 } catch (Exception e2) {
		     e2.printStackTrace();
		     cust.label = new String(cust.prevLabel);
		     networkCanvas.refresh(vpnContainerNode);
		     return;
		 }
 
	     }
	     else if (cust instanceof VpnNode) {
		 if (cust.firstTime) {
		     System.out.println("Created the Vpn " + cust);
		     try {
			 String secret = "-";
			 String enc  = "default";
			 String hash = "default";
			 vapp.addVpn(cust.label, secret, enc, hash);
		     } catch (Exception e3) {
			 cust.label = "failed";
			 e3.printStackTrace();
			 networkCanvas.refresh(vpnContainerNode);
			 return;
		     }
		 }
		 else {
		     System.out.println("Renamed Vpn " + cust +
					" with prevLabel = " + cust.prevLabel);
		 }
	     }
             cust.firstTime = false;
	     networkCanvas.refresh(vpnContainerNode);
	     
        }

        public void treeNodesInserted(TreeModelEvent e) {
            networkCanvas.refresh(vpnContainerNode);        
	}

	public void printE(String s, TreeModelEvent e) {
	    System.out.println("A: "+s);
	    TreePath tp = e.getTreePath();
	    System.out.println("tp = " + tp);
	    Object[] arr = tp.getPath();
	    for (int i=0; i<arr.length; i++) {
		DefaultMutableTreeNode node =  (DefaultMutableTreeNode)arr[i];
		CustomNode customNode =
		    (CustomNode)node.getUserObject();
		System.out.println("i=" + i + " " + customNode);
	    }
	}

        public void treeNodesRemoved(TreeModelEvent e) {
            System.out.println("The user has removed a node" + e);
	    networkCanvas.refresh(vpnContainerNode);
	}
        

        public void treeStructureChanged(TreeModelEvent e) {
            System.out.println("The user has changed the node structure");
            networkCanvas.refresh(vpnContainerNode);            
        }
    }
    
    class MyTreeCellRenderer extends DefaultTreeCellRenderer {
        public MyTreeCellRenderer() {
            rootIcon =
		new ImageIcon(getClass().getResource("/manager/root.png"));
            vpnIcon =
		new ImageIcon(getClass().getResource("/manager/vpn.png"));
            routerIcon =
                new ImageIcon(getClass().getResource("/manager/router.png"));
            endpointIcon =
                new ImageIcon(getClass().getResource("/manager/endpoint.png"));
            networkIcon =
                new ImageIcon(getClass().getResource("/manager/network.png"));
            defaultIcon =
                new ImageIcon(getClass().getResource("/manager/default.png"));       
        }
        
        @Override
        public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded,
                                               leaf, row, hasFocus);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            CustomNode customNode = (CustomNode)(node.getUserObject());

            if (customNode.type.equals("root")) {
                return this;
            }
	    
            if (customNode.type.equals("vpn-container")) {
                setIcon(rootIcon);
                setToolTipText("VPNs");
                return this;
            }

            if (customNode.type.equals("vpn")) {
                setIcon(vpnIcon);
                setToolTipText("VPN");
                return this;
            }
            
            if (customNode.type.equals("router")) {
                setIcon(routerIcon);
                setToolTipText("Router");
                return this;
            }                             
               
            if (customNode.type.equals("endpoint")) {
                setIcon(endpointIcon);
                setToolTipText("Endpoint");
                return this;
            }
            
            if (customNode.type.equals("network")) {
                setIcon(networkIcon);
                setToolTipText("Network");
                return this;
            }
            
            if (customNode.type.equals("default-encryption-algo")) {
                setIcon(defaultIcon);
                setToolTipText("Default encryption algorithm");
                return this;
            }
            
            if (customNode.type.equals("default-hash-algo")) {
                setIcon(defaultIcon);
                setToolTipText("Default hash algorithm");
                return this;
            }      

            if (customNode.type.equals("shared-secret")) {
                setIcon(defaultIcon);
                setToolTipText("Shared secret");
                return this;
            }

            if (customNode.type.equals("encryption-algo")) {
                setIcon(defaultIcon);
                setToolTipText("Encryption algorithm");
                return this;
            }
            
            if (customNode.type.equals("hash-algo")) {
                setIcon(defaultIcon);
                setToolTipText("Hash algorithm");
                return this;
            }
            
            setToolTipText(null);
            return this;
        }
    }
    
    class MyTreeCellEditor extends DefaultTreeCellEditor {
        MyTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
            super(tree, renderer);
        }
	
        @Override
	public boolean isCellEditable(EventObject event) {
            return true;
        }
        
        // We must remember the user object or else it will revert back
        // to a string and all will crash and burn!
        @Override
	public Component getTreeCellEditorComponent(
	         JTree tree, Object value, boolean selected, boolean expanded,
		 boolean leaf, int row) {
            DefaultMutableTreeNode node =
		(DefaultMutableTreeNode)value;   
            rememberedCustomNode = (CustomNode)node.getUserObject();
	    
	    if (rememberedCustomNode.type.equals("default-encryption-algo")) {
		String[] choices =
		    {"des", "3des", "blowfish", "cast128", "aes"};
		final JComboBox comboBox = new JComboBox(choices);
                comboBox.setSelectedItem(rememberedCustomNode.label);
                comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
			    System.out.println(
			        "**** New default encryption algo: "+
				comboBox.getSelectedItem());
			    vapp.setDefaultEncryption(
				comboBox.getSelectedItem().toString());
			    rememberedCustomNode.prevLabel =
				rememberedCustomNode.label;
                            rememberedCustomNode.label =
                                "Encryption: "+comboBox.getSelectedItem();
			    stopCellEditing();
			}
		    });
		return (Component)comboBox;
	    } else if (rememberedCustomNode.type.equals("default-hash-algo")) {
		String[] choices = {"md5", "sha1", "sha256"};
		final JComboBox comboBox = new JComboBox(choices);
		comboBox.setSelectedItem(rememberedCustomNode.label);
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
			    System.out.println("**** New default hash algo: "+
					       comboBox.getSelectedItem());
			    vapp.setDefaultHash(
			        comboBox.getSelectedItem().toString());
			    rememberedCustomNode.prevLabel =
				rememberedCustomNode.label;
			    rememberedCustomNode.label =
				"Hash: "+comboBox.getSelectedItem();
			    stopCellEditing();
			}
		    });
		return (Component)comboBox;
	    } else if (rememberedCustomNode.type.equals("encryption-algo")) {
		String[] choices =
		    {"default", "des", "3des", "blowfish", "cast128", "aes"};
		final JComboBox comboBox = new JComboBox(choices);
                comboBox.setSelectedItem(rememberedCustomNode.label);
                comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
			    System.out.println(
			      "**** New encryption algo: "+
			      comboBox.getSelectedItem()+" ("+
			      lastPath+")");
			    DefaultMutableTreeNode vpnNode =
				(DefaultMutableTreeNode)
                                    lastPath.getPathComponent(
				        lastPath.getPathCount()-2);
			    String vpnName = vpnNode.toString();
			    vapp.setEncAlgo(vpnName,
			        comboBox.getSelectedItem().toString());
			    rememberedCustomNode.prevLabel =
				rememberedCustomNode.label;
                            rememberedCustomNode.label =
                                "Encryption: "+comboBox.getSelectedItem();
			    stopCellEditing();
			}
		    });
		return (Component)comboBox;
	    } else if (rememberedCustomNode.type.equals("hash-algo")) {
		String[] choices = {"default", "md5", "sha1", "sha256"};
		final JComboBox comboBox = new JComboBox(choices);
		comboBox.setSelectedItem(rememberedCustomNode.label);
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
			    System.out.println(
			      "**** New hash algo: "+
			      comboBox.getSelectedItem()+" ("+
			      lastPath+")");
			    DefaultMutableTreeNode vpnNode =
				(DefaultMutableTreeNode)
                                    lastPath.getPathComponent(
				        lastPath.getPathCount()-2);
			    String vpnName = vpnNode.toString();
			    vapp.setHashAlgo(vpnName,
			        comboBox.getSelectedItem().toString());
			    rememberedCustomNode.prevLabel =
				rememberedCustomNode.label;
			    rememberedCustomNode.label =
				"Hash: "+comboBox.getSelectedItem();
			    stopCellEditing();
			}
		    });
		return (Component)comboBox;
	    } else if (rememberedCustomNode.type.equals(
		           "shared-secret")) {
		rememberedCustomNode.label =
		    rmLabel(rememberedCustomNode.label);
		return super.getTreeCellEditorComponent(tree, value, selected,
							expanded, leaf, row);
	    } else
		return super.getTreeCellEditorComponent(tree, value, selected,
							expanded, leaf, row);
	}
	
        // Patch and return the remembered user object
	public Object getCellEditorValue() {
            Object value = super.getCellEditorValue();
            
            if (rememberedCustomNode == null)
                return value;
            else {
		if (rememberedCustomNode.type.equals("shared-secret")) {
		    String path = null;
		    System.out.println("**** New shared secret: "+
				       value+" ("+lastPath+")");
		    DefaultMutableTreeNode vpnNode =
			(DefaultMutableTreeNode)
			    lastPath.getPathComponent(
			        lastPath.getPathCount()-2);
		    String vpnName = vpnNode.toString();
		    String secret = value.toString();
		    vapp.setSharedSecret(vpnName, secret);
		    rememberedCustomNode.prevLabel = rememberedCustomNode.label;
                    rememberedCustomNode.label = "Secret: "+value;
		} else			
		    if (!(rememberedCustomNode.type.equals(
			      "default-encryption-algo") ||
			  rememberedCustomNode.type.equals(
			      "default-hash-algo") ||
			  rememberedCustomNode.type.equals(
			      "encryption-algo") ||
			  rememberedCustomNode.type.equals(
			      "hash-algo"))) {
			rememberedCustomNode.prevLabel =
			    rememberedCustomNode.label;
			rememberedCustomNode.label = (String)value;
		    }
		
                return rememberedCustomNode;
            }
	}

	String rmLabel(String value) {
	    int n = value.toString().indexOf(" ");
	    return value.substring(n+1);
	}
	
        // http://lists.apple.com/archives/Java-dev/2005/Aug/msg00120.html
        // It took 12hours to figure this out!
        @Override
        protected void determineOffset(JTree tree, Object value,
                       boolean isSelected, boolean expanded, boolean leaf,
                       int row) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            CustomNode customNode = (CustomNode)(node.getUserObject());
            
            if (customNode.type.equals("vpn-container")) 
                editingIcon = rootIcon;
            else if (customNode.type.equals("vpn"))
                editingIcon = vpnIcon;
            else if (customNode.type.equals("router"))
                editingIcon = routerIcon;
            else if (customNode.type.equals("endpoint"))
                editingIcon = endpointIcon;
            else if (customNode.type.equals("network"))
                editingIcon = networkIcon;
            else if (customNode.type.equals("default-encryption-algo"))
                editingIcon = defaultIcon;
            else if (customNode.type.equals("default-hash-algo"))
                editingIcon = defaultIcon;
            else if (customNode.type.equals("shared-secret"))
                editingIcon = defaultIcon;
            else if (customNode.type.equals("encryption-algo"))
                editingIcon = defaultIcon;
            else if (customNode.type.equals("hash-algo"))
                editingIcon = defaultIcon;
            else
                editingIcon = null;

            if(editingIcon != null)
                offset = renderer.getIconTextGap()+editingIcon.getIconWidth();
            else
                offset = renderer.getIconTextGap();
        }
    }
    
    class MyTreeExpansionListener implements TreeExpansionListener {
        public void treeExpanded(TreeExpansionEvent event) {
            // Only repaint if a VPN is expanded
            if (event.getPath().getPathCount() == 3)
                networkCanvas.refresh(vpnContainerNode);
        }
	
        public void treeCollapsed(TreeExpansionEvent event) {
             // Only repaint if a VPN is collapsed
            if (event.getPath().getPathCount() == 3)
                networkCanvas.refresh(vpnContainerNode);
        }
    }
}
