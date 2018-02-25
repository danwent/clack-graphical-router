package net.clackrouter.netutils;

import java.util.ArrayList;

/**
 * My algo teacher would cry, but i'm implementing a priority queue as a list
 * and am finding the max elem using a linear scan b/c our networks are tiny
 * @author Dan
 *
 */

public class PQueue {
	
	private class Element {
		Element(int p, Object d) { priority = p; data = d; }
		int priority;
		Object data;
		public boolean equals(Object b) { return data.equals(b); }
	}
	
	private static int UNKNOWN = -1;
	int priority_index = UNKNOWN;
	private ArrayList<Element> elems = new ArrayList<Element>();
	
	public void add(int priority, Object o){
		priority_index = UNKNOWN;
		elems.add(new Element(priority, o));
	}
	
	public int size() { return elems.size(); }
	
	public Object peekData() {
		if(size() == 0) return null;
		
		if(priority_index == UNKNOWN)	
			findPriority();
		return elems.get(priority_index).data;
	}
	
	public int peekPriority(){
		if(size() == 0) return -1;
		
		if(priority_index == UNKNOWN)	
			findPriority();
		return elems.get(priority_index).priority;
	}
	
	public Object pop() {
		if(size() == 0) return null;
		
		if(priority_index == UNKNOWN)	
			findPriority();

		Object data = elems.get(priority_index).data;
		elems.remove(priority_index);
		return data;
	}	
	
	// assumes that we've had a size check already to 
	// make sure that there is one elem in queue
	private void findPriority() {
		priority_index = 0;
		
		for(int i = 1; i < elems.size(); i++){
			if(elems.get(priority_index).priority < elems.get(i).priority)
				priority_index = i;
		}
	}
	
	
	

}
