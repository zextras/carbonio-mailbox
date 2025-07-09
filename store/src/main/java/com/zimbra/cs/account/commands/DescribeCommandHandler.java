package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.SetUtil;
import com.zimbra.cs.InvalidCommandException;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.AttributeInfo;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.Command;
import com.zimbra.cs.account.CommandHandler;
import com.zimbra.cs.account.FileGenUtil;
import com.zimbra.cs.account.ProvUtil;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

class DescribeCommandHandler implements CommandHandler {
  private final ProvUtil provUtil;
  private static final Command command = Command.DESCRIBE;

  public DescribeCommandHandler(ProvUtil provUtil) {
    this.provUtil = provUtil;
  }

  @Override public void handle(String[] args) throws ServiceException, InvalidCommandException {
    doDescribe(args);
  }

  private void doDescribe(String[] args) throws ServiceException, InvalidCommandException {
    DescribeArgs descArgs = null;
    try {
      descArgs = DescribeArgs.parseDescribeArgs(args);
    } catch (ServiceException | NumberFormatException e) {
      descAttrsUsage(e);
      return;
    }

    SortedSet<String> attrs = null;
    String specificAttr = null;

    AttributeManager am = AttributeManager.getInstance();

    if (descArgs.mAttr != null) {
      // specific attr
      specificAttr = descArgs.mAttr;
    } else if (descArgs.mAttrClass != null) {
      // attrs in a class
      attrs = new TreeSet<>(am.getAllAttrsInClass(descArgs.mAttrClass));
      if (descArgs.mNonInheritedOnly) {
        Set<String> inheritFrom = null;
        Set<String> netAttrs = null;
        switch (descArgs.mAttrClass) {
          case account:
            netAttrs = new HashSet<>(attrs);
            inheritFrom = new HashSet<>(am.getAllAttrsInClass(AttributeClass.cos));
            netAttrs = SetUtil.subtract(netAttrs, inheritFrom);
            inheritFrom = new HashSet<>(am.getAllAttrsInClass(AttributeClass.domain)); // for
            // accountCosDomainInherited
            netAttrs = SetUtil.subtract(netAttrs, inheritFrom);
            break;
          case domain:
          case server:
            netAttrs = new HashSet<>(attrs);
            inheritFrom = new HashSet<>(am.getAllAttrsInClass(AttributeClass.globalConfig));
            netAttrs = SetUtil.subtract(netAttrs, inheritFrom);
            break;
        }

        if (netAttrs != null) {
          attrs = new TreeSet<>(netAttrs);
        }
      }

      if (descArgs.mOnThisObjectTypeOnly) {
        TreeSet<String> netAttrs = new TreeSet<>();
        for (String attr : attrs) {
          AttributeInfo ai = am.getAttributeInfo(attr);
          if (ai == null) {
            continue;
          }
          Set<AttributeClass> requiredIn = ai.getRequiredIn();
          Set<AttributeClass> optionalIn = ai.getOptionalIn();
          if ((requiredIn == null || requiredIn.size() == 1)
                  && (optionalIn == null || optionalIn.size() == 1)) {
            netAttrs.add(attr);
          }
        }
        attrs = netAttrs;
      }

    } else {
      //
      // all attrs
      //

      // am.getAllAttrs() only contains attrs with AttributeInfo
      // not extension attrs
      // attrs = new TreeSet<String>(am.getAllAttrs());

      // attr sets for each AttributeClass contain attrs in the extensions, use them
      attrs = new TreeSet<>();
      for (AttributeClass ac : AttributeClass.values()) {
        attrs.addAll(am.getAllAttrsInClass(ac));
      }
    }

    var console = provUtil.getConsole();
    if (specificAttr != null) {
      AttributeInfo ai = am.getAttributeInfo(specificAttr);
      if (ai == null) {
        console.println("no attribute info for " + specificAttr);
      } else {
        console.println(ai.getName());
        // description
        String desc = ai.getDescription();
        console.println(FileGenUtil.wrapComments((desc == null ? "" : desc), 70, "    "));
        console.println();

        for (DescribeArgs.Field f : DescribeArgs.Field.values()) {
          console.print(String.format("    %15s : %s%n", f.name(), DescribeArgs.Field.print(f, ai)));
        }
      }
      console.println();

    } else {
      for (String attr : attrs) {
        AttributeInfo ai = am.getAttributeInfo(attr);
        if (ai == null) {
          console.println(attr + " (no attribute info)");
          continue;
        }
        String attrName = ai.getName(); // camel case name
        console.println(attrName);
        if (descArgs.mVerbose) {
          String desc = ai.getDescription();
          console.println(FileGenUtil.wrapComments((desc == null ? "" : desc), 70, "    ") + "\n");
        }
      }
    }
  }

  private String formatAllEntryTypes() {
    StringBuilder sb = new StringBuilder();
    for (AttributeClass ac : AttributeClass.values()) {
      if (ac.isProvisionable()) {
        sb.append(ac.name()).append(",");
      }
    }
    return sb.substring(0, sb.length() - 1); // trim the ending ,
  }

  private void descAttrsUsage(Exception e) throws InvalidCommandException {
    var console = provUtil.getConsole();
    console.println(e.getMessage() + "\n");

    console.print(String.format("usage:  %s(%s) %s%n", command.getName(), command.getAlias(), command.getHelp()));

    console.println();
    console.println("Valid entry types: " + formatAllEntryTypes() + "\n");

    console.println("Examples:");

    console.println("zmprov desc");
    console.println("    print attribute name of all attributes" + "\n");

    console.println("zmprov desc -v");
    console.println("    print attribute name and description of all attributes" + "\n");

    console.println("zmprov desc account");
    console.println("    print attribute name of all account attributes" + "\n");

    console.println("zmprov desc -ni -v account");
    console.println(
            "    print attribute name and description of all non-inherited account" + " attributes, ");
    console.println("    that is, attributes that are on account but not on cos" + "\n");

    console.println("zmprov desc -ni domain");
    console.println("    print attribute name of all non-inherited domain attributes, ");
    console.println("    that is, attributes that are on domain but not on global config" + "\n");

    /*
     * -only is *not* a documented option, we could expose it if we want, handy for engineering tasks, not as useful
     * for users
     *
     * console.println("zmprov desc -only globalConfig");
     * console.println("    print attribute name of all attributes that are on global config only" + "\n");
     */

    console.println("zmprov desc -a zimbraId");
    console.println(
            "    print attribute name, description, and all properties of attribute" + " zimbraId\n");

    console.println("zmprov desc account -a zimbraId");
    console.println("    error: can only specify either an entry type or a specific attribute\n");

    provUtil.usage();
  }
}
