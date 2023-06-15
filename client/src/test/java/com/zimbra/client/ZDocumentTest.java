// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.client;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.base.Strings;
import com.zimbra.common.soap.Element;
import org.junit.jupiter.api.Test;

public class ZDocumentTest {

    @Test
    void note() throws Exception {
        String xml =
                "<doc f='' d='1300925565000' rev='2' ms='2' l='0-0-0:16' ver='1' ct='text/plain' id='0-0-0:257' cr='' " +
                        "loid='' t='' s='18' md='1300925565000' leb='' name='doc.txt' descEnabled='1' cd='1300925565000'><meta/><fr>This is a document</fr></doc>";
        ZDocument doc = new ZDocument(Element.parseXML(xml));
        assertNull(Strings.emptyToNull(doc.getFlags()));

        xml =
                "<doc f='t' d='1300925565000' rev='3' ms='4' l='0-0-0:16' ver='1' ct='text/plain' id='0-0-0:258' cr='' " +
                        "loid='' t='' s='14' md='1300925565000' leb='' name='note.txt' descEnabled='1' cd='1300925565000'><meta/><fr>This is a note</fr></doc>";
        ZDocument note = new ZDocument(Element.parseXML(xml));
        assertEquals("t", note.getFlags());
    }
}
