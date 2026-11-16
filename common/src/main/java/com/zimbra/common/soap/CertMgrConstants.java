// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.soap;

import org.dom4j.Namespace;
import org.dom4j.QName;

public final class CertMgrConstants {
  public static final String NAMESPACE_STR = AdminConstants.NAMESPACE_STR;
  public static final Namespace NAMESPACE = Namespace.get(NAMESPACE_STR);

  // ZimbraCertMgrService
  public static final String E_GET_DOMAIN_CERT_REQUEST = "GetDomainCertRequest";
  public static final String E_GET_DOMAIN_CERT_RESPONSE = "GetDomainCertResponse";
  public static final String E_VERIFY_CERTKEY_REQUEST = "VerifyCertKeyRequest";
  public static final String E_VERIFY_CERTKEY_RESPONSE = "VerifyCertKeyResponse";

  public static final QName GET_DOMAIN_CERT_REQUEST =
      QName.get(E_GET_DOMAIN_CERT_REQUEST, NAMESPACE);
  public static final QName GET_DOMAIN_CERT_RESPONSE =
      QName.get(E_GET_DOMAIN_CERT_RESPONSE, NAMESPACE);
  public static final QName VERIFY_CERTKEY_REQUEST = QName.get(E_VERIFY_CERTKEY_REQUEST, NAMESPACE);
  public static final QName VERIFY_CERTKEY_RESPONSE =
      QName.get(E_VERIFY_CERTKEY_RESPONSE, NAMESPACE);

  // InstallCert
  public static final String E_SUBJECT = "subject";
  public static final String E_cert = "cert";

  // GenerateCSR
  public static final String E_SUBJECT_ALT_NAME = "SubjectAltName";

  // GetCert

  //GetDomainCert
  public static final String E_ISSUER = "issuer";
  public static final String E_NOT_BEFORE = "notBefore";
  public static final String E_NOT_AFTER = "notAfter";

  // GetCSR

  // VerifyCertKey
  public static final String A_verifyResult = "verifyResult";
  public static final String A_privkey = "privkey";
  public static final String A_ca = "ca";

  // UploadDomCert
}
