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

import com.smartitengineering.javasourcetreeparser.StateListener;
import com.smartitengineering.javasourcetreeparser.JavaSourceTreeParser;
import com.smartitengineering.javasourcetreeparser.NodeTraversalEvent;
import com.smartitengineering.javasourcetreeparser.NodeTraversalListener;
import com.smartitengineering.javasourcetreeparser.ParsingUtils;
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
import java.util.Comparator;
import java.util.HashMap;
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
                                             final List<? extends ImportTree> imports,
                                             StateListener listener) {
    boolean hasStaticImport = false;
    if (listener != null) {
      if (listener.isInitialized()) {
        hasStaticImport =
                ((Boolean) listener.getValue("hasStaticImport")).booleanValue();
      }
      else {
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
        listener.setValue("hasStaticImport", Boolean.valueOf(hasStaticImport));
        listener.setInitialized();
      }
      if (listener.getValue("addLoggerClassReturnValueFor" +
                            clazz.getSimpleName()) == null) {
        Object[] objects = addLoggerToClass(false, clazz, workingCopy, make,
                                            workingCopy.getCompilationUnit(),
                                            true,
                                            Level.FINEST);
        listener.setValue("addLoggerClassReturnValueFor" + clazz.getSimpleName(),
                          objects);
        listener.setInitialized();
      }
    }
    else {
      throw new IllegalArgumentException();
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
        addLoggerToClass(ignoreExisting, clazz, workingCopy, make,
                         compilationUnitTree, setLevel, level);
      }
    }
  }

  protected static Object[] addLoggerToClass(final boolean ignoreExisting,
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
     * Fetch and sort the methods of the class
     */
    Comparator<MethodTree> methodComparator =
            ParsingUtils.getMethodTreeComparator();
    List<MethodTree> memberMethods = getMemberMethodTrees(clazz);
    Collections.sort(memberMethods, methodComparator);
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
    if (Collections.binarySearch(memberMethods, initLoggerHandlersMethod,
                                 methodComparator) < 0) {
      modifiedClazz = make.insertClassMember(
              modifiedClazz == null ? clazz : modifiedClazz,
              position++,
              initLoggerHandlersMethod);
    }
    content =
            new StringBuilder("{" + "if (").append(loggerName).
            append(".isLoggable(level)) {" + "for(Object message : messages) {").
            append(loggerName).
            append(".log(level, message != null ? message.toString() : \"NULL\");" +
                   "}" + "}" + "}");
    List<VariableTree> params = new ArrayList<VariableTree>(2);
    params.add(make.Variable(make.Modifiers(new HashSet()), "level",
                             ParsingUtils.getMemberSelectTreeForClassName(make,
                                                                          Level.class),
                             null));
    params.add(make.Variable(make.Modifiers(new HashSet()), "messages",
                             make.Identifier("Object..."), null));
    MethodTree logMethod = make.Method(make.Modifiers(new HashSet(modifiers)),
                                       "log", returnType,
                                       Collections.<TypeParameterTree>emptyList(),
                                       params,
                                       Collections.<ExpressionTree>emptyList(),
                                       content.toString(), null);
    if (Collections.binarySearch(memberMethods, logMethod, methodComparator) < 0) {
      modifiedClazz = make.insertClassMember(modifiedClazz == null ? clazz : modifiedClazz, position++,
                                             logMethod);
    }
    if (modifiedClazz == null) {
      modifiedClazz = clazz;
    }
    workingCopy.rewrite(clazz, modifiedClazz);
    return new Object[]{loggerName, modifiedClazz};
  }

  public static List<MethodTree> getMemberMethodTrees(ClassTree clazz) {
    ArrayList<MethodTree> methodTrees = new ArrayList<MethodTree>();
    List<? extends Tree> members = clazz.getMembers();
    for (Tree member : members) {
      if (member.getKind() == Kind.METHOD) {
        methodTrees.add((MethodTree) member);
      }
    }
    return methodTrees;
  }

  public static void convertSysOutToLog(final WorkingCopy workingCopy,
                                        final boolean ignoreExisting,
                                        final boolean setLevel,
                                        final Level level) throws IOException {
    injectCodeWithListener(workingCopy, ignoreExisting, setLevel, level,
                           new MethodInvocationNodeTraversalListenerImpl());
  }

  public static void addLogs(final WorkingCopy workingCopy,
                             final boolean ignoreExisting,
                             final boolean setLevel,
                             final Level level) throws IOException {
    injectCodeWithListener(workingCopy, ignoreExisting, setLevel, level,
                           new MethodNodeTraversalListenerImpl());
  }

  public static void injectCodeWithListener(final WorkingCopy workingCopy,
                                            final boolean ignoreExisting,
                                            final boolean setLevel,
                                            final Level level,
                                            final NodeTraversalListener listener)
          throws IOException {
    workingCopy.toPhase(Phase.RESOLVED);
    TreeMaker make = workingCopy.getTreeMaker();
    CompilationUnitTree compilationUnitTree =
            workingCopy.getCompilationUnit();
    for (Tree typeDecl : compilationUnitTree.getTypeDecls()) {
      if (Tree.Kind.CLASS == typeDecl.getKind()) {
        ClassTree clazz = (ClassTree) typeDecl;
        JavaSourceTreeParser treeParser = new JavaSourceTreeParser();
        treeParser.addNodeTraversalListener(listener);
        treeParser.logDebugInfoOfWorkingCopy(clazz, workingCopy);
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

  public static void addLogToMethodBlock(ClassTree clazzTree,
                                         MethodTree method,
                                         WorkingCopy workingCopy,
                                         TreeMaker make,
                                         StateListener listener) {
    Object[] objects;
    if (listener != null) {
      if (listener.getValue("addLoggerClassReturnValueFor" +
                            clazzTree.getSimpleName()) != null) {
        objects = (Object[]) listener.getValue("addLoggerClassReturnValueFor" +
                                               clazzTree.getSimpleName());
      }
      else {
        objects = addLoggerToClass(false, clazzTree, workingCopy, make,
                                   workingCopy.getCompilationUnit(), true,
                                   Level.FINEST);
        listener.setValue("addLoggerClassReturnValueFor" +
                          clazzTree.getSimpleName(), objects);
        listener.setInitialized();
      }
      if (listener.getValue("logMethodFor" + clazzTree.getSimpleName()) == null) {
        String loggerName = (String) objects[0];
        addMethodForLoggingMethod(loggerName, make, objects, workingCopy,
                                  clazzTree);
        listener.setValue("logMethodFor" + clazzTree.getSimpleName(),
                          Boolean.TRUE);
      }
    }
    else {
      throw new IllegalArgumentException();
    }
    BlockTree originalMethodBlock = method.getBody();
    List<? extends VariableTree> params = method.getParameters();
    List<ExpressionTree> arguments =
            new ArrayList<ExpressionTree>();
    arguments.add(make.MemberSelect(make.Identifier(Level.class.getName()),
                                    "FINEST"));
    arguments.add(make.Literal(method.getName().
                               toString()));
    for (VariableTree param : params) {
      arguments.add(make.Identifier(param.getName().
                                    toString()));
    }

    MethodInvocationTree newLogMethodInvocationTree;
    newLogMethodInvocationTree =
            make.MethodInvocation(Collections.<ExpressionTree>emptyList(),
                                  make.MemberSelect(make.Identifier(clazzTree.getSimpleName()),
                                                    "logMethod"), arguments);
    BlockTree newMethodBlockTree =
            make.insertBlockStatement(originalMethodBlock, 0,
                                      make.ExpressionStatement(newLogMethodInvocationTree));
    workingCopy.rewrite(originalMethodBlock, newMethodBlockTree);
  }

  private static void addMethodForLoggingMethod(String loggerName,
                                                TreeMaker make,
                                                Object[] objects,
                                                WorkingCopy workingCopy,
                                                ClassTree clazzTree) {
    StringBuilder logMethodContent =
            new StringBuilder("{" + "if (").append(loggerName).
            append(".isLoggable(level)) { StringBuilder message ").
            append("= new StringBuilder(\"Entering Method \")").
            append(".append(methodName).append(\" (\");").
            append("for(Object param : parameters) {").
            append("message.append(param != null ? param.toString() : \"NULL\").append(\", \");" +
                   "}").
            append("if(parameters != null && parameters.length > 0)").
            append("message.delete(message.length() - 2, message.length());").
            append("message.append(\")\")").
            append(loggerName).
            append(".log(level, message.toString());").
            append("}" + "}");
    logMethodContent.delete(logMethodContent.length() - 2,
                            logMethodContent.length());
    List<VariableTree> generatedMethodParams =
            new ArrayList<VariableTree>(3);
    generatedMethodParams.add(make.Variable(make.Modifiers(new HashSet()),
                                            "level",
                                            ParsingUtils.getMemberSelectTreeForClassName(make,
                                                                                         Level.class),
                                            null));
    generatedMethodParams.add(make.Variable(make.Modifiers(new HashSet()),
                                            "methodName",
                                            make.Identifier("String"), null));
    generatedMethodParams.add(make.Variable(make.Modifiers(new HashSet()),
                                            "parameters",
                                            make.Identifier("Object..."), null));
    ArrayList<Modifier> modifiers = new ArrayList();
    Collections.addAll(modifiers, Modifier.FINAL,
                       Modifier.PRIVATE,
                       Modifier.STATIC);
    ExpressionTree returnType = make.Identifier("void");
    MethodTree logMethod =
            make.Method(make.Modifiers(new HashSet(modifiers)), "logMethod",
                        returnType, Collections.<TypeParameterTree>emptyList(),
                        generatedMethodParams,
                        Collections.<ExpressionTree>emptyList(),
                        logMethodContent.toString(), null);
    ClassTree modifiedClazz = (ClassTree) objects[1];
    /**
     * Fetch and sort the methods of the class
     */
    Comparator<MethodTree> methodComparator =
            ParsingUtils.getMethodTreeComparator();
    List<MethodTree> memberMethods = getMemberMethodTrees(modifiedClazz);
    Collections.sort(memberMethods, methodComparator);
    if (Collections.binarySearch(memberMethods, logMethod,
                                 ParsingUtils.getMethodTreeComparator()) < 0) {
      modifiedClazz = make.insertClassMember(modifiedClazz, 0, logMethod);
    }
    workingCopy.rewrite(clazzTree, modifiedClazz);
  }

  private static class MethodInvocationNodeTraversalListenerImpl
          implements NodeTraversalListener, StateListener {

    private boolean initialized = false;
    private HashMap<String, Object> initMap = new HashMap<String, Object>();

    public void notifyAboutNode(NodeTraversalEvent event) {
      List<Tree> nodeStack = event.getParentList();
      convertToLogFromSysOut((ExpressionTree) event.getCurrentNode(),
                             event.getTreeMaker(),
                             (ClassTree) nodeStack.get(0),
                             event.getWorkingCopy(),
                             event.getImports(), this);
    }

    public Kind getTreeKind() {
      return Kind.METHOD_INVOCATION;
    }

    public boolean isInitialized() {
      return initialized;
    }

    public void setInitialized() {
      setInitializationStatus(true);
    }

    public void unsetInitialized() {
      setInitializationStatus(false);
    }

    public void setInitializationStatus(boolean initialized) {
      this.initialized = initialized;
    }

    public Object getValue(String key) {
      return initMap.get(key);
    }

    public void setValue(String key,
                         Object value) {
      initMap.put(key, value);
    }
  }

  private static class MethodNodeTraversalListenerImpl
          implements NodeTraversalListener, StateListener {

    private boolean initialized = false;
    private HashMap<String, Object> initMap = new HashMap<String, Object>();

    public void notifyAboutNode(NodeTraversalEvent event) {
      List<Tree> trees = event.getParentList();
      addLogToMethodBlock((ClassTree) event.getParentList().
                          get(0),
                          (MethodTree) event.getCurrentNode(),
                          event.getWorkingCopy(), event.getTreeMaker(), this);
    }

    public Kind getTreeKind() {
      return Kind.METHOD;
    }

    public boolean isInitialized() {
      return initialized;
    }

    public void setInitialized() {
      setInitializationStatus(true);
    }

    public void unsetInitialized() {
      setInitializationStatus(false);
    }

    public void setInitializationStatus(boolean initialized) {
      this.initialized = initialized;
    }

    public Object getValue(String key) {
      return initMap.get(key);
    }

    public void setValue(String key,
                         Object value) {
      initMap.put(key, value);
    }
  }
}
