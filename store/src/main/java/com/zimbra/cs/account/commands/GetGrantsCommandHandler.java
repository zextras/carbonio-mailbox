package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.accesscontrol.RightCommand;
import com.zimbra.cs.account.accesscontrol.RightModifier;
import com.zimbra.soap.admin.type.GranteeSelector;
import com.zimbra.soap.type.TargetBy;

class GetGrantsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetGrantsCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    doGetGrants(args);
  }

  private void doGetGrants(String[] args) throws ServiceException, ArgException {
    RightArgs ra = new RightArgs(args);

    boolean granteeIncludeGroupsGranteeBelongs = true;

    while (ra.hasNext()) {
      String arg = ra.getNextArg();
      if ("-t".equals(arg)) {
        RightArgs.getRightArgsTarget(ra);
      } else if ("-g".equals(arg)) {
        RightArgs.getRightArgsGrantee(ra, true, false);
        if (ra.hasNext()) {
          String includeGroups = ra.getNextArg();
          if ("1".equals(includeGroups)) {
            granteeIncludeGroupsGranteeBelongs = true;
          } else if ("0".equals(includeGroups)) {
            granteeIncludeGroupsGranteeBelongs = false;
          } else {
            throw ServiceException.INVALID_REQUEST(
                    "invalid value for the include group flag, must be 0 or 1", null);
          }
        }
      }
    }

    TargetBy targetBy = (ra.mTargetIdOrName == null) ? null : ProvUtil.guessTargetBy(ra.mTargetIdOrName);
    GranteeSelector.GranteeBy granteeBy =
            (ra.mGranteeIdOrName == null) ? null : ProvUtil.guessGranteeBy(ra.mGranteeIdOrName);

    RightCommand.Grants grants =
            provUtil.getProvisioning().getGrants(
                    ra.mTargetType,
                    targetBy,
                    ra.mTargetIdOrName,
                    ra.mGranteeType,
                    granteeBy,
                    ra.mGranteeIdOrName,
                    granteeIncludeGroupsGranteeBelongs);

    var console = provUtil.getConsole();
    String format = "%-12.12s %-36.36s %-30.30s %-12.12s %-36.36s %-30.30s %s\n";
    console.print(String.format(
            format,
            "target type",
            "target id",
            "target name",
            "grantee type",
            "grantee id",
            "grantee name",
            "right"));
    console.print(String.format(
            format,
            "------------",
            "------------------------------------",
            "------------------------------",
            "------------",
            "------------------------------------",
            "------------------------------",
            "--------------------"));

    for (RightCommand.ACE ace : grants.getACEs()) {
      RightModifier rightModifier = ace.rightModifier();
      String rm = (rightModifier == null) ? "" : String.valueOf(rightModifier.getModifier());
      console.print(String.format(
              format,
              ace.targetType(),
              ace.targetId(),
              ace.targetName(),
              ace.granteeType(),
              ace.granteeId(),
              ace.granteeName(),
              rm + ace.right()));
    }
    console.println();
  }

}
