package manager;

import com.tailf.confm.*;
import com.tailf.inm.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.font.*;

/**
 *
 * @author jocke
 */
public class NetworkCanvas extends JPanel {
    JTree tree = null;
    final static Color[] color =
       {Color.BLUE, Color.ORANGE, Color.GREEN, Color.PINK, Color.CYAN};
    Graphics2D g2;
    BasicStroke defaultStroke, dashedStroke;
    BufferedImage offScreen = null;
    FontMetrics metrics;
    double fontHeight;
    
    Router routerWest;
    Router routerNorth;
    Router routerEast;
    public Db db;
    public Vapp vapp;

    String[] vpnName = null;            
    Network[] westNetwork = {};
    Network[] northNetwork = {};
    Network[] eastNetwork = {};
 
    final static int WEST = 0;
    final static int NORTH = 1;
    final static int EAST = 2;

    boolean routerWestEnabled = true;
    boolean routerNorthEnabled = true;
    boolean routerEastEnabled = true;

    DefaultMutableTreeNode vpnContainerNode;

    Image logo;
    
    public NetworkCanvas() {
        addMouseListener(mouseListener);

	ImageIcon imageIcon =
	    new ImageIcon(getClass().getResource("/manager/tail-f.png"));
	logo = imageIcon.getImage();
	
        //initContext();
    }

    public void setTree(JTree tree) {
        this.tree = tree;
    }
    
    private int routerNameToNum(String router) {
	if (router.equals("Router-West"))
	    return WEST;
	else if (router.equals("Router-North"))
	    return NORTH;
	else if (router.equals("Router-East"))
	    return EAST;
	else return -1;
    }

    public void enableRouter(String router) {
	enableRouter(routerNameToNum(router));
    }

    public void enableRouter(int router) {
	switch (router) {
	case WEST:
	    routerWestEnabled = true;
	    break;
	case NORTH:
	    routerNorthEnabled = true;
	    break;
	default:
	    routerEastEnabled = true;
	}
	
	refresh(vpnContainerNode);
    }

    public void disableRouter(String router) {
	disableRouter(routerNameToNum(router));
    }

    public void disableRouter(int router) {
	switch (router) {
	case WEST:
	    routerWestEnabled = false;
	    break;
	case NORTH:
	    routerNorthEnabled = false;
	    break;
	default:
	    routerEastEnabled = false;
	}

	refresh(vpnContainerNode);
    }
    
    public void refresh(DefaultMutableTreeNode vpnContainerNode) {
	if (tree == null || vpnContainerNode == null)
	    return;
	
	this.vpnContainerNode = vpnContainerNode;
	vpnName = new String[vpnContainerNode.getChildCount()];
	Enumeration vpnEnumeration = vpnContainerNode.children();
	int vpnIndex = 0;
        
	ArrayList westNetworkList = new ArrayList();
	ArrayList northNetworkList = new ArrayList();
	ArrayList eastNetworkList = new ArrayList();	

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
		CustomNode networkNode =
		    (CustomNode)networkTreeNode.getUserObject();
		westNetworkList.add(new Network(vpnIndex,
                                                networkNode.label,
                                                networkNode.isCommited));
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
		CustomNode networkNode =
		    (CustomNode)networkTreeNode.getUserObject();
		northNetworkList.add(new Network(vpnIndex,
                                                 networkNode.label,
                                                 networkNode.isCommited));
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
		CustomNode networkNode =
		    (CustomNode)networkTreeNode.getUserObject();
		eastNetworkList.add(new Network(vpnIndex,
                                                networkNode.label,
                                                networkNode.isCommited));
	    }                 
            
