package net.clackrouter.component.base;

import java.util.Properties;

/**
 * A simple interface that let's components export/import data to and from property
 * views without writing any GUI code.   
 * 
 * @author Dan
 *
 */

public interface ComponentDataHandler {
	
	public Properties getReadHandlerValues();
	
	public Properties getWriteHandlerValues();
	
	public boolean acceptWrite(String key, String value);
}
