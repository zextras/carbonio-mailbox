package com.zimbra.cs.account;

import com.zimbra.common.cli.CommandExitException;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.InvalidCommandException;
import java.io.IOException;

public interface CommandHandler {
  void handle(String[] args) throws ServiceException, ArgException, IOException, InvalidCommandException, CommandExitException;
}