            vpnIndex++;
	}
	
	westNetwork = makeNetworkArray(westNetworkList);
        northNetwork = makeNetworkArray(northNetworkList);
        eastNetwork = makeNetworkArray(eastNetworkList);        
        
	repaint();
    }

    Network[] makeNetworkArray(ArrayList arrayList) {
        Network[] network = new Network[arrayList.size()];
        Iterator iterator = arrayList.iterator();
        int i = 0;
        
        while (iterator.hasNext())
            network[i++] = (Network)iterator.next();

        return network;
    }
    
    // For testing...
    /*
    private void initContext() {
        vpnName = new String[] {"Volvo", "SAAB"};
        
    	westNetwork = new Network[] {
            new Network(0, "10.2.1.18/24"),
            new Network(0, "128.12.18.1/16"),
	    new Network(1, "210.12.2.34/24")
        };
        
        northNetwork = new Network[] {
            new Network(0, "20.13.2.1/12"),
            new Network(1, "192.1.16.22/6")
        };

    	eastNetwork = new Network[] {
            new Network(0, "192.12.1.2/23")
        };
    }
    */

    @Override
    public void paintComponent(Graphics g) {
        if (vpnName == null)
            return;
        
        Graphics2D visibleG2 = (Graphics2D)g;
        int panelWidth = getWidth();
        int panelHeight = getHeight();

        if(offScreen == null) {
            offScreen = (BufferedImage)createImage(panelWidth, panelHeight);
            g2 = (Graphics2D)offScreen.getGraphics();
        } else
            if (offScreen.getWidth() != panelWidth ||
                offScreen.getHeight() != panelHeight) {
                offScreen = 
                    (BufferedImage) createImage(panelWidth, panelHeight);
                g2 = (Graphics2D)offScreen.getGraphics();
            } else
                g2 = (Graphics2D)offScreen.getGraphics();
                
        initGraphics(visibleG2);
        initGraphics(g2);
        
        g2.clearRect(0, 0, panelWidth, panelHeight);

        g2.drawImage(logo, panelWidth-logo.getWidth(this)-10, 15, this);
        		
	double routerWidth = 100.0;
	double routerHeight = 40.0;
        //double up = panelHeight/4;
	double up = panelHeight/8*2;
	//double down = panelHeight/4*3;
	double down = panelHeight/8*6;
	//double left = panelWidth/5;
	double left = panelWidth/8;
	//double right = panelWidth/5*4;
	double right = panelWidth/8*7;
	double middle = panelWidth/2;

	double routerX = left-routerWidth/2;
	double routerY = down-routerHeight/2;	
	routerWest =
            new Router(NetworkCanvas.WEST, routerX, routerY, routerWidth,
                       routerHeight, 
		       db.getVDevice("Router-West").endpointIp,
		       westNetwork);

	routerX = middle-routerWidth/2;
	routerY = up-routerHeight/2;
	routerNorth =
            new Router(NetworkCanvas.NORTH, routerX, routerY, routerWidth,
		       routerHeight, 
		       db.getVDevice("Router-North").endpointIp,
		       northNetwork);

	routerX = right-routerWidth/2;
	routerY = down-routerHeight/2;
        routerEast =
            new Router(NetworkCanvas.EAST, routerX, routerY, routerWidth,
		       routerHeight, 
		       db.getVDevice("Router-East").endpointIp,
		       eastNetwork);

	double initGap = -120.0;
        double gap = 30.0;

        // WEST <-> EAST
        double westAnchorX = left+routerWidth/2;
	double westAnchorY = down;
        double eastAnchorX = right-routerWidth/2;
	double eastAnchorY = down;
        int k = 0;
        
	Font font = g2.getFont();
	FontRenderContext frc = g2.getFontRenderContext();

        for (int i = 0; i < westNetwork.length; i++)
            for (int j = 0; j < eastNetwork.length; j++)
                if (westNetwork[i].vpn == eastNetwork[j].vpn) {
                    double currentGap = initGap+(k++)*gap;
                    String label =
                        westNetwork[i].ipAddress+" - "+eastNetwork[j].ipAddress;

                    if (westNetwork[i].isCommited && eastNetwork[j].isCommited)
                        g2.setStroke(defaultStroke);
                    else
                        g2.setStroke(dashedStroke);
                    
                    if ((int)currentGap != 0)
                        drawCurve(westAnchorX, westAnchorY, currentGap,
                                  eastAnchorX, eastAnchorY, g2, label,
                                  color[westNetwork[i].vpn], font, frc);
                } 
        
        // NORTH <-> EAST
        double northAnchorX = middle+routerWidth/2;
	double northAnchorY = up;
        eastAnchorX = right;
	eastAnchorY = down-routerHeight/2;
        k = 0;
        
        for (int i = 0; i < northNetwork.length; i++)
            for (int j = 0; j < eastNetwork.length; j++)
                if (northNetwork[i].vpn == eastNetwork[j].vpn) {
                    double currentGap = initGap+(k++)*gap;
                    String label =
                        northNetwork[i].ipAddress+" - "+
                            eastNetwork[j].ipAddress;                    

                    if (northNetwork[i].isCommited && eastNetwork[j].isCommited)
                        g2.setStroke(defaultStroke);
                    else
                        g2.setStroke(dashedStroke);

                    if ((int)currentGap != 0)
                        drawCurve(northAnchorX, northAnchorY, -currentGap,
                                  eastAnchorX, eastAnchorY, g2, label,
                                  color[northNetwork[i].vpn], font, frc);
                }
        
        // WEST <-> NORTH
        westAnchorX = left;
	westAnchorY = down-routerHeight/2;
        northAnchorX = middle-routerWidth/2;
	northAnchorY = up;
        k = 0;
        
        for (int i = 0; i < westNetwork.length; i++)
            for (int j = 0; j < northNetwork.length; j++)
                if (westNetwork[i].vpn == northNetwork[j].vpn) {
                    double currentGap = initGap+(k++)*gap;
                    String label =
                        westNetwork[i].ipAddress+" - "+
                        northNetwork[j].ipAddress;

                    if (westNetwork[i].isCommited && northNetwork[j].isCommited)
                        g2.setStroke(defaultStroke);
                    else
                        g2.setStroke(dashedStroke);

                    if ((int)currentGap != 0)
                        drawCurve(westAnchorX, westAnchorY, -currentGap,
                                  northAnchorX, northAnchorY, g2, label,
                                  color[westNetwork[i].vpn], font, frc);
                }
        
        visibleG2.drawImage(offScreen, 0, 0, this);
    };

    void initGraphics(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                           RenderingHints.VALUE_ANTIALIAS_ON);
        defaultStroke =
            new BasicStroke(2.0f, BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND, 10.0f, null, 0.0f);
        g.setStroke(defaultStroke);

	float dash[] = {5.0f};
	dashedStroke =
	    new BasicStroke(2.0f, BasicStroke.CAP_ROUND,
			    BasicStroke.JOIN_ROUND, 10.0f, dash, 0.0f);
	
        Font font = new Font("SansSerif", Font.PLAIN, 10);
        g.setFont(font);
        metrics = g.getFontMetrics(font);
        fontHeight = metrics.getHeight();
    }

    void drawCurve(double x1, double y1, double r, double x2, double y2,
                   Graphics2D g2, String label, Color color, Font font,
                   FontRenderContext frc) {
        double x = x2-x1;
        double y = y2-y1;
        Point2D.Double ctrl = new Point2D.Double();
        AffineTransform myTrans = new AffineTransform();
        double theta = Math.atan(y/x);
        //System.out.println("theta: "+theta);
        myTrans.rotate(theta);
        double length = Math.sqrt(x*x+y*y);
        //System.out.println("length: "+length);
        double ctrlx = length/2;
        double ctrly = r;
        myTrans.transform(new Point2D.Double(ctrlx, ctrly), ctrl);

        QuadCurve2D.Double curve =
            new QuadCurve2D.Double(x1, y1, ctrl.x+x1, ctrl.y+y1, x2, y2);
        new CurveLabel(g2, curve, label, color, font, frc);
    }
    
    class Router implements ActionListener {
        double routerX;
        double routerY;
        double routerWidth;
        double routerHeight;
	int type;
	
	Router(int type, double routerX, double routerY, double routerWidth,
	       double routerHeight, String endpointLabel, Network[] network) {
            this.routerX = routerX;
            this.routerY = routerY;
            this.routerWidth = routerWidth;
            this.routerHeight = routerHeight;
	    this.type = type;
            
	    // Router body
	    if ((type == WEST && routerWestEnabled) ||
		(type == NORTH && routerNorthEnabled) ||
		(type == EAST && routerEastEnabled))
                g2.draw(new RoundRectangle2D.Double(routerX, routerY,
						    routerWidth, routerHeight,
						    10, 10));
	    else {
		Color oldColor = g2.getColor();
		g2.setColor(Color.RED);
		g2.setStroke(dashedStroke);
		g2.draw(new RoundRectangle2D.Double(routerX, routerY,
						    routerWidth, routerHeight,
						    10, 10));
		g2.setStroke(defaultStroke);
		g2.setColor(oldColor);
	    }
	    
	    // Router labels
	    String routerLabel =
		type == WEST ? "West" : type == NORTH ? "North" : "East";
	    drawString("Router "+routerLabel, routerX, routerY, routerWidth,
		       routerHeight/2);
	    drawString(endpointLabel, routerX, routerY+routerHeight/2,
		       routerWidth, routerHeight/2);
	    
	    // Networks
	    if (type == NORTH) {
		double networkY = routerY-fontHeight/2;
		
		for (int i = 0; i < network.length; i++) {
		    double networkX = routerX+routerWidth;
		    
		    // Label
		    double stringWidth =
			metrics.stringWidth(vpnName[network[i].vpn]);
		    g2.setColor(color[network[i].vpn]);
		    drawString(vpnName[network[i].vpn], networkX-stringWidth,
			       networkY);
		    g2.setColor(Color.BLACK);
		    
		    // IP address
		    String address = network[i].ipAddress+" - ";
		    stringWidth += metrics.stringWidth(address);
		    drawString(address, networkX-stringWidth, networkY);
		    
		    networkY -= fontHeight;
		}
	    } else if (type == WEST) {
		double networkY = routerY+routerHeight+fontHeight;
		
		for (int i = 0; i < network.length; i++) {
		    double networkX = routerX+routerWidth;
		    
		    // Label
		    double stringWidth =
			metrics.stringWidth(vpnName[network[i].vpn]);
		    g2.setColor(color[network[i].vpn]);
		    drawString(vpnName[network[i].vpn], networkX-stringWidth,
			       networkY);
		    g2.setColor(Color.BLACK);
		    
		    // IP address
		    String address = network[i].ipAddress+" - ";
		    stringWidth += metrics.stringWidth(address);
		    drawString(address, networkX-stringWidth, networkY);
		    
		    networkY += fontHeight;
		}
	    } else { // EAST
		double networkY = routerY+routerHeight+fontHeight;
		
		for (int i = 0; i < network.length; i++) {
		    double networkX = routerX;			
		    
		    // IP address
		    String address = network[i].ipAddress+" - ";
		    drawString(address, networkX, networkY);
		    double stringWidth = metrics.stringWidth(address);
		    
		    // Label
		    g2.setColor(color[network[i].vpn]);
		    drawString(vpnName[network[i].vpn], networkX+stringWidth,
			       networkY);
		    g2.setColor(Color.BLACK);
		    
		    networkY += fontHeight;
		}		    
	    }
        }
        
        boolean within(int x, int y) {
          return (x > routerX && x < routerX+routerWidth &&
                  y > routerY && y < routerY+routerHeight);
        }

        private JPopupMenu createPopup() {
            JPopupMenu menu = new JPopupMenu();
            JMenuItem item = menu.add("Show Backlog");
            JMenuItem item2 = menu.add("Diff Uncommited");
            JMenuItem item3 = menu.add("Check Sync");
            JMenuItem item4 = menu.add("Sync");
            JMenuItem item5 = menu.add("Edit with webUI");
            item.addActionListener(this);
            item2.addActionListener(this);
            item3.addActionListener(this);
            item4.addActionListener(this);
            item5.addActionListener(this);
            return menu;
        }

        public void actionPerformed(ActionEvent e) {
	    VDevice d = null;
	    switch (type) {
	    case WEST:
		d = db.getVDevice("Router-West");
		break;
	    case EAST:
		d = db.getVDevice("Router-East");
		break;
	    case NORTH:
		d = db.getVDevice("Router-North");
		break;
	    }
	    if (d == null) 
		return;
            String cmd = e.getActionCommand();
	    //System.out.println("CMD = " + cmd);
	    if (cmd.compareTo("Diff Uncommited") == 0) {
		vapp.prune(d);
                if (!d.hasConfig("cfg")) {
                    Vapp.log(d, "Nothing to do");
                    return;
                }   
                Vapp.log(d, d.getConfig("cfg").toXMLString());
            }
	    
	    else if (cmd.compareTo("Check Sync") == 0) {
		try {
		    synchronized (vapp) { 
			String str = vapp.checkSync(d);
			if (str.length() == 0)
			    Vapp.log(d, "in sync", true);
			else {
			    Vapp.log(d, str);
			    Vapp.log(d, "NOT in sync", true);
			}
		    }
		}
		catch (Exception e2) {
		    Vapp.log(d, "Error " + e2);
		}
	    }
	    else if (cmd.compareTo("Sync") == 0) {
		try {
		    synchronized (vapp) { 
			String reason = new String();
			boolean b = vapp.makeSync(d);
			if (b) {
			    quagga.System s = (quagga.System)d.getConfig("cfg");
			    if (s == null)  {  
				Vapp.log(d, "already uptodate", true); 
				return;
			    }
			    d.getSession("cfg").editConfig(NetconfSession.RUNNING, s);
			    d.clearConfig("cfg");
			    Vapp.log(d, "in sync", true);
			}
			else {
			    Vapp.log(d, "Cannot sync");
			}
			vapp.startTrans();
		    }
		} catch (Exception e3) {
		    e3.printStackTrace();
		    Vapp.log(d, "Error " + e3);
		}
	    } else if (cmd.compareTo("Edit with webUI") == 0) {
		LaunchBrowser.openURL(d.webuiUrl);
	    } else if (cmd.compareTo("Show Backlog") == 0) {
		vapp.showBacklog(d);
	    }
        }
    }
    
    void drawString(String string, double x, double y) {
	g2.drawString(string, Math.round(x), Math.round(y));
    }
    
    void drawString(String string, double x, double y, double width,
		    double height) {
	drawString(metrics, fontHeight, string, x, y, width, height);
    }
    
    void drawString(FontMetrics metrics, double fontHeight, String string,
		    double x, double y, double width, double height) {
        double stringWidth = metrics.stringWidth(string);
	double middleY =
	    height == 0 ? y-fontHeight/2 : y+height/2+fontHeight/3; // 3???
	g2.drawString(string, Math.round(x+width/2-stringWidth/2),
		      Math.round(middleY));
    }

    class Network {
	int vpn;
	String ipAddress;
        boolean isCommited;
	
	Network(int vpn, String ipAddress, boolean isCommited) {
	    this.vpn = vpn;
	    this.ipAddress = ipAddress;
            this.isCommited = isCommited;
	}
    }
    
    private  MouseListener mouseListener = 
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
                    JPopupMenu popup = null;
                    
                    if (routerWest.within(event.getX(), event.getY()))
                        popup = routerWest.createPopup();
                    else if (routerNorth.within(event.getX(), event.getY()))
                        popup = routerNorth.createPopup();
                    else if (routerEast.within(event.getX(), event.getY()))
                        popup = routerEast.createPopup();     
                        
                    if (popup != null)
                        popup.show(event.getComponent(), event.getX(),
                                   event.getY());
                }
            }
        };
}
