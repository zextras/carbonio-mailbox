// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import java.util.List;

import com.zimbra.soap.type.AttributeName;
import com.zimbra.soap.type.CursorInfo;
import com.zimbra.soap.type.WantRecipsSetting;

public interface SearchParameters {

    // Used for requests which use attributes and elements processed by SearchParams.parse
    public void setIncludeTagDeleted(Boolean includeTagDeleted);
    public void setIncludeTagMuted(Boolean includeTagMuted);
    public void setAllowableTaskStatus(String allowableTaskStatus);
    public void setCalItemExpandStart(Long calItemExpandStart);
    public void setCalItemExpandEnd(Long calItemExpandEnd);
    public void setQuery(String query);
    public void setInDumpster(Boolean inDumpster);
    public void setSearchTypes(String searchTypes);
    public void setGroupBy(String groupBy);
    public void setQuick(Boolean quick);
    public void setSortBy(String sortBy);
    public void setFetch(String fetch);
    public void setMarkRead(Boolean markRead);
    public void setMaxInlinedLength(Integer maxInlinedLength);
    public void setWantHtml(Boolean wantHtml);
    public void setNeedCanExpand(Boolean needCanExpand);
    public void setNeuterImages(Boolean neuterImages);
    public void setWantRecipients(WantRecipsSetting wantRecipients);
    public void setPrefetch(Boolean prefetch);
    public void setResultMode(String resultMode);
    public void setField(String field);
    public void setLimit(Integer limit);
    public void setOffset(Integer offset);
    public void setHeaders(Iterable <AttributeName> headers);
    public void addHeader(AttributeName header);
    public void setCalTz(CalTZInfoInterface calTz);
    public void setLocale(String locale);
    public void setCursor(CursorInfo cursor);

    public Boolean getIncludeTagDeleted();
    public Boolean getIncludeTagMuted();
    public String getAllowableTaskStatus();
    public Long getCalItemExpandStart();
    public Long getCalItemExpandEnd();
    public String getQuery();
    public Boolean getInDumpster();
    public String getSearchTypes();
    public String getGroupBy();
    public Boolean getQuick();
    public String getSortBy();
    public String getFetch();
    public Boolean getMarkRead();
    public Integer getMaxInlinedLength();
    public Boolean getWantHtml();
    public Boolean getNeedCanExpand();
    public Boolean getNeuterImages();
    public WantRecipsSetting getWantRecipients();
    public Boolean getPrefetch();
    public String getResultMode();
    public String getField();
    public Integer getLimit();
    public Integer getOffset();
    public List<AttributeName> getHeaders();
    public CalTZInfoInterface getCalTz();
    public String getLocale();
    public CursorInfo getCursor();
}
