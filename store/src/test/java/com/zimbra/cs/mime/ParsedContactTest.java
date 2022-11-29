// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mime;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Strings;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.MailServiceException;

/**
 * Unit test for {@link ParsedContact}.
 *
 * @author ysasaki
 */
public final class ParsedContactTest {

    @Test
    public void tooBigField() throws Exception {
        try {
            new ParsedContact(Collections.singletonMap(Strings.repeat("k", 101), "v"));
            Assert.fail();
        } catch (ServiceException e) {
            Assert.assertEquals(ServiceException.INVALID_REQUEST, e.getCode());
        }

        try {
            new ParsedContact(Collections.singletonMap("k", Strings.repeat("v", 10000001)));
            Assert.fail();
        } catch (MailServiceException e) {
            Assert.assertEquals(MailServiceException.CONTACT_TOO_BIG, e.getCode());
        }

        Map<String, String> fields = new HashMap<String, String>();
        for (int i = 0; i < 1001; i++) {
           fields.put("k" + i, "v" + i);
        }
        try {
            new ParsedContact(fields);
            Assert.fail();
        } catch (ServiceException e) {
            Assert.assertEquals(ServiceException.INVALID_REQUEST, e.getCode());
        }

    }

}
