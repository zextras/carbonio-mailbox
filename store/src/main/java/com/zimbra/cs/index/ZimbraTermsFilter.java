// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import org.apache.lucene.index.Term;

/**
 * Constructs a filter for docs matching any of a set of terms. This can be used for filtering on
 * multiple terms that are not necessarily in a sequence. An example might be a collection of
 * primary keys from a database query result or perhaps a choice of "category" labels picked by the
 * end user.
 */
public class ZimbraTermsFilter {

  private final Set<Term> terms = Sets.newTreeSet();

  /**
   * @param terms is the list of acceptable terms
   */
  public ZimbraTermsFilter(Collection<Term> terms) {
    this.terms.addAll(terms);
  }

  public Collection<Term> getTerms() {
    return terms;
  }
}
