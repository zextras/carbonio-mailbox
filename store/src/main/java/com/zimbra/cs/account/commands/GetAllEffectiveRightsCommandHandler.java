package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.ArgException;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.accesscontrol.RightCommand;
import com.zimbra.cs.account.accesscontrol.TargetType;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.soap.admin.type.GranteeSelector;
import org.apache.http.HttpException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

class GetAllEffectiveRightsCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;

  public GetAllEffectiveRightsCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, ArgException {
    doGetAllEffectiveRights(args);
  }

  private void doGetAllEffectiveRights(String[] args) throws ServiceException, ArgException {
    RightArgs ra = new RightArgs(args);

    var prov = provUtil.getProvisioning();
    if (prov instanceof LdapProv) {
      // must provide grantee info
      RightArgs.getRightArgsGrantee(ra, true, false);
    } else {
      // has more args, use it for the requested grantee
      if (ra.mCurPos < args.length) {
        RightArgs.getRightArgsGrantee(ra, true, false);
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

    GranteeSelector.GranteeBy granteeBy =
            (ra.mGranteeIdOrName == null) ? null : ProvUtil.guessGranteeBy(ra.mGranteeIdOrName);

    RightCommand.AllEffectiveRights allEffRights =
            prov.getAllEffectiveRights(
                    ra.mGranteeType, granteeBy, ra.mGranteeIdOrName, expandSetAttrs, expandGetAttrs);

    var console = provUtil.getConsole();
    console.println(
            allEffRights.granteeType()
                    + " "
                    + allEffRights.granteeName()
                    + "("
                    + allEffRights.granteeId()
                    + ")"
                    + " has the following rights:");

    for (Map.Entry<TargetType, RightCommand.RightsByTargetType> rightsByTargetType :
            allEffRights.rightsByTargetType().entrySet()) {
      RightCommand.RightsByTargetType rbtt = rightsByTargetType.getValue();
      if (!rbtt.hasNoRight()) {
        dumpRightsByTargetType(rightsByTargetType.getKey(), rbtt, expandSetAttrs, expandGetAttrs);
      }
    }
  }

  private void dumpRightsByTargetType(
          TargetType targetType,
          RightCommand.RightsByTargetType rbtt,
          boolean expandSetAttrs,
          boolean expandGetAttrs) {
    var console = provUtil.getConsole();
    console.println("------------------------------------------------------------------");
    console.println("Target type: " + targetType.getCode());
    console.println("------------------------------------------------------------------");

    RightCommand.EffectiveRights er = rbtt.all();
    if (er != null) {
      console.println("On all " + targetType.getPrettyName() + " entries");
      provUtil.dumpEffectiveRight(er, expandSetAttrs, expandGetAttrs);
    }

    if (rbtt instanceof RightCommand.DomainedRightsByTargetType domainedRights) {
      for (RightCommand.RightAggregation rightsByDomains : domainedRights.domains()) {
        dumpRightAggregation(targetType, rightsByDomains, true, expandSetAttrs, expandGetAttrs);
      }
    }

    for (RightCommand.RightAggregation rightsByEntries : rbtt.entries()) {
      dumpRightAggregation(targetType, rightsByEntries, false, expandSetAttrs, expandGetAttrs);
    }
  }

  private void dumpRightAggregation(
          TargetType targetType,
          RightCommand.RightAggregation rightAggr,
          boolean domainScope,
          boolean expandSetAttrs,
          boolean expandGetAttrs) {
    Set<String> entries = rightAggr.entries();
    RightCommand.EffectiveRights er = rightAggr.effectiveRights();
    var console = provUtil.getConsole();
    for (String entry : entries) {
      if (domainScope) {
        console.println("On " + targetType.getCode() + " entries in domain " + entry);
      } else {
        console.println("On " + targetType.getCode() + " " + entry);
      }
    }
    provUtil.dumpEffectiveRight(er, expandSetAttrs, expandGetAttrs);
  }

}
