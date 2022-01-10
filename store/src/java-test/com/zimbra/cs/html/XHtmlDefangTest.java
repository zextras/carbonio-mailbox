package com.zimbra.cs.html;

// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Test;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import com.zimbra.common.soap.XmlParseException;
import com.zimbra.common.util.Constants;

import junit.framework.Assert;

public class XHtmlDefangTest {
    @Test
    public void testMakeSAXParserFactory() throws XmlParseException, SAXNotRecognizedException, SAXNotSupportedException, ParserConfigurationException {
        SAXParserFactory sf = XHtmlDefang.makeSAXParserFactory();
        Assert.assertTrue(sf.getFeature(XMLConstants.FEATURE_SECURE_PROCESSING));
        Assert.assertTrue(sf.getFeature(Constants.DISALLOW_DOCTYPE_DECL));
        Assert.assertFalse(sf.getFeature(Constants.EXTERNAL_GENERAL_ENTITIES));
        Assert.assertFalse(sf.getFeature(Constants.EXTERNAL_PARAMETER_ENTITIES));
        Assert.assertFalse(sf.getFeature(Constants.LOAD_EXTERNAL_DTD));
    }

    @Test
    public void testDefang() {
        XHtmlDefang defang = new XHtmlDefang();
        String text = "<?xml version='1.0'\n" +
            "encoding='UTF-8'?><svg xmlns=\"http://www.w3.org/2000/svg\"\n" +
            "onload=\"alert('XSS in the attacchment')\"></svg>";
        StringReader reader = new StringReader(text);

        try {
            String sanitizedText = defang.defang(reader, true);
            Assert.assertTrue("Does not contain onload attribute.", sanitizedText.indexOf("onload") == -1);
        } catch (IOException e) {
            fail("No Exception should be thrown");
        }
    }

}
