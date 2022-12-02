// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

public class BrowseTerm {
    private final String text;
    private final int freq;

    public BrowseTerm(String text, int freq) {
        this.text = text;
        this.freq = freq;
    }

    public String getText() {
        return text;
    }

    public int getFreq() {
        return freq;
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof BrowseTerm) {
            BrowseTerm other = (BrowseTerm) obj;
            if (text != null) {
                return text.equals(other.text);
            } else { // both null
                return other.text == null;
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return text;
    }

}
