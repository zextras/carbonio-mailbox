// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import com.zimbra.client.event.ZModifyEvent;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import org.json.JSONException;

public class ZDocumentHit implements ZSearchHit {

  private ZDocument mDoc;
  private String mId;
  private String mSortField;

  public ZDocumentHit(Element e) throws ServiceException {
    mId = e.getAttribute(MailConstants.A_ID);
    mSortField = e.getAttribute(MailConstants.A_SORT_FIELD, null);
    mDoc = new ZDocument(e);
  }

  public ZDocument getDocument() {
    return mDoc;
  }

  @Override
  public String getId() {
    return mId;
  }

  @Override
  public String getSortField() {
    return mSortField;
  }

  @Override
  public void modifyNotification(ZModifyEvent event) throws ServiceException {}

  @Override
  public ZJSONObject toZJSONObject() throws JSONException {
    ZJSONObject zjo = new ZJSONObject();
    zjo.put("id", mId);
    zjo.put("sortField", mSortField);
    zjo.put("document", mDoc);
    return zjo;
  }

  @Override
  public String toString() {
    return String.format("[ZDocumentHit %s]", mId);
  }

  public String dump() {
    return ZJSONObject.toString(this);
  }
}
