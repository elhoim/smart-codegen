/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.smartitengineering.javasourcetreeparser.test.resources;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.Set;
import java.io.*;
import javax.swing.JButton;
import javax.swing.JFrame;
import static java.lang.System.out;

/**
 *
 * @author imyousuf
 */
class NonPubClass {

  protected void test() {
    int i = 1;
    i += i;
    out.println("test 1");
    System.out.println("test 1");
  }

  public void test(String args,
                    String... vArgs) {
    out.println(args);
    System.out.println(vArgs);
    return ;
  }
}

/**
 *
 * @author imyousuf
 */
public class Main_1_1 {

  String member;
  private String memberPrivate;
  protected String memberProtected;
  public static String staticVar;
  private InputStream inputStream;
  String[] arrayMembers;
  List<String> collectionMembers;
  java.util.List<String> qCollectionMembers;
  Vector<String> vector;
  Set setMembers;
  int test = 10;
  

  static {
    staticVar = "Static Variable";
    System.out.println(staticVar);
    System.out.println("Static: " + staticVar);
    int i = 1;
    try {
      System.out.println("try");
      while (i++ < 5) {
        switch (i) {
          case 1:
            System.out.println(1);
            break;
          case 2:
            System.out.println(2);
            System.out.println("Again " + 2);
            break;
          case 3: {
            System.out.println(3);
            System.out.println("Again " + 3);
            break;
          }
          default:
            System.out.println("default!");
        }
        System.out.println("While!");
      }
    }
    catch (Exception ex) {
      System.out.println("Catch");
    }
    for (int j = 0; j < 2; ++j) {
      if (i < 2) {
        System.out.println("i < 2");
      }
      else if (i < 4) {
        System.out.println("i < 4");
      }
      else {
        System.out.println("i >= 4");
      }
    }
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    Main_1_1 main = new Main_1_1();
    int arrayTemp[] = new int[10];
    main.arrayMembers = new String[]{"Modhu", "Adnan", "Imran", "Urmee"};
    main.collectionMembers = new ArrayList(Arrays.asList(main.arrayMembers));
    ActionListener firstActionListener = new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        System.out.println("Not Supported yet!");
        throw new UnsupportedOperationException("Not supported yet.");
      }
    };
    JButton button = new JButton();
    JFrame frame = new JFrame("Test");
    button.addActionListener(firstActionListener);
    button.addActionListener(new ActionListener() {

                       public void actionPerformed(ActionEvent e) {
                         System.out.println("Not Supported yet!");
                         throw new UnsupportedOperationException("Not supported yet.");
                       }
                     });
    main.collectionMembers.add("Upoma");
    System.out.println(main);
    for (String member : main.collectionMembers) {
      System.out.println("For Each: " + member);
    }
    System.out.println("Static: " + main);
    do {
      System.out.println("Do-While");
    } while (main.test-- > 0);
    NonPubClass nonPubClass = new NonPubClass() {

      @Override
      protected void test() {
        super.test();
      }
      
    };
    nonPubClass.test(staticVar, "1", "2", "3", "4");
  }

  public String toString() {
    StringBuilder toStringBuilder = new StringBuilder();
    toStringBuilder.append(super.toString());
    toStringBuilder.append("\n");
    toStringBuilder.append("\nmember: ");
    toStringBuilder.append(member);
    toStringBuilder.append("\nmemberPrivate: ");
    toStringBuilder.append(memberPrivate);
    toStringBuilder.append("\nmemberProtected: ");
    toStringBuilder.append(memberProtected);
    toStringBuilder.append("\ninputStream: ");
    toStringBuilder.append(inputStream);
    toStringBuilder.append("\narrayMembers: ");
    if (arrayMembers != null) {
      toStringBuilder.append("\nSize: ");
      toStringBuilder.append(arrayMembers.length);
      for (int i = 0; i < arrayMembers.length; ++i) {
        toStringBuilder.append("\nIndex ");
        toStringBuilder.append(i);
        toStringBuilder.append(": ");
        toStringBuilder.append(arrayMembers[i]);
      }
    }
    else {
      toStringBuilder.append("NULL");
    }
    toStringBuilder.append("\ncollectionMembers: ");
    if (collectionMembers != null) {
      toStringBuilder.append("\nSize: ");
      toStringBuilder.append(collectionMembers.size());
      java.util.Iterator collectionIiterator = collectionMembers.iterator();
      for (int i = 0; collectionIiterator.hasNext(); ++i) {
        toStringBuilder.append("\nIndex ");
        toStringBuilder.append(i);
        toStringBuilder.append(": ");
        toStringBuilder.append(collectionIiterator.next());
      }
    }
    else {
      toStringBuilder.append("NULL");
    }
    toStringBuilder.append("\nqCollectionMembers: ");
    if (qCollectionMembers != null) {
      toStringBuilder.append("\nSize: ");
      toStringBuilder.append(qCollectionMembers.size());
      java.util.Iterator collectionIiterator = qCollectionMembers.iterator();
      for (int i = 0; collectionIiterator.hasNext(); ++i) {
        toStringBuilder.append("\nIndex ");
        toStringBuilder.append(i);
        toStringBuilder.append(": ");
        toStringBuilder.append(collectionIiterator.next());
      }
    }
    else {
      toStringBuilder.append("NULL");
    }
    toStringBuilder.append("\nvector: ");
    if (vector != null) {
      toStringBuilder.append("\nSize: ");
      toStringBuilder.append(vector.size());
      java.util.Iterator collectionIiterator = vector.iterator();
      for (int i = 0; collectionIiterator.hasNext(); ++i) {
        toStringBuilder.append("\nIndex ");
        toStringBuilder.append(i);
        toStringBuilder.append(": ");
        toStringBuilder.append(collectionIiterator.next());
        System.out.println("Traditional FOR");
      }
    }
    else {
      toStringBuilder.append("NULL");
    }
    toStringBuilder.append("\nsetMembers: ");
    if (setMembers != null) {
      toStringBuilder.append("\nSize: ");
      toStringBuilder.append(setMembers.size());
      java.util.Iterator collectionIiterator = setMembers.iterator();
      for (int i = 0; collectionIiterator.hasNext(); ++i) {
        toStringBuilder.append("\nIndex ");
        toStringBuilder.append(i);
        toStringBuilder.append(": ");
        toStringBuilder.append(collectionIiterator.next());
      }
    }
    else {
      toStringBuilder.append("NULL");
    }
    toStringBuilder.append("\ntest: ");
    toStringBuilder.append(test);
    {
      System.out.println("Nested block!");
    }
    return toStringBuilder.toString();
  }

  protected class InnerClass {

    protected int test() {
      out.println("test 1");
      System.out.println((String)"test 1");
      ;
      return 2 + 4;
    }
  }
}
