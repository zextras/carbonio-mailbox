// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import com.zimbra.common.soap.Element;
import com.zimbra.cs.service.formatter.FormatterFactory.FormatType;

public class JsonFormatter extends XmlFormatter {

  @Override
  public FormatType getType() {
    return FormatType.JSON;
  }

  @Override
  Element.ElementFactory getFactory() {
    return Element.JSONElement.mFactory;
  }
}
