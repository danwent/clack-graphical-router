

package net.clackrouter.gui.tcp;


import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.net.InetAddress;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.clackrouter.application.TCPSocket;
import net.clackrouter.component.tcp.TCB;

/**  
 * View of all current TCB's that exist on a local clack TCP stack.
 * 
 * Modified from code from Richard Stanford provided as part of Java tutorial at 
 * http://java.sun.com/docs/books/tutorial/uiswing/components/tree.html
 * 
 * <p> To be incorporated into the TCP hierarchical component, showing each
 * socket open in the stack, and each connection to one of those sockets. 
 */

public class TCPTreeView extends JPanel implements TreeSelectionListener {

	protected DefaultMutableTreeNode rootNode;
    protected DefaultTreeModel treeModel;
    protected JTree tree;
    private Toolkit toolkit = Toolkit.getDefaultToolkit();
    private TCPView frame;

    public TCPTreeView(TCPView frame) {
        super(new GridLayout(1,0));
        this.frame = frame;
        
        rootNode = new DefaultMutableTreeNode("TCP Sockets");
        treeModel = new DefaultTreeModel(rootNode);
        treeModel.addTreeModelListener(new MyTreeModelListener());

        tree = new JTree(treeModel);
        tree.setEditable(true);
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        //Enable tool tips.
        //ToolTipManager.sharedInstance().registerComponent(tree);
        tree.setShowsRootHandles(true);
        //tree.setRootVisible(false);
        
        ImageIcon rootIcon = createImageIcon("rectangle.gif");
        ImageIcon bindNodeIcon = createImageIcon("diamond.gif");
        if (rootIcon != null && bindNodeIcon != null) {
            tree.setCellRenderer(new MyRenderer(rootIcon, bindNodeIcon));
        } else {
            System.err.println("One of the icons is missing; using default.");
        }

        
        //Listen for when the selection changes.
        tree.addTreeSelectionListener(this);

        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane);
        setPreferredSize(new Dimension(235, 140));
    }
    
    public Object getLastSelectedPathComponent() {
    	return tree.getLastSelectedPathComponent();
    }

    /** Remove all nodes except the root node. */
    public void clear() {
        rootNode.removeAllChildren();
        treeModel.reload();
    }

    /** Remove the currently selected node. */
    public void removeCurrentNode() {
        TreePath currentSelection = tree.getSelectionPath();
        if (currentSelection != null) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode)
                         (currentSelection.getLastPathComponent());
            MutableTreeNode parent = (MutableTreeNode)(currentNode.getParent());
            if (parent != null) {
                treeModel.removeNodeFromParent(currentNode);
                return;
            }
        } 

        // Either there was no selection, or the root was selected.
        toolkit.beep();
    }

    /** Add child to the currently selected node. */
    public DefaultMutableTreeNode addObject(Object child) {
        DefaultMutableTreeNode parentNode = null;
        TreePath parentPath = tree.getSelectionPath();

        if (parentPath == null) {
            parentNode = rootNode;
        } 
        else {
            parentNode = (DefaultMutableTreeNode)
                         (parentPath.getLastPathComponent());
        }

        return addObject(parentNode, child, true);
    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent, Object child) {
        return addObject(parent, child, false);
    }

    public DefaultMutableTreeNode addObject(DefaultMutableTreeNode parent,
                                            Object child, 
                                            boolean shouldBeVisible) {
        DefaultMutableTreeNode childNode =  new DefaultMutableTreeNode(child);

        if (parent == null) {
            parent = rootNode;
        }

        treeModel.insertNodeInto(childNode, parent, parent.getChildCount());

        //Make sure the user can see the new node
        if (shouldBeVisible) {
            tree.scrollPathToVisible(new TreePath(childNode.getPath()));
        }
        return childNode;
    }

    public void socketBound(TCPSocket sock) {
    	BindNode bn = new BindNode(sock.getCurrentApplication(), sock.getTCB().local_address, sock.getTCB().local_port);
    	DefaultMutableTreeNode child = new DefaultMutableTreeNode(bn);
    	System.out.println("new bind node: " + bn);
    	treeModel.insertNodeInto(child, rootNode, rootNode.getChildCount());
    	tree.scrollPathToVisible(new TreePath(child.getPath()));
    	// don't auto-select.  it may just be a server
    	//tree.setSelectionPath(new TreePath(child.getPath()));  
    }
    
    public void tcbCreated(TCB tcb){
    	TCBNode sn = new TCBNode(tcb);
    	System.out.println("New socket node: " + sn);
    	DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(sn);
    	int local_port = tcb.local_port;
    	for(int i = 0; i < rootNode.getChildCount(); i++){
    		DefaultMutableTreeNode test = (DefaultMutableTreeNode)rootNode.getChildAt(i);
    		BindNode test_bn = (BindNode) test.getUserObject();
    		System.out.println("Comparing local (" + local_port + ") with existing (" + test_bn.mPort + ")");
    		if(test_bn.mPort == local_port){
    	    	treeModel.insertNodeInto(newNode, test, test.getChildCount());
    	    	tree.scrollPathToVisible(new TreePath(newNode.getPath()));
    	    	tree.setSelectionPath(new TreePath(newNode.getPath()));
    	    	System.out.println("parent " + test_bn + " found for " + sn);
    	    	return;
    		}
    	}
    }
    
    
    /** Required by TreeSelectionListener interface. */
    public void valueChanged(TreeSelectionEvent e) {
    	
    	DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getLastSelectedPathComponent();

        if (node == null) 
        	return;

        Object selectedNode = node.getUserObject();
        if (selectedNode instanceof TCBNode) {
        	TCBNode tNode = (TCBNode)selectedNode;
        		frame.TCBSelected(tNode.mTCB);
        }
        

    }
    
    
    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = TCPTreeView.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }
    
    class MyTreeModelListener implements TreeModelListener {
        public void treeNodesChanged(TreeModelEvent e) {
            DefaultMutableTreeNode node;
            node = (DefaultMutableTreeNode)
                     (e.getTreePath().getLastPathComponent());

            /*
             * If the event lists children, then the changed
             * node is the child of the node we've already
             * gotten.  Otherwise, the changed node and the
             * specified node are the same.
             */
            try {
                int index = e.getChildIndices()[0];
                node = (DefaultMutableTreeNode)
                       (node.getChildAt(index));
            } catch (NullPointerException exc) {}

            System.out.println("The user has finished editing the node.");
            System.out.println("New value: " + node.getUserObject());
        }
        
        public void treeNodesInserted(TreeModelEvent e) {
        }
        
        public void treeNodesRemoved(TreeModelEvent e) {
        }
        
        public void treeStructureChanged(TreeModelEvent e) {
        }
    }
    
    private class MyRenderer extends DefaultTreeCellRenderer {
        Icon rootIcon, bindNodeIcon;

        public MyRenderer(Icon rootIcon, Icon bindNodeIcon) {
            this.rootIcon = rootIcon;
            this.bindNodeIcon = bindNodeIcon;
        }

        public Component getTreeCellRendererComponent(
                            JTree tree,
                            Object value,
                            boolean sel,
                            boolean expanded,
                            boolean leaf,
                            int row,
                            boolean hasFocus) {

            super.getTreeCellRendererComponent(
                            tree, value, sel,
                            expanded, leaf, row,
                            hasFocus);
            if (row == 0) {
            	setIcon(rootIcon);
            }
            if (isBindNode(value)) {
                setIcon(bindNodeIcon);
                //setToolTipText("This book is in the Tutorial series.");
            } 
            //else {
            //    setToolTipText(null); //no tool tip
            //}

            return this;
        }

        protected boolean isBindNode(Object value) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            Object nodeInfo = node.getUserObject();
            if (nodeInfo instanceof BindNode) {
            	return true;
            } 

            return false;
        }
    }

    
    
    
    private class BindNode {
    	public Thread mApplication;
    	public int mPort;
    	public InetAddress mAddress;
    	public String mName; 
    	public BindNode(Thread app, InetAddress addr, int p) { 
    		mApplication = app; 
    		mAddress = addr;
    		mPort = p; 
    		String[] arr = mApplication.getClass().getName().split("\\.");
    		String app_name = (arr.length < 1) ? "Unknown" : arr[arr.length - 1];
    		mName =  app_name + "(" + mAddress.getHostAddress() + " : " + mPort + ")";
    	}
    	public String toString() { return mName;	}
    }
    
    private class TCBNode {
    	public TCB mTCB;
    	public String mName;
    	public TCBNode(TCB t) {
    		mTCB = t; 
    		mName = mTCB.remote_address.getHostAddress() + " : " + mTCB.remote_port;
    	}
    	public String toString() { return mName; }
    	
    }
}
