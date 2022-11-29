// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.util.EnumSet;
import java.util.Set;

/**
 * From/To/CC domain term.
 *
 * @author ysasaki
 */
public final class DomainBrowseTerm extends BrowseTerm {

    public enum Field {
        FROM, TO, CC;
    }

    private Set<Field> fields = EnumSet.noneOf(Field.class);

    public DomainBrowseTerm(BrowseTerm term) {
        super(term.getText(), term.getFreq());
    }

    public void addField(Field field) {
        fields.add(field);
    }

    public String getHeaderFlags() {
        StringBuilder result = new StringBuilder();
        if (fields.contains(Field.FROM)) {
            result.append('f');
        }
        if (fields.contains(Field.TO)) {
            result.append('t');
        }
        if (fields.contains(Field.CC)) {
            result.append('c');
        }
        return result.toString();
    }

    public boolean contains(Field field) {
        return fields.contains(field);
    }

}
