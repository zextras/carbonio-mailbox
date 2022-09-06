// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.base;

public interface CertSubjectAttrs {
  public String getC();

  public String getSt();

  public String getL();

  public String getO();

  public String getOu();

  public String getCn();
}
