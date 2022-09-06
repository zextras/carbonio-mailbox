// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import com.zimbra.client.ToZJSONObject;
import com.zimbra.client.ZItem;
import com.zimbra.client.ZJSONObject;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import org.json.JSONException;

public class ZCreateAppointmentEvent implements ZCreateItemEvent, ToZJSONObject {

  protected Element mApptEl;

  public ZCreateAppointmentEvent(Element e) throws ServiceException {
    mApptEl = e;
  }

  /**
   * @return id
   * @throws com.zimbra.common.service.ServiceException on error
   */
  public String getId() throws ServiceException {
    return mApptEl.getAttribute(MailConstants.A_ID);
  }

  public ZJSONObject toZJSONObject() throws JSONException {
    try {
      ZJSONObject zjo = new ZJSONObject();
      zjo.put("id", getId());
      return zjo;
    } catch (ServiceException se) {
      throw new JSONException(se);
    }
  }

  public String toString() {
    try {
      return String.format("[ZCreateAppointmentEvent %s]", getId());
    } catch (ServiceException e) {
      throw new RuntimeException(e);
    }
  }

  public String dump() {
    return ZJSONObject.toString(this);
  }

  public ZItem getItem() throws ServiceException {
    return null;
  }
}
