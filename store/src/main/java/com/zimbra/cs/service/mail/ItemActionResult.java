// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.soap.MailConstants;
import java.util.ArrayList;
import java.util.List;

public class ItemActionResult {

  protected List<String> mSuccessIds;

  public ItemActionResult() {
    mSuccessIds = new ArrayList<String>();
  }

  public ItemActionResult(int[] ids) {
    mSuccessIds = new ArrayList<String>(ids.length);
    for (int id : ids) {
      mSuccessIds.add(Integer.toString(id));
    }
  }

  public ItemActionResult(List<Integer> ids) {
    mSuccessIds = new ArrayList<String>(ids.size());
    for (Integer id : ids) {
      mSuccessIds.add(id.toString());
    }
  }

  public static ItemActionResult create(ItemActionHelper.Op operation) {
    switch (operation) {
      case COPY:
        return new CopyActionResult();
      case HARD_DELETE:
        return new DeleteActionResult();
      default:
        return new ItemActionResult();
    }
  }

  public static ItemActionResult create(String operation) {
    if (MailConstants.OP_COPY.equalsIgnoreCase(operation)) {
      return new CopyActionResult();
    } else if (MailConstants.OP_HARD_DELETE.equalsIgnoreCase(operation)) {
      return new DeleteActionResult();
    }
    return new ItemActionResult();
  }

  public List<String> getSuccessIds() {
    return mSuccessIds;
  }

  public void setSuccessIds(List<String> ids) {
    mSuccessIds = ids;
  }

  public void appendSuccessIds(List<String> ids) {
    for (String id : ids) {
      appendSuccessId(id);
    }
  }

  public void appendSuccessId(String id) {
    mSuccessIds.add(id);
  }
}
