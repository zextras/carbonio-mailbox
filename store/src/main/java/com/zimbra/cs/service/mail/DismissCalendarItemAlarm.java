// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.CalendarItem;
import com.zimbra.cs.mailbox.CalendarItem.AlarmData;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.MailServiceException.NoSuchItemException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.service.util.ItemId;
import com.zimbra.cs.service.util.ItemIdFormatter;
import com.zimbra.soap.DocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.Iterator;
import java.util.Map;

public class DismissCalendarItemAlarm extends DocumentHandler {

  /*
   * <DismissCalendarItemAlarmRequest>
   *   <appt|task id="cal item id" lastAlarm="alarm time in millis"/>+
   * </DismissCalendarItemAlarmRequest>
   *
   * <DismissCalendarItemAlarmResponse>
   *   <appt|task id="cal item id">
   *     <alarmData ...>  // so the client knows when to trigger the next alarm
   *   </appt|task>+
   * </DismissCalendarItemAlarmResponse>
   */

  private static final String[] sCalItemElems = {MailConstants.E_APPOINTMENT};

  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Mailbox mbox = getRequestedMailbox(zsc);
    String acctId = mbox.getAccountId();
    Account authAcct = getAuthenticatedAccount(zsc);
    OperationContext octxt = getOperationContext(zsc, context);

    Element response = getResponseElement(zsc);
    for (String calItemElement : sCalItemElems) {
      for (Iterator<Element> iter = request.elementIterator(calItemElement); iter.hasNext(); ) {
        Element calItemElem = iter.next();
        ItemId iid = new ItemId(calItemElem.getAttribute(MailConstants.A_ID), zsc);
        long dismissedAt = calItemElem.getAttributeLong(MailConstants.A_CAL_ALARM_DISMISSED_AT);

        // trace logging
        ZimbraLog.calendar.info(
            "<DismissCalendarItemAlarm> id=%s, at=%d", iid.toString(), dismissedAt);

        Mailbox ciMbox =
            null; // mailbox for this calendar item; not necessarily same as requested mailbox
        String ciAcctId = iid.getAccountId();
        if (ciAcctId == null || ciAcctId.equals(acctId)) {
          ciMbox = mbox;
        } else {
          throw MailServiceException.PERM_DENIED("cannot dismiss alarms of a shared calendar");
        }
        int calItemId = iid.getId();
        ItemIdFormatter ifmt = new ItemIdFormatter(authAcct.getId(), acctId, false);
        try {
          ciMbox.dismissCalendarItemAlarm(octxt, calItemId, dismissedAt);

          CalendarItem calItem = ciMbox.getCalendarItemById(octxt, calItemId);
          Element calItemRespElem = response.addElement(MailConstants.E_APPOINTMENT);
          calItemRespElem.addAttribute(MailConstants.A_CAL_ID, iid.toString(ifmt));

          boolean hidePrivate = !calItem.allowPrivateAccess(authAcct, zsc.isUsingAdminPrivileges());
          boolean showAll = !hidePrivate || calItem.isPublic();
          if (showAll) {
            AlarmData alarmData = calItem.getAlarmData();
            if (alarmData != null) ToXML.encodeAlarmData(calItemRespElem, calItem, alarmData);
          }
        } catch (NoSuchItemException nsie) {
          // item must not exist in db anymore.
          // this can happen if an alarm is dismissed while a big sync is happening which deletes
          // the item (e.g. bug 48560)
          // since item is not in db, it has effectively been dismissed; return ok and no further
          // alarms
          Element calItemRespElem = response.addElement(calItemElement);
          calItemRespElem.addAttribute(MailConstants.A_CAL_ID, iid.toString(ifmt));
        }
      }
    }
    return response;
  }
}
