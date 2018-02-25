// $Id: Timeval.java,v 1.3 2006/04/08 21:03:31 ptarjan Exp $

/***************************************************************************
 * Copyright (C) 2001, Patrick Charles and Jonas Lehmann                   *
 * Distributed under the Mozilla Public License                            *
 *   http://www.mozilla.org/NPL/MPL-1.1.txt                                *
 ***************************************************************************/
package net.clackrouter.jpcap;

import java.io.Serializable;
import java.util.Date;


/**
 * POSIX.4 timeval for Java.
 * <p>
 * Container for java equivalent of c's struct timeval.
 *
 * @author Patrick Charles and Jonas Lehmann
 * @version $Revision: 1.3 $
 */
public class Timeval implements Serializable
{
  public Timeval(long seconds, int microseconds) {
    this.seconds = seconds;
    this.microseconds = microseconds;
  }

  public String toString() {
    StringBuffer sb = new StringBuffer();
    sb.append(seconds);
    sb.append('.');
    sb.append(microseconds);
    sb.append('s');

    return sb.toString();
  }

  /**
   * Convert this timeval to a java Date.
   */
  public Date getDate() {
    return new Date(seconds * 1000 + microseconds / 1000);
  }

  public long getSeconds() {
    return seconds;
  }

  public int getMicroSeconds() {
    return microseconds;
  }

  long seconds;
  int microseconds;

  private String _rcsid = 
  "$Id: Timeval.java,v 1.3 2006/04/08 21:03:31 ptarjan Exp $";
}
