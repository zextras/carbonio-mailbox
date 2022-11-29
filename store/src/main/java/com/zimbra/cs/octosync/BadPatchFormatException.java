// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.octosync;

@SuppressWarnings("serial")
public class BadPatchFormatException extends PatchException
{
    public BadPatchFormatException()
    {
    }

    public BadPatchFormatException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public BadPatchFormatException(String message)
    {
        super(message);
    }

    public BadPatchFormatException(Throwable cause)
    {
        super(cause);
    }
}
