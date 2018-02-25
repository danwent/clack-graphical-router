/*
 * This file is part of the Librarian project
 * (C) 2002 The Librarian Community
 *
 * Please visit our website at http://librarian.sf.de
 */
package net.clackrouter.jgraph.pad.resources;

import java.util.Enumeration;

/**Container for proper names. (e.g. Look and Feel names are
 * equal in any language, sothat this container.)
 */

public interface ProperNameProvider {

  /** Returns the Keys of this proper name container
   *
   */
  public abstract  Enumeration getKeys()  ;

  /** Returns the proper name for the locale key.
   *
   */
  public String getString(String key) ;

}
