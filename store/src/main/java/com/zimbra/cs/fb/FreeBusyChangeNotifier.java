// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.fb;

import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.fb.FreeBusyProvider.FreeBusySyncQueue;
import com.zimbra.cs.mailbox.MailItem.Type;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class FreeBusyChangeNotifier {

  public void mailboxChanged(String accountId, Set<Type> changedType) {
    for (FreeBusyProvider prov : FreeBusyProvider.sPROVIDERS)
      if (prov.registerForMailboxChanges(accountId)
          && !Collections.disjoint(changedType, prov.registerForItemTypes())) {
        FreeBusySyncQueue queue = FreeBusyProvider.sPUSHQUEUES.get(prov.getName());
        if (queue == null) queue = startConsumerThread(prov);
        synchronized (queue) {
          if (queue.contains(accountId)) continue;
          queue.addLast(accountId);
          try {
            queue.writeToDisk();
          } catch (IOException e) {
            ZimbraLog.fb.error("can't write to the queue " + queue.getFilename());
          }
          queue.notify();
        }
      }
  }

  public void mailboxChanged(String accountId) {
    mailboxChanged(accountId, EnumSet.of(Type.APPOINTMENT));
  }

  private FreeBusySyncQueue startConsumerThread(FreeBusyProvider p) {
    String name = p.getName();
    FreeBusySyncQueue queue = FreeBusyProvider.sPUSHQUEUES.get(name);
    if (queue != null) {
      ZimbraLog.fb.warn("free/busy provider " + name + " has been already registered.");
    }
    queue = new FreeBusySyncQueue(p);
    FreeBusyProvider.sPUSHQUEUES.put(name, queue);
    new Thread(queue).start();
    return queue;
  }
}
