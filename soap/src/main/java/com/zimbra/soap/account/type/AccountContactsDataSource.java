// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.account.type;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

import com.zimbra.soap.type.ContactsDataSource;

@XmlRootElement(name="contacts")
@XmlType(propOrder = {})
public class AccountContactsDataSource
extends AccountDataSource
implements ContactsDataSource {

}
