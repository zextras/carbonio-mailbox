package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.soap.admin.type.GranteeSelector;
import com.zimbra.soap.type.TargetBy;
import org.apache.http.HttpException;

import java.io.IOException;

class RevokeRightCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public RevokeRightCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    doRevokeRight(args);
  }

  private void doRevokeRight(String[] args) throws ServiceException, ArgException {
    RightArgs ra = new RightArgs(args);
    RightArgs.getRightArgs(ra, true, false);

    TargetBy targetBy = (ra.mTargetIdOrName == null) ? null : ProvUtil.guessTargetBy(ra.mTargetIdOrName);
    GranteeSelector.GranteeBy granteeBy =
            (ra.mGranteeIdOrName == null) ? null : ProvUtil.guessGranteeBy(ra.mGranteeIdOrName);

    provUtil.getProvisioning().revokeRight(
            ra.mTargetType,
            targetBy,
            ra.mTargetIdOrName,
            ra.mGranteeType,
            granteeBy,
            ra.mGranteeIdOrName,
            ra.mRight,
            ra.mRightModifier);
  }

}
