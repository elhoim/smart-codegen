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
package com.smartitengineering.xml2props.xml.parser;

import com.smartitengineering.xml2props.maven.MojoReferenceHolder;
import com.smartitengineering.xml2props.util.LogUtil;
import com.smartitengineering.xml2props.util.XmlXomUtil;
import nu.xom.*;
import org.apache.maven.plugin.AbstractMojo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * @author <a href="mailto:shams.mahmood@gmail.com">Shams Mahmood</a>
 * @created Mar 30, 2008 3:34:34 PM
 */

public final class XmlXomInputParser implements InputParser,
    MojoReferenceHolder {
  
  public static final String XML_NAME_ENTRIES = "entries";

  public static final String XML_NAME_ENTRY = "entry";

  public static final String XML_NAME_KEY = "key";

  public static final String XML_NAME_LANG = "lang";

  public static final String XML_NAME_COUNTRY = "country";

  public static final String XML_NAME_VALUE = "value";

  AbstractMojo mMojo;

  public XmlXomInputParser() {
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
   * @throws IOException
   * @throws ParsingException
   * @throws
   * @see com.smartitengineering.xml2props.xml.parser.InputParser#parseInput(java.io.File)
   */
  public Map<Locale, Properties> parseInput(final File pInputFile)
      throws IOException {
    final Builder eBuilder = new Builder();
    Document eParsedDocument = null;
    try {
      LogUtil.debug(this, "Parsing file: " + pInputFile);
      eParsedDocument = eBuilder.build(pInputFile);
    }
    catch (ValidityException ex) {
      LogUtil.error(this, ex.getMessage());
      throw new IOException(ex.getMessage());
    }
    catch (ParsingException ex) {
      LogUtil.error(this, ex.getMessage());
      throw new IOException(ex.getMessage());
    }
    
    // process the nodes once document has been parsed successfully
    final Element eRootElement = eParsedDocument.getRootElement();
    if (XmlXomUtil.isNodeName(eRootElement, XML_NAME_ENTRIES)) {
      return processRootNode(eRootElement);
    }
    else {
      LogUtil.debug(this, "Expected root node not found");
      throw new IOException("Expected root node: " + XML_NAME_ENTRIES
          + ", found: " + eRootElement.getLocalName());
    }
  }

  private Map<Locale, Properties> processRootNode(final Node pCurrentNode) {
    LogUtil.debug(this, "Processing root node");
    if (pCurrentNode instanceof Element) {
      final Element eElement = (Element) pCurrentNode;
      if (XmlXomUtil.isNodeName(eElement, XML_NAME_ENTRIES)) {
        LogUtil.debug(this, "Found expected root node: "
            + XML_NAME_ENTRIES);
        final Map<Locale, Properties> eLocaleProperties =
            new HashMap<Locale, Properties>();

        // Extract child nodes and process them
        for (int i = 0; i < eElement.getChildCount(); i++) {
          final Node eChildNode = eElement.getChild(i);
          processEntryNodes(eChildNode, eLocaleProperties);
        }

        return eLocaleProperties;
      }
    }
    return null;
  }

  private void processEntryNodes(
      final Node pCurrentNode,
      final Map<Locale, Properties> pLocaleProperties) {
    if (pCurrentNode instanceof Element) {
      final Element eElement = (Element) pCurrentNode;
      if (XmlXomUtil.isNodeName(eElement, XML_NAME_ENTRY)) {
        final String eKeyValue = XmlXomUtil
            .getValueFromAttributeOrChildNode(eElement,
                XML_NAME_KEY);

        for (int i = 0; i < eElement.getChildCount(); i++) {
          final Node eChildNode = eElement.getChild(i);
          if (eChildNode instanceof Element) {
            final Element eChildElement = (Element) eChildNode;

            // if the attribute name matches, retrieve the value of
            // this Element and insert it into the
            // Locale-Properties Map
            if (XmlXomUtil
                .isNodeName(eChildElement, XML_NAME_VALUE)) {
              final Locale eLocale =
                  getLocaleFromElementLangValue(eChildElement);

              final String eValue = XmlXomUtil
                  .getValueFromAttributeOrElement(eChildElement);

              // save the entry in the Locale-Properties Map

              final Properties eProperties =
                  getNonNullPropertiesForLocaleFromMap(
                      pLocaleProperties, eLocale);

              eProperties.put(eKeyValue, eValue);

            }
          }
        }
      }
    }
  }

  private Properties getNonNullPropertiesForLocaleFromMap(
      final Map<Locale, Properties> pLocaleProperties,
      final Locale pLangLocale) {
    Properties eProperties = pLocaleProperties.get(pLangLocale);
    if (eProperties == null) {
      eProperties = new Properties();
      pLocaleProperties.put(pLangLocale, eProperties);
    }
    return eProperties;
  }

  protected Locale getLocaleFromElementLangValue(final Element pChildElement) {
    final String eLangValue = XmlXomUtil.getValueFromAttributeOrChildNode(
        pChildElement, XML_NAME_LANG);

    final String eCountryValue = XmlXomUtil
        .getValueFromAttributeOrChildNode(pChildElement,
            XML_NAME_COUNTRY);

    final Locale eLangLocale;
    if (eLangValue == null) {
      if (eCountryValue == null) {
        eLangLocale = null;
      }
      else {
        eLangLocale = new Locale("", eCountryValue.trim());
      }
    }
    else {
      if (eCountryValue == null) {
        eLangLocale = new Locale(eLangValue.trim());
      }
      else {
        eLangLocale = new Locale(eLangValue.trim(), eCountryValue
            .trim());
      }
    }
    return eLangLocale;
  }

}
