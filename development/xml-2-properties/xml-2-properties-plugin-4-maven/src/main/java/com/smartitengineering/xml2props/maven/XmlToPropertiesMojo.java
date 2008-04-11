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
package com.smartitengineering.xml2props.maven;

import com.smartitengineering.xml2props.exception.ServiceException;
import com.smartitengineering.xml2props.exception.ServiceValidator;
import com.smartitengineering.xml2props.writer.PropertiesWriter.PropertiesMode;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Goal processes an input xml file to produce a properties file.
 *
 * @goal xml2property
 * @phase process-sources
 */
public class XmlToPropertiesMojo extends AbstractXmlToPropertiesMojo {
  public void execute() throws MojoExecutionException {
    getLog().info("Executing XmlToPropertiesMojo");
    try {
      validateConfigurationParameters();

      prepareParameters();

      executeRequest();
    }
    catch (ServiceException ex) {
      throw new MojoExecutionException(ex.getMessage(), ex);
    }
    catch (IOException ex) {
      throw new MojoExecutionException(ex.getMessage(), ex);
    }
  }

  /** @throws ServiceException  */
  private void validateConfigurationParameters() throws ServiceException {
    ServiceValidator.validateNotNull(mInputXmlFile,
        "The input xml must be specified");
    ServiceValidator.validateNotNull(mOutputDirectory,
        "The output directory must be specified");
    if (!mOutputDirectory.exists()) {
      mOutputDirectory.mkdirs();
    }
    ServiceValidator.validateNotNull(mOutputMode,
        "The output mode for properties must be specified");
  }

  private void prepareParameters() {
    getInputParser().setMojo(this);
    getPropertiesWriter().setMojo(this);
    mOutputPropertiesMode = new PropertiesMode(mOutputMode);

    if (getBundleName() == null || getBundleName().length() == 0) {
      getLog().debug("Output Bundle Name is empty");
      String eInputXmlFileName = getInputFile().getName();
      final int eLastIndexOfDot = eInputXmlFileName.lastIndexOf('.');
      if (eLastIndexOfDot > 0) {
        eInputXmlFileName = eInputXmlFileName.substring(0,
            eLastIndexOfDot);
      }
      getLog().info(
          "Setting bundle name to input xml file name: "
              + eInputXmlFileName);
      setBundleName(eInputXmlFileName);
    }
  }

  private void executeRequest() throws IOException {
    getLog().info("Parsing input xml file");
    final Map<Locale, Properties> eParsedLocaleProperties = getInputParser()
        .parseInput(getInputFile());
    getLog().debug("Writing parsed properties");
    for (Iterator i = eParsedLocaleProperties.keySet().iterator(); i
        .hasNext();) {
      final Locale eLoopLocale = (Locale) i.next();

      Properties eLocaleProperties = (Properties) eParsedLocaleProperties
          .get(eLoopLocale);
      getLog().info(
          "Writing parsed properties for Locale: " + eLoopLocale);
      getPropertiesWriter().writePropertiesToFile(eLoopLocale,
          eLocaleProperties, mOutputPropertiesMode,
          getOutputDirectory(), getBundleName());
    }
  }
}
