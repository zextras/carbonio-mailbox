package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Map;

public class GetCalendarGroups extends MailDocumentHandler {
  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Element response = zsc.createElement(MailConstants.GET_CALENDAR_GROUPS_RESPONSE);
    return response;
  }
}
