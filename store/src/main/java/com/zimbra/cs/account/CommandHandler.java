package com.zimbra.cs.account;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.UsageException;
import com.zimbra.cs.account.ProvUtil.Exit1Exception;
import com.zimbra.cs.account.ProvUtil.Exit2Exception;
import org.apache.http.HttpException;

import java.io.IOException;

public interface CommandHandler {
  void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException, UsageException, Exit1Exception, Exit2Exception;
}
