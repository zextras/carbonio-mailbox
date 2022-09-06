// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.admin.message;

import com.zimbra.common.soap.AdminConstants;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required true
 * @zm-api-command-description Reloads the memcached client configuration on this server. Memcached
 *     client layer is reinitialized accordingly. Call this command after updating the memcached
 *     server list, for example.
 */
@XmlRootElement(name = AdminConstants.E_RELOAD_MEMCACHED_CLIENT_CONFIG_REQUEST)
public class ReloadMemcachedClientConfigRequest {}
