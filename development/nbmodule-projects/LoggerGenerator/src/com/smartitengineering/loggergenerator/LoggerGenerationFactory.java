/**
 *    This NetBeans Module is responsible for generating Java Util Logger and 
 *    initializing its handlers
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
package com.smartitengineering.loggergenerator;

import com.smartitengineering.javasourcetreeparser.JavaSourceTreeParser;
import com.smartitengineering.javasourcetreeparser.NodeTraversalEvent;
import com.smartitengineering.javasourcetreeparser.NodeTraversalListener;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author imyousuf
 */
public class LoggerGenerationFactory {

  private static final Logger LOGGER;
  

  static {
    LOGGER = Logger.getLogger(LoggerGenerationFactory.class.getName());
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

  private static void convertToLogFromSysOut(final ExpressionTree expressionTree,
                                             final TreeMaker make,
                                             final ClassTree clazz,
                                             final WorkingCopy workingCopy,
                                             final List<? extends ImportTree> imports) {
    boolean hasStaticImport = false;
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
    Kind expressionKind = expressionTree.getKind();
    switch (expressionKind) {
      case METHOD_INVOCATION:
        MethodInvocationTree methodInvocationTree =
                (MethodInvocationTree) expressionTree;
        String methodSelect = methodInvocationTree.getMethodSelect().
                toString();
        if (methodSelect.equals("System.out.println") ||
                methodSelect.equals("System.out.print") ||
                (hasStaticImport &&
                (methodSelect.equals("out.println") ||
                methodSelect.equals("out.print")))) {
          List<ExpressionTree> arguments =
                  new ArrayList<ExpressionTree>();
          //Add the level
          arguments.add(make.MemberSelect(make.Identifier(Level.class.getName()),
                                          "FINEST"));
          //Add the sys out args
          arguments.addAll(methodInvocationTree.getArguments());
          MethodInvocationTree newLogMethodInvocationTree;
          newLogMethodInvocationTree =
                  make.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                                        make.MemberSelect(make.Identifier(clazz.getSimpleName()),
                                                          "log"), arguments);
          workingCopy.rewrite(methodInvocationTree, newLogMethodInvocationTree);
        }
        break;
      default:
    }
  }

  private final static void log(Level level,
                                Object... messages) {
    if (LOGGER.isLoggable(level)) {
      for (Object message : messages) {
        LOGGER.log(level, message != null ? message.toString() : "NULL");
      }
    }
  }

  public static void addLogger(WorkingCopy workingCopy,
                               boolean ignoreExisting,
                               boolean setLevel,
                               Level level) throws IOException {
    workingCopy.toPhase(Phase.RESOLVED);
    TreeMaker make = workingCopy.getTreeMaker();
    CompilationUnitTree compilationUnitTree =
            workingCopy.getCompilationUnit();
    for (Tree typeDecl : compilationUnitTree.getTypeDecls()) {
      if (Tree.Kind.CLASS == typeDecl.getKind()) {
        ClassTree clazz = (ClassTree) typeDecl;
        JavaSourceTreeParser treeParser = new JavaSourceTreeParser();
        treeParser.logDebugInfoOfWorkingCopy(clazz, workingCopy);
        addLoggerToClass(ignoreExisting, clazz, workingCopy, make,
                         compilationUnitTree, setLevel, level);
      }
    }
  }

