// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.service.UserServletContext;
import com.zimbra.cs.service.UserServletException;
import com.zimbra.cs.service.formatter.FormatterFactory.FormatType;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Set;
import javax.servlet.ServletException;

public class FreeBusyFormatter extends Formatter {

  private static final String ATTR_FREEBUSY = "zimbra_freebusy";

  @Override
  public FormatType getType() {
    return FormatType.FREE_BUSY;
  }

  @Override
  public boolean requiresAuth() {
    return false;
  }

  @Override
  public Set<MailItem.Type> getDefaultSearchTypes() {
    return EnumSet.of(MailItem.Type.APPOINTMENT);
  }

  @Override
  public void formatCallback(UserServletContext context)
      throws IOException, ServiceException, UserServletException, ServletException {
    context.req.setAttribute(ATTR_FREEBUSY, "true");
    HtmlFormatter.dispatchJspRest(context.getServlet(), context);
  }
}
