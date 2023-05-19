// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mailbox;

import java.util.Set;
import java.util.TimeZone;

public interface ZimbraSearchParams {
    boolean getIncludeTagDeleted();
    void setIncludeTagDeleted(boolean value);
    boolean getPrefetch();
    void setPrefetch(boolean value);
    String getQueryString();
    void setQueryString(String value);
    Set<MailItemType> getMailItemTypes();
    ZimbraSearchParams setMailItemTypes(Set<MailItemType> values);
    ZimbraSortBy getZimbraSortBy();
    ZimbraSearchParams setZimbraSortBy(ZimbraSortBy value);
    int getLimit();
    void setLimit(int value);
    ZimbraFetchMode getZimbraFetchMode();
    ZimbraSearchParams setZimbraFetchMode(ZimbraFetchMode value);
    TimeZone getTimeZone();
    void setTimeZone(TimeZone value);
}
