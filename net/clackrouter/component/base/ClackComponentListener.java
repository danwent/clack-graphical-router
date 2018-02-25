/*
 * Created on Nov 9, 2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package net.clackrouter.component.base;

/**
 * @author Dan
 *
 * This interface is implemented by any class that wants to stay notified of changes to a single
 * ClackComponent class.  The ClackComponentEvent object describes what type of and event happened, so the
 * listening class knows what if any action to take. 
 * @see ClackComponent
 */
public interface ClackComponentListener
{
    public void componentEvent(ClackComponentEvent event);
}
