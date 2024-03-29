// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Strings;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.index.DBQueryOperation;
import com.zimbra.cs.index.NoResultsQueryOperation;
import com.zimbra.cs.index.QueryOperation;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.service.util.ItemId;

/**
 * Query by item ID.
 *
 * @author tim
 * @author ysasaki
 */
public final class ItemQuery extends Query {

    private final boolean isAllQuery;
    private final boolean isNoneQuery;
    private final boolean isRangeQuery;
    private final List<ItemId> itemIds;

    public static Query create(Mailbox mbox, String str) throws ServiceException {
        boolean allQuery = false;
        boolean noneQuery = false;
        boolean rangeQuery = false;
        List<ItemId> itemIds = new ArrayList<>();

        if (str.equalsIgnoreCase("all")) {
            allQuery = true;
        } else if (str.equalsIgnoreCase("none")) {
            noneQuery = true;
        } else if(str.indexOf("--") > 0) {
            String[] items = str.split("--");
            if(items.length != 2) {
                throw ServiceException.PARSE_ERROR(String.format("Invalid range expression in search query: %s",  str), null);
            }
            itemIds.add(new ItemId(items[0], mbox.getAccountId()));
            itemIds.add(new ItemId(items[1], mbox.getAccountId()));
            rangeQuery = true;
        } else {
            String[] items = str.split(",");
          for (String item : items) {
            if (item.length() > 0) {
              ItemId iid = new ItemId(item, mbox.getAccountId());
              itemIds.add(iid);
            }
          }
            if (itemIds.size() == 0) {
                noneQuery = true;
            }
        }

        return new ItemQuery(allQuery, noneQuery, rangeQuery, itemIds);
    }

    ItemQuery(boolean all, boolean none, boolean range, List<ItemId> ids) {
        this.isAllQuery = all;
        this.isNoneQuery = none;
        this.itemIds = ids;
        this.isRangeQuery = range;
    }

    @Override
    public boolean hasTextOperation() {
        return false;
    }

    @Override
    public QueryOperation compile(Mailbox mbox, boolean bool) {
        DBQueryOperation dbOp = new DBQueryOperation();
        bool = evalBool(bool);
        if (bool && isAllQuery || !bool && isNoneQuery) {
            // adding no constraints should match everything...
        } else if (bool && isNoneQuery || !bool && isAllQuery) {
            return new NoResultsQueryOperation();
        } else if(isRangeQuery && itemIds.size() == 2) {
            dbOp.addItemIdRange(itemIds.get(0).getId(), true, itemIds.get(1).getId(), true, bool);
        } else {
            for (ItemId iid : itemIds) {
                dbOp.addItemIdClause(mbox, iid, bool);
            }
        }
        return dbOp;
    }

    @Override
    public void dump(StringBuilder out) {
        out.append("ITEMID");
        if (isAllQuery) {
            out.append(",all");
        } else if (isNoneQuery) {
            out.append(",none");
        } else if (isRangeQuery && itemIds.size() == 2) {
            out.append(',');
            out.append(itemIds.get(0).toString());
            out.append("--");
            out.append(itemIds.get(1).toString());
        } else {
            for (ItemId id : itemIds) {
                out.append(',');
                out.append(id.toString());
            }
        }
    }

    @Override
    public void sanitizedDump(StringBuilder out) {
        out.append("ITEMID");
        if (isAllQuery) {
            out.append(",all");
        } else if (isNoneQuery) {
            out.append(",none");
        } else if (isRangeQuery && itemIds.size() == 2) {
            out.append(',');
            out.append(itemIds.get(0).toString());
            out.append("--");
            out.append(itemIds.get(1).toString());
        } else {
            out.append(Strings.repeat(",$TEXT", itemIds.size()));
        }
    }
}
