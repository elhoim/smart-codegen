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
package com.escenic.maven.plugin.writer;

import com.smartitengineering.xml2props.util.LogUtil;
import com.smartitengineering.xml2props.util.WriterUtil;
import com.smartitengineering.xml2props.writer.PropertiesWriter;
import com.smartitengineering.xml2props.writer.PropertiesWriter.PropertiesMode;
import com.smartitengineering.xml2props.writer.PropertiesWriterImpl;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author <a href="mailto:shams.mahmood@gmail.com">Shams Mahmood</a>
 * @created Apr 5, 2008 12:46:16 PM
 */

public class PropertiesWriterImplTest
    extends TestCase {
  private Map<Locale, Properties> mLocaleProperties;

  private PropertiesWriter mPropertiesWriter;

  private String mBundleName;

  private File mOutputDirectory;

  /** @see junit.framework.TestCase#setUp() */
  protected void setUp()
      throws Exception {
    super.setUp();

    if (mPropertiesWriter == null) {
      mPropertiesWriter = new PropertiesWriterImpl();
      LogUtil.debug(null, "Using Writer: " + mPropertiesWriter);
    }
    if (mBundleName == null) {
      mBundleName = "TestProperties";
    }
    if (mOutputDirectory == null) {
      mOutputDirectory = new File("target/test-classes/output");
      mOutputDirectory.mkdirs();
    }
    if (mLocaleProperties == null) {
      mLocaleProperties = new HashMap<Locale, Properties> ();

      {
        Properties eDefaultProperties = new Properties();
        eDefaultProperties.put("key-1-df", "value-1-df");
        eDefaultProperties.put("key-2-df", "value-2-df");
        eDefaultProperties.put("key-3-df", "value-3-df");
        mLocaleProperties.put(null, eDefaultProperties);
      }
      {
        Properties eBnBdProperties = new Properties();
        eBnBdProperties.put("key-1-bn-BD", "value-1-bn-BD");
        eBnBdProperties.put("key-2-bn-BD", "value-2-bn-BD");
        eBnBdProperties.put("key-3-bn-BD", "value-3-bn-BD");
        mLocaleProperties.put(new Locale("bn", "BD"), eBnBdProperties);
      }
      {
        Properties eBnProperties = new Properties();
        eBnProperties.put("key-1-bn", "value-1-bn");
        eBnProperties.put("key-2-bn", "value-2-bn");
        eBnProperties.put("key-3-bn", "value-3-bn");
        mLocaleProperties.put(new Locale("bn"), eBnProperties);
      }
      {
        Properties eBdProperties = new Properties();
        eBdProperties.put("key-1-BD", "value-1-BD");
        eBdProperties.put("key-2-BD", "value-2-BD");
        eBdProperties.put("key-3-BD", "value-3-BD");
        mLocaleProperties.put(new Locale("", "BD"), eBdProperties);
      }

    }
  }

  public void testWriteProperties()
      throws IOException {
    LogUtil.debug(null, "PropertiesWriterImplTest.testWriteProperties() starts...");

    writeProperties(PropertiesMode.PROPERTIES);

    writeProperties(PropertiesMode.XML);

    LogUtil.debug(null, "PropertiesWriterImplTest.testWriteProperties() ends.");
  }

  private void writeProperties(final PropertiesMode pPropertiesMode)
      throws IOException {
    LogUtil.debug(null, "Writing properties in mode: " + pPropertiesMode);

    for (Iterator i = mLocaleProperties.keySet().iterator(); i.hasNext();) {
      final Locale eLoopLocale = (Locale) i.next();

      final Properties eLoopProperties = (Properties) mLocaleProperties.get(eLoopLocale);
      LogUtil.debug(null, "Writing properties for locale: " + eLoopLocale);
      mPropertiesWriter.writePropertiesToFile(eLoopLocale, eLoopProperties, pPropertiesMode, mOutputDirectory,
          mBundleName);

      final String eFileNameForLocalizedProperties =
          WriterUtil.getLocalizedFileName(eLoopLocale, mBundleName, pPropertiesMode);
      final File ePropertiesFile = new File(mOutputDirectory, eFileNameForLocalizedProperties);

      LogUtil.debug(null, "Checking if file exists: " + ePropertiesFile);
      assertTrue(ePropertiesFile.exists());
    }
  }

}
