/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.smartitengineering.javasourcetreeparser;

import com.sun.source.tree.Tree.Kind;

/**
 *
 * @author imyousuf
 */
public interface NodeTraversalListener<E extends Kind> {
  
  public void notifyAboutNode(NodeTraversalEvent<E> event);
  
  public E getTreeKind();

}
