// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.octosync;

import java.io.IOException;

@SuppressWarnings("serial")
public class PatchException extends IOException
{
    public PatchException()
    {
    }

    public PatchException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public PatchException(String message)
    {
        super(message);
    }

    public PatchException(Throwable cause)
    {
        super(cause);
    }
}
