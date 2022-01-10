// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import java.util.Set;

public abstract class AbstractListCommand extends ImapCommand {

    protected String referenceName;
    protected Set<String> mailboxNames;

    public AbstractListCommand(String referenceName, Set<String> mailboxNames) {
        super();
        this.referenceName = referenceName;
        this.mailboxNames = mailboxNames;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public Set<String> getMailboxNames() {
        return mailboxNames;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mailboxNames == null) ? 0 : mailboxNames.hashCode());
        result = prime * result + ((referenceName == null) ? 0 : referenceName.hashCode());
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
        AbstractListCommand other = (AbstractListCommand) obj;
        if (mailboxNames == null) {
            if (other.mailboxNames != null) {
                return false;
            }
        } else if (!mailboxNames.equals(other.mailboxNames)) {
            return false;
        }
        if (referenceName == null) {
            if (other.referenceName != null) {
                return false;
            }
        } else if (!referenceName.equals(other.referenceName)) {
            return false;
        }
        return true;
    }

}