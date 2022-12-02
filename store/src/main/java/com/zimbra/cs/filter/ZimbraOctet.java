// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import java.util.List;

import org.apache.jsieve.comparators.Octet;
import org.apache.jsieve.exception.FeatureException;
import org.apache.jsieve.exception.SievePatternException;

import com.zimbra.common.soap.HeaderConstants;

/**
 * Class ZimbraOctet enhances the jsieve's Octet class to
 * support the :values match type for the i;octet
 */
public class ZimbraOctet extends Octet implements ZimbraComparator {

    public boolean values(String operator, String left, String right)
        throws FeatureException {
        switch (operator) {
        case HeaderConstants.GT_OP:
            return (left.compareTo(right) > 0);
        case HeaderConstants.GE_OP:
            return (left.compareTo(right) >= 0);
        case HeaderConstants.LT_OP:
            return (left.compareTo(right) < 0);
        case HeaderConstants.LE_OP:
            return (left.compareTo(right) <= 0);
        case HeaderConstants.EQ_OP:
            return (left.compareTo(right) == 0);
        case HeaderConstants.NE_OP:
            return (left.compareTo(right) != 0);
        }
        return false;
    }

    @Override
    public boolean counts(String operator, List<String> lhs, String rhs)
            throws FeatureException {
        throw new FeatureException("Substring counts unsupported by octet");
    }

    /**
     * @see org.apache.jsieve.comparators.Octet.matches#matches(String, String)
     */
    public boolean matches(String string, String glob)
            throws SievePatternException {
        return ZimbraComparatorUtils.matches(string, glob);
    }

    /**
     * @see org.apache.jsieve.comparators.Octet#equals(String, String)
     */
    @Override
    public boolean equals2(String string1, String string2) throws FeatureException {
        return equals(string1, string2);
    }
}
