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

import com.zimbra.cs.account.AttributeVersion;
import com.zimbra.cs.account.StoreAttributeManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
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

    AttributeManager am = StoreAttributeManager.getInstance();

    if (descArgs.mListSinceVersions) {
      printAvailableSinceVersions(am);
      return;
    }

    SortedSet<String> attrs = null;
    String specificAttr = null;

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

    } else if (descArgs.hasSinceFilter()) {
      printSinceFiltered(am, attrs, descArgs);
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

  private void printSinceFiltered(AttributeManager am, SortedSet<String> attrs, DescribeArgs descArgs) {
    var console = provUtil.getConsole();

    List<AttributeInfo> matched = new ArrayList<>();
    for (String attr : attrs) {
      AttributeInfo ai = am.getAttributeInfo(attr);
      if (ai == null) {
        continue;
      }
      if (descArgs.matchesSinceFilter(ai)) {
        matched.add(ai);
      }
    }

    String header = descArgs.mSince != null
            ? "Attributes introduced in " + descArgs.mSince + ":"
            : "Attributes introduced after " + descArgs.mSinceAfter + ":";
    console.println(header);
    console.println();

    if (matched.isEmpty()) {
      console.println("  (none)");
      console.println();
      console.println("Tip: run \"zmprov desc -list-since-versions\" to see all available since values.");
      return;
    }

    if (descArgs.mVerbose) {
      for (AttributeInfo ai : matched) {
        console.println(ai.getName());
        String desc = ai.getDescription();
        console.println(FileGenUtil.wrapComments((desc == null ? "" : desc), 70, "    "));
        console.println();
        for (DescribeArgs.Field f : DescribeArgs.Field.values()) {
          console.print(String.format("    %15s : %s%n", f.name(), DescribeArgs.Field.print(f, ai)));
        }
        console.println();
      }
      console.println(matched.size() + " attribute(s).");
      return;
    }

    int sinceWidth = "SINCE".length();
    int appliesWidth = "APPLIES TO".length();
    int nameWidth = "ATTRIBUTE".length();
    int defaultWidth = "DEFAULT".length();
    String[][] rows = new String[matched.size()][4];
    for (int i = 0; i < matched.size(); i++) {
      AttributeInfo ai = matched.get(i);
      rows[i][0] = formatSince(ai);
      rows[i][1] = formatAppliesTo(ai);
      rows[i][2] = ai.getName();
      rows[i][3] = formatDefaultOrDash(ai);
      sinceWidth = Math.max(sinceWidth, rows[i][0].length());
      appliesWidth = Math.max(appliesWidth, rows[i][1].length());
      nameWidth = Math.max(nameWidth, rows[i][2].length());
      defaultWidth = Math.max(defaultWidth, rows[i][3].length());
    }

    String fmt = "  %-" + sinceWidth + "s  %-" + appliesWidth + "s  %-" + nameWidth + "s  %-" + defaultWidth + "s%n";
    console.print(String.format(fmt, "SINCE", "APPLIES TO", "ATTRIBUTE", "DEFAULT"));
    console.print(String.format(fmt,
            "-".repeat(sinceWidth),
            "-".repeat(appliesWidth),
            "-".repeat(nameWidth),
            "-".repeat(defaultWidth)));
    for (String[] row : rows) {
      console.print(String.format(fmt, row[0], row[1], row[2], row[3]));
    }
    console.println();
    console.println(matched.size() + " attribute(s). Use -v for descriptions, or -a <name> for full details.");
  }

  private void printAvailableSinceVersions(AttributeManager am) {
    var console = provUtil.getConsole();

    Set<String> seenNames = new HashSet<>();
    List<AttributeInfo> all = new ArrayList<>();
    for (AttributeClass ac : AttributeClass.values()) {
      for (String name : am.getAllAttrsInClass(ac)) {
        if (seenNames.add(name)) {
          AttributeInfo ai = am.getAttributeInfo(name);
          if (ai != null) {
            all.add(ai);
          }
        }
      }
    }

    SortedMap<AttributeVersion, Integer> counts = countSinceVersions(all);
    int totalAttrsWithSince = counts.values().stream().mapToInt(Integer::intValue).sum();

    console.println("Available \"since\" versions in attribute definitions:");
    console.println();

    if (counts.isEmpty()) {
      console.println("  (none)");
      console.println();
      return;
    }

    int versionWidth = "VERSION".length();
    for (AttributeVersion v : counts.keySet()) {
      versionWidth = Math.max(versionWidth, v.toString().length());
    }
    String fmt = "  %-" + versionWidth + "s  %s%n";
    console.print(String.format(fmt, "VERSION", "ATTRIBUTES"));
    console.print(String.format(fmt, "-".repeat(versionWidth), "----------"));
    for (Map.Entry<AttributeVersion, Integer> e : counts.entrySet()) {
      console.print(String.format(fmt, e.getKey().toString(), e.getValue()));
    }
    console.println();
    console.println(counts.size() + " distinct version(s), " + totalAttrsWithSince + " attribute(s) with version metadata.");
    console.println("Use one of these with -since {version} or -since-after {version}.");
  }

  static SortedMap<AttributeVersion, Integer> countSinceVersions(Iterable<AttributeInfo> attrs) {
    SortedMap<AttributeVersion, Integer> counts = new TreeMap<>();
    for (AttributeInfo ai : attrs) {
      List<AttributeVersion> since = ai.getSince();
      if (since == null) {
        continue;
      }
      for (AttributeVersion v : since) {
        counts.merge(v, 1, Integer::sum);
      }
    }
    return counts;
  }

  private static String formatSince(AttributeInfo ai) {
    List<AttributeVersion> since = ai.getSince();
    if (since == null || since.isEmpty()) {
      return "-";
    }
    StringBuilder sb = new StringBuilder();
    for (AttributeVersion v : since) {
      if (sb.length() > 0) sb.append(",");
      sb.append(v.toString());
    }
    return sb.toString();
  }

  private static String formatAppliesTo(AttributeInfo ai) {
    Set<AttributeClass> applies = new LinkedHashSet<>();
    if (ai.getRequiredIn() != null) applies.addAll(ai.getRequiredIn());
    if (ai.getOptionalIn() != null) applies.addAll(ai.getOptionalIn());
    if (applies.isEmpty()) {
      return "-";
    }
    StringBuilder sb = new StringBuilder();
    for (AttributeClass ac : applies) {
      if (sb.length() > 0) sb.append(",");
      sb.append(ac.name());
    }
    return sb.toString();
  }

  private static String formatDefaultOrDash(AttributeInfo ai) {
    String defaults = DescribeArgs.Field.formatDefaults(ai);
    return defaults.isEmpty() ? "-" : defaults;
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

    console.println("zmprov desc -since 26.6");
    console.println("    print attributes introduced exactly in 26.6.x" + "\n");

    console.println("zmprov desc -since-after 26.5");
    console.println("    print attributes introduced after 26.5 (use after upgrades to discover");
    console.println("    new attributes that may need to be set on existing entries)" + "\n");

    console.println("zmprov desc -since-after 26.5 -v cos");
    console.println("    same as above, restricted to CoS-applicable attributes, with descriptions" + "\n");

    console.println("zmprov desc -list-since-versions");
    console.println("    list every distinct since-version found in attribute definitions, with");
    console.println("    a count of attributes per version (use to discover what to query)" + "\n");

    console.println("zmprov desc account -a zimbraId");
    console.println("    error: can only specify either an entry type or a specific attribute\n");

    provUtil.usage();
  }
}
