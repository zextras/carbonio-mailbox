// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap;

import com.zextras.mailbox.metric.Metrics;

public class MockSoapEngine extends SoapEngine {
    public MockSoapEngine(DocumentService service) {
        super(Metrics.METER_REGISTRY);
        service.registerHandlers(getDocumentDispatcher());
    }
}
