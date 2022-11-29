// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.lmtpserver;

import java.io.InputStream;

public interface LmtpBackend {
    /**
     * Gets account status.
     */
    public LmtpReply getAddressStatus(LmtpAddress address);

    /**
     * Delivers this message to the list of recipients in the message, and sets the
     * delivery status on each recipient address.
     *
     * @param env
     * @param in
     * @param sizeHint
     * @throws UnrecoverableLmtpException for errors such as disk-full
     */
    public void deliver(LmtpEnvelope env, InputStream in, int sizeHint) throws UnrecoverableLmtpException;
}