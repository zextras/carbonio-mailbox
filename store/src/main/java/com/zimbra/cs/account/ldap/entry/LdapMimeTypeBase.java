// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.ldap.entry;

import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mime.MimeTypeInfo;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author pshao
 */
public abstract class LdapMimeTypeBase extends Entry implements LdapEntry, MimeTypeInfo {

  protected String mDn;

  protected LdapMimeTypeBase(
      Map<String, Object> attrs, Map<String, Object> defaults, Provisioning provisioning) {
    super(attrs, defaults, provisioning);
  }

  @Override
  public EntryType getEntryType() {
    return EntryType.MIMETYPE;
  }

  public String getLabel() {
    return mDn;
  }

  public String getDN() {
    return mDn;
  }

  public String[] getMimeTypes() {
    return super.getMultiAttr(Provisioning.A_zimbraMimeType);
  }

  public String getHandlerClass() {
    return super.getAttr(Provisioning.A_zimbraMimeHandlerClass, null);
  }

  public boolean isIndexingEnabled() {
    return super.getBooleanAttr(Provisioning.A_zimbraMimeIndexingEnabled, true);
  }

  public String getDescription() {
    return super.getAttr(Provisioning.A_description, "");
  }

  public Set<String> getFileExtensions() {
    String[] extensions = super.getMultiAttr(Provisioning.A_zimbraMimeFileExtension);
    Set<String> extSet = new TreeSet<String>();
    for (String ext : extensions) {
      if (ext != null) {
        extSet.add(ext.toLowerCase());
      }
    }
    return extSet;
  }

  public String getExtension() {
    return super.getAttr(Provisioning.A_zimbraMimeHandlerExtension, null);
  }

  public int getPriority() {
    return super.getIntAttr(Provisioning.A_zimbraMimePriority, 0);
  }
}
