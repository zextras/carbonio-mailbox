// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.stats;

import java.util.List;

/**
 * Defines an interface to an object that keeps track of
 * one or more statistics.
 */
public interface Accumulator {

    /**
     * Returns stat names.  The size of the <code>List</code> must match the size of the
     * <code>List</code> returned by {@link #getData()}.
     */
    public List<String> getNames();
    
    /**
     * Returns stat values.  The size of the <code>List</code> must match the size of the
     * <code>List</code> returned by {@link #getNames()}.
     */
    public List<Object> getData();
    
    /**
     * Resets the values tracked by this <code>Accumulator</code>.
     */
    public void reset();
}
