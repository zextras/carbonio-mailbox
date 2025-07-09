package com.zimbra.cs.account;

import com.zimbra.common.cli.ExitCodeException;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.InvalidCommandException;
import java.io.IOException;
import org.apache.http.HttpException;

public interface CommandHandler {
  void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException, InvalidCommandException, ExitCodeException;
}
