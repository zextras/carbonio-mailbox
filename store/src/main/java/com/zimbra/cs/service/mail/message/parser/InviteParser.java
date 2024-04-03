// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service.mail.message.parser;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * Callback routine for parsing the <inv> element and building a iCal4j Calendar from it
 * <p>
 * We use a callback b/c there are differences in the parsing depending on the operation: Replying
 * to an invite is different than Creating or Modifying one, etc etc...
 */
public abstract class InviteParser {

  private InviteParserResult mResult;

  protected abstract InviteParserResult parseInviteElement(ZimbraSoapContext zsc,
      OperationContext octxt, Account account, Element invElement) throws ServiceException;

  public final InviteParserResult parse(ZimbraSoapContext zsc, OperationContext octxt,
      Account account, Element invElement) throws ServiceException {
    mResult = parseInviteElement(zsc, octxt, account, invElement);
    return mResult;
  }

  public InviteParserResult getResult() {
    return mResult;
  }
}
