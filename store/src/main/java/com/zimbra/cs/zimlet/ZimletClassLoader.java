// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.zimlet;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jylee
 */
public class ZimletClassLoader extends URLClassLoader {

  private List mZimletClassNames = new ArrayList();

  /** Load Zimlet server extension class. */
  public ZimletClassLoader(File rootDir, String className, ClassLoader parent)
      throws MalformedURLException {
    super(fileToURL(rootDir, className), parent);
    mZimletClassNames.add(className);
  }

  public List getExtensionClassNames() {
    return mZimletClassNames;
  }

  private static URL[] fileToURL(File dir, String file) throws MalformedURLException {
    URL url = new File(dir, file).toURL();
    return new URL[] {url};
  }
}
