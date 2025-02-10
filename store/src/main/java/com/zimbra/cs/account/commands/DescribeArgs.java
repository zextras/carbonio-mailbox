package com.zimbra.cs.account.commands;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Version;
import com.zimbra.cs.account.AttributeCallback;
import com.zimbra.cs.account.AttributeCardinality;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.AttributeFlag;
import com.zimbra.cs.account.AttributeInfo;
import com.zimbra.cs.account.AttributeServerType;

import java.util.List;
import java.util.Set;

class DescribeArgs {

  static DescribeArgs parseDescribeArgs(String[] args) throws ServiceException {
    DescribeArgs descArgs = new DescribeArgs();

    int i = 1;
    while (i < args.length) {
      if ("-v".equals(args[i])) {
        if (descArgs.mAttr != null) {
          throw ServiceException.INVALID_REQUEST("cannot specify -v when -a is specified", null);
        }
        descArgs.mVerbose = true;
      } else if (args[i].startsWith("-ni")) {
        if (descArgs.mAttr != null) {
          throw ServiceException.INVALID_REQUEST("cannot specify -ni when -a is specified", null);
        }
        descArgs.mNonInheritedOnly = true;
      } else if (args[i].startsWith("-only")) {
        if (descArgs.mAttr != null) {
          throw ServiceException.INVALID_REQUEST("cannot specify -only when -a is specified", null);
        }
        descArgs.mOnThisObjectTypeOnly = true;
      } else if (args[i].startsWith("-a")) {
        if (descArgs.mAttrClass != null) {
          throw ServiceException.INVALID_REQUEST(
              "cannot specify -a when entry type is specified", null);
        }
        if (descArgs.mAttr != null) {
          throw ServiceException.INVALID_REQUEST(
              "attribute is already specified as " + descArgs.mAttr, null);
        }
        if (args.length <= i + 1) {
          throw ServiceException.INVALID_REQUEST("not enough args", null);
        }
        i++;
        descArgs.mAttr = args[i];

      } else {
        if (descArgs.mAttr != null) {
          throw ServiceException.INVALID_REQUEST("too many args", null);
        }
        if (descArgs.mAttrClass != null) {
          throw ServiceException.INVALID_REQUEST(
              "entry type is already specified as " + descArgs.mAttrClass, null);
        }
        AttributeClass ac = AttributeClass.fromString(args[i]);
        if (ac == null || !ac.isProvisionable()) {
          String name = ac == null ? "null" : ac.name();
          throw ServiceException.INVALID_REQUEST("invalid entry type " + name, null);
        }
        descArgs.mAttrClass = ac;
      }
      i++;
    }

    if ((descArgs.mNonInheritedOnly || descArgs.mOnThisObjectTypeOnly)
        && descArgs.mAttrClass == null) {
      throw ServiceException.INVALID_REQUEST(
          "-ni -only must be specified with an entry type", null);
    }

    return descArgs;
  }

  enum Field {
    type, // attribute type
    value, // value for enum or regex attributes
    callback, // class name of AttributeCallback object to invoke on changes to attribute.
    immutable, // whether this attribute can be modified directly
    cardinality, // single or multi
    requiredIn, // comma-seperated list containing classes in which this attribute is
    // required
    optionalIn, // comma-seperated list containing classes in which this attribute can
    // appear
    flags, // attribute flags
    defaults, // default value on global config or default COS(for new install) and all
    // upgraded COS's
    min, // min value for integers and durations. defaults to Integer.MIN_VALUE"
    max, // max value for integers and durations, max length for strings/email, defaults to
    // Integer.MAX_VALUE
    id, // leaf OID of the attribute
    requiresRestart, // server(s) need be to restarted after changing this attribute
    since, // version since which the attribute had been introduced
    deprecatedSince; // version since which the attribute had been deprecaed

    static String formatDefaults(AttributeInfo ai) {
      StringBuilder sb = new StringBuilder();
      for (String d : ai.getDefaultCosValues()) {
        sb.append(d).append(",");
      }
      for (String d : ai.getGlobalConfigValues()) {
        sb.append(d).append(",");
      }
      return sb.length() == 0 ? "" : sb.substring(0, sb.length() - 1); // trim the ending ,
    }

