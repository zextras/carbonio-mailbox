// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.io;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.StringUtil;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zimbra
 */
public class SecureObjectInputStream extends ObjectInputStream {

  /**
   * @param string
   * @param fileInputStream
   * @throws IOException
   * @throws SecurityException
   */
  private Set<String> acceptedClassname;

  protected SecureObjectInputStream() throws IOException, SecurityException {
    super();
  }

  /**
   * @param in
   * @throws IOException
   */
  public SecureObjectInputStream(InputStream in, String acceptedClassname) throws IOException {
    super(in);
    this.acceptedClassname = new HashSet<String>();
    this.acceptedClassname.add(acceptedClassname);
  }

  /**
   * @param in
   * @throws IOException
   */
  public SecureObjectInputStream(InputStream in, Set<String> acceptedClassname) throws IOException {
    super(in);
    this.acceptedClassname = acceptedClassname;
  }

  /** Only deserialize instances of known zimbra classes */
  @Override
  protected Class<?> resolveClass(ObjectStreamClass desc)
      throws IOException, ClassNotFoundException {

    String acceptedClassString = LC.zimbra_deserialize_classes.value();
    if (!StringUtil.isNullOrEmpty(acceptedClassString)) {
      this.acceptedClassname.addAll(Arrays.asList(acceptedClassString.split(",")));
    }

    if (desc.getName().startsWith("java.") || desc.getName().startsWith("[Ljava.")) {
      return super.resolveClass(desc);
    } else {
      for (String className : this.acceptedClassname) {
        if (desc.getName().equals(className)) {
          return super.resolveClass(desc);
        }
      }
    }
    throw new InvalidClassException("Unauthorized deserialization attempt", desc.getName());
  }
}
