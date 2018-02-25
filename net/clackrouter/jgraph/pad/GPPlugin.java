package net.clackrouter.jgraph.pad;

/* Example: Save and open remote files

import org.jgraph.pad.*;
import org.jgraph.GPGraphpad;
import org.jgraph.graph.*;

public class TestPlugin implements GPPlugin {
	
	public void execute(GPGraphpad pad) {
		GPGraph graph = (GPGraph) pad.getGraph();
		// Access the Graph
		System.out.println("Number of Cells: "+graph.getAll().length);
		// Successfull Save
		pad.setModified(false);
		// Open a (New) Model
    	pad.addDocument("newGraph.jgp", new DefaultGraphModel(), null);
	}

}

*/

public interface GPPlugin {

	public void execute(net.clackrouter.gui.ClackFramework pad);

}
