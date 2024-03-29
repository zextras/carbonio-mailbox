// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import com.zimbra.soap.type.AttributeName;
import com.zimbra.soap.type.CursorInfo;
import com.zimbra.soap.type.WantRecipsSetting;
import java.util.List;

public interface SearchParameters {

  // Used for requests which use attributes and elements processed by SearchParams.parse
  void setIncludeTagDeleted(Boolean includeTagDeleted);

  void setIncludeTagMuted(Boolean includeTagMuted);

  void setCalItemExpandStart(Long calItemExpandStart);

  void setCalItemExpandEnd(Long calItemExpandEnd);

  void setQuery(String query);

  void setInDumpster(Boolean inDumpster);

  void setSearchTypes(String searchTypes);

  void setGroupBy(String groupBy);

  void setQuick(Boolean quick);

  void setSortBy(String sortBy);

  void setFetch(String fetch);

  void setMarkRead(Boolean markRead);

  void setMaxInlinedLength(Integer maxInlinedLength);

  void setWantHtml(Boolean wantHtml);

  void setNeedCanExpand(Boolean needCanExpand);

  void setNeuterImages(Boolean neuterImages);

  void setWantRecipients(WantRecipsSetting wantRecipients);

  void setPrefetch(Boolean prefetch);

  void setResultMode(String resultMode);

  void setField(String field);

  void setLimit(Integer limit);

  void setOffset(Integer offset);

  void setHeaders(Iterable<AttributeName> headers);

  void addHeader(AttributeName header);

  void setCalTz(CalTZInfoInterface calTz);

  void setLocale(String locale);

  void setCursor(CursorInfo cursor);

  Boolean getIncludeTagDeleted();

  Boolean getIncludeTagMuted();

  Long getCalItemExpandStart();

  Long getCalItemExpandEnd();

  String getQuery();

  Boolean getInDumpster();

  String getSearchTypes();

  String getGroupBy();

  Boolean getQuick();

  String getSortBy();

  String getFetch();

  Boolean getMarkRead();

  Integer getMaxInlinedLength();

  Boolean getWantHtml();

  Boolean getNeedCanExpand();

  Boolean getNeuterImages();

  WantRecipsSetting getWantRecipients();

  Boolean getPrefetch();

  String getResultMode();

  String getField();

  Integer getLimit();

  Integer getOffset();

  List<AttributeName> getHeaders();

  CalTZInfoInterface getCalTz();

  String getLocale();

  CursorInfo getCursor();
}
