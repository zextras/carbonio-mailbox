// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cert;

import com.zimbra.cert.util.ProcessStarterProvider;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.soap.CertMgrConstants;
import com.zimbra.cs.ldap.LdapUtil;
import com.zimbra.soap.DocumentDispatcher;
import com.zimbra.soap.DocumentService;
import java.io.File;

public class ZimbraCertMgrService implements DocumentService {

    public void registerHandlers(DocumentDispatcher dispatcher) {
        dispatcher.registerHandler(CertMgrConstants.INSTALL_CERT_REQUEST, new InstallCert());
        dispatcher.registerHandler(CertMgrConstants.GET_CERT_REQUEST, new GetCert());
        dispatcher.registerHandler(CertMgrConstants.GET_DOMAIN_CERT_REQUEST, new GetDomainCert());
        dispatcher.registerHandler(CertMgrConstants.GEN_CSR_REQUEST, new GenerateCSR());
        dispatcher.registerHandler(CertMgrConstants.GET_CSR_REQUEST, new GetCSR());
	      dispatcher.registerHandler(CertMgrConstants.VERIFY_CERTKEY_REQUEST, new VerifyCertKey(
          new ProcessStarterProvider(), () -> LC.zimbra_tmp_directory.value() +
          File.separator + LdapUtil.generateUUID() + File.separator));
        dispatcher.registerHandler(CertMgrConstants.UPLOAD_DOMCERT_REQUEST, new UploadDomCert());
        dispatcher.registerHandler(CertMgrConstants.UPLOAD_PROXYCA_REQUEST, new UploadProxyCA());
    }
}