  protected static boolean addLoggerToClass(final boolean ignoreExisting,
                                            final ClassTree clazz,
                                            final WorkingCopy workingCopy,
                                            final TreeMaker make,
                                            final CompilationUnitTree compilationUnitTree,
                                            final boolean setLevel,
                                            final Level level) {
    boolean hasLogger = false;
    String loggerName = null;
    ClassTree modifiedClazz = null;
    if (!ignoreExisting) {
      loggerName = checkWhetherLoggerExists(clazz, workingCopy);
      hasLogger = loggerName != null;
    }
    ArrayList<Modifier> modifiers = new ArrayList();
    Collections.addAll(modifiers, Modifier.FINAL, Modifier.PRIVATE,
                       Modifier.STATIC);
    int position = 1;
    if (!hasLogger) {
      loggerName = "LOGGER";
      VariableTree variableTree = make.Variable(make.Modifiers(
                                                new HashSet<Modifier>(modifiers),
                                                Collections.<AnnotationTree>emptyList()),
                                                "LOGGER",
                                                make.Identifier(LOGGER.getClass().
                                                                getName()),
                                                null);
      modifiedClazz = make.insertClassMember(clazz, position++,
                                             variableTree);
      String className = new StringBuilder().append(
              compilationUnitTree.getPackageName().
              toString()).
              append('.').
              append(clazz.getSimpleName().
                     toString()).
              toString();
      AssignmentTree assignmentTree =
              make.Assignment(
              make.Identifier(loggerName),
              make.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                                    make.MemberSelect(make.Identifier(LOGGER.getClass().
                                                                      getName()),
                                                      "getLogger"),
                                    Collections.<ExpressionTree>singletonList(make.Literal(className))));
      MethodInvocationTree methodInvocationTree = make.MethodInvocation(
              Collections.<ExpressionTree>emptyList(),
              make.MemberSelect(make.Identifier(className),
                                "initLoggerHandlers"),
              Collections.<ExpressionTree>emptyList());
      List<StatementTree> statements = new ArrayList<StatementTree>();
      Collections.addAll(statements,
                         make.ExpressionStatement(assignmentTree),
                         make.ExpressionStatement(methodInvocationTree));
      BlockTree staticInitializer = make.Block(statements, true);
      modifiedClazz = make.insertClassMember(modifiedClazz, position++,
                                             staticInitializer);
    }
    /**
     * Adds a logger initializer
     * TODO check whether method with same signature already exists or not
     */
    StringBuilder content =
            new StringBuilder("{ java.util.logging.Handler[] handlers = ").append(loggerName).
            append(".getHandlers();").
            append(
            "boolean hasConsoleHandler = false;" +
            "for (java.util.logging.Handler handler : handlers) {" +
            "if (handler instanceof java.util.logging.ConsoleHandler) {" +
            "hasConsoleHandler = true;" + "}" + "}" +
            "if (!hasConsoleHandler) {").
            append("java.util.logging.ConsoleHandler consoleHandler = " +
                   "new java.util.logging.ConsoleHandler();").
            append(loggerName).
            append(
            ".addHandler(consoleHandler);");
    if (setLevel) {
      content.append("consoleHandler.setLevel(").
              append(Level.class.getName()).
              append(".").
              append(level.getName()).
              append(");");
    }
    content.append("}");
    if (setLevel) {
      content.append(loggerName).
              append(".setLevel(").
              append(Level.class.getName()).
              append(".").
              append(level.getName()).
              append(");");
    }
    content.append("}");
    ExpressionTree returnType = make.Identifier("void");
    MethodTree initLoggerHandlersMethod = make.Method(make.Modifiers(
                                                      new HashSet(modifiers)),
                                                      "initLoggerHandlers",
                                                      returnType,
                                                      Collections.<TypeParameterTree>emptyList(),
                                                      Collections.<VariableTree>emptyList(),
                                                      Collections.<ExpressionTree>emptyList(),
                                                      content.toString(), null);
    modifiedClazz = make.insertClassMember(
            modifiedClazz == null ? clazz : modifiedClazz,
            position++,
            initLoggerHandlersMethod);
    /**
     * Adds a logger for all levels
     * TODO check whether method with same signature already exists or not
     */
    content =
            new StringBuilder("{" + "if (").append(loggerName).
            append(".isLoggable(level)) {" + "for(Object message : messages) {").
            append(loggerName).
            append(".log(level, message != null ? message.toString() : \"NULL\");" +
                   "}" + "}" + "}");
    List<VariableTree> params = new ArrayList<VariableTree>(2);
    params.add(make.Variable(make.Modifiers(new HashSet()), "level",
                             make.Identifier(Level.class.getName()), null));
    params.add(make.Variable(make.Modifiers(new HashSet()), "messages",
                             make.Identifier("Object..."), null));
    MethodTree logMethod = make.Method(make.Modifiers(new HashSet(modifiers)),
                                       "log", returnType,
                                       Collections.<TypeParameterTree>emptyList(),
                                       params,
                                       Collections.<ExpressionTree>emptyList(),
                                       content.toString(), null);
    modifiedClazz = make.insertClassMember(modifiedClazz, position++,
                                           logMethod);
    workingCopy.rewrite(clazz, modifiedClazz);
    return hasLogger;
  }

  public static void convertSysOutToLog(final WorkingCopy workingCopy,
                                        final boolean ignoreExisting,
                                        final boolean setLevel,
                                        final Level level) throws IOException {
    workingCopy.toPhase(Phase.RESOLVED);
    TreeMaker make = workingCopy.getTreeMaker();
    CompilationUnitTree compilationUnitTree =
            workingCopy.getCompilationUnit();
    for (Tree typeDecl : compilationUnitTree.getTypeDecls()) {
      if (Tree.Kind.CLASS == typeDecl.getKind()) {
        ClassTree clazz = (ClassTree) typeDecl;
        JavaSourceTreeParser treeParser = new JavaSourceTreeParser();
        treeParser.addNodeTraversalListener(new NodeTraversalListenerImpl(workingCopy));
        treeParser.logDebugInfoOfWorkingCopy(clazz, workingCopy);
        addLoggerToClass(false, clazz, workingCopy, make,
                         compilationUnitTree, true, Level.FINEST);
        treeParser.parseWorkingCopy(clazz, workingCopy, make);
      }
    }
  }

  private static String checkWhetherLoggerExists(final ClassTree clazz,
                                                 final WorkingCopy workingCopy) {
    String loggerName = null;
    List<? extends ImportTree> imports =
            workingCopy.getCompilationUnit().
            getImports();
    boolean importExists = false;
    for (ImportTree importTree : imports) {
      MemberSelectTree memberSelectTree =
              (MemberSelectTree) importTree.getQualifiedIdentifier();
      if (Pattern.matches(
              LoggerGenerator.JAVA_UTIL_LOGGER_QUALIFIER,
              memberSelectTree.toString())) {
        importExists = true;
        break;
      }
    }
    boolean staticIdentifierExists = false;
    List<? extends Tree> members = clazz.getMembers();
    for (Tree member : members) {
      if (member.getKind().
              equals(Kind.VARIABLE)) {
        VariableTree variableTree = (VariableTree) member;
        Tree varTypeTree = variableTree.getType();
        switch (varTypeTree.getKind()) {
          case IDENTIFIER:
            IdentifierTree identifierTree =
                    (IdentifierTree) varTypeTree;
            staticIdentifierExists = importExists &&
                    Pattern.matches(
                    LoggerGenerator.JAVA_UTIL_LOGGER_IDENTIFIER,
                    identifierTree.getName().
                    toString()) &&
                    variableTree.getModifiers().
                    getFlags().
                    contains(Modifier.STATIC);
            break;
          case MEMBER_SELECT:
            MemberSelectTree memberSelectTree =
                    (MemberSelectTree) varTypeTree;
            staticIdentifierExists = Pattern.matches(
                    LoggerGenerator.JAVA_UTIL_LOGGER_IDENTIFIER,
                    memberSelectTree.toString()) &&
                    variableTree.getModifiers().
                    getFlags().
                    contains(Modifier.STATIC);
            break;
        }
        if (staticIdentifierExists) {
          loggerName = variableTree.getName().
                  toString();
        }
      }
      if (staticIdentifierExists) {
        break;
      }
    }
    return loggerName;
  }

  private static class NodeTraversalListenerImpl
          implements NodeTraversalListener {

    private final WorkingCopy workingCopy;

    public NodeTraversalListenerImpl(WorkingCopy workingCopy) {
      this.workingCopy = workingCopy;
    }

    public void notifyAboutNode(NodeTraversalEvent event) {
      List<Tree> nodeStack = event.getParentList();
      convertToLogFromSysOut((ExpressionTree)event.getCurrentNode(), event.getTreeMaker(),
                             (ClassTree)nodeStack.get(0), workingCopy, event.getImports());
    }

    public Kind getTreeKind() {
      return Kind.METHOD_INVOCATION;
    }
  }
}
