// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest.prov.ldap;

import java.util.List;

import org.junit.*;
import static org.junit.Assert.*;

import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mime.MimeTypeInfo;
import com.zimbra.cs.mime.handler.UnknownTypeHandler;

public class TestLdapProvMimeType extends LdapTest {
    private static Provisioning prov;
    
    @BeforeClass
    public static void init() throws Exception {
        prov = new LdapProvTestUtil().getProv();
    }
    
    @Test
    public void getMimeTypes() throws Exception {
        String MIME_TYPE = "all";
        List<MimeTypeInfo> mimeTypes = prov.getMimeTypes(MIME_TYPE);
        assertEquals(1, mimeTypes.size());
        assertEquals(UnknownTypeHandler.class.getSimpleName(), mimeTypes.get(0).getHandlerClass());
    }
    
    @Test
    public void getAllMimeTypes() throws Exception {
        List<MimeTypeInfo> allMimeType = prov.getAllMimeTypes();
        assertEquals(6, allMimeType.size()); // mime types installed by r-t-w
    }
    
}
