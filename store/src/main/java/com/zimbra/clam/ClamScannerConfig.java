// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.clam;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;

public class ClamScannerConfig {

  // fallback configuration is used when set URLs are not valid
  static final String FALLBACK_HOSTNAME = "127.78.0.17";
  static final int FALLBACK_PORT = 20000;
  static final int FALLBACK_TIMEOUT = 20000; // 20seconds
  static final int FALLBACK_CHUNK_SIZE = 2048;

  private final boolean mEnabled;
  private final String[] mURLs;

  public ClamScannerConfig() throws ServiceException {
    try {
      final Server serverConfig = Provisioning.getInstance().getLocalServer();
      mEnabled = serverConfig.getBooleanAttr(ZAttrProvisioning.A_zimbraAttachmentsScanEnabled,
          false);
      mURLs = serverConfig.getAttachmentsScanURL();
    } catch (Exception e) {
      throw ServiceException.FAILURE("Failed to initialize ClamScannerConfig", e);
    }
  }

  public boolean getEnabled() {
    return mEnabled;
  }

  public String[] getURLs() {
    return mURLs;
  }
}
