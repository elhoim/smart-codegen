/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.smartitengineering.javasourcetreeparser;

/**
 *
 * @author imyousuf
 */
public interface InitializationListener {
  
  public boolean isInitialized();
  
  public void setInitialized();
  
  public void unsetInitialized();
  
  public void setInitializationStatus(boolean initialized);
  
  public Object getValue(String key);
  
  public void setValue(String key, Object value);

}
