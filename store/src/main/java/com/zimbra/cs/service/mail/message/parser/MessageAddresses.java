// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail.message.parser;

import com.zimbra.common.mime.shim.JavaMailInternetAddress;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.CharsetUtil;
import com.zimbra.cs.account.IDNUtil;
import com.zimbra.cs.service.mail.ToXML.EmailType;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.mail.internet.InternetAddress;

public final class MessageAddresses {

  private final HashMap<String, Object> addrs = new HashMap<>();

  public void add(Element elem, String defaultCharset)
      throws ServiceException, UnsupportedEncodingException {
    String emailAddress = IDNUtil.toAscii(elem.getAttribute(MailConstants.A_ADDRESS));
    String personalName = elem.getAttribute(MailConstants.A_PERSONAL, null);
    String addressType = elem.getAttribute(MailConstants.A_ADDRESS_TYPE);

    InternetAddress addr = new JavaMailInternetAddress(emailAddress, personalName,
        CharsetUtil.checkCharset(personalName, defaultCharset));

    Object content = addrs.get(addressType);
    if (content == null || addressType.equals(EmailType.FROM.toString()) ||
        addressType.equals(EmailType.SENDER.toString())) {
      addrs.put(addressType, addr);
    } else if (content instanceof List) {
      @SuppressWarnings("unchecked")
      List<InternetAddress> list = (List<InternetAddress>) content;
      list.add(addr);
    } else {
      List<InternetAddress> list = new ArrayList<>();
      list.add((InternetAddress) content);
      list.add(addr);
      addrs.put(addressType, list);
    }
  }

  @SuppressWarnings("unchecked")
  public InternetAddress[] get(String addressType) {
    Object content = addrs.get(addressType);
    if (content == null) {
      return null;
    } else if (content instanceof InternetAddress) {
      return new InternetAddress[]{(InternetAddress) content};
    } else {
      return ((List<InternetAddress>) content).toArray(new InternetAddress[0]);
    }
  }

  public boolean isEmpty() {
    return addrs.isEmpty();
  }
}
