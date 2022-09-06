// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import static com.zimbra.cs.filter.JsieveConfigMapHandler.CAPABILITY_EDITHEADER;
import static org.apache.jsieve.Constants.COMPARATOR_PREFIX;
import static org.apache.jsieve.Constants.COMPARATOR_PREFIX_LENGTH;

import com.zimbra.cs.filter.RuleManager;
import com.zimbra.cs.filter.ZimbraMailAdapter;
import java.util.List;
import org.apache.jsieve.Arguments;
import org.apache.jsieve.Block;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.StringListArgument;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.exception.SyntaxException;
import org.apache.jsieve.mail.MailAdapter;

/** Class Require implements the Require control as defined in RFC 5228, section 3.2. */
public class Require extends org.apache.jsieve.commands.Require {

  @Override
  protected Object executeBasic(
      MailAdapter mail, Arguments arguments, Block block, SieveContext context)
      throws SieveException {
    if (!(mail instanceof ZimbraMailAdapter)) {
      return null;
    }
    ZimbraMailAdapter mailAdapter = (ZimbraMailAdapter) mail;

    final List<String> stringArgumentList =
        ((StringListArgument) arguments.getArgumentList().get(0)).getList();
    for (String stringArgument : stringArgumentList) {
      validateFeature(stringArgument, mail, context);
      if (CAPABILITY_EDITHEADER.equals(getCapabilityString(stringArgument))
          && mailAdapter.isUserScriptExecuting()) {
        throw new SieveException(RuleManager.editHeaderUserScriptError);
      }
      mailAdapter.addCapabilities(getCapabilityString(stringArgument));
    }
    return null;
  }

  private String getCapabilityString(String name) {
    if (name.startsWith(COMPARATOR_PREFIX)) {
      return name.substring(COMPARATOR_PREFIX_LENGTH);
    } else {
      return name;
    }
  }

  public static void checkCapability(MailAdapter mail, String capability) throws SyntaxException {
    if (!(mail instanceof ZimbraMailAdapter)) {
      return;
    }
    ZimbraMailAdapter zma = (ZimbraMailAdapter) mail;
    if (!Require.isSieveRequireControlRFCCompliant(zma)) {
      return;
    }

    if (!zma.isCapable(capability)) {
      throw new SyntaxException("Undeclared extension (" + capability + ")");
    }
  }

  public static boolean isSieveRequireControlRFCCompliant(MailAdapter mail) {
    if (!(mail instanceof ZimbraMailAdapter)) {
      return true;
    }
    ZimbraMailAdapter mailAdapter = (ZimbraMailAdapter) mail;
    return mailAdapter.getAccount().isSieveRequireControlEnabled();
  }
}
