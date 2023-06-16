// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link DbSearchConstraints}.
 *
 * @author ysasaki
 */
public final class DbSearchConstraintsTest {

 @Test
 void copy() {
  DbSearchConstraints.Leaf leaf = new DbSearchConstraints.Leaf();
  leaf.addDateRange(100, true, 200, false, true);
  DbSearchConstraints.Leaf clone = leaf.clone();
  clone.addSizeRange(300, true, 300, false, true);

  assertEquals(1, leaf.ranges.size());
  assertEquals(2, clone.ranges.size());
 }
}
