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
package com.smartitengineering.javasourcetreeparser.test;

import com.smartitengineering.javasourcetreeparser.JavaSourceTreeParser;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * A Test based on NbTestCase. It is a NetBeans extension to JUnit TestCase
 * which among othres allows to compare files via assertFile methods, create
 * working directories for testcases, write to log files, compare log files
 * against reference (golden) files, etc.
 * 
 * More details here http://xtest.netbeans.org/NbJUnit/NbJUnit-overview.html.
 * 
 * @author imyousuf
 */
public class JavaSourceTreeParserTest
        extends NbTestCase {

  /** Default constructor.
   * @param testName name of particular test case
   */
  public JavaSourceTreeParserTest(String testName) {
    super(testName);
  }

  /** Creates suite from particular test cases. You can define order of testcases here. */
  public static NbTestSuite suite() {
    NbTestSuite suite = new NbTestSuite();
    suite.addTest(new JavaSourceTreeParserTest("test1"));
    return suite;
  }

  /* Method allowing test execution directly from the IDE. */
  public static void main(java.lang.String[] args) {
    // run whole suite
    junit.textui.TestRunner.run(suite());
  // run only selected test case
  //junit.textui.TestRunner.run(new JavaSourceTreeParserTest("test1"));
  }

  /** Called before every test case. */
  public void setUp() {
    System.out.println("########  " + getName() + "  #######");
  }

  /** Called after every test case. */
  public void tearDown() {
  }

  // Add test methods here, they have to start with 'test'.
  /** Test case 1. */
  public void test1() {
    try {
      File testJavaFile =
              new File("/home/imyousuf/projects/smart-codegen/trunk/" +
                       "development/nbmodule-projects/JavaSourceTreeParser/test/unit/" +
                       "src/com/smartitengineering/javasourcetreeparser/test/resources/" +
                       "Main_1_1.java");
      Logger.getLogger(JavaSourceTreeParserTest.class.getName()).
              log(Level.INFO, testJavaFile.getAbsolutePath());
      /**
       * ----------------------------------------------------------------
       */
      FileObject fileObject = FileUtil.toFileObject(testJavaFile);
      JavaSource eSource =
              JavaSource.forFileObject(fileObject);
      Logger.getLogger(JavaSourceTreeParserTest.class.getName()).
              log(Level.INFO, (eSource == null ? "NULL!" : eSource.toString()));
      if (eSource == null) {
      }
      else {
        CancellableTask<WorkingCopy> cancellableTask = new CancellableTask<WorkingCopy>() {

          public void cancel() {
          }

          public void run(WorkingCopy workingCopy) throws Exception {
            workingCopy.toPhase(Phase.PARSED);
            TreeMaker make = workingCopy.getTreeMaker();
            CompilationUnitTree compilationUnitTree =
                    workingCopy.getCompilationUnit();
            for (Tree typeDecl : compilationUnitTree.getTypeDecls()) {
              if (Tree.Kind.CLASS == typeDecl.getKind()) {
                ClassTree clazz = (ClassTree) typeDecl;
                JavaSourceTreeParser treeParser = new JavaSourceTreeParser();
                treeParser.logDebugInfoOfWorkingCopy(clazz, workingCopy);
              }
            }
          }
        };
        ModificationResult modificationResult =
                eSource.runModificationTask(cancellableTask);
        List<? extends Difference> differences =
                modificationResult.getDifferences(fileObject);
        Logger.getLogger(JavaSourceTreeParserTest.class.getName()).
                log(Level.INFO,
                    differences != null ? differences.toString() : "NONE!");
      }
    }
    catch (Exception ex) {
      Logger.getLogger(JavaSourceTreeParserTest.class.getName()).
              log(Level.SEVERE, null, ex);
      TestCase.fail();
    }
    finally {
    }
  }

}