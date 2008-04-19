/**
 *    This NetBeans Module is responsible for implementing observer pattern for
 *    parsing Java Source file so that users can simply write listeners and
 *    implement source code changes.
 * 
 *    Copyright (C) 2008  Imran M Yousuf (imran@smartitengineering.com)
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License along
 *    with this program; if not, write to the Free Software Foundation, Inc.,
 *    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.smartitengineering.javasourcetreeparser;

import com.sun.source.tree.Tree.Kind;

/**
 *
 * @author imyousuf
 */
public abstract class AbstractNodeTraversalListener<E extends Kind> implements NodeTraversalListener<E>, StateContainer{
  
  private StateContainer stateContainer;
  
  protected AbstractNodeTraversalListener(StateContainer stateContainer) {
    if(stateContainer == null) {
      throw new IllegalArgumentException("Initialization Listener can not be null!");
    }
    this.stateContainer = stateContainer;
  }

  public Object getValue(String key) {
    return stateContainer.getValue(key);
  }

  public boolean isInitialized() {
    return stateContainer.isInitialized();
  }

  public void setInitializationStatus(boolean initialized) {
    stateContainer.setInitializationStatus(initialized);
  }

  public void setInitialized() {
    stateContainer.setInitialized();
  }

  public void setValue(String key,
                       Object value) {
    stateContainer.setValue(key, value);
  }

  public void unsetInitialized() {
    stateContainer.unsetInitialized();
  }

  
}
