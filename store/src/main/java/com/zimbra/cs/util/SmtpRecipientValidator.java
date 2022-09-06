// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.Key.AccountBy;
import com.zimbra.common.lmtp.SmtpToLmtp;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.DynamicGroup;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.Provisioning;
import java.util.Arrays;
import java.util.Collections;

/** Validates recipients and expands distribution lists for the dev SMTP server. */
public class SmtpRecipientValidator implements SmtpToLmtp.RecipientValidator {

  private static final Log log = LogFactory.getLog(SmtpRecipientValidator.class);

  @Override
  public Iterable<String> validate(String recipient) {
    try {
      Provisioning prov = Provisioning.getInstance();
      Account account = prov.get(AccountBy.name, recipient);
      if (account != null) {
        return Arrays.asList(account.getName());
      } else {
        Group group = prov.getGroup(Key.DistributionListBy.name, recipient);
        if (group != null) {
          String[] members;
          if (group instanceof DynamicGroup) {
            members = ((DynamicGroup) group).getAllMembers(true);
          } else {
            members = group.getAllMembers();
          }
          return Arrays.asList(members);
        }
      }
    } catch (ServiceException e) {
      log.error("Unable to validate recipient %s", recipient, e);
    }
    return Collections.emptyList();
  }
}
