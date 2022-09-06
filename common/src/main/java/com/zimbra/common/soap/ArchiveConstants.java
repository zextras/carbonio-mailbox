// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.soap;

import org.dom4j.Namespace;
import org.dom4j.QName;

public final class ArchiveConstants {

  public static final String NAMESPACE_STR = AdminConstants.NAMESPACE_STR;
  public static final Namespace NAMESPACE = Namespace.get(NAMESPACE_STR);

  public static final String E_NO_OP_REQUEST = "NoOpRequest";

  public static final String E_CREATE_ARCHIVE_REQUEST = "CreateArchiveRequest";
  public static final String E_CREATE_ARCHIVE_RESPONSE = "CreateArchiveResponse";

  public static final String E_ENABLE_ARCHIVE_REQUEST = "EnableArchiveRequest";
  public static final String E_ENABLE_ARCHIVE_RESPONSE = "EnableArchiveResponse";

  public static final String E_DISABLE_ARCHIVE_REQUEST = "DisableArchiveRequest";
  public static final String E_DISABLE_ARCHIVE_RESPONSE = "DisableArchiveResponse";

  public static final QName CREATE_ARCHIVE_REQUEST = QName.get(E_CREATE_ARCHIVE_REQUEST, NAMESPACE);
  public static final QName CREATE_ARCHIVE_RESPONSE =
      QName.get(E_CREATE_ARCHIVE_RESPONSE, NAMESPACE);

  public static final QName ENABLE_ARCHIVE_REQUEST = QName.get(E_ENABLE_ARCHIVE_REQUEST, NAMESPACE);
  public static final QName ENABLE_ARCHIVE_RESPONSE =
      QName.get(E_ENABLE_ARCHIVE_RESPONSE, NAMESPACE);

  public static final QName DISABLE_ARCHIVE_REQUEST =
      QName.get(E_DISABLE_ARCHIVE_REQUEST, NAMESPACE);
  public static final QName DISABLE_ARCHIVE_RESPONSE =
      QName.get(E_DISABLE_ARCHIVE_RESPONSE, NAMESPACE);

  public static final String E_ARCHIVE = "archive";
  public static final String A_CREATE = "create";
}
