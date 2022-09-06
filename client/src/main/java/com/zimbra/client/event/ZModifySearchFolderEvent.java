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
import com.zimbra.soap.type.SearchSortBy;
import org.json.JSONException;

public class ZModifySearchFolderEvent extends ZModifyFolderEvent implements ToZJSONObject {

  public ZModifySearchFolderEvent(Element e) throws ServiceException {
    super(e);
  }

  /**
   * @param defaultValue value to return if unchanged
   * @return new name or defaultValue if unchanged
   */
  public String getQuery(String defaultValue) {
    return mFolderEl.getAttribute(MailConstants.A_QUERY, defaultValue);
  }

  /**
   * @param defaultValue value to return if unchanged
   * @return new name or defaultValue if unchanged
   */
  public String getTypes(String defaultValue) {
    return mFolderEl.getAttribute(MailConstants.A_SEARCH_TYPES, defaultValue);
  }

  /**
   * @param defaultValue value to return if unchanged
   * @return new name or defaultValue if unchanged
   */
  public SearchSortBy getSortBy(SearchSortBy defaultValue) {
    try {
      String newSort = mFolderEl.getAttribute(MailConstants.A_SORTBY, null);
      return newSort == null ? defaultValue : SearchSortBy.fromString(newSort);
    } catch (ServiceException se) {
      return defaultValue;
    }
  }

  @Override
  public ZJSONObject toZJSONObject() throws JSONException {
    ZJSONObject zjo = super.toZJSONObject();
    if (getQuery(null) != null) zjo.put("query", getQuery(null));
    if (getTypes(null) != null) zjo.put("types", getTypes(null));
    if (getSortBy(null) != null) zjo.put("sortBy", getSortBy(null).name());
    return zjo;
  }
}
