// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.qa.unittest.prov.soap;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.common.soap.SoapTransport;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.account.message.CreateSignatureRequest;
import com.zimbra.soap.account.type.Signature;


/**
 * @author zimbra
 *
 */
public class TestCsrfRequest extends SoapTest {
    private static SoapProvTestUtil provUtil;
    private static Provisioning prov;
    private static Domain domain;

    @BeforeClass
    public static void init() throws Exception {
        provUtil = new SoapProvTestUtil();
        prov = provUtil.getProv();
        domain = provUtil.createDomain(baseDomainName());
    }

    @AfterClass
    public static void cleanup() throws Exception {
        Cleanup.deleteAll(baseDomainName());
    }

    @Test
    public void getCreateSigWithAuthAndCsrfDisabled() throws Exception {
        Account acct = provUtil.createAccount(genAcctNameLocalPart(), domain);
        boolean csrfEnabled = Boolean.FALSE;
        SoapTransport transport = authUser(acct.getName(), csrfEnabled, Boolean.FALSE);


        String sigContent = "xss&lt;script&gt;alert(\"XSS\")&lt;/script&gt;&lt;a href=javascript:alert(\"XSS\")&gt;&lt;";

        Signature sig = new Signature(null, "testSig", sigContent, "text/html");
        CreateSignatureRequest req = new CreateSignatureRequest(sig);
        SoapProtocol proto = SoapProtocol.Soap12;
        Element sigReq = JaxbUtil.jaxbToElement(req, proto.getFactory());

        try {
            Element element = transport.invoke(sigReq, false, false, null);
            String sigt = element.getElement("signature").getAttribute("id");
            assertNotNull(sigt);
        } catch (SoapFaultException e) {
            e.printStackTrace();
            assertNull(e);

        }
    }


    @Test
    public void getCreateSigWithAuthAndCsrfEnabledNoCsrfToken() throws Exception {
        Account acct = provUtil.createAccount(genAcctNameLocalPart(), domain);
        boolean csrfEnabled = Boolean.TRUE;
        SoapTransport transport = authUser(acct.getName(), csrfEnabled, Boolean.FALSE);


        String sigContent = "xss&lt;script&gt;alert(\"XSS\")&lt;/script&gt;&lt;a href=javascript:alert(\"XSS\")&gt;&lt;";

        Signature sig = new Signature(null, "testSig", sigContent, "text/html");
        CreateSignatureRequest req = new CreateSignatureRequest(sig);
        SoapProtocol proto = SoapProtocol.Soap12;
        Element sigReq = JaxbUtil.jaxbToElement(req, proto.getFactory());

        try {
            Element element = transport.invoke(sigReq, false, false, null);

        } catch (SoapFaultException e) {
            assertNotNull(e);
            Assert.assertEquals(true, e.getCode().contains("AUTH_REQUIRED"));

        }
    }

    @Test
    public void getCreateSigWithAuthAndCsrfEnabledAndCsrfToken() throws Exception {
        Account acct = provUtil.createAccount(genAcctNameLocalPart(), domain);
        boolean csrfEnabled = Boolean.TRUE;
        SoapTransport transport = authUser(acct.getName(), csrfEnabled, Boolean.TRUE);


        String sigContent = "xss&lt;script&gt;alert(\"XSS\")&lt;/script&gt;&lt;a href=javascript:alert(\"XSS\")&gt;&lt;";

        Signature sig = new Signature(null, "testSig", sigContent, "text/html");
        CreateSignatureRequest req = new CreateSignatureRequest(sig);
        SoapProtocol proto = SoapProtocol.Soap12;
        Element sigReq = JaxbUtil.jaxbToElement(req, proto.getFactory());

        try {
            Element element = transport.invoke(sigReq, false, false, null);
            String sigt = element.getElement("signature").getAttribute("id");
            assertNotNull(sigt);
        } catch (SoapFaultException e) {
            assertNull(e);
        }
    }


    @Test
    public void getCreateSigWithAuthAndCsrfEnabledAndInvalidCsrfToken() throws Exception {
        Map<String, Object>attrs = new HashMap<String, Object>();
        attrs.put(Provisioning.A_zimbraCsrfTokenCheckEnabled, "TRUE");
        prov.modifyAttrs(prov.getConfig(), attrs, true);
        Account acct = provUtil.createAccount(genAcctNameLocalPart(), domain);
        boolean csrfEnabled = Boolean.TRUE;
        SoapTransport transport = authUser(acct.getName(), csrfEnabled, Boolean.TRUE);
        String temp =  transport.getCsrfToken().substring(7);
        transport.setCsrfToken(temp);



        String sigContent = "xss&lt;script&gt;alert(\"XSS\")&lt;/script&gt;&lt;a href=javascript:alert(\"XSS\")&gt;&lt;";

        Signature sig = new Signature(null, "testSig", sigContent, "text/html");
        CreateSignatureRequest req = new CreateSignatureRequest(sig);
        SoapProtocol proto = SoapProtocol.Soap12;
        Element sigReq = JaxbUtil.jaxbToElement(req, proto.getFactory());

        try {
            Element element = transport.invoke(sigReq, false, false, null);
            String sigt = element.getElement("signature").getAttribute("id");
            assertNull(sigt);
        } catch (SoapFaultException e) {
            assertNotNull(e);
            Assert.assertEquals(true, e.getCode().contains("AUTH_REQUIRED"));
        }
    }


    @Test
    public void getCreateSigWithCsrfFeatureDisbaledAndAuthTokenIsCsrfEnabled() throws Exception {

        Map<String, Object>attrs = new HashMap<String, Object>();
        attrs.put(Provisioning.A_zimbraCsrfTokenCheckEnabled, "FALSE");
        prov.modifyAttrs(prov.getConfig(), attrs, true);
        Account acct = provUtil.createAccount(genAcctNameLocalPart(), domain);

        boolean csrfEnabled = Boolean.TRUE;
        SoapTransport transport = authUser(acct.getName(), csrfEnabled, Boolean.FALSE);


        String sigContent = "xss&lt;script&gt;alert(\"XSS\")&lt;/script&gt;&lt;a href=javascript:alert(\"XSS\")&gt;&lt;";

        Signature sig = new Signature(null, "testSig", sigContent, "text/html");
        CreateSignatureRequest req = new CreateSignatureRequest(sig);
        SoapProtocol proto = SoapProtocol.Soap12;
        Element sigReq = JaxbUtil.jaxbToElement(req, proto.getFactory());

        try {
            Element element = transport.invoke(sigReq, false, false, null);
            String sigt = element.getElement("signature").getAttribute("id");
            assertNotNull(sigt);
        } catch (SoapFaultException e) {
            e.printStackTrace();
            assertNull(e);

        }
    }

}

