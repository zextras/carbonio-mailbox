// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cert;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.extension.ExtensionDispatcherServlet;
import com.zimbra.cs.extension.ZimbraExtension;
import com.zimbra.soap.SoapServlet;

public class ZimbraCertMgrExt implements ZimbraExtension {
  public static final String EXTENSION_NAME_CERTMGR = "com_zimbra_cert_manager";

  // Remote commands
  public static final String GET_STAGED_CERT_CMD = "zmcertmgr viewstagedcrt";
  public static final String GET_DEPLOYED_CERT_CMD = "zmcertmgr viewdeployedcrt";
  public static final String CREATE_CSR_CMD = "zmcertmgr createcsr";
  public static final String CREATE_CRT_CMD = "zmcertmgr createcrt";
  public static final String DEPLOY_CERT_CMD = "zmcertmgr deploycrt";
  public static final String GET_CSR_CMD = "zmcertmgr viewcsr";
  public static final String VERIFY_CRTKEY_CMD = "zmcertmgr verifycrtkey";
  public static final String VERIFY_COMM_CRTKEY_CMD = "zmcertmgr verifycrt";
  public static final String VERIFY_CRTCHAIN_CMD = "zmcertmgr verifycrtchain";
  public static final String DOWNLOAD_CSR_CMD = "downloadcsr";
  public static final String COMM_CRT_KEY_FILE_NAME = "commercial.key";
  public static final String COMM_CRT_FILE_NAME = "commercial.crt";
  public static final String COMM_CRT_CA_FILE_NAME = "commercial_ca.crt";
  public static final String ALL_SERVERS = "--- All Servers ---";
  public static final String CERT_TYPE_SELF = "self";
  public static final String CERT_TYPE_COMM = "comm";

  public void destroy() {}

  public String getName() {
    return EXTENSION_NAME_CERTMGR;
  }

  public void init() throws ServiceException {
    SoapServlet.addService("AdminServlet", new ZimbraCertMgrService());
    ExtensionDispatcherServlet.register(this, new DownloadCSRHandler());
  }
}