    static String formatRequiredIn(AttributeInfo ai) {
      Set<AttributeClass> requiredIn = ai.getRequiredIn();
      if (requiredIn == null) {
        return "";
      }
      StringBuilder sb = new StringBuilder();

      for (AttributeClass ac : requiredIn) {
        sb.append(ac.name()).append(",");
      }
      return sb.substring(0, sb.length() - 1); // trim the ending ,
    }

    static String formatOptionalIn(AttributeInfo ai) {
      Set<AttributeClass> optionalIn = ai.getOptionalIn();
      if (optionalIn == null) {
        return "";
      }
      StringBuilder sb = new StringBuilder();
      for (AttributeClass ac : optionalIn) {
        sb.append(ac.name()).append(",");
      }
      return sb.substring(0, sb.length() - 1); // trim the ending ,
    }

    static String formatFlags(AttributeInfo ai) {
      StringBuilder sb = new StringBuilder();
      for (AttributeFlag f : AttributeFlag.values()) {
        if (ai.hasFlag(f)) {
          sb.append(f.name()).append(",");
        }
      }
      return sb.length() == 0 ? "" : sb.substring(0, sb.length() - 1); // trim the ending ,
    }

    static String formatRequiresRestart(AttributeInfo ai) {
      StringBuilder sb = new StringBuilder();
      List<AttributeServerType> requiresRetstart = ai.getRequiresRestart();
      if (requiresRetstart != null) {
        for (AttributeServerType ast : requiresRetstart) {
          sb.append(ast.name()).append(",");
        }
      }
      return sb.length() == 0 ? "" : sb.substring(0, sb.length() - 1); // trim the ending ,
    }

    static String print(Field field, AttributeInfo ai) {
      String out = null;

      switch (field) {
        case type:
          out = ai.getType().getName();
          break;
        case value:
          out = ai.getValue();
          break;
        case callback:
          AttributeCallback acb = ai.getCallback();
          if (acb != null) {
            out = acb.getClass().getSimpleName();
          }
          break;
        case immutable:
          out = Boolean.toString(ai.isImmutable());
          break;
        case cardinality:
          AttributeCardinality card = ai.getCardinality();
          if (card != null) {
            out = card.name();
          }
          break;
        case requiredIn:
          out = formatRequiredIn(ai);
          break;
        case optionalIn:
          out = formatOptionalIn(ai);
          break;
        case flags:
          out = formatFlags(ai);
          break;
        case defaults:
          out = formatDefaults(ai);
          break;
        case min:
          long min = ai.getMin();
          if (min != Long.MIN_VALUE && min != Integer.MIN_VALUE) {
            out = Long.toString(min);
          }
          break;
        case max:
          long max = ai.getMax();
          if (max != Long.MAX_VALUE && max != Integer.MAX_VALUE) {
            out = Long.toString(max);
          }
          break;
        case id:
          int id = ai.getId();
          if (id != -1) {
            out = Integer.toString(ai.getId());
          }
          break;
        case requiresRestart:
          out = formatRequiresRestart(ai);
          break;
        case since:
          List<Version> since = ai.getSince();
          if (since != null) {
            StringBuilder sb = new StringBuilder();
            for (Version version : since) {
              sb.append(version.toString()).append(",");
            }
            sb.setLength(sb.length() - 1);
            out = sb.toString();
          }
          break;
        case deprecatedSince:
          Version depreSince = ai.getDeprecatedSince();
          if (depreSince != null) {
            out = depreSince.toString();
          }
          break;
      }

      if (out == null) {
        out = "";
      }
      return out;
    }
  }

  /*
   * args when an object class is specified
   */
  boolean mNonInheritedOnly;
  boolean mOnThisObjectTypeOnly;
  AttributeClass mAttrClass;
  boolean mVerbose;

  /*
   * args when a specific attribute is specified
   */
  String mAttr;
}
