// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest.prov.soap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.zimbra.common.account.ProvisioningConstants;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.soap.SoapProvisioning;
import com.zimbra.cs.httpclient.URLUtil;
import com.zimbra.qa.unittest.TestUtil;
import com.zimbra.soap.admin.message.GetAllServersRequest;
import com.zimbra.soap.admin.message.GetAllServersResponse;

public class TestSoapProvisioning {
    
    private static SoapProvTestUtil provUtil;
    private static Provisioning prov;
    private String ADMIN_NAME = "TestSoapProvisioningAdmin";
    private String ADMIN_PASS = TestUtil.DEFAULT_PASSWORD;
    private int LIFETIME = 5;

    @BeforeClass
    public static void init() throws Exception {
        provUtil = new SoapProvTestUtil();
        prov = provUtil.getProv();
    }

    @Before
    public void setUp() throws Exception {
        cleanup();
        Map<String, Object> attrs = Maps.newHashMap();
        attrs.put(Provisioning.A_zimbraIsAdminAccount, ProvisioningConstants.TRUE);
        attrs.put(Provisioning.A_zimbraAdminAuthTokenLifetime, String.valueOf(LIFETIME) + "s");
        TestUtil.createAccount(ADMIN_NAME, attrs);
    }

    @After
    public void tearDown() throws Exception {
        cleanup();
    }

    private void cleanup() throws Exception {
        if(TestUtil.accountExists(ADMIN_NAME)) {
            TestUtil.deleteAccount(ADMIN_NAME);
        }
    }

    @Test
    public void isExpired() throws Exception {
        long lifeTimeSecs = 5;  // 5 seconds
        
        SoapProvisioning soapProv = new SoapProvisioning();
        
        Server server = prov.getLocalServer();
        soapProv.soapSetURI(URLUtil.getAdminURL(server));
        
        assertTrue(soapProv.isExpired());
        
        soapProv.soapAdminAuthenticate(ADMIN_NAME, ADMIN_PASS);
        
        assertFalse(soapProv.isExpired());
        
        System.out.println("Waiting for " + lifeTimeSecs + " seconds");
        Thread.sleep((lifeTimeSecs+1)*1000);
        
        assertTrue(soapProv.isExpired());
    }

    @Test
    public void testInvokeJaxbAsAdminWithRetry() throws Exception {
        long lifeTimeSecs = 5;  // 5 seconds
        SoapProvisioning soapProv = new SoapProvisioning();
        Server server = prov.getLocalServer();
        soapProv.soapSetURI(URLUtil.getAdminURL(server));
        soapProv.soapZimbraAdminAuthenticate();
        assertFalse("SoapProvisioning should have a valid token after authenticating as cn=zimbra", soapProv.isExpired());
        GetAllServersRequest req = new GetAllServersRequest(Provisioning.SERVICE_MAILBOX, false);
        GetAllServersResponse resp = soapProv.invokeJaxbAsAdminWithRetry(req);
        assertNotNull("GetAllServersResponse should not be null when executed normally", resp);
        assertFalse(soapProv.isExpired());

        soapProv.soapAdminAuthenticate(ADMIN_NAME, ADMIN_PASS);
        assertFalse("SoapProvisioning should have a valid token after authenticating", soapProv.isExpired());
        req = new GetAllServersRequest(Provisioning.SERVICE_MAILBOX, false);
        resp = soapProv.invokeJaxbAsAdminWithRetry(req);
        assertNotNull("GetAllServersResponse should not be null when executed by an admin account", resp);
        assertFalse("SoapProvisioning should have a valid token before waiting", soapProv.isExpired());

        Thread.sleep((lifeTimeSecs+1)*1000);

        assertTrue("SoapProvisioning should have an expired token after waiting", soapProv.isExpired());
        resp = soapProv.invokeJaxbAsAdminWithRetry(req);
        assertNotNull("GetAllServersResponse should not be null after retrying", resp);
        assertFalse(soapProv.isExpired());
    }
}
