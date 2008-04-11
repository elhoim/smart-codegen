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

import com.smartitengineering.xml2props.writer.PropertiesWriter;
import com.smartitengineering.xml2props.xml.parser.InputParser;
import org.apache.maven.plugin.AbstractMojo;

import java.io.File;

/**
 * Abstract mojo class to hold the configurations that can be passed to this
 * MOJO. The implementing class will override the {@link AbstractMojo#execute()}
 * method.
 *
 * @author <a href="mailto:shams.mahmood@gmail.com">Shams Mahmood</a>
 * @created Apr 5, 2008 9:58:21 AM
 */

public abstract class AbstractXmlToPropertiesMojo extends AbstractMojo {

  /**
   * Location of the output properties files.
   *
   * @parameter property="outputDirectory"
   *            expression="${project.build.directory}"
   * @required
   */
  protected File mOutputDirectory;

  /**
   * Location of the input xml file.
   *
   * @parameter property="inputFile"
   * @required
   */
  protected File mInputXmlFile;

  /**
   * Name of the output resource bundle
   *
   * @parameter property="bundleName"
   */
  private String mBundleName;

  /**
   * Name of the output resource bundle
   *
   * @parameter property="outputMode"
   *            expression="PROPERTIES"
   */
  protected String mOutputMode;
  protected PropertiesWriter.PropertiesMode mOutputPropertiesMode;

  /**
   * The Input Xml parser
   *
   * @component
   */
  protected InputParser mInputParser;

  /**
   * The Properties File Writer
   *
   * @component
   */
  protected PropertiesWriter mPropertiesWriter;

  /**
   *
   */
  public AbstractXmlToPropertiesMojo() {
    super();
  }

  /**
   * @return the outputDirectory
   */
  public File getOutputDirectory() {
    return mOutputDirectory;
  }

  /**
   * @param pOutputDirectory the outputDirectory to set
   */
  public void setOutputDirectory(File pOutputDirectory) {
    mOutputDirectory = pOutputDirectory;
  }

  /** @return the inputXmlFile */
  public File getInputFile() {
    return mInputXmlFile;
  }

  /**
   * @param pInputXmlFile the inputXmlFile to set
   */
  public void setInputFile(File pInputXmlFile) {
    mInputXmlFile = pInputXmlFile;
  }

  /**
   * @return the bundleName
   */
  public String getBundleName() {
    return mBundleName;
  }

  /**
   * @param pBundleName the bundleName to set
   */
  public void setBundleName(String pBundleName) {
    mBundleName = pBundleName;
  }

  /**
   * @return the outputMode
   */
  public String getOutputMode() {
    return mOutputMode;
  }

  /**
   * @param pOutputMode the outputMode to set
   */
  public void setOutputMode(String pOutputMode) {
    mOutputMode = pOutputMode;
  }

  /**
   * @return the inputParser
   */
  public InputParser getInputParser() {
    return mInputParser;
  }

  /**
   * @param pInputParser the inputParser to set
   */
  public void setInputParser(InputParser pInputParser) {
    mInputParser = pInputParser;
  }

  /**
   * @return the propertiesWriter
   */
  public PropertiesWriter getPropertiesWriter() {
    return mPropertiesWriter;
  }

  /**
   * @param pPropertiesWriter the propertiesWriter to set
   */
  public void setPropertiesWriter(PropertiesWriter pPropertiesWriter) {
    mPropertiesWriter = pPropertiesWriter;
  }

}