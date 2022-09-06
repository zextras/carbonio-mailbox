// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.filter.jsieve;

import org.apache.jsieve.exception.FeatureException;

public interface Values {
  /**
   * Method values answers a <code>boolean</code> indicating if parameter <code>string1</code> is
   * gt/ge/lt/le/eq/ne to parameter <code>string2</code> using the comparison rules defind by the
   * implementation.
   *
   * @param operator
   * @param lhs left hand side
   * @param rhs right hand side
   * @return boolean
   */
  public boolean values(String operator, String lhs, String rhs) throws FeatureException;
}
