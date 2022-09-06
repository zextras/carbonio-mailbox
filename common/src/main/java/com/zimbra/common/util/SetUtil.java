// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.util.HashSet;
import java.util.Set;

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
    HashSet<T> out = new HashSet<T>();

    for (T o : lhs) {
      if (!rhs.contains(o)) out.add(o);
    }
    return out;
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
    HashSet<T> out = new HashSet<T>();

    for (T o : lhs) {
      if (rhs.contains(o)) out.add(o);
    }
    return out;
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

    for (T o : lhs) {
      if (rhs.contains(o)) out.add(o);
    }
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
  public static <T> Set<T> union(Set<T> out, Set<T> lhs, Set<T> rhs) {

    for (T o : lhs) {
      out.add(o);
    }
    for (T o : rhs) {
      out.add(o);
    }
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
  public static <T> Set<T> union(Set<T> lhs, Set<T> rhs) {
    for (T o : rhs) {
      lhs.add(o);
    }
    return lhs;
  }
}
