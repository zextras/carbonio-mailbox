// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap.apidesc;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * Used for conveying element related information appropriate at a certain level. For instance,
 * ordinary elements have an associated data type wrapper elements don't. Where there is a choice of
 * elements, we regard choice information as being the appropriate information for the level
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface SoapApiElementArtifact {}
