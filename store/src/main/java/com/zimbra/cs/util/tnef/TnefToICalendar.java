// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.tnef;

import com.zimbra.common.service.ServiceException;
import java.io.InputStream;
import javax.mail.internet.MimeMessage;
import net.fortuna.ical4j.data.ContentHandler;

public interface TnefToICalendar {

  /**
   * @param mimeMsg is the entire MIME message containing the TNEF winmail.dat attachment
   * @param tnefInput is an InputStream to the TNEF winmail.dat data. It's not the entire MIME
   *     message.
   * @param icalOutput is the ical generator object.
   * @return true if the TNEF represented a Scheduling or Task related object that was converted
   *     successfully to ICAL in <code>icalOutput</code>
   * @throws ServiceException
   */
  public boolean convert(MimeMessage mimeMsg, InputStream tnefInput, ContentHandler icalOutput)
      throws ServiceException;
}
