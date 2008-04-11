/*
    This Module is a Maven plugin to convert an xml to a java resource bundle
    Copyright (C) 2008  Shams Mahmood

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package com.smartitengineering.xml2props.util;

import com.smartitengineering.xml2props.maven.MojoReferenceHolder;

/**
 * @author <a href="mailto:shams.mahmood@gmail.com">Shams Mahmood</a>
 * @created Apr 5, 2008 10:15:33 AM
 */

public final class LogUtil {
  private LogUtil() {
    super();
  }

  public static void debug(final MojoReferenceHolder pReferenceHolder,
                           final String pDebugMessage) {
    if (pReferenceHolder == null || pReferenceHolder.getMojo() == null
        || pReferenceHolder.getMojo().getLog() == null) {
      System.out.println(" [debug]: " + pDebugMessage);
      return;
    }
    pReferenceHolder.getMojo().getLog().debug(pDebugMessage);
  }

  public static void info(final MojoReferenceHolder pReferenceHolder,
                          final String pInfoMessage) {
    if (pReferenceHolder == null || pReferenceHolder.getMojo() == null
        || pReferenceHolder.getMojo().getLog() == null) {
      System.out.println("  [info]: " + pInfoMessage);
      return;
    }
    pReferenceHolder.getMojo().getLog().info(pInfoMessage);
  }

  public static void warn(final MojoReferenceHolder pReferenceHolder,
                          final String pWarnMessage) {
    if (pReferenceHolder == null || pReferenceHolder.getMojo() == null
        || pReferenceHolder.getMojo().getLog() == null) {
      System.out.println("  [warn]: " + pWarnMessage);
      return;
    }
    pReferenceHolder.getMojo().getLog().warn(pWarnMessage);
  }

  public static void error(final MojoReferenceHolder pReferenceHolder,
                           final String pErrorMessage) {
    if (pReferenceHolder == null || pReferenceHolder.getMojo() == null
        || pReferenceHolder.getMojo().getLog() == null) {
      System.out.println(" [error]: " + pErrorMessage);
      return;
    }
    pReferenceHolder.getMojo().getLog().error(pErrorMessage);
  }

}
