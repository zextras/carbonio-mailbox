// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.soap.DocumentHandler;
import java.util.Map;

public class SendMsgWithSmartLinks extends DocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    return null;
  }
}
