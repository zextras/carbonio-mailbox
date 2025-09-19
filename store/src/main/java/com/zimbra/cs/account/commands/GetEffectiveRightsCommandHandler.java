package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.accesscontrol.RightCommand;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.soap.admin.type.GranteeSelector;
import com.zimbra.soap.type.TargetBy;

class GetEffectiveRightsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetEffectiveRightsCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    doGetEffectiveRights(args);
  }

  private void doGetEffectiveRights(String[] args) throws ServiceException, ArgException {
    RightArgs ra = new RightArgs(args);
    RightArgs.getRightArgsTarget(ra);

    var prov = provUtil.getProvisioning();
    if (prov instanceof LdapProv) {
      // must provide grantee info
      RightArgs.getRightArgsGrantee(ra, false, false);
    } else {
      // has more args, use it for the requested grantee
      if (ra.mCurPos < args.length) {
        RightArgs.getRightArgsGrantee(ra, false, false);
      }
    }

    boolean expandSetAttrs = false;
    boolean expandGetAttrs = false;

    // if there are more args, see if they are expandSetAttrs/expandGetAttrs
    for (int i = ra.mCurPos; i < args.length; i++) {
      if ("expandSetAttrs".equals(args[i])) {
        expandSetAttrs = true;
      } else if ("expandGetAttrs".equals(args[i])) {
        expandGetAttrs = true;
      } else {
        throw new ArgException("unrecognized arg: " + args[i]);
      }
    }

    TargetBy targetBy = (ra.mTargetIdOrName == null) ? null : ProvUtil.guessTargetBy(ra.mTargetIdOrName);
    GranteeSelector.GranteeBy granteeBy =
            (ra.mGranteeIdOrName == null) ? null : ProvUtil.guessGranteeBy(ra.mGranteeIdOrName);

    RightCommand.EffectiveRights effRights =
            prov.getEffectiveRights(
                    ra.mTargetType,
                    targetBy,
                    ra.mTargetIdOrName,
                    granteeBy,
                    ra.mGranteeIdOrName,
                    expandSetAttrs,
                    expandGetAttrs);

    provUtil.getConsole().println(
            "Account "
                    + effRights.granteeName()
                    + " has the following rights on target "
                    + effRights.targetType()
                    + " "
                    + effRights.targetName());
    provUtil.dumpEffectiveRight(effRights, expandSetAttrs, expandGetAttrs);
  }
}
