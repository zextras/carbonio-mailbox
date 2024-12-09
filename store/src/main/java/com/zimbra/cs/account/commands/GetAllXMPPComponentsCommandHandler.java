package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.XMPPComponent;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.List;

public class GetAllXMPPComponentsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetAllXMPPComponentsCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doGetAllXMPPComponents();
  }

  private void doGetAllXMPPComponents() throws ServiceException {
    List<XMPPComponent> components = provUtil.getProvisioning().getAllXMPPComponents();
    for (XMPPComponent comp : components) {
      provUtil.dumpXMPPComponent(comp, null);
    }
  }
}
