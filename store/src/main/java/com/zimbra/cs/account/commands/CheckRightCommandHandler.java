package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AccessManager;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.soap.admin.type.GranteeSelector;
import com.zimbra.soap.type.TargetBy;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.Map;

public class CheckRightCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public CheckRightCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doCheckRight(args);
  }

  private void doCheckRight(String[] args) throws ServiceException, ArgException {
    RightArgs ra = new RightArgs(args);
    RightArgs.getRightArgs(ra, false, false); // todo, handle secret

    Map<String, Object> attrs = provUtil.getMap(args, ra.mCurPos);

    TargetBy targetBy = (ra.mTargetIdOrName == null) ? null : ProvUtil.guessTargetBy(ra.mTargetIdOrName);
    GranteeSelector.GranteeBy granteeBy = ProvUtil.guessGranteeBy(ra.mGranteeIdOrName);

    AccessManager.ViaGrant via = new AccessManager.ViaGrant();
    boolean allow =
            provUtil.getProvisioning().checkRight(
                    ra.mTargetType,
                    targetBy,
                    ra.mTargetIdOrName,
                    granteeBy,
                    ra.mGranteeIdOrName,
                    ra.mRight,
                    attrs,
                    via);

    var console = provUtil.getConsole();
    console.println(allow ? "ALLOWED" : "DENIED");
    if (via.available()) {
      console.println("Via:");
      console.println("    target type  : " + via.getTargetType());
      console.println("    target       : " + via.getTargetName());
      console.println("    grantee type : " + via.getGranteeType());
      console.println("    grantee      : " + via.getGranteeName());
      console.println(
              "    right        : " + (via.isNegativeGrant() ? "DENY " : "") + via.getRight());
      console.println();
    }
  }
}
