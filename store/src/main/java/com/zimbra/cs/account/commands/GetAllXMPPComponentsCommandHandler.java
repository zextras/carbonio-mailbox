package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.XMPPComponent;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.List;

class GetAllXMPPComponentsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;
  private final ProvUtilDumper dumper;

  public GetAllXMPPComponentsCommandHandler(ProvUtil provUtil, ProvUtilDumper dumper) {
    this.provUtil = provUtil;
    this.dumper = dumper;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doGetAllXMPPComponents();
  }

  private void doGetAllXMPPComponents() throws ServiceException {
    List<XMPPComponent> components = provUtil.getProvisioning().getAllXMPPComponents();
    for (XMPPComponent comp : components) {
      dumper.dumpXMPPComponent(comp, null);
    }
  }
}
