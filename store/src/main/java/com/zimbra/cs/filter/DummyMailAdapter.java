// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

/*
 * Created on Apr 11, 2005
 *
 */
package com.zimbra.cs.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import org.apache.jsieve.SieveContext;
import org.apache.jsieve.exception.SieveException;
import org.apache.jsieve.mail.Action;
import org.apache.jsieve.mail.MailAdapter;
import org.apache.jsieve.mail.SieveMailException;

public class DummyMailAdapter implements MailAdapter {

  private List mHeaders = new ArrayList(1);
  private List mActions = new ArrayList(1);

  public void setContext(SieveContext context) {}

  public List getActions() {
    return mActions;
  }

  public ListIterator getActionsIterator() {
    return mActions.listIterator();
  }

  public List getHeader(String name) throws SieveMailException {
    return Collections.EMPTY_LIST;
  }

  public List getMatchingHeader(String name) throws SieveMailException {
    return mHeaders;
  }

  public List getHeaderNames() throws SieveMailException {
    return Collections.EMPTY_LIST;
  }

  public void addAction(Action action) {}

  public void executeActions() throws SieveException {}

  public int getSize() throws SieveMailException {
    return 0;
  }

  public Object getContent() {
    return "";
  }

  public String getContentType() {
    return "text/plain";
  }

  public boolean isInBodyText(String phraseCaseInsensitive) throws SieveMailException {
    return false;
  }

  public Address[] parseAddresses(String headerName) {
    return FilterAddress.EMPTY_ADDRESS_ARRAY;
  }
}
