/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.smartitengineering.javasourcetreeparser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 *
 * @author imyousuf
 */
public class SimpleLagacyLogFormatter extends Formatter {

  @Override
  public String format(LogRecord record) {
    StringBuilder formattedMessage = new StringBuilder();
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(record.getMillis());
    SimpleDateFormat dateFormat 
            = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    formattedMessage.append(dateFormat.format(calendar.getTime()));
    formattedMessage.append(' ');
    formattedMessage.append("[");
    formattedMessage.append(record.getLoggerName());
    formattedMessage.append("] ");
    formattedMessage.append(record.getLevel().getName());
    formattedMessage.append(": ");
    formattedMessage.append(record.getMessage());
    formattedMessage.append('\n');
    return formattedMessage.toString();
  }

}
