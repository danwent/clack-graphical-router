package net.clackrouter.ethereal;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

public class EtherealTreeNode extends DefaultMutableTreeNode {

	protected int hexStart;
	protected int hexEnd;
	
	private static int curIndex = 0;
	
	public EtherealTreeNode() {
	}
	
	public EtherealTreeNode(String s, byte[] b) {
		this(s, 0, b.length);
	}
	
	public EtherealTreeNode(String s, int len) {
		this(s, curIndex, curIndex + len);
		curIndex += len;
	}
	
	public EtherealTreeNode(String s, int start, int end) {
		super(s);
		hexStart = start;
		hexEnd = end;
	}

	public static void startParse() {
		curIndex = 0;
	}
	public static void rewindParse(int len) {
		curIndex -= len;
	}
	
	public int getHexEnd() {
		return hexEnd;
	}

	public int getHexStart() {
		return hexStart;
	}
	
	public EtherealTreeNode ofset(int len) {
		hexStart += len;
		hexEnd += len;
		
		Enumeration<EtherealTreeNode> en = children();
		while (en.hasMoreElements()) {
			EtherealTreeNode node = en.nextElement();
			node.ofset(len);
		}
		return this;
	}
	
	@Override
	public void add(MutableTreeNode newChild) {
		super.add(newChild);
	}
	
	/* For debugging
	public String toString() {
		String s =  super.toString() + "[" + getHexStart() + "," + getHexEnd() + "]";
		if (children == null) return s;
		Iterator<EtherealTreeNode> iter = children.iterator();
		while (iter.hasNext()) {
			s += "\n\t" + iter.next().toString();
		}
		return s;
	}
		*/
	
	
}
