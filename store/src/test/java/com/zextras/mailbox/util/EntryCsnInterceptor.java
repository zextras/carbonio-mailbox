package com.zextras.mailbox.util;

import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedAddResult;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedModifyResult;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchEntry;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

class EntryCsnInterceptor extends InMemoryOperationInterceptor {

  private static final String ENTRY_CSN = "entryCSN";

  private final AtomicLong sequence = new AtomicLong();
  private final Map<String, Long> versions = new ConcurrentHashMap<>();

  @Override
  public void processAddResult(InMemoryInterceptedAddResult add) {
    if (add.getResult().getResultCode() == ResultCode.SUCCESS) {
      bump(add.getRequest().getDN());
    }
  }

  @Override
  public void processModifyResult(InMemoryInterceptedModifyResult modify) {
    if (modify.getResult().getResultCode() == ResultCode.SUCCESS) {
      bump(modify.getRequest().getDN());
    }
  }

  @Override
  public void processSearchEntry(InMemoryInterceptedSearchEntry search) {
    if (!search.getRequest().getAttributeList().contains(ENTRY_CSN)) {
      return;
    }
    Entry entry = search.getSearchEntry().duplicate();
    entry.setAttribute(ENTRY_CSN, csn(versions.getOrDefault(normalize(entry.getDN()), 0L)));
    search.setSearchEntry(entry);
  }

  private void bump(String dn) {
    versions.put(normalize(dn), sequence.incrementAndGet());
  }

  private static String csn(long version) {
    return "20260101000000.000000Z#%06d#000#000000".formatted(version);
  }

  private static String normalize(String dn) {
    try {
      return DN.normalize(dn);
    } catch (LDAPException e) {
      return dn;
    }
  }
}
