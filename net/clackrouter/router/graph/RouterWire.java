
package net.clackrouter.router.graph;

import net.clackrouter.component.base.ClackComponent;

/**
* A RouterWire is essentially a DefaultEdge with a small amount of functionality built on top
* to remember what components and specific ports within the route that it represents a 
* connection between.  Since we have only completely connected wires allowed in our graphs,
* this is pretty straightforward.
*/
public class RouterWire  extends Wire {
	
//	 keep this info around until we disconnect lower-level router	
	public ClackComponent sourceComponent, targetComponent;
	public int sourcePortNum, targetPortNum;

	private static int UNIQUE_COUNT = 0;
	
	public RouterWire(String name, RouterView view, ClackComponent srcComp, int srcPort, 
				ClackComponent targetComp, int targetPort){
		super(name, view);
		sourceComponent = srcComp;
		sourcePortNum = srcPort;
		targetComponent = targetComp;
		targetPortNum = targetPort;
	}
	
	public ClackComponent getSourceComponent() { return sourceComponent; }
	public ClackComponent getTargetComponent() { return targetComponent; }
	public int getSourcePortNum() { return sourcePortNum; }
	public int getTargetPortNum() { return targetPortNum; }
	
	public static int getUniqueCount() { return UNIQUE_COUNT++; }
	


		

}
