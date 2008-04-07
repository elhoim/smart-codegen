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
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import java.io.IOException;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author imyousuf
 */
public class LoggerGenerationFactory {

    private static final Logger LOGGER = Logger.getLogger(LoggerGenerationFactory.class.getName());
    

    static {
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

    public static void addLogger(WorkingCopy workingCopy, boolean ignoreExisting) throws IOException {
        workingCopy.toPhase(Phase.RESOLVED);
        TreeMaker make = workingCopy.getTreeMaker();
        CompilationUnitTree compilationUnitTree = workingCopy.getCompilationUnit();
        for (Tree typeDecl : compilationUnitTree.getTypeDecls()) {
            if (Tree.Kind.CLASS == typeDecl.getKind()) {
                ClassTree clazz = (ClassTree) typeDecl;
                LoggerGenerationFactory.logDebugInfoOfWorkingCopy(clazz, workingCopy);
                if (!ignoreExisting) {
                    checkWhetherLoggerExists(clazz, workingCopy);
                }
            }
        }
    }

    private static void checkWhetherLoggerExists(ClassTree clazz, WorkingCopy workingCopy) {
        
    }

    public static void logDebugInfoOfWorkingCopy(ClassTree clazz, WorkingCopy workingCopy) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            List<? extends Tree> members = clazz.getMembers();
            List<? extends ImportTree> imports = workingCopy.getCompilationUnit().getImports();
            List<? extends TypeParameterTree> types = clazz.getTypeParameters();
            for (TypeParameterTree paramType : types) {
                LOGGER.finest("Type Name: " + paramType.getName());
            }
            for (ImportTree importTree : imports) {
                LOGGER.finest("Import Q-Id: " + importTree.getQualifiedIdentifier().getKind().name());
                LOGGER.finest("Static Import?: " + importTree.isStatic());
                LOGGER.finest("Import Tree: " + importTree.toString() + " Length: " + importTree.toString().length() + '\n');
                MemberSelectTree memberSelectTree = (MemberSelectTree) importTree.getQualifiedIdentifier();
                LOGGER.finest("Member Selected ID: " + memberSelectTree.getIdentifier());
                LOGGER.finest("Member toString: " + memberSelectTree.toString());
                LOGGER.finest("Member Exp toString: " + memberSelectTree.getExpression().toString());
                LOGGER.finest("Util Import Pattern matches: " + Pattern.matches(LoggerGenerator.IMPORT_JAVA_UTIL_LOGGER_QUALIFIER, memberSelectTree.toString()));
            }
            for (Tree member : members) {
                Kind memberKind = member.getKind();
                LOGGER.finest("Member Type: " + memberKind.name());
                if (memberKind.equals(Kind.METHOD)) {
                    MethodTree methodTree = (MethodTree) member;
                    LOGGER.finest("Method Name: " + methodTree.getName().toString());
                    BlockTree blockTree = methodTree.getBody();
                    logBlockTree(blockTree);
                } else if (memberKind.equals(Kind.VARIABLE)) {
                    VariableTree variableTree = (VariableTree) member;
                    logVariableTree(variableTree);
                } else if (memberKind.equals(Kind.BLOCK)) {
                    logBlockTree((BlockTree) member);
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
                    ;
                case PARAMETERIZED_TYPE:
                    ;
                default:
                    LOGGER.finest("Type Tree (" + variableTypeKind + "): " + type.toString());
            }
        }
    }
}
