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
 * This class represents a generic exception that can be thrown from Service
 * methods
 *
 * @author <a href="mailto:shams.mahmood@gmail.com">Shams Mahmood</a>
 * @creationDate Mar 06, 2008
 */
public class ServiceException extends Exception {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception with <code>null</code> as its detail message.
   * The cause is not initialized, and may subsequently be initialized by a call
   * to {@link #initCause}.
   */
  public ServiceException() {
    super();
  }

  /**
   * Constructs a new exception with the specified cause and a detail message of
   * <tt>(cause==null ? null : cause.toString())</tt> (which typically contains
   * the class and detail message of <tt>cause</tt>). This constructor is useful
   * for exceptions that are little more than wrappers for other throwables (for
   * example, {@link java.security.PrivilegedActionException}).
   *
   * @param pCause the cause (which is saved for later retrieval by the {@link
   *              #getCause()} method). (A <tt>null</tt> value is permitted, and
   *              indicates that the cause is nonexistent or unknown.)
   *
   * @since 1.4
   */
  public ServiceException(Throwable pCause) {
    super(pCause);
  }

  /**
   * Constructs a new exception with the specified detail message. The cause is
   * not initialized, and may subsequently be initialized by a call to {@link
   * #initCause}.
   *
   * @param pMessage the detail message. The detail message is saved for later
   *                retrieval by the {@link #getMessage()} method.
   */
  public ServiceException(String pMessage) {
    super(pMessage);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   * <p/>
   * Note that the detail message associated with <code>cause</code> is
   * <i>not</i> automatically incorporated in this exception's detail message.
   *
   * @param pMessage the detail message (which is saved for later retrieval by
   *                the {@link #getMessage()} method).
   * @param pCause   the cause (which is saved for later retrieval by the {@link
   *                #getCause()} method). (A <tt>null</tt> value is permitted,
   *                and indicates that the cause is nonexistent or unknown.)
   *
   * @since 1.4
   */
  public ServiceException(String pMessage, Throwable pCause) {
    super(pMessage, pCause);
  }
}
