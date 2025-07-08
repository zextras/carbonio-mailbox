package com.zimbra.cs.account;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.UsageException;
import org.apache.http.HttpException;

import java.io.IOException;

public interface CommandHandler {
  void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException, UsageException;
}
