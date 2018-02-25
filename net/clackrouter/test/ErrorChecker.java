
package net.clackrouter.test;

import java.util.ArrayList;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.component.base.ClackPort;
import net.clackrouter.component.base.Interface;
import net.clackrouter.component.extension.Counter;
import net.clackrouter.component.simplerouter.InterfaceIn;
import net.clackrouter.router.core.Router;

/**
 * Example of a static router checker, which enforces certain things about the contents of a router.
 * 
 * <p> Currently, this type of static check is not integrated into the Clack application in any way,
 * but it serves as a demonstration of what could be done to statically check that a constructed router
 * is correct. </p>
 */
public class ErrorChecker {
	
	public static void main(String[] args){
		try {
			Counter c = new Counter(null, "name");
			test(Interface.class, c);
			test2(ClackComponent.class, Interface.class);
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	private static void test(Class cls, Object obj){
		System.out.println(cls.isInstance(obj));
	}
	
	private static void test2(Class class1, Class class2) throws Exception {
		System.out.println(class2.getName() + " is assignable from " + class1.getName() + " = " + class2.isAssignableFrom(class1));
		System.out.println(class1.getName() + " is assignable from " + class2.getName() + " = " + class1.isAssignableFrom(class2));
	}
	
	public static String  checkRouter(Router router){
		InterfaceIn[] ifaces = router.getInputInterfaces();
		ArrayList queue = new ArrayList(); 
		ArrayList visited = new ArrayList();
		StringBuffer sb = new StringBuffer(100);
		sb.append("Port Type Errors: \n");
		boolean errorFound = false;
		
		for(int i = 0; i < ifaces.length; i++) {
			queue.add(ifaces[i]);
		}
		
		// there's no good reason for this to be breadth first
		while(queue.size() > 0){
			ClackComponent comp =(ClackComponent)queue.remove(0);
			
			for(int i = 0; i < comp.getNumPorts(); i++){
				ClackPort source = comp.getPort(i);
				if(source == null) continue; // shouldn't happen
				if(source.getDirection() != ClackPort.DIR_OUT) continue;
				if(source.getNumConnectedPorts() <= 0) continue;
	
				Class sourceType = source.getType();
				for(int j = 0; j < source.getNumConnectedPorts(); j++){
					ClackPort dest = source.getConnectedPort(j);
					Class destType = dest.getType();
					ClackComponent destParent = dest.getOwner();
					
					if(!(destType.isAssignableFrom(sourceType))) {
			//			System.out.println("Checking for destination: " + destParent.getName());
						ArrayList type_list = new ArrayList();
						addAllPossibleInputTypes(source, type_list);
						for(int k = 0; k < type_list.size(); k++){
							SourceTypePair pair = (SourceTypePair)type_list.get(k);
				//			System.out.println("Possible Type: " + pair.m_type.getName() + " from " + pair.m_source.getName());
							if(!(destType.isAssignableFrom(pair.m_type))){
								sb.append("Error for connection from " + comp.getTypeName() + "(port:" + i + ")" 
										+ " to " + destParent.getTypeName() + "(port:" + dest.getPortNumber() + ")\n");
								sb.append("Port typed for " + getClassNameOnly(destType) + " cannot accept type " + getClassNameOnly(pair.m_type)
										+ " (original source: " + pair.m_source.getTypeName() + ")\n\n");
								errorFound = true;
							}
						}
					}
					
					if(!(visited.contains(destParent))) {
						visited.add(destParent);
						queue.add(destParent);
					}
				}
			}
			
		}
		
		if(errorFound)
			return sb.toString();
		else
			return null;
	}
	
	private static void addAllPossibleInputTypes(ClackPort source, ArrayList addTypesHere){
		ClackComponent owner = source.getOwner();
		
		if(owner.isModifying()) {
			//System.out.println("recursion ends in non-modifiable " + owner.getName());
			addTypesHere.add(new SourceTypePair(owner, source.getType())); // simple base case
		}else {
			// this is a non-modifying component, so we must get our input types via recursive calls
			//System.out.println("recursing through: " + owner.getName());
			for(int i = 0; i < owner.getNumPorts(); i++){
				ClackPort port = owner.getPort(i);
				if(port.getDirection() == ClackPort.DIR_IN){
					for(int j = 0; j < port.getNumConnectedPorts(); j++){
						addAllPossibleInputTypes(port.getConnectedPort(j), addTypesHere);
					}
				}
			}
				
		}
	}
	
	private static class SourceTypePair {
		public Class  m_type;
		public ClackComponent m_source;
		public SourceTypePair(ClackComponent source, Class type) { m_source = source; m_type = type; }
	}
	
	
	public static String getClassNameOnly(Class packetClass) {
		String[] arr = packetClass.getName().split("\\.");
		if(arr.length < 1) return "Unknown";
		else return arr[arr.length - 1];
	}

}
