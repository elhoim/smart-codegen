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

    public static final String IMPORT_JAVA_UTIL_LOGGER_QUALIFIER = "java\\.util\\.logging\\.(\\*|Logger)";
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
                        LoggerGenerationFactory.addLogger(workingCopy, true);
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

