package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.soap.SoapProvisioning;
import org.apache.http.HttpException;

import java.io.IOException;

public class SoapCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public SoapCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    // HACK FOR NOW
    SoapProvisioning sp = new SoapProvisioning();
    var serverPort = provUtil.getServerPort();
    sp.soapSetURI("https://localhost:" + serverPort + AdminConstants.ADMIN_SERVICE_URI);
    sp.soapZimbraAdminAuthenticate();
    provUtil.setProvisioning(sp);
  }
}
