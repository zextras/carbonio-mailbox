// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Strings;
import com.zimbra.client.ZDocument;
import com.zimbra.common.soap.Element;

public class ZDocumentTest {

    @Test
    public void note() throws Exception {
        String xml =
            "<doc f='' d='1300925565000' rev='2' ms='2' l='0-0-0:16' ver='1' ct='text/plain' id='0-0-0:257' cr='' " +
            "loid='' t='' s='18' md='1300925565000' leb='' name='doc.txt' descEnabled='1' cd='1300925565000'><meta/><fr>This is a document</fr></doc>";
        ZDocument doc = new ZDocument(Element.parseXML(xml));
        Assert.assertEquals(null, Strings.emptyToNull(doc.getFlags()));
        
        xml =
            "<doc f='t' d='1300925565000' rev='3' ms='4' l='0-0-0:16' ver='1' ct='text/plain' id='0-0-0:258' cr='' " +
            "loid='' t='' s='14' md='1300925565000' leb='' name='note.txt' descEnabled='1' cd='1300925565000'><meta/><fr>This is a note</fr></doc>";
        ZDocument note = new ZDocument(Element.parseXML(xml));
        Assert.assertEquals("t", note.getFlags());
    }
}
