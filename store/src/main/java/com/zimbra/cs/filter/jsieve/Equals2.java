// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import org.apache.jsieve.exception.FeatureException;

/**
 * Interface of the match type :is for the comparator i;ascii-numeric. For other comparator's :is,
 * see {@link org.apache.jsieve.comparators.Equals#equals(String, String)}
 */
public interface Equals2 {
  public boolean equals2(String string1, String string2) throws FeatureException;
}
