/*
 * Created on Sep 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.example;

import net.clackrouter.application.ClackApplication;

/**
 * @author Dan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class HelloApp extends ClackApplication {
	
	public void application_main(String[] args){
		print("hello...\n");
		if(args.length > 1){
			print("You said: " + args[1]);
		}else {
			print("You're mighty quiet today...");
		}
	}
	
	public String getDescription() { return "silly example application"; }

}
