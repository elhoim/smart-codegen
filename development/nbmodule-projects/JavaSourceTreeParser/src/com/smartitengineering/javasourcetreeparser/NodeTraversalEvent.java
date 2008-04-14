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

import com.sun.source.tree.ImportTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author imyousuf
 */
public class NodeTraversalEvent<E extends Kind> {

  private E kind;
  private Tree currentNode;
  private List<Tree> allParents;
  private WorkingCopy workingCopy;
  private List<ImportTree> imports;
  private TreeMaker treeMaker;

  NodeTraversalEvent(E kind,
                     Tree currentNode,
                     List<Tree> allParents,
                     WorkingCopy workingCopy,
                     TreeMaker treeMaker,
                     List<ImportTree> imports) {
    if (kind == null || currentNode == null || allParents == null ||
            workingCopy == null || treeMaker == null || imports == null) {
      throw new IllegalArgumentException("No argument can be null!");
    }
    this.kind = kind;
    this.allParents = allParents;
    this.workingCopy = workingCopy;
    this.treeMaker = treeMaker;
    this.currentNode = currentNode;
    this.imports = imports;
  }

  public Stack<Tree> getParentStack() {
    final Stack<Tree> stack =
            new Stack<Tree>();
    Collections.<Tree>copy(stack, allParents);
    return stack;
  }

  public Queue<Tree> getParentQueue() {
    Queue<Tree> queue = new LinkedList<Tree>(allParents);
    return queue;
  }

  public List<Tree> getParentList() {
    ArrayList<Tree> clonedAllParents = new ArrayList<Tree>(allParents.size());
    Collections.<Tree>copy(clonedAllParents, allParents);
    return clonedAllParents;
  }

  public E getKind() {
    return kind;
  }

  public Tree getCurrentNode() {
    return currentNode;
  }

  public WorkingCopy getWorkingCopy() {
    return workingCopy;
  }

  public TreeMaker getTreeMaker() {
    return treeMaker;
  }
  
  public List<ImportTree> getImports() {
    return imports;
  }
}