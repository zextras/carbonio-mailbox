package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.DateUtil;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.AuthTokenException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.ldap.ZLdapElement;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class GetAuthTokenInfoCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetAuthTokenInfoCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doGetAuthTokenInfo(args);
  }

  private void doGetAuthTokenInfo(String[] args) {
    String authToken = args[1];

    var console = provUtil.getConsole();
    try {
      Map attrs = AuthToken.getInfo(authToken);
      List keys = new ArrayList(attrs.keySet());
      Collections.sort(keys);

      for (Object k : keys) {
        String key = k.toString();
        String value = attrs.get(k).toString();

        if ("exp".equals(key)) {
          long exp = Long.parseLong(value);
          console.print(String.format("%s: %s (%s)\n", key, value, DateUtil.toRFC822Date(new Date(exp))));
        } else {
          console.print(String.format("%s: %s\n", key, value));
        }
      }
    } catch (AuthTokenException e) {
      console.println("Unable to parse auth token: " + e.getMessage());
    }

    console.println();
  }


}
