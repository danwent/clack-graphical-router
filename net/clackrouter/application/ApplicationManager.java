/*
 * Created on Sep 2, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.clackrouter.application;

import java.lang.reflect.Constructor;
import java.util.Properties;

import net.clackrouter.router.core.Router;

/**
 * @author Dan
 *
 */
public class ApplicationManager {
	private static int UNIQUE_COUNT = 0;
	
	public static int getUniqueInteger() { return UNIQUE_COUNT++; }
	
	private Properties appName2ClassNameMap;
	
	public ApplicationManager() {
		appName2ClassNameMap = new Properties();
		
		// add standard clack applications
		addApplicationMapping("show", "net.clackrouter.application.Show");
		addApplicationMapping("ping", "net.clackrouter.application.Ping");
		addApplicationMapping("httpget", "net.clackrouter.application.HTTPGetter");
		addApplicationMapping("miniws", "net.clackrouter.application.MiniWebServer");
		addApplicationMapping("tcpproxy", "net.clackrouter.application.TCPRedirector");
		addApplicationMapping("udpproxy", "net.clackrouter.application.UDPRedirector");
		addApplicationMapping("ifconfig", "net.clackrouter.application.Ifconfig");
		addApplicationMapping("route", "net.clackrouter.application.UnixRoute");
	}
	
	public void addApplicationMapping(String appname, String classname){
		appName2ClassNameMap.setProperty(appname, classname);
	}
	
	public Properties getMap() { return appName2ClassNameMap; }
	
	// shell can be null, which is fine
	public ClackApplication runApplication(Router r, String[] command_args, ClackShell shell) throws Exception, NoSuchMethodException {
		String classname = appName2ClassNameMap.getProperty(command_args[0]);
		if(classname == null)
			throw new Exception("No application registered for command: " + command_args[0]);
		
		Class class_obj = Class.forName(classname);
		Constructor constructor = class_obj.getConstructor(new Class[] { });
		ClackApplication app = (ClackApplication) constructor.newInstance(new Object[] { });
		app.setShell(shell);
		
		app.configure(command_args[0] + " " + getUniqueInteger(), r, command_args);
		app.start();
		return app;
	}

}
