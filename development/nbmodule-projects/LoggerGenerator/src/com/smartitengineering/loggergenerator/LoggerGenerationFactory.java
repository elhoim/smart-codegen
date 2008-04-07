/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartitengineering.loggergenerator;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.WhileLoopTree;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.java.source.WorkingCopy;

/**
 *
 * @author imyousuf
 */
public class LoggerGenerationFactory {

    private static final Logger LOGGER = Logger.getLogger(LoggerGenerator.class.getName());
    

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
                handleStatement(null, statementTree);
            }
            LOGGER.finest("-------------------Block End-------------------");
        }
    }

    public static void handleStatement(String source, StatementTree statementTree) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            Kind statementKind = statementTree.getKind();
            LOGGER.finest("Kind of " + (source != null ? source : "Statement") + ": " + statementKind);
            switch (statementKind) {
                case IF:
                    LOGGER.finest("If block");
                    IfTree ifTree = (IfTree) statementTree;
                    LOGGER.finest("Condition: " + ifTree.getCondition());
                    handleStatement("If - Then body", ifTree.getThenStatement());
                    handleStatement("If - Else body", ifTree.getElseStatement());
                    break;
                case FOR_LOOP:
                    LOGGER.finest("For Loop");
                    ForLoopTree forLoopTree = (ForLoopTree) statementTree;
                    LOGGER.finest("Initializer" + forLoopTree.getInitializer());
                    LOGGER.finest("Condition: " + forLoopTree.getCondition());
                    LOGGER.finest("Update: " + forLoopTree.getUpdate());
                    handleStatement("For Loop body", forLoopTree.getStatement());
                    break;
                case ENHANCED_FOR_LOOP:
                    LOGGER.finest("Enhanced For Loop");
                    EnhancedForLoopTree enhancedForLoopTree = (EnhancedForLoopTree) statementTree;
                    LOGGER.finest("Expression: " + enhancedForLoopTree.getExpression());
                    LOGGER.finest("Variable: " + enhancedForLoopTree.getVariable());
                    handleStatement("Enhanced For Loop body", enhancedForLoopTree.getStatement());
                    break;
                case WHILE_LOOP:
                    LOGGER.finest("While Loop");
                    WhileLoopTree whileTree = (WhileLoopTree) statementTree;
                    LOGGER.finest("Condition: " + whileTree.getCondition());
                    handleStatement("While Loop body", whileTree.getStatement());
                    break;
                case DO_WHILE_LOOP:
                    LOGGER.finest("Do-While Loop");
                    DoWhileLoopTree doWhileLoopTree = (DoWhileLoopTree) statementTree;
                    LOGGER.finest("Condition: " + doWhileLoopTree.getCondition());
                    handleStatement("Do-While Loop body", doWhileLoopTree.getStatement());
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
                            handleStatement("Case " + caseExpression, caseStatement);
                        }
                    }
                    break;
                case BLOCK:
                    logBlockTree((BlockTree) statementTree);
                    break;
                default:
                case EXPRESSION_STATEMENT:
                    LOGGER.finest(statementTree.toString());
            }
        }
    }
}
