// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;

public class SetUtil {

    
    /**
     * Out gets intersection of elements in lhs and rhs
     * 
     * @param out
     * @param lhs
     * @param rhs
     * @return
     */
    public static <T> Set<T> subtract(Set<T> lhs, Set<T> rhs) {
        return new HashSet<>(Sets.difference(lhs, rhs));
    }
    
    /**
     * Out gets the intersection of elements in lhs and rhs
     * 
     * @param out
     * @param lhs
     * @param rhs
     * @return
     */
    public static <T> Set<T> intersect(Set<T> lhs, Set<T> rhs) {
        return new HashSet<>(Sets.intersection(lhs, rhs));
    }
	
	/**
	 * Out gets the intersection of elements in lhs and rhs
	 * 
	 * @param out
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	public static <T> Set<T> intersect(Set<T> out, Set<T> lhs, Set<T> rhs) {
		out.addAll(Sets.intersection(lhs, rhs));
		return out;
	}
	
	/**
	 * Out gets the union of elements in lhs and rhs
	 * 
	 * @param out
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	public static <T> Set<T> union (Set<T> out, Set<T> lhs, Set<T> rhs) {

    out.addAll(lhs);
    out.addAll(rhs);
		return out;
	}
	
	/**
	 * Union into lhs 
	 * 
	 * @param out
	 * @param lhs
	 * @param rhs
	 * @return
	 */
	public static <T> Set<T> union (Set<T> lhs, Set<T> rhs) {
    lhs.addAll(rhs);
		return lhs;
	}
	
	
	
}
