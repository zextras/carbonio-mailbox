package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.mail.message.DeleteCalendarGroupRequest;

import java.util.Map;

public class DeleteCalendarGroup extends MailDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    final var zsc = getZimbraSoapContext(context);
    final var account = getRequestedAccount(zsc);

    if (!canAccessAccount(zsc, account))
      throw ServiceException.PERM_DENIED("can not access account");

    final var mbox = getRequestedMailbox(zsc);
    final var octxt = getOperationContext(zsc, context);

    DeleteCalendarGroupRequest req = zsc.elementToJaxb(request);

    mbox.deleteFolder(octxt, req.getId());

    return zsc.createElement(MailConstants.DELETE_CALENDAR_GROUP_RESPONSE);
  }

}
