// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.clamscanner;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.extension.ZimbraExtension;
import com.zimbra.cs.service.mail.UploadScanner;
import java.net.MalformedURLException;
import java.util.LinkedList;
import java.util.List;

public class ClamScannerExtension implements ZimbraExtension {

  private static final Log LOG = ZimbraLog.extensions;
  public static final String NAME = "clamscanner";
  private final List<ClamScanner> clamScannerList = new LinkedList<>();

  @Override
  public synchronized void init() {

    try {
      final ClamScannerConfig config = new ClamScannerConfig();
      if (!config.getEnabled()) {
        LOG.info("attachment scan is disabled");
        return;
      }

      final String[] urls = config.getURLs();
      if (urls.length == 0) {
        final ClamScanner clamScanner = new ClamScanner();
        clamScanner.setURL(null);
        UploadScanner.registerScanner(clamScanner);
        clamScannerList.add(clamScanner);
      } else {
        for (String url : urls) {
          final ClamScanner clamScanner = new ClamScanner();
          clamScanner.setURL(url);
          UploadScanner.registerScanner(clamScanner);
          clamScannerList.add(clamScanner);
        }
      }
    } catch (ServiceException | MalformedURLException e) {
      LOG.error("error creating scanner", e);
    }
  }

  @Override
  public void destroy() {
    for (ClamScanner clamScanner : clamScannerList) {
      UploadScanner.unregisterScanner(clamScanner);
    }
  }

  @Override
  public String getName() {
    return NAME;
  }
}
