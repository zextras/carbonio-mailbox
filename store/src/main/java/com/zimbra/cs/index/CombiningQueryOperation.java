// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for query operations that combine sets of sub-operations (e.g. Intersections or
 * Unions).
 */
abstract class CombiningQueryOperation extends QueryOperation {

  protected List<QueryOperation> operations = new ArrayList<QueryOperation>();

  int getNumSubOps() {
    return operations.size();
  }
}
