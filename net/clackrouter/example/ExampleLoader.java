/*
 * Created on Sep 6, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.example;

import java.applet.Applet;

import net.clackrouter.gui.ClackFramework;
import net.clackrouter.gui.ClackFrameworkHelper;

/**
 * @author Dan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ExampleLoader extends Applet {

	//When launched as an Applet
	public void init() {
		String[] params = {};
		if(getParameter(ClackFramework.PARAM_STRING) != null)
			params = getParameter(ClackFramework.PARAM_STRING).split(" ");
		load(params, this);
	}
	
	// When launched as an application
	public static void main(String[] args) {
		load(args, null);
	}
	
	public static void load(String[] args, Applet parent){

		ClackFramework framework = new ClackFramework(parent);
		framework.addAdditionalComponent("EvenOdd", "net.clackrouter.example.EvenOdd");
		framework.addAdditionalComponent("SourceTracker1", "net.clackrouter.example.SourceTracker1");
		
		framework.getApplicationManager().addApplicationMapping("hello", "net.clackrouter.example.HelloApp");
		ClackFrameworkHelper.configureClackFramework(args, framework);	

	}
	
}
