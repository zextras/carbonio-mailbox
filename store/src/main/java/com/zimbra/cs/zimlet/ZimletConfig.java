// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.zimlet;

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.ZimletConstants;
import com.zimbra.common.util.ZimbraLog;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author jylee
 */
public class ZimletConfig extends ZimletMeta implements ZimletConf {

  public static final String CONFIG_REGEX_VALUE = "ZIMLET_CONFIG_REGEX_VALUE";

  private Map mGlobalConfig;
  private Map mSiteConfig;
  private String mLocalHost;

  public ZimletConfig(String c) throws ZimletException {
    super(c);
  }

  public ZimletConfig(File f) throws ZimletException {
    super(f);
  }

  protected void initialize() throws ZimletException {
    mGlobalConfig = new HashMap();
    mSiteConfig = new HashMap();
    try {
      mLocalHost = InetAddress.getLocalHost().getHostName().toLowerCase();
    } catch (UnknownHostException uhe) {
      throw ZimletException.INVALID_ZIMLET_CONFIG("cannot figure out hostname on localhost");
    }
  }

  protected void validateElement(Element elem) throws ZimletException {
    if (elem.getName().equals(ZimletConstants.ZIMLET_TAG_GLOBAL)) {
      parseConfig(elem, mGlobalConfig);
    } else if (elem.getName().equals(ZimletConstants.ZIMLET_TAG_HOST)) {
      String hostname = elem.getAttribute(ZimletConstants.ZIMLET_ATTR_NAME, "").toLowerCase();
      if (mLocalHost.equals(hostname)) {
        parseConfig(elem, mSiteConfig);
      } else {
        elem.detach();
      }
    } else {
      ZimbraLog.zimlet.warn("unrecognized config element " + elem.getName());
    }
  }

  private void parseConfig(Element elem, Map config) {
    Iterator iter = elem.listElements().iterator();
    while (iter.hasNext()) {
      Element e = (Element) iter.next();
      config.put(e.getAttribute(ZimletConstants.ZIMLET_ATTR_NAME, ""), e.getText());
    }
  }

  public String getConfigValue(String key) {
    if (mSiteConfig.containsKey(key)) {
      return (String) mSiteConfig.get(key);
    }
    return (String) mGlobalConfig.get(key);
  }

  public void setRegExValue(String regex) {
    mGlobalConfig.put(CONFIG_REGEX_VALUE, regex);
  }

  public Map getGlobalConfig() {
    return mGlobalConfig;
  }

  public Map getSiteConfig() {
    return mSiteConfig;
  }

  public String getGlobalConf(String key) {
    return (String) mGlobalConfig.get(key);
  }

  public String getSiteConf(String key) {
    return (String) mSiteConfig.get(key);
  }

  public String toXMLString() {
    return mTopElement.toString();
  }
}
