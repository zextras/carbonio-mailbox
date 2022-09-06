// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.util;

import com.zimbra.soap.account.type.HABMember;
import java.util.Comparator;

/**
 * @author zimbra
 */
public class SortBySeniorityIndexThenName implements Comparator<HABMember> {

  @Override
  public int compare(HABMember a, HABMember b) {
    if (a == null || b == null) {
      return 0;
    }

    int s1 = a.getSeniorityIndex();
    int s2 = b.getSeniorityIndex();
    int result = Integer.compare(s2, s1);
    // if seniority index is same, compare by name
    if (result == 0) {
      String name1 = a.getName();
      String name2 = b.getName();

      if (name1 == null && name2 == null) {
        return 0;
      } else if (name1 != null && name2 == null) {
        return -1;
      } else if (name1 == null && name2 != null) {
        return 1;
      } else {
        return name1.compareTo(name2);
      }
    } else {
      return result;
    }
  }
}
