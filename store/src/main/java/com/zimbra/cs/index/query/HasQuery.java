// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import com.google.common.collect.ImmutableMap;
import com.zimbra.cs.index.LuceneFields;
import java.util.Map;

/**
 * Query by special objects.
 *
 * @author tim
 * @author ysasaki
 */
public final class HasQuery extends LuceneQuery {
  private static final Map<String, String> MAP =
      ImmutableMap.<String, String>builder()
          .put("attachment", "any")
          .put("att", "any")
          .put("phone", "phone")
          .put("u.po", "u.po")
          .put("ssn", "ssn")
          .put("url", "url")
          .build();

  public HasQuery(String what) {
    super("has:", LuceneFields.L_OBJECTS, lookup(MAP, what));
  }
}
