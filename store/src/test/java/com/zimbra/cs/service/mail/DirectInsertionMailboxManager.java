// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.DeliveryOptions;
import com.zimbra.cs.mailbox.Flag;
import com.zimbra.cs.mailbox.MailSender;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.Mailbox.MailboxData;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mime.ParsedMessage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class DirectInsertionMailboxManager extends MailboxManager {

  public DirectInsertionMailboxManager() throws ServiceException {
    super();
  }

  @Override
  protected Mailbox instantiateMailbox(MailboxData data) {
    return new Mailbox(data) {
      @Override
      public MailSender getMailSender() {
        return new MailSender() {
          @Override
          protected Collection<Address> sendMessage(Mailbox mbox, MimeMessage mm,
              Collection<RollbackData> rollbacks) {
            List<Address> successes = new ArrayList<Address>();
            Address[] addresses;
            try {
              addresses = getRecipients(mm);
            } catch (Exception e) {
              addresses = new Address[0];
            }
            DeliveryOptions dopt = new DeliveryOptions().setFolderId(Mailbox.ID_FOLDER_INBOX)
                .setFlags(
                    Flag.BITMASK_UNREAD);
            for (Address addr : addresses) {
              try {
                Account acct = Provisioning.getInstance()
                    .getAccountByName(((InternetAddress) addr).getAddress());
                if (acct != null) {
                  Mailbox target = MailboxManager.getInstance().getMailboxByAccount(acct);
                  target.addMessage(null, new ParsedMessage(mm, false), dopt, null);
                  successes.add(addr);
                }
              } catch (Exception e) {
                e.printStackTrace(System.out);
              }
            }
            if (successes.isEmpty() && !isSendPartial()) {
              for (RollbackData rdata : rollbacks) {
                if (rdata != null) {
                  rdata.rollback();
                }
              }
            }
            return successes;
          }
        };
      }
    };
  }
}
