// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util.yauth;

import java.io.IOException;

public interface Authenticator {
    RawAuth authenticate() throws AuthenticationException, IOException;
    void invalidate();
}
