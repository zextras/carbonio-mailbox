// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;

/**
 * Suggested query string.
 *
 * @author ysasaki
 */
public final class SuggestQueryInfo implements QueryInfo {
    private final String suggest;

    public SuggestQueryInfo(String suggest) {
        this.suggest = suggest;
    }

    @Override
    public Element toXml(Element parent) {
        return parent.addElement(MailConstants.E_SUGEST).setText(suggest);
    }

    @Override
    public String toString() {
        return suggest;
    }
}
