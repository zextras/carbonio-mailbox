// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.client.soap;

import com.zimbra.cs.client.*;

public class LmcCreateNoteResponse extends LmcSoapResponse {

    private LmcNote mNote;

    public LmcNote getNote() { return mNote; }

    public void setNote(LmcNote n) { mNote = n; }
}
