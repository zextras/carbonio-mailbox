// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import org.apache.jsieve.Arguments;
import org.apache.jsieve.Block;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.mail.MailAdapter;

/**
 * Class Keep implements the explicit Keep Command which is explicitly specified in the sieve
 * script.
 */
public class Keep extends org.apache.jsieve.commands.Keep {

  @Override
  protected Object executeBasic(
      MailAdapter mail, Arguments arguments, Block block, SieveContext context)
      throws SieveException {
    mail.addAction(new ActionExplicitKeep());
    return null;
  }
}
