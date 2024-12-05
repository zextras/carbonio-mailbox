package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Signature;

import java.util.Map;
import java.util.Set;

public class GetSignaturesCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetSignaturesCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    doGetAccountSignatures(args);
  }

  private void doGetAccountSignatures(String[] args) throws ServiceException {
    Account account = provUtil.lookupAccount(args[1]);
    Set<String> argNameSet = provUtil.getArgNameSet(args, 2);
    for (Signature signature : provUtil.getProvisioning().getAllSignatures(account)) {
      dumpSignature(signature, argNameSet);
    }
  }

  public void dumpSignature(Signature signature, Set<String> attrNameSet) throws ServiceException {
    var console = provUtil.getConsole();
    console.println("# name " + signature.getName());
    Map<String, Object> attrs = signature.getAttrs();
    provUtil.dumpAttrs(attrs, attrNameSet);
    console.println();
  }
}
