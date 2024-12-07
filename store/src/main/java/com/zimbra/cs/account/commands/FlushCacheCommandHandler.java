package com.zimbra.cs.account.commands;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.soap.SoapProvisioning;

public class FlushCacheCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public FlushCacheCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    doFlushCache(args);
  }

  private void doFlushCache(String[] args) throws ServiceException {
    if (!(provUtil.getProvisioning() instanceof SoapProvisioning)) {
      provUtil.throwSoapOnly();
    }

    boolean allServers = false;

    int argIdx = 1;
    if (args[argIdx].equals("-a")) {
      allServers = true;
      if (args.length > 2) {
        argIdx++;
      } else {
        provUtil.usage();
        return;
      }
    }
    String type = args[argIdx++];

    Provisioning.CacheEntry[] entries = null;

    if (args.length > argIdx) {
      entries = new Provisioning.CacheEntry[args.length - argIdx];
      for (int i = argIdx; i < args.length; i++) {
        Key.CacheEntryBy entryBy;
        if (Provisioning.isUUID(args[i])) {
          entryBy = Key.CacheEntryBy.id;
        } else {
          entryBy = Key.CacheEntryBy.name;
        }
        entries[i - argIdx] = new Provisioning.CacheEntry(entryBy, args[i]);
      }
    }

    SoapProvisioning sp = (SoapProvisioning) provUtil.getProvisioning();
    sp.flushCache(type, entries, allServers);
  }

}
