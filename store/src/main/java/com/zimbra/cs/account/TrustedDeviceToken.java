// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import javax.servlet.http.HttpServletResponse;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;

public interface TrustedDeviceToken {

    Integer getId();

    Long getExpires();

    void setExpires(long expires);

    void setDelete();

    void encode(HttpServletResponse resp, Element el, boolean secure) throws ServiceException;

    boolean isExpired();
}
