// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.google.common.base.Joiner;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;

/**
 * @since Jul 8, 2004
 * @author dkarp
 */
public class MsgAction extends ItemAction {

  @Override
  public Element handle(Element request, Map<String, Object> context)
      throws ServiceException, SoapFaultException {
    ZimbraSoapContext lc = getZimbraSoapContext(context);

    Element action = request.getElement(MailConstants.E_ACTION);
    String operation = action.getAttribute(MailConstants.A_OPERATION).toLowerCase();

    String successes =
        Joiner.on(",").join(handleCommon(context, request, MailItem.Type.MESSAGE).getSuccessIds());

    Element response = lc.createElement(MailConstants.MSG_ACTION_RESPONSE);
    Element act = response.addUniqueElement(MailConstants.E_ACTION);
    act.addAttribute(MailConstants.A_ID, successes);
    act.addAttribute(MailConstants.A_OPERATION, operation);
    return response;
  }
}
