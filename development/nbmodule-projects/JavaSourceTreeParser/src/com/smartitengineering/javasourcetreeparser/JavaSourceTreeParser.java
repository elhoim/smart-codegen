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
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author imyousuf
 */
public class JavaSourceTreeParser {

  private static final Logger LOGGER;
  

  static {
    LOGGER = Logger.getLogger(JavaSourceTreeParser.class.getName());
    LOGGER.setLevel(Level.ALL);
    Handler[] handlers = LOGGER.getHandlers();
    boolean hasConsoleHandler = false;
    for (Handler handler : handlers) {
      if (handler instanceof ConsoleHandler) {
        hasConsoleHandler = true;
      }
    }
    if (!hasConsoleHandler) {
      LOGGER.addHandler(new ConsoleHandler());
    }
  }

  public void parseWorkingCopy(final ClassTree clazz,
                               final WorkingCopy workingCopy,
                               final TreeMaker make) {
    boolean hasStaticImport = false;
    List<? extends ImportTree> imports =
            workingCopy.getCompilationUnit().
            getImports();
    for (ImportTree importTree : imports) {
      if (importTree.isStatic()) {
        MemberSelectTree memberSelectTree =
                (MemberSelectTree) importTree.getQualifiedIdentifier();
        if (memberSelectTree.toString().
                equals("java.lang.System.out")) {
          hasStaticImport = true;
        }
      }
    }
    parseClassTree(clazz, workingCopy, make, null, hasStaticImport);
  }

  public void parseClassTree(final ClassTree clazz,
                             final WorkingCopy workingCopy,
                             final TreeMaker make,
                             final ClassTree parentClassTree,
                             final boolean hasStaticImport) {
    List<? extends Tree> members = clazz.getMembers();
    for (Tree member : members) {
      Kind memberKind = member.getKind();
      if (memberKind.equals(Kind.METHOD)) {
        MethodTree methodTree = (MethodTree) member;
        BlockTree blockTree = methodTree.getBody();
        parseBlockTree(blockTree, workingCopy, make,
                       parentClassTree == null ? clazz : parentClassTree,
                       hasStaticImport);
      }
      else if (memberKind.equals(Kind.VARIABLE)) {
        VariableTree variableTree = (VariableTree) member;
        parseVariableTree(variableTree, workingCopy, make);
      }
      else if (memberKind.equals(Kind.BLOCK)) {
        parseBlockTree((BlockTree) member, workingCopy, make,
                       parentClassTree == null ? clazz : parentClassTree,
                       hasStaticImport);
      }
      else if (memberKind.equals(Kind.CLASS)) {
        parseClassTree((ClassTree) member, workingCopy, make, clazz,
                       hasStaticImport);
      }
      else {
      }
    }
  }

  public void parseBlockTree(final BlockTree blockTree,
                             final WorkingCopy workingCopy,
                             final TreeMaker make,
                             final ClassTree clazz,
                             final boolean hasStaticImport) {
    List<? extends StatementTree> statements = blockTree.getStatements();
    for (StatementTree statementTree : statements) {
      Kind statementKind = statementTree.getKind();
      parseStatementTree(null, statementTree, workingCopy, make, clazz,
                         hasStaticImport);
    }
  }

