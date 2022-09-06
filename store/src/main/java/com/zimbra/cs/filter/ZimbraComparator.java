// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter;

import com.zimbra.cs.filter.jsieve.Counts;
import com.zimbra.cs.filter.jsieve.Equals2;
import com.zimbra.cs.filter.jsieve.Values;
import org.apache.jsieve.comparators.Comparator;

/**
 * Class ZimbraComparator enhances the jsieve's Comparator to support the RFC 5231: Relational
 * Extension
 */
public interface ZimbraComparator extends Comparator, Values, Counts, Equals2 {}
