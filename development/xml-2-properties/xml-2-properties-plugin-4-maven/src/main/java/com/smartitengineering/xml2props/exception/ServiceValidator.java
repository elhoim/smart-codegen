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
package com.smartitengineering.xml2props.exception;

/**
 * Simple class to handle common validation requests. ServiceException is throw
 * whenever a validation fails.
 *
 * @author <a href="mailto:shams.mahmood@gmail.com">Shams Mahmood</a>
 * @creationDate Mar 06, 2008
 */
public final class ServiceValidator {

  private ServiceValidator() {

  }

  public static String createAppendedString(final Object[] pErrorMessage) {
    if (pErrorMessage == null) {
      return "";
    }
    StringBuffer eBuilder = new StringBuffer();
    for (int i = 0; i < pErrorMessage.length; i++) {
      Object eLoopObject = pErrorMessage[i];
      eBuilder.append(eLoopObject).append(" ");
    }
    return eBuilder.toString();
  }

  /**
   * Method to validate that the input Object is not null.
   *
   * @param pObject       The object to test
   * @param pErrorMessage The error message to throw if the object is found to
   *                      be null
   *
   * @throws ServiceException The Exception throws if the input object is null.
   */
  public static void validateNotNull(
      final Object pObject,
      final Object[] pErrorMessage)
      throws ServiceException {
    if (pObject == null) {
      throwServiceException(pErrorMessage);
    }
  }

  public static void validateNotNull(Object pObject, String pErrorMessage)
      throws ServiceException {
    validateNotNull(pObject, new Object[]{pErrorMessage});
  }

  /**
   * Method to validate that the input Object is null.
   *
   * @param pObject       The object to test
   * @param pErrorMessage The error message to throw if the object is found to
   *                      be not null
   *
   * @throws ServiceException The Exception throws if the input object is not
   *                          null.
   */
  public static void validateNull(
      final Object pObject,
      final Object[] pErrorMessage)
      throws ServiceException {

    if (pObject != null) {
      throwServiceException(pErrorMessage);
    }
  }

  public static void validateNull(Object pObject, String pErrorMessage)
      throws ServiceException {
    validateNull(pObject, new Object[]{pErrorMessage});
  }

  /**
   * Method to validate the the two input int are not equal
   *
   * @param pFirstObject  The first primitive type
   * @param pSecondObject The second primitive type
   * @param pErrorMessage The error message to throw if the two objects are
   *                      found to be equal
   *
   * @throws ServiceException The Exception throws if the input objects are
   *                          equal.
   */
  public static void validateNotEquals(
      final Object pFirstObject,
      final Object pSecondObject,
      final Object[] pErrorMessage)
      throws ServiceException {

    if (pFirstObject != null && pFirstObject.equals(pSecondObject)) {
      throwServiceException(pErrorMessage);
    }
  }

  public static void validateNotEquals(
      final Object pFirstObject,
      final Object pSecondObject,
      final String pMsg)
      throws ServiceException {
    validateNotEquals(pFirstObject, pSecondObject, new Object[]{pMsg});
  }

  /**
   * Method to validate the the two input int are equal
   *
   * @param pFirstObject  The first primitive type
   * @param pSecondObject The second primitive tpe
   * @param pErrorMessage The error message to throw if the two objects are
   *                      found to be unequal
   *
   * @throws ServiceException The Exception throws if the input objects are not
   *                          equal.
   */
  public static void validateEquals(
      final Object pFirstObject,
      final Object pSecondObject,
      final Object[] pErrorMessage)
      throws ServiceException {

    if (pFirstObject != null && !pFirstObject.equals(pSecondObject)) {
      throwServiceException(pErrorMessage);
    }
  }

  public static void validateEquals(
      final Object pFirstObject,
      final Object pSecondObject,
      String pErrorMessage)
      throws ServiceException {
    validateEquals(pFirstObject, pSecondObject, new Object[]{pErrorMessage});
  }

  public static void throwServiceException(final Object[] pErrorMessage)
      throws ServiceException {
    throw new ServiceException(createAppendedString(pErrorMessage));
  }

  public static void throwServiceException(final String pErrorMessage)
      throws ServiceException {
    throw new ServiceException(pErrorMessage);
  }

  public static void throwServiceException(
      final String pErrorMessage,
      final Throwable pThrowable)
      throws ServiceException {
    throw new ServiceException(pErrorMessage, pThrowable);
  }

  /**
   * Method to validate that the input boolean value is true.
   *
   * @param pBooleanValue The boolean value to test for 'trueness'
   * @param pErrorMessage The error message to throw if the boolean value is
   *                      false
   *
   * @throws ServiceException The Exception is thrown if the input boolean value
   *                          is false.
   */
  public static void validateTrue(
      final boolean pBooleanValue,
      final Object[] pErrorMessage)
      throws ServiceException {
    if (!pBooleanValue) {
      throwServiceException(pErrorMessage);
    }
  }

  public static void validateTrue(
      final boolean pBoolean,
      final String pErrorMessage)
      throws ServiceException {
    validateTrue(pBoolean, new Object[]{pErrorMessage});
  }

}