  public void parseStatementTree(final String source,
                                 final StatementTree statementTree,
                                 final WorkingCopy workingCopy,
                                 final TreeMaker make,
                                 final ClassTree clazz,
                                 final boolean hasStaticImport) {
    if (statementTree == null) {
      return;
    }
    Kind statementKind = statementTree.getKind();
    switch (statementKind) {
      case IF:
        IfTree ifTree = (IfTree) statementTree;
        parseStatementTree("If - Then body", ifTree.getThenStatement(),
                           workingCopy, make, clazz, hasStaticImport);
        parseStatementTree("If - Else body", ifTree.getElseStatement(),
                           workingCopy, make, clazz, hasStaticImport);
        break;
      case FOR_LOOP:
        ForLoopTree forLoopTree = (ForLoopTree) statementTree;
        parseStatementTree("For Loop body", forLoopTree.getStatement(),
                           workingCopy, make, clazz, hasStaticImport);
        break;
      case ENHANCED_FOR_LOOP:
        EnhancedForLoopTree enhancedForLoopTree =
                (EnhancedForLoopTree) statementTree;
        parseStatementTree("Enhanced For Loop body",
                           enhancedForLoopTree.getStatement(),
                           workingCopy, make,
                           clazz, hasStaticImport);
        break;
      case WHILE_LOOP:
        WhileLoopTree whileTree = (WhileLoopTree) statementTree;
        parseStatementTree("While Loop body", whileTree.getStatement(),
                           workingCopy, make, clazz, hasStaticImport);
        break;
      case DO_WHILE_LOOP:
        DoWhileLoopTree doWhileLoopTree =
                (DoWhileLoopTree) statementTree;
        parseStatementTree("Do-While Loop body",
                           doWhileLoopTree.getStatement(), workingCopy,
                           make,
                           clazz, hasStaticImport);
        break;
      case TRY:
        TryTree tryTree = (TryTree) statementTree;
        parseBlockTree(tryTree.getBlock(), workingCopy, make, clazz,
                       hasStaticImport);
        List<? extends CatchTree> catches = tryTree.getCatches();
        for (CatchTree catchTree : catches) {
          parseBlockTree(catchTree.getBlock(), workingCopy, make,
                         clazz, hasStaticImport);
        }
        break;
      case SWITCH:
        SwitchTree switchTree = (SwitchTree) statementTree;
        List<? extends CaseTree> cases = switchTree.getCases();
        for (CaseTree caseTree : cases) {
          ExpressionTree caseExpression = caseTree.getExpression();
          List<? extends StatementTree> caseStatements =
                  caseTree.getStatements();
          for (StatementTree caseStatement : caseStatements) {
            parseStatementTree("Case " + caseExpression,
                               caseStatement, workingCopy, make,
                               clazz,
                               hasStaticImport);
          }
        }
        break;
      case BLOCK:
        parseBlockTree((BlockTree) statementTree, workingCopy, make,
                       clazz, hasStaticImport);
        break;
      case EXPRESSION_STATEMENT:
        parseExpressionStatementTree(
                (ExpressionStatementTree) statementTree, workingCopy,
                make, clazz, hasStaticImport);
        break;
      case VARIABLE:
        parseVariableTree((VariableTree) statementTree, workingCopy,
                          make);
        break;
      default:
    }
  }

  public void parseExpressionStatementTree(ExpressionStatementTree expressionStatementTree,
                                           final WorkingCopy workingCopy,
                                           final TreeMaker make,
                                           final ClassTree clazz,
                                           final boolean hasStaticImport) {
    parseExpressionTree(expressionStatementTree.getExpression(), workingCopy,
                        make, clazz, hasStaticImport);
  }

  public void parseExpressionTree(ExpressionTree expressionTree,
                                  final WorkingCopy workingCopy,
                                  final TreeMaker make,
                                  final ClassTree clazz,
                                  final boolean hasStaticImport) {
    Kind expressionKind = expressionTree.getKind();
    switch (expressionKind) {
      case METHOD_INVOCATION:
        break;
      default:
    }
  }

  public void parseVariableTree(VariableTree variableTree,
                                final WorkingCopy workingCopy,
                                final TreeMaker make) {
    Tree type = variableTree.getType();
    Kind variableTypeKind = type.getKind();
    switch (variableTypeKind) {
      case IDENTIFIER:
        break;
      case MEMBER_SELECT:
        break;
      case ARRAY_TYPE:
        break;
      case PARAMETERIZED_TYPE:
        break;
      default:

    }
  }

  public void logDebugInfoOfWorkingCopy(ClassTree clazz,
                                        WorkingCopy workingCopy) {
    if (LOGGER.isLoggable(Level.FINEST)) {
      if (workingCopy != null) {
        List<? extends ImportTree> imports = workingCopy.getCompilationUnit().
                getImports();
        LOGGER.finest("Package: " + workingCopy.getCompilationUnit().
                      getPackageName());
        for (ImportTree importTree : imports) {
          LOGGER.finest("Import Q-Id: " + importTree.getQualifiedIdentifier().
                        getKind().
                        name());
          LOGGER.finest("Static Import?: " + importTree.isStatic());
          LOGGER.finest("Import Tree: " + importTree.toString() +
                        " Length: " + importTree.toString().
                        length() + '\n');
          MemberSelectTree memberSelectTree =
                  (MemberSelectTree) importTree.getQualifiedIdentifier();
          LOGGER.finest("Member Selected ID: " +
                        memberSelectTree.getIdentifier());
          LOGGER.finest("Member toString: " +
                        memberSelectTree.toString());
          LOGGER.finest("Member Exp toString: " + memberSelectTree.getExpression().
                        toString());
        }
      }
      logClassTree(clazz);
    }
  }

