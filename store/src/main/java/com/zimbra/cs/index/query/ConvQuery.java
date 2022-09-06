// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.index.DBQueryOperation;
import com.zimbra.cs.index.QueryOperation;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.service.util.ItemId;
import java.util.ArrayList;
import java.util.List;

/**
 * Query by conversation ID.
 *
 * @author tim
 * @author ysasaki
 */
public final class ConvQuery extends Query {
  private final ItemId convId;

  private ConvQuery(ItemId convId) throws ServiceException {
    this.convId = convId;

    if (convId.getId() < 0) { // should never happen (make an ItemQuery instead)
      throw ServiceException.FAILURE(
          "Illegal Negative ConvID: " + convId + ", use ItemQuery for virtual convs", null);
    }
  }

  public static Query create(Mailbox mbox, String target) throws ServiceException {
    ItemId convId = new ItemId(target, mbox.getAccountId());
    if (convId.getId() < 0) {
      // ...convert negative convId to positive ItemId...
      convId = new ItemId(convId.getAccountId(), -1 * convId.getId());
      List<ItemId> iidList = new ArrayList<ItemId>(1);
      iidList.add(convId);
      return new ItemQuery(false, false, false, iidList);
    } else {
      return new ConvQuery(convId);
    }
  }

  @Override
  public boolean hasTextOperation() {
    return false;
  }

  @Override
  public QueryOperation compile(Mailbox mbox, boolean bool) {
    DBQueryOperation op = new DBQueryOperation();
    op.addConvId(mbox, convId, evalBool(bool));
    return op;
  }

  @Override
  public void dump(StringBuilder out) {
    out.append("CONV:");
    out.append(convId);
  }

  @Override
  public void sanitizedDump(StringBuilder out) {
    out.append("CONV:");
    out.append("$NUM");
  }
}
