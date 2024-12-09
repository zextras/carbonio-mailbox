package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.soap.admin.type.GranteeSelector;
import com.zimbra.soap.type.TargetBy;
import org.apache.http.HttpException;

import java.io.IOException;

class GrantRightCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GrantRightCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException, HttpException, IOException {
    doGrantRight(args);
  }

  private void doGrantRight(String[] args) throws ServiceException, ArgException {
    RightArgs ra = new RightArgs(args);
    RightArgs.getRightArgs(ra, true, true);

    TargetBy targetBy = (ra.mTargetIdOrName == null) ? null : ProvUtil.guessTargetBy(ra.mTargetIdOrName);
    GranteeSelector.GranteeBy granteeBy =
            (ra.mGranteeIdOrName == null) ? null : ProvUtil.guessGranteeBy(ra.mGranteeIdOrName);

    provUtil.getProvisioning().grantRight(
            ra.mTargetType,
            targetBy,
            ra.mTargetIdOrName,
            ra.mGranteeType,
            granteeBy,
            ra.mGranteeIdOrName,
            ra.mSecret,
            ra.mRight,
            ra.mRightModifier);
  }
}
