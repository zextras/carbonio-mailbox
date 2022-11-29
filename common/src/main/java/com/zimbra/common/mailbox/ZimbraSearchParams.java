// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mailbox;

import java.util.Set;
import java.util.TimeZone;

public interface ZimbraSearchParams {
    public boolean getIncludeTagDeleted();
    public void setIncludeTagDeleted(boolean value);
    public boolean getPrefetch();
    public void setPrefetch(boolean value);
    public String getQueryString();
    public void setQueryString(String value);
    public Set<MailItemType> getMailItemTypes();
    public ZimbraSearchParams setMailItemTypes(Set<MailItemType> values);
    public ZimbraSortBy getZimbraSortBy();
    public ZimbraSearchParams setZimbraSortBy(ZimbraSortBy value);
    public int getLimit();
    public void setLimit(int value);
    public ZimbraFetchMode getZimbraFetchMode();
    public ZimbraSearchParams setZimbraFetchMode(ZimbraFetchMode value);
    public TimeZone getTimeZone();
    public void setTimeZone(TimeZone value);
}
