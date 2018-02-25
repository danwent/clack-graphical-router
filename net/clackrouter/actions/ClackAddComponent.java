/*
 * Created on Nov 27, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package net.clackrouter.actions;

import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.Properties;

import net.clackrouter.component.base.ClackComponent;
import net.clackrouter.gui.ClackFramework;
import net.clackrouter.jgraph.pad.resources.Translator;
import net.clackrouter.router.core.Router;




/**
 * Action to implement component addition (via the Router Graph popup menu).
 * 
 * <p> Uses reflection to create a new component.  We use the command string
 * plus an integer to assure that the name is unqiue within the router.  </p>
 * 
 * <p>The class to create is based on mappings from command to fully-qualified classname that are stored in the
 *  net/clackrouter/jgraph/pad/resources/Clack.properties file. </p>  
 */
public class ClackAddComponent extends AbstractActionDefault {

	private Properties menuName2ClassNameMap;
	
	public ClackAddComponent(ClackFramework graphpad) {
		super(graphpad);
		menuName2ClassNameMap = new Properties();
	}
	
	public void addMapping(String menuName, String className){
		menuName2ClassNameMap.setProperty(menuName, className);
	}
	
	public String[] getMenuNames() {
		String[] menu_names = new String[menuName2ClassNameMap.size()];
		Enumeration e = menuName2ClassNameMap.keys();
		for(int i = 0; e.hasMoreElements(); i++) {
			menu_names[i] = (String)e.nextElement();
		}
		return menu_names;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		String cmd = e.getActionCommand();
	
		ClackComponent component = null;
		Router r = graphpad.getCurrentDocument().getCurrentRouter();
		
		
		String name = cmd + " " + ClackComponent.getUniqueCount();
		// assure unique name
		
		while(r.getComponent(name) != null) name = cmd + " " + ClackComponent.getUniqueCount();
		Class class_obj = null;
		
		String className = null;
		try {
			System.out.println("Attempting to create component. cmd = '" + cmd + "'");
			className = menuName2ClassNameMap.getProperty(cmd);
			System.out.println("classname = " + className);
			if(className == null)
				className = Translator.getString(cmd + ".class");
			System.out.println("classname = " + className);
			
			class_obj = Class.forName(className);

			try {
				Constructor constructor = class_obj
					.getConstructor(new Class[] { String.class });
				component = (ClackComponent) constructor
					.newInstance(new Object[] { name });
			} catch (NoSuchMethodException ex) {
				/* do not report exception, this is normal*/
				Constructor constructor = class_obj
					.getConstructor(new Class[] { Router.class,
							String.class });
				component = (ClackComponent) constructor
					.newInstance(new Object[] { r,name });
			}
		
		}catch (Exception ex){
			ex.printStackTrace();
			System.err.println("No class or constructor found for: " + className);
			return;
		}
		
		if(component != null)
			graphpad.getClackGraphHelper().addRouterComponentCell(r, component, null);
		else
			System.err.println("Unable to create component from command: " + cmd);
	}

}
