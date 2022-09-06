// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailclient.imap;

import com.zimbra.cs.mailclient.util.Ascii;
import java.io.IOException;
import java.io.OutputStream;

/** IMAP atom data type. */
public final class Atom extends ImapData {
  private final String name;

  public static final Atom NIL = new Atom("nil");

  public Atom(String name) {
    this.name = name;
  }

  public Type getType() {
    return Type.ATOM;
  }

  public String getName() {
    return name;
  }

  public CAtom getCAtom() {
    return CAtom.get(this);
  }

  public boolean isNumber() {
    return Chars.isNumber(name);
  }

  public long getNumber() {
    return Chars.getNumber(name);
  }

  public int getSize() {
    return name.length();
  }

  public byte[] getBytes() {
    return Ascii.getBytes(name);
  }

  public void write(OutputStream os) throws IOException {
    Ascii.write(os, name);
  }

  public int hashCode() {
    return name.toUpperCase().hashCode();
  }

  public boolean equals(Object obj) {
    return this == obj
        || obj != null && obj.getClass() == Atom.class && name.equalsIgnoreCase(((Atom) obj).name);
  }

  public String toString() {
    return name;
  }
}
