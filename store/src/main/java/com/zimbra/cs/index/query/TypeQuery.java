// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import com.google.common.collect.Lists;
import com.zimbra.cs.index.LuceneFields;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Query by MIME type.
 *
 * @author tim
 * @author ysasaki
 */
public final class TypeQuery extends LuceneQuery {

  private TypeQuery(String what) {
    super("type:", LuceneFields.L_MIMETYPE, what);
  }

  /**
   * Note: returns either a {@link TypeQuery} or a {@link SubQuery}
   *
   * @return
   */
  public static Query createQuery(String what) {
    Collection<String> types = lookup(AttachmentQuery.CONTENT_TYPES_MULTIMAP, what);
    if (types.size() == 1) {
      return new TypeQuery(Lists.newArrayList(types).get(0));
    } else {
      List<Query> clauses = new ArrayList<Query>(types.size() * 2 - 1);
      for (String contentType : types) {
        if (!clauses.isEmpty()) {
          clauses.add(new ConjQuery(ConjQuery.Conjunction.OR));
        }
        clauses.add(new TypeQuery(contentType));
      }
      return new SubQuery(clauses);
    }
  }
}
