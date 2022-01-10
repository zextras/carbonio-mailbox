// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import static com.zimbra.common.account.ZAttrProvisioning.A_zimbraRemoteImapBindPort;
import static com.zimbra.common.account.ZAttrProvisioning.A_zimbraRemoteImapSSLBindPort;

import com.zimbra.common.localconfig.LC;


public class RemoteImapConfig extends ImapConfig {
    public static final int D_REMOTE_IMAP_BIND_PORT = 8143;
    public static final int D_REMOTE_IMAP_SSL_BIND_PORT = 8993;

    public RemoteImapConfig(boolean ssl) {
        super(ssl);
    }

    @Override
    public int getBindPort() {
        return isSslEnabled() ?
            getIntAttr(A_zimbraRemoteImapSSLBindPort, D_REMOTE_IMAP_SSL_BIND_PORT) :
            getIntAttr(A_zimbraRemoteImapBindPort, D_REMOTE_IMAP_BIND_PORT);
    }

    @Override
    public String getKeystorePath() {
        return LC.imapd_keystore.value();
    }

    @Override
    public String getKeystorePassword() {
        return LC.imapd_keystore_password.value();
    }
}
