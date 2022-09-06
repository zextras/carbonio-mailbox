// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.property;

import com.zimbra.cs.dav.DavElements;
import org.dom4j.Element;

public class ResourceTypeProperty extends ResourceProperty {

  public ResourceTypeProperty(Element elem) {
    super(elem);
  }

  public boolean isCollection() {
    for (Element child : super.getChildren()) {
      if (child.getName().equals(DavElements.P_COLLECTION)) {
        return true;
      }
    }
    return false;
  }

  public boolean isAddressBook() {
    for (Element child : super.getChildren()) {
      if (child.getName().equals(DavElements.P_ADDRESSBOOK)) {
        return true;
      }
    }
    return false;
  }

  public boolean isCalendar() {
    for (Element child : super.getChildren()) {
      if (child.getName().equals(DavElements.P_CALENDAR)) {
        return true;
      }
    }
    return false;
  }
}
