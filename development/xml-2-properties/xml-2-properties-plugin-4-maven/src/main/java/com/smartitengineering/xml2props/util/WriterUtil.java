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

import com.smartitengineering.xml2props.writer.PropertiesWriter.PropertiesMode;

import java.util.Locale;

/**
 * @author <a href="mailto:shams.mahmood@gmail.com">Shams Mahmood</a>
 * @created Apr 5, 2008 1:00:52 PM
 */

public final class WriterUtil {
  
  private static final String UNDERSCORE = "_";

  private WriterUtil() {
    super();
  }

  public static String getLocalizedFileName(
      final Locale pLocale,
      final String pFileName,
      final PropertiesMode pMode) {
    final String eLocaleString;
    if (pLocale != null) {
      String eLocaleAsString = pLocale.toString();
      if (!eLocaleAsString.startsWith(UNDERSCORE)) {
        eLocaleAsString = UNDERSCORE + eLocaleAsString;
      }
      eLocaleString = eLocaleAsString;
    }
    else {
      eLocaleString = "";
    }

    return pFileName + eLocaleString + "." + pMode.toString().toLowerCase();
  }

}
