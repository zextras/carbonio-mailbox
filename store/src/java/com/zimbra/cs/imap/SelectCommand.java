// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

public class SelectCommand extends ImapCommand {

    private ImapPath path;
    byte params;
    QResyncInfo qri;

    /**
     * @param path
     * @param params
     * @param qri
     */
    public SelectCommand(ImapPath path, byte params, QResyncInfo qri) {
        super();
        this.path = path;
        this.params = params;
        this.qri = qri;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + params;
        result = prime * result + ((path == null) ? 0 : path.hashCode());
        result = prime * result + ((qri == null) ? 0 : qri.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SelectCommand other = (SelectCommand) obj;
        if (params != other.params) {
            return false;
        }
        if (path == null) {
            if (other.path != null) {
                return false;
            }
        } else if (!path.equals(other.path)) {
            return false;
        }
        if (qri == null) {
            if (other.qri != null) {
                return false;
            }
        } else if (!qri.equals(other.qri)) {
            return false;
        }
        return true;
    }
}
