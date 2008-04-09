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

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
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
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
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

    private final static void log(Level level, Object... messages) {
        if (LOGGER.isLoggable(level)) {
            for(Object message : messages) {
                LOGGER.log(level, message != null ? message.toString() : "NULL");
            }
        }
    }

    public static void addLogger(WorkingCopy workingCopy, boolean ignoreExisting,
            boolean setLevel, Level level) throws IOException {
        workingCopy.toPhase(Phase.RESOLVED);
        TreeMaker make = workingCopy.getTreeMaker();
        CompilationUnitTree compilationUnitTree = workingCopy.getCompilationUnit();
        for (Tree typeDecl : compilationUnitTree.getTypeDecls()) {
            if (Tree.Kind.CLASS == typeDecl.getKind()) {
                ClassTree clazz = (ClassTree) typeDecl;
                LoggerGenerationFactory.logDebugInfoOfWorkingCopy(clazz, workingCopy);
                addLoggerToClass(ignoreExisting, clazz, workingCopy, make, compilationUnitTree, setLevel, level);
            }
        }
    }
    
    protected static boolean addLoggerToClass(boolean ignoreExisting, ClassTree clazz, WorkingCopy workingCopy, TreeMaker make, CompilationUnitTree compilationUnitTree, boolean setLevel, Level level) {
        boolean hasLogger = false;
        String loggerName = null;
        ClassTree modifiedClazz = null;
        if (!ignoreExisting) {
            loggerName = checkWhetherLoggerExists(clazz, workingCopy);
            hasLogger = loggerName != null;
        }
        ArrayList<Modifier> modifiers = new ArrayList();
        Collections.addAll(modifiers, Modifier.FINAL, Modifier.PRIVATE, Modifier.STATIC);
        int position = 1;
        if (!hasLogger) {
            loggerName = "LOGGER";
            VariableTree variableTree = make.Variable(make.Modifiers(new HashSet<Modifier>(modifiers), Collections.<AnnotationTree>emptyList()), "LOGGER", make.Identifier(LOGGER.getClass().getName()), null);
            modifiedClazz = make.insertClassMember(clazz, position++, variableTree);
            String className = new StringBuilder().append(compilationUnitTree.getPackageName().toString()).append('.').append(clazz.getSimpleName().toString()).toString();
            AssignmentTree assignmentTree = make.Assignment(make.Identifier(loggerName), make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier(LOGGER.getClass().getName()), "getLogger"), Collections.<ExpressionTree>singletonList(make.Literal(className))));
            MethodInvocationTree methodInvocationTree = make.MethodInvocation(Collections.<ExpressionTree>emptyList(), make.MemberSelect(make.Identifier(className), "initLoggerHandlers"), Collections.<ExpressionTree>emptyList());
            List<StatementTree> statements = new ArrayList<StatementTree>();
            Collections.addAll(statements, make.ExpressionStatement(assignmentTree), make.ExpressionStatement(methodInvocationTree));
            BlockTree staticInitializer = make.Block(statements, true);
            modifiedClazz = make.insertClassMember(modifiedClazz, position++, staticInitializer);
        }
        /**
         * Adds a logger initializer
         * TODO check whether method with same signature already exists or not
         */
        StringBuilder content = new StringBuilder("{ java.util.logging.Handler[] handlers = ").append(loggerName).append(".getHandlers();").append(
                "boolean hasConsoleHandler = false;"
                + "for (java.util.logging.Handler handler : handlers) {"
                + "if (handler instanceof java.util.logging.ConsoleHandler) {"
                + "hasConsoleHandler = true;"
                + "}"
                + "}"
                + "if (!hasConsoleHandler) {").append(loggerName).append(
                ".addHandler(new java.util.logging.ConsoleHandler());"
                + "}"
                );
        if (setLevel) {
            content.append(loggerName).append(".setLevel(").append(Level.class.getName()).append(".").append(level.getName()).append(");");
        }
        content.append("}");
        ExpressionTree returnType = make.Identifier("void");
        MethodTree initLoggerHandlersMethod = make.Method(make.Modifiers(new HashSet(modifiers)), "initLoggerHandlers", returnType, Collections.<TypeParameterTree>emptyList(), Collections.<VariableTree>emptyList(), Collections.<ExpressionTree>emptyList(), content.toString(), null);
        modifiedClazz = make.insertClassMember(modifiedClazz == null ? clazz : modifiedClazz, position++, initLoggerHandlersMethod);
        /**
         * Adds a logger for all levels
         * TODO check whether method with same signature already exists or not
         */
        content = new StringBuilder("{"
            + "if (").append(loggerName).append(".isLoggable(level)) {"
            + "for(Object message : messages) {"
            ).append(loggerName).append(".log(level, message != null ? message.toString() : \"NULL\");"
            + "}"
            + "}"
            + "}"
            );
        List<VariableTree> params = new ArrayList<VariableTree>(2);
        params.add(make.Variable(make.Modifiers(new HashSet()), "level", make.Identifier(Level.class.getName()), null));
        params.add(make.Variable(make.Modifiers(new HashSet()), "messages", make.Identifier("Object..."), null));
        MethodTree logMethod = make.Method(make.Modifiers(new HashSet(modifiers)), "log", returnType, Collections.<TypeParameterTree>emptyList(), params, Collections.<ExpressionTree>emptyList(), content.toString(), null);
        modifiedClazz = make.insertClassMember(modifiedClazz, position++, logMethod);
        workingCopy.rewrite(clazz, modifiedClazz);
        return hasLogger;
    }

    public static void convertSysOutToLog(WorkingCopy workingCopy, boolean ignoreExisting,
            boolean setLevel, Level level) throws IOException {
        workingCopy.toPhase(Phase.RESOLVED);
        TreeMaker make = workingCopy.getTreeMaker();
        CompilationUnitTree compilationUnitTree = workingCopy.getCompilationUnit();
        for (Tree typeDecl : compilationUnitTree.getTypeDecls()) {
            if (Tree.Kind.CLASS == typeDecl.getKind()) {
                ClassTree clazz = (ClassTree) typeDecl;
                LoggerGenerationFactory.logDebugInfoOfWorkingCopy(clazz, workingCopy);
                addLoggerToClass(false, clazz, workingCopy, make, compilationUnitTree, false, Level.FINEST);
                parseWorkingCopy(clazz, workingCopy, make);
            }
        }
    }

    private static String checkWhetherLoggerExists(ClassTree clazz, WorkingCopy workingCopy) {
        String loggerName = null;
        List<? extends ImportTree> imports = workingCopy.getCompilationUnit().getImports();
        boolean importExists = false;
        for (ImportTree importTree : imports) {
            MemberSelectTree memberSelectTree = (MemberSelectTree) importTree.getQualifiedIdentifier();
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
            if (member.getKind().equals(Kind.VARIABLE)) {
                VariableTree variableTree = (VariableTree) member;
                Tree varTypeTree = variableTree.getType();
                switch (varTypeTree.getKind()) {
                    case IDENTIFIER:
                        IdentifierTree identifierTree = (IdentifierTree) varTypeTree;
                        staticIdentifierExists = importExists &&
                                Pattern.matches(LoggerGenerator.JAVA_UTIL_LOGGER_IDENTIFIER,
                                identifierTree.getName().toString()) &&
                                variableTree.getModifiers().getFlags().contains(Modifier.STATIC);
                        break;
                    case MEMBER_SELECT:
                        MemberSelectTree memberSelectTree = (MemberSelectTree) varTypeTree;
                        staticIdentifierExists = Pattern.matches(LoggerGenerator.JAVA_UTIL_LOGGER_IDENTIFIER,
                                memberSelectTree.toString()) &&
                                variableTree.getModifiers().getFlags().contains(Modifier.STATIC);
                        break;
                }
                if(staticIdentifierExists) {
                    loggerName = variableTree.getName().toString();
                }
            }
            if (staticIdentifierExists) {
                break;
            }
        }
        return loggerName;
    }

    //Following code will walk down to SysOuts
    public static void parseWorkingCopy(ClassTree clazz, WorkingCopy workingCopy, TreeMaker make) {
        parseClassTree(clazz, workingCopy, make, null);
    }

    public static void parseClassTree(ClassTree clazz, WorkingCopy workingCopy, TreeMaker make, ClassTree parentClassTree) {
        List<? extends Tree> members = clazz.getMembers();
        for (Tree member : members) {
            Kind memberKind = member.getKind();
            if (memberKind.equals(Kind.METHOD)) {
                MethodTree methodTree = (MethodTree) member;
                BlockTree blockTree = methodTree.getBody();
                parseBlockTree(blockTree, workingCopy, make, parentClassTree == null ? clazz : parentClassTree);
            } else if (memberKind.equals(Kind.VARIABLE)) {
                VariableTree variableTree = (VariableTree) member;
                parseVariableTree(variableTree, workingCopy, make);
            } else if (memberKind.equals(Kind.BLOCK)) {
                parseBlockTree((BlockTree) member, workingCopy, make, parentClassTree == null ? clazz : parentClassTree);
            } else if (memberKind.equals(Kind.CLASS)) {
                parseClassTree((ClassTree) member, workingCopy, make, clazz);
            } else {
            }
        }
    }

    public static void parseBlockTree(BlockTree blockTree, WorkingCopy workingCopy, TreeMaker make, ClassTree clazz) {
        List<? extends StatementTree> statements = blockTree.getStatements();
        for (StatementTree statementTree : statements) {
            Kind statementKind = statementTree.getKind();
            parseStatementTree(null, statementTree, workingCopy, make, clazz);
        }
    }

    public static void parseStatementTree(String source, StatementTree statementTree, WorkingCopy workingCopy, TreeMaker make, ClassTree clazz) {
        if (statementTree == null) {
            return;
        }
        Kind statementKind = statementTree.getKind();
        switch (statementKind) {
            case IF:
                IfTree ifTree = (IfTree) statementTree;
                parseStatementTree("If - Then body", ifTree.getThenStatement(), workingCopy, make, clazz);
                parseStatementTree("If - Else body", ifTree.getElseStatement(), workingCopy, make, clazz);
                break;
            case FOR_LOOP:
                ForLoopTree forLoopTree = (ForLoopTree) statementTree;
                parseStatementTree("For Loop body", forLoopTree.getStatement(), workingCopy, make, clazz);
                break;
            case ENHANCED_FOR_LOOP:
                EnhancedForLoopTree enhancedForLoopTree = (EnhancedForLoopTree) statementTree;
                parseStatementTree("Enhanced For Loop body", enhancedForLoopTree.getStatement(), workingCopy, make, clazz);
                break;
            case WHILE_LOOP:
                WhileLoopTree whileTree = (WhileLoopTree) statementTree;
                parseStatementTree("While Loop body", whileTree.getStatement(), workingCopy, make, clazz);
                break;
            case DO_WHILE_LOOP:
                DoWhileLoopTree doWhileLoopTree = (DoWhileLoopTree) statementTree;
                parseStatementTree("Do-While Loop body", doWhileLoopTree.getStatement(), workingCopy, make, clazz);
                break;
            case TRY:
                TryTree tryTree = (TryTree) statementTree;
                parseBlockTree(tryTree.getBlock(), workingCopy, make, clazz);
                List<? extends CatchTree> catches = tryTree.getCatches();
                for (CatchTree catchTree : catches) {
                    parseBlockTree(catchTree.getBlock(), workingCopy, make, clazz);
                }
                break;
            case SWITCH:
                SwitchTree switchTree = (SwitchTree) statementTree;
                List<? extends CaseTree> cases = switchTree.getCases();
                for (CaseTree caseTree : cases) {
                    ExpressionTree caseExpression = caseTree.getExpression();
                    List<? extends StatementTree> caseStatements = caseTree.getStatements();
                    for (StatementTree caseStatement : caseStatements) {
                        parseStatementTree("Case " + caseExpression, caseStatement, workingCopy, make, clazz);
                    }
                }
                break;
            case BLOCK:
                parseBlockTree((BlockTree) statementTree, workingCopy, make, clazz);
                break;
            case EXPRESSION_STATEMENT:
                parseExpressionStatementTree((ExpressionStatementTree) statementTree, workingCopy, make, clazz);
                break;
            case VARIABLE:
                parseVariableTree((VariableTree) statementTree, workingCopy, make);
                break;
            default:
        }
    }

    public static void parseExpressionStatementTree(ExpressionStatementTree expressionStatementTree, WorkingCopy workingCopy, TreeMaker make, ClassTree clazz) {
        parseExpressionTree(expressionStatementTree.getExpression(), workingCopy, make, clazz);
    }

    public static void parseExpressionTree(ExpressionTree expressionTree, WorkingCopy workingCopy, TreeMaker make, ClassTree clazz) {
        Kind expressionKind = expressionTree.getKind();
        switch (expressionKind) {
            case METHOD_INVOCATION:
                MethodInvocationTree methodInvocationTree = (MethodInvocationTree) expressionTree;
                if (methodInvocationTree.getMethodSelect().toString().equals("System.out.println")) {
                    List<ExpressionTree> arguments = new ArrayList<ExpressionTree>();
                    //Add the level
                    arguments.add(make.MemberSelect(make.Identifier(Level.class.getName()), "FINEST"));
                    //Add the sys out args
                    arguments.addAll(methodInvocationTree.getArguments());
                    MethodInvocationTree newLogMethodInvocationTree;
                    newLogMethodInvocationTree = make.MethodInvocation(
                            Collections.<ExpressionTree>emptyList(),
                            make.MemberSelect(
                            make.Identifier(clazz.getSimpleName()), "log"),
                            arguments
                            );
                    workingCopy.rewrite(methodInvocationTree, newLogMethodInvocationTree);
                }
                break;
            default:
        }
    }

    public static void parseVariableTree(VariableTree variableTree, WorkingCopy workingCopy, TreeMaker make) {
        Tree type = variableTree.getType();
        Kind variableTypeKind = type.getKind();
        switch (variableTypeKind) {
            case IDENTIFIER:
                break;
            case MEMBER_SELECT:
                MemberSelectTree memberSelectTree = (MemberSelectTree) type;
                LOGGER.finest("Member Select Expression: " + memberSelectTree.getExpression().toString());
                LOGGER.finest("Member Select Name: " + memberSelectTree.getIdentifier().toString());
                break;
            case ARRAY_TYPE:
                break;
            case PARAMETERIZED_TYPE:
                break;
            default:

        }
    }

    public static void logDebugInfoOfWorkingCopy(ClassTree clazz, WorkingCopy workingCopy) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            if (workingCopy != null) {
                List<? extends ImportTree> imports = workingCopy.getCompilationUnit().getImports();
                LOGGER.finest("Package: " + workingCopy.getCompilationUnit().getPackageName());
                for (ImportTree importTree : imports) {
                    LOGGER.finest("Import Q-Id: " + importTree.getQualifiedIdentifier().getKind().name());
                    LOGGER.finest("Static Import?: " + importTree.isStatic());
                    LOGGER.finest("Import Tree: " + importTree.toString() + " Length: " + importTree.toString().length() + '\n');
                    MemberSelectTree memberSelectTree = (MemberSelectTree) importTree.getQualifiedIdentifier();
                    LOGGER.finest("Member Selected ID: " + memberSelectTree.getIdentifier());
                    LOGGER.finest("Member toString: " + memberSelectTree.toString());
                    LOGGER.finest("Member Exp toString: " + memberSelectTree.getExpression().toString());
                    LOGGER.finest("Util Import Pattern matches: " + Pattern.matches(LoggerGenerator.JAVA_UTIL_LOGGER_QUALIFIER, memberSelectTree.toString()));
                }
            }
            logClassTree(clazz);
        }
    }

    public static void logClassTree(ClassTree clazz) {
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
                    LOGGER.finest("Method Name: " + methodTree.getName().toString());
                    List<? extends VariableTree> params = methodTree.getParameters();
                    LOGGER.finest("Parameters:");
                    for (VariableTree param : params) {
                        logVariableTree(param);
                    }
                    BlockTree blockTree = methodTree.getBody();
                    logBlockTree(blockTree);
                } else if (memberKind.equals(Kind.VARIABLE)) {
                    VariableTree variableTree = (VariableTree) member;
                    logVariableTree(variableTree);
                } else if (memberKind.equals(Kind.BLOCK)) {
                    logBlockTree((BlockTree) member);
                } else if (memberKind.equals(Kind.CLASS)) {
                    logDebugInfoOfWorkingCopy((ClassTree) member, null);
                } else {
                    LOGGER.finest("Unrecognized block: " + memberKind);
                }
            }
        }
    }

    public static void logBlockTree(BlockTree blockTree) {
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

    public static void logStatementTree(String source, StatementTree statementTree) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            if (statementTree == null) {
                LOGGER.finest("Kind of " + (source != null ? source : "Statement") + ": " + "NULL");
                return;
            }
            Kind statementKind = statementTree.getKind();
            LOGGER.finest("Kind of " + (source != null ? source : "Statement") + ": " + statementKind);
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
                    EnhancedForLoopTree enhancedForLoopTree = (EnhancedForLoopTree) statementTree;
                    LOGGER.finest("Expression: " + enhancedForLoopTree.getExpression());
                    LOGGER.finest("Variable: " + enhancedForLoopTree.getVariable());
                    logStatementTree("Enhanced For Loop body", enhancedForLoopTree.getStatement());
                    break;
                case WHILE_LOOP:
                    LOGGER.finest("While Loop");
                    WhileLoopTree whileTree = (WhileLoopTree) statementTree;
                    LOGGER.finest("Condition: " + whileTree.getCondition());
                    logStatementTree("While Loop body", whileTree.getStatement());
                    break;
                case DO_WHILE_LOOP:
                    LOGGER.finest("Do-While Loop");
                    DoWhileLoopTree doWhileLoopTree = (DoWhileLoopTree) statementTree;
                    LOGGER.finest("Condition: " + doWhileLoopTree.getCondition());
                    logStatementTree("Do-While Loop body", doWhileLoopTree.getStatement());
                    break;
                case TRY:
                    LOGGER.finest("Try Block");
                    TryTree tryTree = (TryTree) statementTree;
                    logBlockTree(tryTree.getBlock());
                    List<? extends CatchTree> catches = tryTree.getCatches();
                    for (CatchTree catchTree : catches) {
                        LOGGER.finest("Catch Block For: " + catchTree.getParameter());
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
                        List<? extends StatementTree> caseStatements = caseTree.getStatements();
                        for (StatementTree caseStatement : caseStatements) {
                            logStatementTree("Case " + caseExpression, caseStatement);
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
                    LOGGER.finest(statementTree.getKind().name() + ": " + statementTree.toString());
            }
        }
    }

    public static void logExpressionStatementTree(ExpressionStatementTree expressionStatementTree) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(expressionStatementTree.getExpression().getKind().name() + ": " + expressionStatementTree.toString());
            logExpressionTree(expressionStatementTree.getExpression());
        }
    }

    public static void logExpressionTree(ExpressionTree expressionTree) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            Kind expressionKind = expressionTree.getKind();
            switch (expressionKind) {
                case METHOD_INVOCATION:
                    LOGGER.finest("Method Invocation!");
                    MethodInvocationTree methodInvocationTree = (MethodInvocationTree) expressionTree;
                    LOGGER.finest(methodInvocationTree.getMethodSelect().getKind().name() + ": " + methodInvocationTree.getMethodSelect());
                    break;
                default:
                    LOGGER.finest(expressionKind.name() + ": " + expressionTree.toString());
            }
        }
    }

    public static void logVariableTree(VariableTree variableTree) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("Variable Name: " + variableTree.getName().toString());
            Tree type = variableTree.getType();
            Kind variableTypeKind = type.getKind();
            switch (variableTypeKind) {
                case IDENTIFIER:
                    IdentifierTree identifierTree = (IdentifierTree) type;
                    LOGGER.finest("Identifier Type: " + identifierTree.getName().toString());
                    break;
                case MEMBER_SELECT:
                    MemberSelectTree memberSelectTree = (MemberSelectTree) type;
                    LOGGER.finest("Member Select Expression: " + memberSelectTree.getExpression().toString());
                    LOGGER.finest("Member Select Name: " + memberSelectTree.getIdentifier().toString());
                    break;
                case ARRAY_TYPE:
                    ArrayTypeTree arrayTypeTree = (ArrayTypeTree)type;
                    LOGGER.finest("Array Type(" + arrayTypeTree.getType().getKind() + "): " + arrayTypeTree.getType().toString());
                    ;
                case PARAMETERIZED_TYPE:
                    ;
                default:
                    LOGGER.finest("Type Tree (" + variableTypeKind + "): " + type.toString());
            }
        }
    }

}
