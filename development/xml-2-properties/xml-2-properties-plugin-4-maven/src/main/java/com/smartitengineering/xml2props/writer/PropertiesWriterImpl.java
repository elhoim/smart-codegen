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

import com.smartitengineering.xml2props.util.LogUtil;
import com.smartitengineering.xml2props.util.WriterUtil;
import org.apache.maven.plugin.AbstractMojo;

import java.io.*;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

/**
 * @author <a href="mailto:shams.mahmood@gmail.com">Shams Mahmood</a>
 * @created Mar 30, 2008 4:23:48 PM
 */

public class PropertiesWriterImpl implements PropertiesWriter {
  AbstractMojo mMojo;

  /**
   *
   */
  private static final String PROPERTIES_COMMENT =
      "Auto-generated properties from maven-xml-to-properties plugin";

  public PropertiesWriterImpl() {
    super();
  }

  /** @return the mojo */
  public AbstractMojo getMojo() {
    return mMojo;
  }

  /** @param pMojo the mojo to set */
  public void setMojo(AbstractMojo pMojo) {
    mMojo = pMojo;
  }

  /**
   * @see com.smartitengineering.xml2props.writer.PropertiesWriter#writePropertiesToFile(Locale, Properties, com.smartitengineering.xml2props.writer.PropertiesWriter.PropertiesMode,  File,String)
   */
  public void writePropertiesToFile(
      final Locale pLocale,
      final Properties pProperties,
      final PropertiesMode pMode,
      final File pFileDirectory,
      final String pFileNameWithoutExtension)
      throws IOException {
    if (pProperties == null) {
      throw new IllegalArgumentException(
          "Properties was null, expected non-null value");
    }
    if (pMode == null) {
      throw new IllegalArgumentException(
          "Properties Mode was null, expected non-null value");
    }

    final String eFileNameForLocalizedProperties = WriterUtil
        .getLocalizedFileName(pLocale, pFileNameWithoutExtension, pMode);
    final File eFileForLocalizedProperties = new File(pFileDirectory,
        eFileNameForLocalizedProperties);

    final Properties eNewProperties;
    if (eFileForLocalizedProperties.exists()) {
      eNewProperties = mergePropertiesInFile(pProperties, pMode,
          eFileForLocalizedProperties);
    }
    else {
      eNewProperties = pProperties;
    }

    OutputStream eOutputStream = null;
    try {
      final File eOutputFileForLocalizedProperties = new File(
          pFileDirectory, eFileNameForLocalizedProperties);
      eOutputStream = new FileOutputStream(
          eOutputFileForLocalizedProperties);
      LogUtil.debug(this, "Writing properties: " + eNewProperties);
      if (PropertiesMode.PROPERTIES.equals(pMode)) {
        eNewProperties.store(eOutputStream, PROPERTIES_COMMENT);
      }
      else if (PropertiesMode.XML.equals(pMode)) {
        eNewProperties.storeToXML(eOutputStream, PROPERTIES_COMMENT);
      }
    }
    finally {
      if (eOutputStream != null) {
        eOutputStream.close();
      }
    }

  }

  /**
   * @param pProperties
   * @param pFileForLocalizedProperties
   *
   * @return
   *
   * @throws IOException
   */
  private Properties mergePropertiesInFile(
      final Properties pProperties,
      final PropertiesMode pMode,
      final File pFileForLocalizedProperties)
      throws IOException {
    final Properties eResultProperties = new Properties();

    LogUtil.debug(this, "Loading properties from existing file: "
        + pFileForLocalizedProperties);
    InputStream eInputStream = null;
    try {
      eInputStream = new FileInputStream(pFileForLocalizedProperties);
      if (PropertiesMode.PROPERTIES.equals(pMode)) {
        eResultProperties.load(eInputStream);
      }
      else if (PropertiesMode.XML.equals(pMode)) {
        eResultProperties.loadFromXML(eInputStream);
      }
      LogUtil.debug(this,
          "Successfully loaded properties from existing file: "
              + eResultProperties);

      mergeProperties(pProperties, eResultProperties);

      return eResultProperties;
    }
    finally {
      if (eInputStream != null) {
        eInputStream.close();
      }
    }
  }

  /**
   * @param pSourceProperties
   * @param pDestinationProperties
   */
  private void mergeProperties(Properties pSourceProperties,
                               Properties pDestinationProperties) {
    LogUtil.debug(this, "Merging properties files");

    final Set<Object> ePropertiesKeySet = pSourceProperties.keySet();
    for (Iterator i = ePropertiesKeySet.iterator(); i.hasNext();) {
      Object eLoopKey = i.next();

      if (pDestinationProperties.containsKey(eLoopKey)) {
        LogUtil.debug(this, "Overwriting property with key: "
            + eLoopKey);
      }
      final Object eSourcePropertyValue = pSourceProperties.get(eLoopKey);
      pDestinationProperties.put(eLoopKey, eSourcePropertyValue);
    }
    LogUtil.debug(this, "Done Merging properties files");
  }

}
