// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.dav;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.zimbra.cs.dav.resource.DavResource;
import com.zimbra.cs.dav.resource.RemoteCalendarCollection;
import com.zimbra.cs.dav.resource.UrlNamespace;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailItem.Type;
import com.zimbra.cs.mailbox.Mountpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

public class UrlNamespaceTest {
  private DavContext ctxt;
  private Mountpoint item;
  private RemoteCalendarCollection rcc;

  /**
   * @throws java.lang.Exception
   */
  @BeforeEach
  public void setUp() throws Exception {
    ctxt = mock(DavContext.class);
    item = mock(Mountpoint.class);
    rcc = mock(RemoteCalendarCollection.class);
  }

  /**
   * @throws java.lang.Exception
   */
  @Test
  void testGetResourceFromMailItem() throws Exception {
    when(ctxt.useIcalDelegation()).thenReturn(Boolean.FALSE);
    when(item.getType()).thenReturn(MailItem.Type.MOUNTPOINT);
    when(item.getDefaultView()).thenReturn(Type.APPOINTMENT);
    try (MockedStatic<UrlNamespace> urlNamespaceMockedStatic = mockStatic(UrlNamespace.class)) {
      urlNamespaceMockedStatic
          .when(() -> UrlNamespace.getRemoteCalendarCollection(ctxt, item))
          .thenReturn(rcc);
      urlNamespaceMockedStatic
          .when(() -> UrlNamespace.getResourceFromMailItem(ctxt, item))
          .thenCallRealMethod();
      DavResource resource = UrlNamespace.getResourceFromMailItem(ctxt, item);
      assertTrue(resource instanceof RemoteCalendarCollection);
    }
  }
}
