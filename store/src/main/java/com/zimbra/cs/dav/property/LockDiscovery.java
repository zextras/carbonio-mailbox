// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav.property;

import com.zimbra.cs.dav.DavContext;
import com.zimbra.cs.dav.DavElements;
import com.zimbra.cs.dav.LockMgr;
import com.zimbra.cs.dav.LockMgr.Lock;
import java.util.HashSet;
import org.dom4j.Element;

/**
 * RFC 2518bis section 15.8
 *
 * @author jylee
 */
public class LockDiscovery extends ResourceProperty {

  private HashSet<LockMgr.Lock> mLocks;

  public LockDiscovery() {
    super(DavElements.E_LOCKDISCOVERY);
    setProtected(true);
    mLocks = new HashSet<LockMgr.Lock>();
  }

  public LockDiscovery(LockMgr.Lock lock) {
    this();
    addLock(lock);
  }

  public Element toElement(DavContext ctxt, Element parent, boolean nameOnly) {
    Element activelock = super.toElement(ctxt, parent, true);
    if (nameOnly) return activelock;

    for (Lock l : mLocks) {
      Element lock = activelock.addElement(DavElements.E_ACTIVELOCK);
      Element el = lock.addElement(DavElements.E_LOCKTYPE);
      switch (l.type) {
        case write:
          el.addElement(DavElements.E_WRITE);
      }

      el = lock.addElement(DavElements.E_LOCKSCOPE);
      switch (l.scope) {
        case shared:
          el.addElement(DavElements.E_SHARED);
          break;
        case exclusive:
          el.addElement(DavElements.E_EXCLUSIVE);
          break;
      }

      lock.addElement(DavElements.E_DEPTH).setText(l.depth);
      lock.addElement(DavElements.E_TIMEOUT).setText(l.getTimeoutStr());
      if (l.owner != null)
        lock.addElement(DavElements.E_OWNER).addElement(DavElements.E_HREF).setText(l.owner);
      lock.addElement(DavElements.E_LOCKTOKEN).addElement(DavElements.E_HREF).setText(l.token);
    }
    return activelock;
  }

  public void addLock(LockMgr.Lock l) {
    mLocks.add(l);
  }
}
