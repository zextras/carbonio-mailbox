// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactory;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.zimbra.common.util.Constants;


/**
 * @author zimbra
 *
 */
public class AutoDiscoverServletTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Test method for {@link com.zimbra.cs.service.AutoDiscoverServlet#isEwsClient(java.lang.String)}.
     */
    @Test
    public void testIsEwsClient() {
        // Active Sync
        boolean result = AutoDiscoverServlet.isEwsClient("http://schemas.microsoft.com/exchange/autodiscover/mobilesync/responseschema/2006");
        Assert.assertTrue(!result);

        // Ews CLient
        result = AutoDiscoverServlet.isEwsClient("http://schemas.microsoft.com/exchange/autodiscover/outlook/responseschema/2006a");
        Assert.assertTrue(result);
    }

    @Test
    public void testMakeDocumentBuilderFactory() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = AutoDiscoverServlet.makeDocumentBuilderFactory();
        Assert.assertTrue(dbf.isNamespaceAware());
        Assert.assertTrue(dbf.getFeature(Constants.DISALLOW_DOCTYPE_DECL));
        Assert.assertFalse(dbf.getFeature(Constants.EXTERNAL_GENERAL_ENTITIES));
        Assert.assertFalse(dbf.getFeature(Constants.EXTERNAL_PARAMETER_ENTITIES));
        Assert.assertFalse(dbf.getFeature(Constants.LOAD_EXTERNAL_DTD));
    }

    @Test
    public void testMakeTransformerFactory() {
        TransformerFactory factory = AutoDiscoverServlet.makeTransformerFactory();
        Assert.assertEquals("", factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_DTD));
        Assert.assertEquals("", factory.getAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET));
    }
}
