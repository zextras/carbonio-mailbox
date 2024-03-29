// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap.apidesc;

public interface SoapApiNamedElement
extends SoapApiElementArtifact {
    String getName();
    String getNamespace();
}