  public void logClassTree(ClassTree clazz) {
    if (LOGGER.isLoggable(Level.FINEST)) {
      List<? extends Tree> members = clazz.getMembers();
      List<? extends TypeParameterTree> types = clazz.getTypeParameters();
      for (TypeParameterTree paramType : types) {
        LOGGER.finest("Type Name: " + paramType.getName());
      }
      for (Tree member : members) {
        Kind memberKind = member.getKind();
        LOGGER.finest("Member Type: " + memberKind.name());
        if (memberKind.equals(Kind.METHOD)) {
          MethodTree methodTree = (MethodTree) member;
          LOGGER.finest("Method Name: " + methodTree.getName().
                        toString());
          List<? extends VariableTree> params =
                  methodTree.getParameters();
          LOGGER.finest("Parameters:");
          for (VariableTree param : params) {
            logVariableTree(param);
          }
          BlockTree blockTree = methodTree.getBody();
          logBlockTree(blockTree);
        }
        else if (memberKind.equals(Kind.VARIABLE)) {
          VariableTree variableTree = (VariableTree) member;
          logVariableTree(variableTree);
        }
        else if (memberKind.equals(Kind.BLOCK)) {
          logBlockTree((BlockTree) member);
        }
        else if (memberKind.equals(Kind.CLASS)) {
          logDebugInfoOfWorkingCopy((ClassTree) member, null);
        }
        else {
          LOGGER.finest("Unrecognized block: " + memberKind);
        }
      }
    }
  }

