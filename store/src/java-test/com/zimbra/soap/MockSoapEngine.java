// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap;

public class MockSoapEngine extends SoapEngine {
    public MockSoapEngine(DocumentService service) {
        service.registerHandlers(getDocumentDispatcher());
    }
}
