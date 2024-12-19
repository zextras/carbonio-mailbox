package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Console;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.ShareInfoData;

class ShareInfoVisitor implements Provisioning.PublishedShareInfoVisitor {

  private final Console console;

  private static final String mFormat =
      "%-36.36s %-15.15s %-15.15s %-5.5s %-20.20s %-10.10s %-10.10s %-10.10s %-5.5s"
          + " %-5.5s %-36.36s %-15.15s %-15.15s\n";

  ShareInfoVisitor(Console console) {
    this.console = console;
  }

  static String getPrintHeadings(){

    final String heading = String.format(
        mFormat,
        "owner id",
        "owner email",
        "owner display",
        "id",
        "path",
        "view",
        "type",
        "rights",
        "mid",
        "gt",
        "grantee id",
        "grantee name",
        "grantee display"
    );

    final String heading2 = String.format(
        mFormat,
        "------------------------------------", // owner id
        "---------------", // owner email
        "---------------", // owner display
        "-----", // id
        "--------------------", // path
        "----------", // default view
        "----------", // type
        "----------", // rights
        "-----", // mountpoint id if mounted
        "-----", // grantee type
        "------------------------------------", // grantee id
        "---------------", // grantee name
        "---------------"
    );
    return heading + heading2;
  }

  @Override
  public void visit(ShareInfoData shareInfoData) {
    console.print(String.format(
        mFormat,
        shareInfoData.getOwnerAcctId(),
        shareInfoData.getOwnerAcctEmail(),
        shareInfoData.getOwnerAcctDisplayName(),
        shareInfoData.getItemId(),
        shareInfoData.getPath(),
        shareInfoData.getFolderDefaultView(),
        shareInfoData.getType().name(),
        shareInfoData.getRights(),
        shareInfoData.getMountpointId_zmprov_only(),
        shareInfoData.getGranteeType(),
        shareInfoData.getGranteeId(),
        shareInfoData.getGranteeName(),
        shareInfoData.getGranteeDisplayName()));
  }
}
