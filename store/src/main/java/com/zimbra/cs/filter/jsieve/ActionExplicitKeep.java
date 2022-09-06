// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import org.apache.jsieve.mail.Action;

/**
 * Class ActionKeep encapsulates the information required to keep a mail. See RFC 5228, Section 4.3.
 * This class will be called when the "keep" action is explicitly specified from the sieve script.
 */
public class ActionExplicitKeep implements Action {}
