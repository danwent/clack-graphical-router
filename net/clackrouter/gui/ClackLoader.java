/*
 * @(#)Graphpad.java	1.2 11/11/02
 *
 * Copyright (C) 2001-2004 Gaudenz Alder
 *
 * JGraphpad is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * JGraphpad is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JGraphpad; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package net.clackrouter.gui;

import java.applet.Applet;


/**
 * The outermost Clack class, which can be launched either as an Applet, or an application.  
 * 
 * <p>  This class exists primarily to parse arguments, show the splashscreen, and report an 
 * error is the @see ClackGraphpad object cannot be loaded.  Based on the JGraphpad class.  </p>
 */
public class ClackLoader extends Applet {
		
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
		ClackFrameworkHelper.configureClackFramework(args, framework);	

	}
	

}
