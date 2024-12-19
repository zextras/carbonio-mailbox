package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.Category;
import com.zimbra.cs.account.Command;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import org.apache.http.HttpException;

import java.io.IOException;

class HelpCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public HelpCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) {
    doHelp(args);
  }

  private void doHelp(String[] args) {
    Category cat = null;
    if (args != null && args.length >= 2) {
      String s = args[1].toUpperCase();
      try {
        cat = Category.valueOf(s);
      } catch (IllegalArgumentException e) {
        for (Category c : Category.values()) {
          if (c.name().startsWith(s)) {
            cat = c;
            break;
          }
        }
      }
    }

    var console = provUtil.getConsole();
    if (args == null || args.length == 1 || cat == null) {
      console.println(" zmprov is used for provisioning. Try:");
      console.println("");
      for (Category c : Category.values()) {
        console.print(String.format("     zmprov help %-15s %s%n", c.name().toLowerCase(), c.getDescription()));
      }
    }

    if (cat != null) {
      console.println("");
      for (Command c : Command.values()) {
        if (!c.hasHelp()) {
          continue;
        }
        if (cat == Category.COMMANDS || cat == c.getCategory()) {
          Command.Via via = c.getVia();
          console.print(String.format("  %s(%s) %s%n", c.getName(), c.getAlias(), c.getHelp()));
          if (via == Command.Via.ldap) {
            console.print(String.format(
                    "    -- NOTE: %s can only be used with \"zmprov -l/--ldap\"%n", c.getName()));
          }
          console.println();
        }
      }

      console.println(provUtil.helpCategory(cat));
    }
    console.println();
  }
}
