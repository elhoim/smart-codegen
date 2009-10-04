/**
 *    This NetBeans Module is responsible for generating toString() for
 *    Java Classes
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
package com.smartitengineering.tostringgenerator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author imyousuf
 */
public class ToStringCodeGenerator
				implements CodeGenerator {

		private static final Logger LOGGER = Logger.getLogger(
						ToStringCodeGenerator.class.getName());
		private JTextComponent activatedNode;

		private ToStringCodeGenerator(JTextComponent activatedNode) {
				this.activatedNode = activatedNode;
		}

		public String getDisplayName() {
				return NbBundle.getMessage(GenerateToString.class,
								"CTL_GenerateToString");
		}

		public void invoke() {
				JavaSource eSource = JavaSource.forDocument(activatedNode.getDocument());
				if (eSource == null) {
						if (LOGGER.isLoggable(Level.FINER)) {
								LOGGER.finer("eSource is null");
						}
				}
				else {
						CancellableTask<WorkingCopy> cancellableTask = new CancellableTask<WorkingCopy>() {

								public void cancel() {
										if (LOGGER.isLoggable(Level.FINER)) {
												LOGGER.finer("Cancelled generating toString");
										}
								}

								public void run(WorkingCopy workingCopy)
												throws Exception {
										ToStringGeneratorFactory.generateToString(workingCopy);
								}
						};
						try {
								ModificationResult modificationResult =
																	 eSource.runModificationTask(cancellableTask);
								modificationResult.commit();
						}
						catch (Exception ex) {
								LOGGER.warning(ex.getMessage());
								Exceptions.printStackTrace(ex);
						}
				}
		}

		public static class Factory
						implements CodeGenerator.Factory {

				public List<? extends CodeGenerator> create(Lookup context) {
						Collection<? extends JTextComponent> activatedNodes = context.
										lookupAll(
										JTextComponent.class);
						if (activatedNodes != null && !activatedNodes.isEmpty()) {
								List<CodeGenerator> generators = new ArrayList<CodeGenerator>();
								for (JTextComponent activatedNode : activatedNodes) {
										generators.add(new ToStringCodeGenerator(activatedNode));
								}
								return generators;
						}
						else {
								return Collections.emptyList();
						}
				}
		}
}
