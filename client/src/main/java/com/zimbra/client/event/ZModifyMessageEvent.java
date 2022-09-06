// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client.event;

import com.zimbra.client.ToZJSONObject;
import com.zimbra.client.ZJSONObject;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import org.json.JSONException;

public class ZModifyMessageEvent
    implements ZModifyItemEvent, ZModifyItemFolderEvent, ToZJSONObject {

  protected Element mMessageEl;

  public ZModifyMessageEvent(Element e) throws ServiceException {
    mMessageEl = e;
  }

  /**
   * @return id
   * @throws com.zimbra.common.service.ServiceException on error
   */
  public String getId() throws ServiceException {
    return mMessageEl.getAttribute(MailConstants.A_ID);
  }

  /**
   * @param defaultValue value to return if unchanged
   * @return new flags or default value if flags didn't change
   */
  public String getFlags(String defaultValue) {
    return mMessageEl.getAttribute(MailConstants.A_FLAGS, defaultValue);
  }

  /**
   * @param defaultValue value to return if unchanged
   * @return new tags or default value if tags didn't change
   */
  public String getTagIds(String defaultValue) {
    return mMessageEl.getAttribute(MailConstants.A_TAGS, defaultValue);
  }

  public String getFolderId(String defaultValue) {
    return mMessageEl.getAttribute(MailConstants.A_FOLDER, defaultValue);
  }

  /**
   * @param defaultValue value to return if unchanged
   * @return new conv id or defaultValue if unchanged
   */
  public String getConversationId(String defaultValue) {
    return mMessageEl.getAttribute(MailConstants.A_CONV_ID, defaultValue);
  }

  public ZJSONObject toZJSONObject() throws JSONException {
    try {
      ZJSONObject zjo = new ZJSONObject();
      zjo.put("id", getId());
      if (getConversationId(null) != null) zjo.put("conversationId", getConversationId(null));
      if (getFlags(null) != null) zjo.put("flags", getFlags(null));
      if (getTagIds(null) != null) zjo.put("tags", getTagIds(null));
      if (getFolderId(null) != null) zjo.put("folderId", getFolderId(null));
      return zjo;
    } catch (ServiceException se) {
      throw new JSONException(se);
    }
  }

  public String toString() {
    try {
      return String.format("[ZModifyMessageEvent %s]", getId());
    } catch (ServiceException e) {
      throw new RuntimeException(e);
    }
  }

  public String dump() {
    return ZJSONObject.toString(this);
  }
}
