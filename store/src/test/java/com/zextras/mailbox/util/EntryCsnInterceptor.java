package com.zextras.mailbox.util;

import com.unboundid.ldap.sdk.RDN;
import com.unboundid.ldap.sdk.ReadOnlyModifyDNRequest;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedModifyDNResult;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedAddResult;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedModifyResult;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchEntry;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.Control;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ReadOnlyEntry;
import com.unboundid.ldap.sdk.controls.PostReadResponseControl;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class EntryCsnInterceptor extends InMemoryOperationInterceptor {

  private static final String ENTRY_CSN = "entryCSN";
  private static final AtomicLong ENTRY_CSN_READS = new AtomicLong();

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
    if (modify.getResult().getResultCode() != ResultCode.SUCCESS) {
      return;
    }
    bump(modify.getRequest().getDN());
    LDAPResult result = modify.getResult();
    try {
      PostReadResponseControl postRead = PostReadResponseControl.get(result);
      if (postRead == null) {
        return;
      }
      Entry entry = postRead.getEntry().duplicate();
      entry.setAttribute(ENTRY_CSN, csn(versions.get(normalize(entry.getDN()))));
      List<Control> controls = new ArrayList<>();
      for (Control control : result.getResponseControls()) {
        if (!PostReadResponseControl.POST_READ_RESPONSE_OID.equals(control.getOID())) {
          controls.add(control);
        }
      }
      controls.add(new PostReadResponseControl(new ReadOnlyEntry(entry)));
      modify.setResult(
          new LDAPResult(
              result.getMessageID(),
              result.getResultCode(),
              result.getDiagnosticMessage(),
              result.getMatchedDN(),
              result.getReferralURLs(),
              controls.toArray(new Control[0])));
    } catch (LDAPException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void processModifyDNResult(InMemoryInterceptedModifyDNResult modifyDN) {
    if (modifyDN.getResult().getResultCode() != ResultCode.SUCCESS) {
      return;
    }
    try {
      ReadOnlyModifyDNRequest request = modifyDN.getRequest();
      DN oldDn = new DN(request.getDN());
      DN parent =
          request.getNewSuperiorDN() == null
              ? oldDn.getParent()
              : new DN(request.getNewSuperiorDN());
      bump(new DN(new RDN(request.getNewRDN()), parent).toString());
    } catch (LDAPException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void processSearchEntry(InMemoryInterceptedSearchEntry search) {
    if (!search.getRequest().getAttributeList().contains(ENTRY_CSN)) {
      return;
    }
    ENTRY_CSN_READS.incrementAndGet();
    Entry entry = search.getSearchEntry().duplicate();
    entry.setAttribute(ENTRY_CSN, csn(versions.getOrDefault(normalize(entry.getDN()), 0L)));
    search.setSearchEntry(entry);
  }

  public static long entryCsnReads() {
    return ENTRY_CSN_READS.get();
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
