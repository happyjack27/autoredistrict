/**
 * <p>Title: java访问DBF文件的接口</p>
 * <p>Description: 这个类用于表示DBF文件中的读写异常</p>
 * <p>Copyright: Copyright (c) 2004~2012~2012</p>
 * <p>Company: iihero.com</p>
 * @author : He Xiong
 * @version 1.1
 */

package com.hexiong.jdbf;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * <p>Title: java访问DBF文件的接口</p>
 * <p>Description: 这个类用于表示DBF文件中的读写异常</p>
 * <p>Copyright: Copyright (c) 2004~2012~2012</p>
 * <p>Company: iihero.com</p>
 * @author : He Xiong
 * @version 1.1
 */
public class JDBFException
    extends Exception {
  /**
   * 使用一个字符串来构造JDBFException
   * @param s 异常的内容
   */
  public JDBFException(String s) {
    this(s, null);
  }

  /**
   * 使用一个异常来构造JDBFException
   * @param throwable 要抛出的异常
   */
  public JDBFException(Throwable throwable) {
    this(throwable.getMessage(), throwable);
  }

  /**
   * 构造函数
   * @param s 异常的内容
   * @param throwable 一种异常
   */
  public JDBFException(String s, Throwable throwable) {
    super(s);
    detail = throwable;
  }

  /**
   * 获取异常的具体内容
   * @return 异常JDBFException的具体内容
   */
  public String getMessage() {
    if (detail == null) {
      return super.getMessage();
    }
    else {
      return super.getMessage();
    }
  }

  /**
   * 输出异常至屏幕
   * @param printstream
   */
  public void printStackTrace(PrintStream printstream) {
    if (detail == null) {
      super.printStackTrace(printstream);
      return;
    }
    PrintStream printstream1 = printstream;
    printstream1.println(this);
    detail.printStackTrace(printstream);
    return;
  }

  /**
   * @see printStackTrace(PrintStream printstream)
   */
  public void printStackTrace() {
    printStackTrace(System.err);
  }
  /**
   * @see printStackTrace(PrintStream printstream)
   * @param printwriter
   */
  public void printStackTrace(PrintWriter printwriter) {
    if (detail == null) {
      super.printStackTrace(printwriter);
      return;
    }
    PrintWriter printwriter1 = printwriter;

    printwriter1.println(this);
    detail.printStackTrace(printwriter);
    return;
  }

  /**
   * 异常细节
   */
  private Throwable detail;
}
