// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.session.WaitSetMgr;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;

/*
  *************************************
  DestroyWaitSet: Use this to close out the wait set.  Note that the
  server will automatically time out a wait set if there is no reference
  to it for (default of) 10 minutes.
  *************************************
  <DestroyWaitSetRequest waitSet="setId"/>

  <DestroyWaitSetResponse waitSet="setId"/>
*/

/** */
public class DestroyWaitSet extends MailDocumentHandler {

  /* (non-Javadoc)
   * @see com.zimbra.soap.DocumentHandler#handle(com.zimbra.common.soap.Element, java.util.Map)
   */

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Element response = zsc.createElement(MailConstants.DESTROY_WAIT_SET_RESPONSE);
    return staticHandle(request, context, response);
  }

  public static Element staticHandle(Element request, Map<String, Object> context, Element response)
      throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    String waitSetId = request.getAttribute(MailConstants.A_WAITSET_ID);
    WaitSetMgr.destroy(zsc, zsc.getRequestedAccountId(), waitSetId);

    response.addAttribute(MailConstants.A_WAITSET_ID, waitSetId);
    return response;
  }
}
