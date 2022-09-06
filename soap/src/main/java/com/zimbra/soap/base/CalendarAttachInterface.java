// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

import com.zimbra.common.calendar.Attach;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.NONE)
public interface CalendarAttachInterface {
  public CalendarAttachInterface createFromAttach(Attach att);

  public void setUri(String uri);

  public void setContentType(String contentType);

  public void setBinaryB64Data(String binaryB64Data);

  public String getUri();

  public String getContentType();

  public String getBinaryB64Data();
}
