// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class MockSoapEngine extends SoapEngine {
  public MockSoapEngine(DocumentService service) {
    super(new SimpleMeterRegistry());
    service.registerHandlers(getDocumentDispatcher());
  }
}
