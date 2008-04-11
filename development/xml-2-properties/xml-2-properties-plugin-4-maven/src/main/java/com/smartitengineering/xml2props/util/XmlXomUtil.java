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

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Node;

import java.util.Set;

/**
 * @author <a href="mailto:shams.mahmood@gmail.com">Shams Mahmood</a>
 * @creationDate Mar 30, 2008
 */
public final class XmlXomUtil {

  /**
   * @param pElement      The Element whose name to test
   * @param pElementNames The set of names to test against
   *
   * @return whether the Set contains the Element name
   */
  public static boolean isNodeNameInSet(
      final Element pElement,
      final Set<String> pElementNames) {
    if (pElement == null || pElementNames == null
        || pElementNames.isEmpty()) {
      return false;
    }
    return pElementNames.contains(pElement.getLocalName());
  }

  /**
   * @param pElement     The Element whose name to test
   * @param pElementName The name to test against
   *
   * @return whether the name of Element is equal to the specified name
   */
  public static boolean isNodeName(
      final Element pElement,
      final String pElementName) {
    if (pElement == null || pElementName == null) {
      return false;
    }
    return pElementName.equals(pElement.getLocalName());
  }

  /**
   * @param pElement The Element whose value to return
   *
   * @return The value of element or the value of the 'value' attribute in the
   *         Element
   */
  public static String getValueFromAttributeOrElement(final Element pElement) {
    if (pElement == null) {
      throw new IllegalArgumentException(
          "Input Element was null, expected non-null value");
    }

    String eResult = null;
    // First retrieve the value from the tag as text
    eResult = pElement.getValue();

    if (eResult == null || eResult.length() == 0) {
      // if not found, search for an attribute with name 'value'
      final String eValueAttributeName = "value";
      eResult = getValueFromAttribute(pElement, eValueAttributeName);
    }
    return eResult;
  }

  /**
   * @param pElement            The Element from whom value of an Attribute to
   *                            extract
   * @param pValueAttributeName The name of the Attribute whose value to extract
   *
   * @return The value of Attribute with the specified name in the Element
   */
  public static String getValueFromAttribute(
      final Element pElement,
      final String pValueAttributeName) {
    if (pElement == null) {
      throw new IllegalArgumentException(
          "Input Element was null, expected non-null value");
    }
    if (pValueAttributeName == null
        || pValueAttributeName.trim().length() == 0) {
      throw new IllegalArgumentException(
          "Input Attribute name was empty, expected non-null value");
    }

    String eAttributeValue = null;
    final Attribute eTheAttribute = pElement
        .getAttribute(pValueAttributeName);
    if (eTheAttribute != null) {
      eAttributeValue = eTheAttribute.getValue();
    }
    return eAttributeValue;
  }

  /**
   * @param pCurrentElement              The Element whose value to return
   * @param pAttributeOrChildElementName The name of attribute / child element
   *
   * @return The value of the specified attribute in the 'current' Element or
   *         the value of a child Element of the 'current' Element with the
   *         specified name.
   */
  public static String getValueFromAttributeOrChildNode(
      final Element pCurrentElement,
      final String pAttributeOrChildElementName) {
    if (pCurrentElement == null) {
      throw new IllegalArgumentException(
          "Input Element was null, expected non-null value");
    }
    if (pAttributeOrChildElementName == null
        || pAttributeOrChildElementName.trim().length() == 0) {
      throw new IllegalArgumentException(
          "Input name was empty, expected non-null value");
    }

    String eResult = null;
    // First retrieve the value from the attribute
    if (eResult == null) {
      // if search for an attribute with the specified name
      eResult = getValueFromAttribute(pCurrentElement,
          pAttributeOrChildElementName);
    }
    if (eResult == null) {
      String eChildNodeValue = getValueFromChildElement(pCurrentElement,
          pAttributeOrChildElementName);
      eResult = eChildNodeValue;
    }
    return eResult;
  }

  public static String getValueFromChildElement(
      final Element pCurrentElement,
      final String pChildElementName) {
    if (pCurrentElement == null) {
      throw new IllegalArgumentException(
          "Input Element was null, expected non-null value");
    }
    if (pChildElementName == null || pChildElementName.trim().length() == 0) {
      throw new IllegalArgumentException(
          "Input name was empty, expected non-null value");
    }

    String eChildNodeValue = null;
    // If value not found from the attribute, process child nodes
    for (int i = 0; i < pCurrentElement.getChildCount(); i++) {
      final Node eChildNode = pCurrentElement.getChild(i);
      if (eChildNode instanceof Element) {
        final Element eChildElement = (Element) eChildNode;
        // if the attribute name matches, retrieve the value of this
        // Element
        if (isNodeName(eChildElement, pChildElementName)) {
          eChildNodeValue = getValueFromAttributeOrElement(eChildElement);
          break;
        }
      }
    }
    return eChildNodeValue;
  }

}
