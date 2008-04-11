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
package com.smartitengineering.xml2props.writer;

import com.smartitengineering.xml2props.maven.MojoReferenceHolder;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

/**
 * @author <a href="mailto:shams.mahmood@gmail.com">Shams Mahmood</a>
 * @created Mar 30, 2008 4:21:21 PM
 */

public interface PropertiesWriter extends MojoReferenceHolder {

  /**
   * Class to hold all the allowable property modes
   * @author <a href="mailto:shams.mahmood@gmail.com">Shams Mahmood</a>
   * @created Mar 30, 2008 4:21:21 PM
   */
  class PropertiesMode {

    private static final String STRING_PROPERTIES = "PROPERTIES";
    private static final String STRING_XML = "XML";
        
    public static final PropertiesMode PROPERTIES =
        new PropertiesMode(STRING_PROPERTIES);

    public static final PropertiesMode XML =
        new PropertiesMode(STRING_XML);

    final String mRepresentation;

    public PropertiesMode(final String pRepresentation) {
      if (pRepresentation == null) {
        throw new IllegalArgumentException(
            "representation string may not be null");
      }
      if (!pRepresentation.equalsIgnoreCase(STRING_PROPERTIES)
          && !pRepresentation.equalsIgnoreCase(STRING_XML)) {
        throw new IllegalArgumentException(
            "representation string must be one of " + PROPERTIES
                + " or " + XML + ", but found: "
                + pRepresentation);
      }
      mRepresentation = pRepresentation;
    }

    /** @see java.lang.Object#toString() */
    public String toString() {
      return mRepresentation;
    }

    /** @see java.lang.Object#hashCode() */
    public int hashCode() {
      return mRepresentation.hashCode();
    }

    /** @see java.lang.Object#equals(java.lang.Object) */
    public boolean equals(Object pOther) {
      if (pOther instanceof PropertiesMode) {
        return this.mRepresentation
            .equalsIgnoreCase(((PropertiesMode) pOther).mRepresentation);
      }
      return false;
    }
  }

  public void writePropertiesToFile(
      Locale pLocale,
      Properties pProperties,
      PropertiesMode pMode,
      File pFileDirectory,
      String pFileNameWithoutExtension)
      throws IOException;

}
