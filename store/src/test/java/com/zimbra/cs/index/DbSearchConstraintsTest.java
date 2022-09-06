// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link DbSearchConstraints}.
 *
 * @author ysasaki
 */
public final class DbSearchConstraintsTest {

  @Test
  public void copy() {
    DbSearchConstraints.Leaf leaf = new DbSearchConstraints.Leaf();
    leaf.addDateRange(100, true, 200, false, true);
    DbSearchConstraints.Leaf clone = leaf.clone();
    clone.addSizeRange(300, true, 300, false, true);

    Assert.assertEquals(1, leaf.ranges.size());
    Assert.assertEquals(2, clone.ranges.size());
  }
}
