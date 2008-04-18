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

import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.VariableTree;
import java.util.Comparator;
import org.netbeans.api.java.source.TreeMaker;

/**
 *
 * @author imyousuf
 */
public class ParsingUtils {

  private static MethodTreeComparator methodTreeComparator;
  private static VariableTreeComparator variableTreeComparator;

  public static Comparator<MethodTree> getMethodTreeComparator() {
    if (methodTreeComparator == null) {
      methodTreeComparator = new MethodTreeComparator();
    }
    return methodTreeComparator;
  }

  public static Comparator<VariableTree> getVariableTreeComparator() {
    if (variableTreeComparator == null) {
      variableTreeComparator = new VariableTreeComparator();
    }
    return variableTreeComparator;
  }

  private static class MethodTreeComparator
          implements Comparator<MethodTree> {

    public int compare(MethodTree methodTree,
                       MethodTree comparedToMethodTree) {
      if (methodTree == null && comparedToMethodTree == null) {
        return 0;
      }
      if (methodTree == null && comparedToMethodTree != null) {
        return -1;
      }
      if (methodTree != null && comparedToMethodTree == null) {
        return 1;
      }
      int result = methodTree.getName().
              toString().
              compareTo(comparedToMethodTree.getName().
                        toString());
      if (result == 0) {
        result = methodTree.getParameters().
                size() - comparedToMethodTree.getParameters().
                size();
        if (result == 0) {
          VariableTreeComparator varComparator = new VariableTreeComparator();
          int length = methodTree.getParameters().
                  size();
          for (int paramIndex = 0; paramIndex < length; ++paramIndex) {
            int varCompareResult = varComparator.compare(methodTree.getParameters().
                                                         get(paramIndex), comparedToMethodTree.getParameters().
                                                         get(paramIndex));
            if (varCompareResult != 0) {
              return varCompareResult;
            }
          }
        }
      }
      return result;
    }
  }

  private static class VariableTreeComparator
          implements Comparator<VariableTree> {

    public int compare(VariableTree varTree,
                       VariableTree comparedVarTree) {
      if (varTree.toString().
              equals(comparedVarTree.toString())) {
        return 0;
      }
      int result = varTree.getType().
              toString().
              equals(comparedVarTree.getType().
                     toString()) ? 0 : 1;
      if (result == 0) {
        String type = getTypeAsString(varTree.getType());
        String comparedType = getTypeAsString(comparedVarTree.getType());
        return comparedType.compareTo(type);
      }
      else {
        return varTree.getType().
                toString().
                compareTo(comparedVarTree.getType().
                          toString());
      }
    }

    private String getTypeAsString(final Tree varTreeType) {
      Tree type = varTreeType;
      Kind variableTypeKind = type.getKind();
      switch (variableTypeKind) {
        case IDENTIFIER:
          return type.toString();
        case MEMBER_SELECT:
          MemberSelectTree memberSelectTree =
                  (MemberSelectTree) type;
          return memberSelectTree.getIdentifier().
                  toString();
        case ARRAY_TYPE:
          ArrayTypeTree arrayTypeTree = (ArrayTypeTree) type;
          return getTypeAsString(arrayTypeTree.getType());
        case PARAMETERIZED_TYPE:
          ParameterizedTypeTree parameterizedTypeTree =
                  (ParameterizedTypeTree) type;
          return getTypeAsString(parameterizedTypeTree.getType());
        case PRIMITIVE_TYPE:
        default:
          return type.toString();
      }
    }
  }

  public static Tree getMemberSelectTreeForClassName(TreeMaker make,
                                              Class clazz) {
    if (make == null) {
      return null;
    }
    int lastIndex = clazz.getName().
            lastIndexOf('.');
    if (lastIndex > -1) {
      return getMemberSelectTreeForString(make, clazz.getName().
                                          substring(0, lastIndex), clazz.getName().
                                          substring(lastIndex + 1));
    }
    else {
      return make.Identifier(clazz.getName());
    }
  }

  public static MemberSelectTree getMemberSelectTreeForString(TreeMaker make,
                                                       String dotSeparatedMemberSelect,
                                                       String id) {
    if (make == null) {
      return null;
    }
    int lastIndex = dotSeparatedMemberSelect.lastIndexOf('.');
    if (lastIndex > -1) {
      String nextId = dotSeparatedMemberSelect.substring(lastIndex + 1);
      return make.MemberSelect(getMemberSelectTreeForString(make,
                                                            dotSeparatedMemberSelect.substring(0,
                                                                                               lastIndex),
                                                            nextId), id);
    }
    else {
      return make.MemberSelect(make.Identifier(dotSeparatedMemberSelect), id);
    }
  }
}