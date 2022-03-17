// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.dav;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.dav.resource.DavResource;
import com.zimbra.cs.dav.resource.RemoteCalendarCollection;
import com.zimbra.cs.dav.resource.UrlNamespace;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Mountpoint;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class UrlNamespaceTest {
    private DavContext ctxt;
    private Mountpoint item;
    private RemoteCalendarCollection rcc;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        ctxt = Mockito.mock(DavContext.class);
        item = Mockito.mock(Mountpoint.class);
        rcc = Mockito.mock(RemoteCalendarCollection.class);
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void testGetResourceFromMailItem() throws Exception {
        Mockito.when(ctxt.useIcalDelegation()).thenReturn(Boolean.FALSE);
        Mockito.when(item.getType()).thenReturn(MailItem.Type.MOUNTPOINT);
        Mockito.when(item.getDefaultView()).thenReturn(MailItem.Type.TASK);
        try (MockedStatic<UrlNamespace> urlNamespaceMockedStatic = Mockito.mockStatic(UrlNamespace.class)) {
            urlNamespaceMockedStatic.when(() -> UrlNamespace.getRemoteCalendarCollection(ctxt, item))
                .thenReturn(rcc);
            DavResource resource = UrlNamespace.getResourceFromMailItem(ctxt, item);
            Assert.assertTrue(resource instanceof RemoteCalendarCollection);
        }
    }
}
