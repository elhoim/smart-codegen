/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartitengineering.loggergenerator;

import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public final class LoggerGenerator extends CookieAction {

    protected void performAction(Node[] activatedNodes) {
        EditorCookie editorCookie = activatedNodes[0].getLookup().lookup(EditorCookie.class);
    // TODO use editorCookie
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

