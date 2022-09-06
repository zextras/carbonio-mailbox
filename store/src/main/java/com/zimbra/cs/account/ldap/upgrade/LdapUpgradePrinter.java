// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.upgrade;

import com.zimbra.cs.account.Entry;
import java.io.PrintStream;
import java.io.PrintWriter;

class LdapUpgradePrinter {

  private PrintStream printer = System.out;

  void print(String str) {
    printer.print(str);
  }

  void println() {
    printer.println();
  }

  void println(String str) {
    printer.println(str);
  }

  void format(String format, Object... objects) {
    printer.format(format, objects);
  }

  PrintWriter getPrintWriter() {
    return new PrintWriter(printer, true);
  }

  void printStackTrace(Throwable e) {
    e.printStackTrace(getPrintWriter());
  }

  void printStackTrace(String str, Throwable e) {
    println(str);
    e.printStackTrace(getPrintWriter());
  }

  void printCheckingEntry(Entry entry) {
    printer.println("\nChecking " + entry.getEntryType().getName() + " entry " + entry.getLabel());
  }
}
