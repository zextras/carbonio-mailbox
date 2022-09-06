// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import com.zimbra.common.soap.Element;

public class ProxiedQueryInfo implements QueryInfo {

  private Element mElt;

  ProxiedQueryInfo(Element e) {
    mElt = e;
    mElt.detach();
  }

  public Element toXml(Element parent) {
    parent.addElement(mElt);
    return mElt;
  }

  @Override
  public String toString() {
    return mElt.toString();
  }
}
