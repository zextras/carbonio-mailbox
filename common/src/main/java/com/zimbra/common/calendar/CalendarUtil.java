// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.calendar;

import com.zimbra.common.calendar.ZCalendar.ZParameter;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Common calendar utilities. */
public class CalendarUtil {

  public static List<ZParameter> parseXParams(Element element) throws ServiceException {
    List<ZParameter> params = new ArrayList<ZParameter>();
    for (Iterator<Element> paramIter = element.elementIterator(MailConstants.E_CAL_XPARAM);
        paramIter.hasNext(); ) {
      Element paramElem = paramIter.next();
      String paramName = paramElem.getAttribute(MailConstants.A_NAME);
      String paramValue = paramElem.getAttribute(MailConstants.A_VALUE, null);
      ZParameter xparam = new ZParameter(paramName, paramValue);
      params.add(xparam);
    }
    return params;
  }

  /** Use JAXB e.g. {@link com.zimbra.soap.mail.type.XParam} where possible instead of using this */
  public static void encodeXParams(Element parent, Iterator<ZParameter> xparamsIterator) {
    while (xparamsIterator.hasNext()) {
      ZParameter xparam = xparamsIterator.next();
      String paramName = xparam.getName();
      if (paramName == null) continue;
      Element paramElem = parent.addElement(MailConstants.E_CAL_XPARAM);
      paramElem.addAttribute(MailConstants.A_NAME, paramName);
      String paramValue = xparam.getValue();
      if (paramValue != null) paramElem.addAttribute(MailConstants.A_VALUE, paramValue);
    }
  }
}
