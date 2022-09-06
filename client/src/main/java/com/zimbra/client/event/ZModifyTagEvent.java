// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import com.zimbra.client.ToZJSONObject;
import com.zimbra.client.ZJSONObject;
import com.zimbra.client.ZTag.Color;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.type.RetentionPolicy;
import org.json.JSONException;

public class ZModifyTagEvent implements ZModifyItemEvent, ToZJSONObject {

  protected Element mTagEl;

  public ZModifyTagEvent(Element e) {
    mTagEl = e;
  }

  /**
   * @return folder id of modified tag
   * @throws com.zimbra.common.service.ServiceException
   */
  public String getId() throws ServiceException {
    return mTagEl.getAttribute(MailConstants.A_ID);
  }

  /**
   * @param defaultValue value to return if unchanged
   * @return new name or defaultValue if unchanged
   */
  public String getName(String defaultValue) {
    return mTagEl.getAttribute(MailConstants.A_NAME, defaultValue);
  }

  /**
   * @param defaultValue value to return if unchanged
   * @return new color, or default value.
   */
  public Color getColor(Color defaultValue) {
    String newColor = mTagEl.getAttribute(MailConstants.A_RGB, null);
    if (newColor != null) {
      return Color.rgbColor.setRgbColor(newColor);
    } else {
      String s = mTagEl.getAttribute(MailConstants.A_COLOR, null);
      if (s != null) {
        try {
          return Color.values()[(byte) Long.parseLong(s)];
        } catch (NumberFormatException se) {
          return defaultValue;
        }
      }
    }
    return defaultValue;
  }

  /** Returns the modified retention policy, or {@code defaultValue} if it hasn't been modified. */
  public RetentionPolicy getRetentionPolicy(RetentionPolicy defaultValue) throws ServiceException {
    Element rpEl = mTagEl.getOptionalElement(MailConstants.E_RETENTION_POLICY);
    if (rpEl == null) {
      return defaultValue;
    }
    return new RetentionPolicy(rpEl);
  }

  /**
   * @param defaultValue value to return if unchanged
   * @return new unread count, or defaultVslue if unchanged
   * @throws com.zimbra.common.service.ServiceException on error
   */
  public int getUnreadCount(int defaultValue) throws ServiceException {
    return (int) mTagEl.getAttributeLong(MailConstants.A_UNREAD, defaultValue);
  }

  public ZJSONObject toZJSONObject() throws JSONException {
    try {
      ZJSONObject zjo = new ZJSONObject();
      zjo.put("id", getId());
      String name = getName(null);
      if (name != null) zjo.put("name", name);
      if (getColor(null) != null) zjo.put("color", getColor(null).name());
      if (getUnreadCount(-1) != -1) zjo.put("unreadCount", getUnreadCount(-1));
      return zjo;
    } catch (ServiceException se) {
      throw new JSONException(se);
    }
  }

  public String toString() {
    try {
      return String.format("[ZModifyTagEvent %s]", getId());
    } catch (ServiceException e) {
      throw new RuntimeException(e);
    }
  }

  public String dump() {
    return ZJSONObject.toString(this);
  }
}