  public void logBlockTree(BlockTree blockTree) {
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest("------------------Block Start------------------");
      LOGGER.finest("isStatic: " + blockTree.isStatic());
      List<? extends StatementTree> statements = blockTree.getStatements();
      for (StatementTree statementTree : statements) {
        Kind statementKind = statementTree.getKind();
        LOGGER.finest("Statement Kind: " + statementKind.name());
        logStatementTree(null, statementTree);
      }
      LOGGER.finest("-------------------Block End-------------------");
    }
  }

  public void logStatementTree(String source,
                               StatementTree statementTree) {
    if (LOGGER.isLoggable(Level.FINEST)) {
      if (statementTree == null) {
        LOGGER.finest("Kind of " +
                      (source != null ? source : "Statement") + ": " +
                      "NULL");
        return;
      }
      Kind statementKind = statementTree.getKind();
      LOGGER.finest("Kind of " + (source != null ? source : "Statement") +
                    ": " + statementKind);
      switch (statementKind) {
        case IF:
          LOGGER.finest("If block");
          IfTree ifTree = (IfTree) statementTree;
          LOGGER.finest("Condition: " + ifTree.getCondition());
          logStatementTree("If - Then body", ifTree.getThenStatement());
          logStatementTree("If - Else body", ifTree.getElseStatement());
          break;
        case FOR_LOOP:
          LOGGER.finest("For Loop");
          ForLoopTree forLoopTree = (ForLoopTree) statementTree;
          LOGGER.finest("Initializer: " + forLoopTree.getInitializer());
          LOGGER.finest("Condition: " + forLoopTree.getCondition());
          LOGGER.finest("Update: " + forLoopTree.getUpdate());
          logStatementTree("For Loop body", forLoopTree.getStatement());
          break;
        case ENHANCED_FOR_LOOP:
          LOGGER.finest("Enhanced For Loop");
          EnhancedForLoopTree enhancedForLoopTree =
                  (EnhancedForLoopTree) statementTree;
          LOGGER.finest("Expression: " +
                        enhancedForLoopTree.getExpression());
          LOGGER.finest("Variable: " +
                        enhancedForLoopTree.getVariable());
          logStatementTree("Enhanced For Loop body",
                           enhancedForLoopTree.getStatement());
          break;
        case WHILE_LOOP:
          LOGGER.finest("While Loop");
          WhileLoopTree whileTree = (WhileLoopTree) statementTree;
          LOGGER.finest("Condition: " + whileTree.getCondition());
          logStatementTree("While Loop body", whileTree.getStatement());
          break;
        case DO_WHILE_LOOP:
          LOGGER.finest("Do-While Loop");
          DoWhileLoopTree doWhileLoopTree =
                  (DoWhileLoopTree) statementTree;
          LOGGER.finest("Condition: " + doWhileLoopTree.getCondition());
          logStatementTree("Do-While Loop body",
                           doWhileLoopTree.getStatement());
          break;
        case TRY:
          LOGGER.finest("Try Block");
          TryTree tryTree = (TryTree) statementTree;
          logBlockTree(tryTree.getBlock());
          List<? extends CatchTree> catches = tryTree.getCatches();
          for (CatchTree catchTree : catches) {
            LOGGER.finest("Catch Block For: " +
                          catchTree.getParameter());
            logBlockTree(catchTree.getBlock());
          }
          break;
        case SWITCH:
          LOGGER.finest("Switch Block");
          SwitchTree switchTree = (SwitchTree) statementTree;
          List<? extends CaseTree> cases = switchTree.getCases();
          for (CaseTree caseTree : cases) {
            ExpressionTree caseExpression = caseTree.getExpression();
            LOGGER.finest("Case Expression: " + caseExpression);
            List<? extends StatementTree> caseStatements =
                    caseTree.getStatements();
            for (StatementTree caseStatement : caseStatements) {
              logStatementTree("Case " + caseExpression,
                               caseStatement);
            }
          }
          break;
        case BLOCK:
          logBlockTree((BlockTree) statementTree);
          break;
        case EXPRESSION_STATEMENT:
          logExpressionStatementTree((ExpressionStatementTree) statementTree);
          break;
        case VARIABLE:
          logVariableTree((VariableTree) statementTree);
          break;
        default:
          LOGGER.finest("UNKNOWN STMT (" + statementTree.getKind().
                        name() + "): " + statementTree.toString());
      }
    }
  }

  public void logExpressionStatementTree(ExpressionStatementTree expressionStatementTree) {
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest(expressionStatementTree.getExpression().
                    getKind().
                    name() + ": " + expressionStatementTree.toString());
      logExpressionTree(expressionStatementTree.getExpression());
    }
  }

  public void logExpressionTree(ExpressionTree expressionTree) {
    if (LOGGER.isLoggable(Level.FINEST)) {
      Kind expressionKind = expressionTree.getKind();
      switch (expressionKind) {
        case METHOD_INVOCATION:
          LOGGER.finest("Method Invocation!");
          MethodInvocationTree methodInvocationTree =
                  (MethodInvocationTree) expressionTree;
          LOGGER.finest(methodInvocationTree.getMethodSelect().
                        getKind().
                        name() + ": " +
                        methodInvocationTree.getMethodSelect());
          break;
        default:
          LOGGER.finest("UNKNOWN EXPR (" + expressionKind.name() + "): " +
                        expressionTree.toString());
      }
    }
  }

  public void logVariableTree(VariableTree variableTree) {
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest("Variable Name: " + variableTree.getName().
                    toString());
      Tree type = variableTree.getType();
      logVariableType(type);
    }
  }

  public void logVariableType(Tree type) {
    if (LOGGER.isLoggable(Level.FINEST)) {
      if (type == null) {
        LOGGER.finest("type is NULL!");
        return;
      }
      Kind variableTypeKind = type.getKind();
      switch (variableTypeKind) {
        case IDENTIFIER:
          IdentifierTree identifierTree = (IdentifierTree) type;
          LOGGER.finest("Identifier Type: " + identifierTree.getName().
                        toString());
          break;
        case MEMBER_SELECT:
          MemberSelectTree memberSelectTree =
                  (MemberSelectTree) type;
          LOGGER.finest("Member Select Expression: " +
                        memberSelectTree.getExpression().
                        toString());
          LOGGER.finest("Member Select Name: " +
                        memberSelectTree.getIdentifier().
                        toString());
          logExpressionTree(memberSelectTree.getExpression());
          break;
        case ARRAY_TYPE:
          ArrayTypeTree arrayTypeTree = (ArrayTypeTree) type;
          LOGGER.finest("Array Type(" + arrayTypeTree.getType().
                        getKind() + "): " + arrayTypeTree.getType().
                        toString());
          logVariableType(arrayTypeTree.getType());
          break;
        case PARAMETERIZED_TYPE:
          LOGGER.finest("Parameterized Type");
          ParameterizedTypeTree parameterizedTypeTree =
                  (ParameterizedTypeTree) type;
          logVariableType(parameterizedTypeTree.getType());
          List<? extends Tree> paramTypeArgs =
                  parameterizedTypeTree.getTypeArguments();
          for(Tree tree : paramTypeArgs) {
            logVariableType(tree);
          }
          break;
        case PRIMITIVE_TYPE:
          PrimitiveTypeTree primitiveTypeTree = (PrimitiveTypeTree) type;
          LOGGER.finest("Primitive Type Kind: " + primitiveTypeTree.getPrimitiveTypeKind().name());
          break;
        default:
          LOGGER.finest("UNKNOWN TYPE (" + variableTypeKind + "): " +
                        type.toString());
      }
    }
  }
}
