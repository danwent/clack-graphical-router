
package net.clackrouter.component.base;

import java.util.Vector;

/**
 * Implements the flow-based component discovery in a Clack router graph (not currently used).
 * 
 * <p> It handles queries such as "find a queue that is downstream from me". </p>
 */
public class ComponentFinder {
	
	 /**
     * Find connected component whose packets may reach this component
     * by recursing over all incoming connected links.
     * @param comp the component uses as the search starting point
     * @param port_num the exact port on the source component to search from
     * @param component_type_name the type of component we are searching for (tested using Class.getName.equals())
     * */
    protected ClackComponent findConnectedIncoming(ClackComponent comp, int port_num, String component_type_name)
    {
        if ( port_num >= comp.m_ports.length || 
        	 comp.m_ports[port_num] == null    ||
        	 comp.m_ports[port_num].getDirection() != ClackPort.DIR_IN)
        {
            System.err.println("findConnectedIncoming on invalid port");
            System.exit(-1); // -- ASSERT
        }

        ClackPort start_port = comp.m_ports[port_num]; 
        Vector visited = new Vector();

        return findConnectedIncomingRecurse(start_port, visited, component_type_name);
    }

    /**
     * Recursive helper function to implement incoming flow-based search.
     * @param cur_port
     * @param visited
     * @param type_name
     * @return
     */
    private static ClackComponent findConnectedIncomingRecurse(ClackPort cur_port, 
                                                      Vector visited,
                                                      String type_name)
    {
        ClackComponent how_bout_you = cur_port.getOwner(); 

        if ( visited.contains(how_bout_you) )
        { return null; }

        if (how_bout_you.getClass().getName().equals(type_name))
        { return how_bout_you; } // -- Match wohoo!!

        visited.add(how_bout_you);

        for ( int i = 0; i < how_bout_you.getNumPorts(); ++i)
        {
            ClackPort new_port = how_bout_you.getPort(i); 
            if ( new_port.getDirection() == ClackPort.DIR_IN &&
                 new_port.getConnectedPort(0) != null)
            { 
                ClackComponent res =
                    findConnectedIncomingRecurse(new_port.getConnectedPort(0),
                                                                visited,
                                                                type_name);
                if ( res != null )
                { return res; }
            }
        }
        return null;
    } // -- findConnectedIncomingRecurse(..)

   /**
     * Find connected component whose packets may reach this component
     * by recursing over all outgoing connected links.
    * @param comp the starting component
    * @param port_num the port to start the search on
    * @param component_type_name name of component to search for
    */
    public static ClackComponent findConnectedOutgoing(ClackComponent comp, int port_num, String component_type_name)
    {
    	
        if ( port_num >= comp.m_ports.length || 
        	comp.m_ports[port_num] == null    ||
        	comp.m_ports[port_num].getDirection() != ClackPort.DIR_OUT)
        {
            System.err.println("findConnectedOutgoing on invalid port");
            System.exit(-1); // -- ASSERT
        }

        ClackPort start_port = comp.m_ports[port_num]; 
        Vector visited = new Vector();

        return findConnectedOutgoingRecurse(start_port, visited, component_type_name);
    }

    /**
     * Recursive helper class to implement outgoing component search.  
     * @param cur_port
     * @param visited
     * @param type_name
     * @return
     */
    private static ClackComponent findConnectedOutgoingRecurse(ClackPort cur_port, 
                                                      Vector visited,
                                                      String type_name)
    {
        ClackComponent how_bout_you = cur_port.getOwner(); 

        if ( visited.contains(how_bout_you) )
        { return null; }

        if (how_bout_you.getClass().getName().equals(type_name))
        { return how_bout_you; } // -- Match wohoo!!

        visited.add(how_bout_you);

        for ( int i = 0; i < how_bout_you.getNumPorts(); ++i)
        {
            ClackPort new_port = how_bout_you.getPort(i); 
            if ( new_port.getDirection() == ClackPort.DIR_OUT &&
                 new_port.getConnectedPort(0) != null)
            { 
                ClackComponent res =
                    findConnectedOutgoingRecurse(new_port.getConnectedPort(0),
                                                                visited,
                                                                type_name);
                if ( res != null )
                { return res; }
            }
        }
        return null;
    } // -- findConnectedIncomingRecurse(..)

}
