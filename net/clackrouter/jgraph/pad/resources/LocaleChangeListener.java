/*
 * @(#)LocaleChangeListener.java	1.0 23/01/02
 *
 * Copyright (C) 2003 Sven Luzar
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */
package net.clackrouter.jgraph.pad.resources;

/**If the locale is changing, the Translator is firing events
 * to all registered LocaleChangeListeners.
 */

public interface LocaleChangeListener {
  /** Method was called if the locale changes
   */
  public void localeChanged(LocaleChangeEvent e);
}
