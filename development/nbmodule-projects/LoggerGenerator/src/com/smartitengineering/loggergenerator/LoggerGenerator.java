/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartitengineering.loggergenerator;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TypeParameterTree;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public final class LoggerGenerator extends CookieAction {

    private static final String IMPORT_JAVA_UTIL_LOGGER_QUALIFIER = "java\\.util\\.logging\\.(\\*|Logger)";
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

    protected void performAction(Node[] activatedNodes) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("Activated Nodes: " + activatedNodes);
        }
        for (Node activatedNode : activatedNodes) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(activatedNode.getDisplayName());
            }
            FileObject fileObject = activatedNode.getLookup().lookup(FileObject.class);
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer(fileObject.getPath());
            }
            JavaSource eSource = JavaSource.forFileObject(fileObject);
            if (eSource == null) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("eSource is null");
                }
            } else {
                CancellableTask<WorkingCopy> cancellableTask = new CancellableTask<WorkingCopy>() {

                    public void cancel() {
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.finer("Cancelled generating toString");
                        }
                    }

                    public void run(WorkingCopy workingCopy) throws Exception {
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.finer("Received Working Copy: " + workingCopy);
                        }
                        workingCopy.toPhase(Phase.RESOLVED);
                        TreeMaker make = workingCopy.getTreeMaker();
                        CompilationUnitTree compilationUnitTree = workingCopy.getCompilationUnit();
                        for (Tree typeDecl : compilationUnitTree.getTypeDecls()) {
                            if (Tree.Kind.CLASS == typeDecl.getKind()) {
                                ClassTree clazz = (ClassTree) typeDecl;
                                logDebugInfoOfWorkingCopy(clazz, workingCopy);
                            }
                        }

                    }
                };
                try {
                    ModificationResult modificationResult = eSource.runModificationTask(cancellableTask);
                    modificationResult.commit();
                } catch (Exception ex) {
                    LOGGER.warning(ex.getMessage());
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    private void logDebugInfoOfWorkingCopy(ClassTree clazz, WorkingCopy workingCopy) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            List<? extends Tree> members = clazz.getMembers();
            List<? extends ImportTree> imports = workingCopy.getCompilationUnit().getImports();
            List<? extends TypeParameterTree> types = clazz.getTypeParameters();
            for (TypeParameterTree paramType : types) {
                LOGGER.finest("Type Name: " + paramType.getName());
            }
            for (ImportTree importTree : imports) {
                LOGGER.finest("Import Q-Id: " + importTree.getQualifiedIdentifier().getKind().name());
                LOGGER.finest("Import Tree: " + importTree.toString() + " Length: " + importTree.toString().length() + '\n');
                MemberSelectTree memberSelectTree = (MemberSelectTree) importTree.getQualifiedIdentifier();
                LOGGER.finest("Member Selected ID: " + memberSelectTree.getIdentifier());
                LOGGER.finest("Member toString: " + memberSelectTree.toString());
                LOGGER.finest("Member Exp toString: " + memberSelectTree.getExpression().toString());
                LOGGER.finest("Util Import Pattern matches: " + Pattern.matches(IMPORT_JAVA_UTIL_LOGGER_QUALIFIER, memberSelectTree.toString()));
            }
            for (Tree member : members) {
                Kind memberKind = member.getKind();
                LOGGER.finest("Member Type: " + memberKind.name());
                if(memberKind.equals(Kind.METHOD)) {
                    
                }
                else if(memberKind.equals(Kind.VARIABLE)) {
                    
                }
                else if(memberKind.equals(Kind.BLOCK)) {
                    
                }
                else {
                    LOGGER.finest("Unrecognized block: " + memberKind);
                }
            }
        }
    }

    protected int mode() {
        return CookieAction.MODE_ALL;
    }

    public String getName() {
        return NbBundle.getMessage(LoggerGenerator.class, "CTL_LoggerGenerator");
    }

    protected Class[] cookieClasses() {
        return new Class[]{EditorCookie.class};
    }

    @Override
    protected String iconResource() {
        return "com/smartitengineering/loggergenerator/GenerateLogger.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}

