// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import com.google.common.annotations.VisibleForTesting;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.CliUtil;
import com.zimbra.common.util.DateUtil;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.common.util.SetUtil;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.Version;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AttributeManager.ObjectClassInfo;
import com.zimbra.cs.account.ldap.LdapProv;
import com.zimbra.cs.util.MemoryUnitUtil;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class AttributeManagerUtil {

  private static final Log logger = LogFactory.getLog(AttributeManagerUtil.class);

  private static final Options options = new Options();

  // multi-line continuation prefix chars
  private static final String ML_CONT_PREFIX = "  ";

  static {
    options.addOption("h", "help", false, "display this  usage info");
    options.addOption("o", "output", true, "output file (default it to generate output to stdout)");
    options.addOption(
        "a",
        "action",
        true,
        "[generateLdapSchema | generateGlobalConfigLdif | generateDefaultCOSLdif |"
            + " generateSchemaLdif]");
    options.addOption("t", "template", true, "template for LDAP schema");
    options.addOption("r", "regenerateFile", true, "Java file to regenerate");

    Option iopt = new Option("i", "input", true, "attrs definition xml input file (can repeat)");
    iopt.setArgs(Option.UNLIMITED_VALUES);
    options.addOption(iopt);

    /*
     * options for the listAttrs action
     */
    Option copt = new Option("c", "inclass", true, "list attrs in class  (can repeat)");
    copt.setArgs(Option.UNLIMITED_VALUES);
    options.addOption(copt);

    Option nopt = new Option("n", "notinclass", true, "not list attrs in class  (can repeat)");
    nopt.setArgs(Option.UNLIMITED_VALUES);
    options.addOption(nopt);

    Option fopt = new Option("f", "flags", true, "flags to print  (can repeat)");
    fopt.setArgs(Option.UNLIMITED_VALUES);
    options.addOption(fopt);
  }

  private final AttributeManager attrMgr;

  private AttributeManagerUtil(AttributeManager am) {
    attrMgr = am;
  }

  private static String enumName(AttributeInfo ai) {
    String enumName = ai.getName();
    if (enumName.startsWith("zimbra")) {
      enumName = enumName.substring(6);
    }
    enumName =
        StringUtil.escapeJavaIdentifier(
            enumName.substring(0, 1).toUpperCase() + enumName.substring(1));
    return enumName;
  }

  private static void generateEnum(StringBuilder result, AttributeInfo ai) {

    Map<String, String> values = new LinkedHashMap<>();
    for (String v : ai.getEnumSet()) {
      values.put(v, StringUtil.escapeJavaIdentifier(v));
    }

    String enumName = enumName(ai);

    result.append(String.format("%n"));
    result.append(String.format("    public static enum %s {%n", enumName));
    Set<Map.Entry<String, String>> set = values.entrySet();
    int i = 1;
    for (Map.Entry<String, String> entry : set) {
      result.append(
          String.format(
              "        %s(\"%s\")%s%n",
              entry.getValue(), entry.getKey(), i == set.size() ? ";" : ","));
      i++;
    }

    result.append(String.format("        private String mValue;%n"));
    result.append(
        String.format("        private %s(String value) { mValue = value; }%n", enumName));
    result.append(String.format("        public String toString() { return mValue; }%n"));
    result.append(
        String.format(
            "        public static %s fromString(String s) throws ServiceException {%n", enumName));
    result.append(String.format("            for (%s value : values()) {%n", enumName));
    result.append(String.format("                if (value.mValue.equals(s)) return value;%n"));
    result.append(String.format("             }%n"));
    result.append(
        String.format(
            "             throw ServiceException.INVALID_REQUEST(\"invalid value: \"+s+\", valid"
                + " values: \"+ Arrays.asList(values()), null);%n"));
    result.append(String.format("        }%n"));
    for (Map.Entry<String, String> entry : set) {
      result.append(
          String.format(
              "        public boolean is%s() { return this == %s;}%n",
              StringUtil.capitalize(entry.getValue()), entry.getValue()));
    }
    result.append(String.format("    }%n"));
  }

  private static String defaultValue(AttributeInfo ai, AttributeClass ac) {
    List<String> values;
    switch (ac) {
      case account:
      case calendarResource:
      case cos:
        values = ai.getDefaultCosValues();
        break;
      case domain:
        if (ai.hasFlag(AttributeFlag.domainInherited)) {
          values = ai.getGlobalConfigValues();
        } else {
          return null;
        }
        break;
      case server:
        if (ai.hasFlag(AttributeFlag.serverInherited)) {
          values = ai.getGlobalConfigValues();
        } else {
          return null;
        }
        break;
      case globalConfig:
        values = ai.getGlobalConfigValues();
        break;
      default:
        return null;
    }
    if (values == null || values.isEmpty()) {
      return null;
    }

    if (ai.getCardinality() != AttributeCardinality.multi) {
      return values.get(0);
    } else {
      StringBuilder result = new StringBuilder();
      result.append("new String[] {");
      boolean first = true;
      for (String v : values) {
        if (!first) {
          result.append(",");
        } else {
          first = false;
        }
        result.append("\"");
        result.append(v.replace("\"", "\\\""));
        result.append("\"");
      }
      result.append("}");
      return result.toString();
    }
  }

  @VisibleForTesting
  public static void generateGetter(
      StringBuilder result, AttributeInfo ai, boolean asString, AttributeClass ac) {
    String javaType;
    String javaBody;
    String javaDocReturns;
    String name = ai.getName();
    AttributeType type = asString ? AttributeType.TYPE_STRING : ai.getType();
    boolean asStringDoc = false;

    String methodName = ai.getName();
    if (methodName.startsWith("zimbra")) {
      methodName = methodName.substring(6);
    }
    methodName =
        (type == AttributeType.TYPE_BOOLEAN ? "is" : "get")
            + methodName.substring(0, 1).toUpperCase()
            + methodName.substring(1);
    if (asString) {
      methodName += "AsString";
    }

    String defaultValue = defaultValue(ai, ac);
    String dynamic = Boolean.TRUE.equals(ai.isDynamic()) ? "dynamicComponent" : "null";

    switch (type) {
      case TYPE_BOOLEAN:
        defaultValue = "TRUE".equalsIgnoreCase(defaultValue) ? "true" : "false";
        javaType = "boolean";
        if (Boolean.TRUE.equals(ai.isEphemeral())) {
          javaBody =
              String.format(
                  "return getEphemeralAttr(ZAttrProvisioning.A_%s, %s).getBoolValue(%s);",
                  name, dynamic, defaultValue);
        } else {
          javaBody =
              String.format(
                  "return getBooleanAttr(ZAttrProvisioning.A_%s, %s, true);", name, defaultValue);
        }
        javaDocReturns = String.format(", or %s if unset", defaultValue);
        break;
      case TYPE_BINARY:
      case TYPE_CERTIFICATE:
        defaultValue = "null";
        javaType = "byte[]";
        if (Boolean.TRUE.equals(ai.isEphemeral())) {
          javaBody =
              String.format(
                  "String v = getEphemeralAttr(ZAttrProvisioning.A_%s, %s).getValue(%s); return v"
                      + " == null ? null : ByteUtil.decodeLDAPBase64(v);",
                  name, dynamic, defaultValue);
        } else {
          javaBody = String.format("return getBinaryAttr(ZAttrProvisioning.A_%s, true);", name);
        }
        javaDocReturns = ", or null if unset";
        break;
      case TYPE_INTEGER:
        if (defaultValue == null) {
          defaultValue = "-1";
        }
        javaType = "int";
        if (Boolean.TRUE.equals(ai.isEphemeral())) {
          javaBody =
              String.format(
                  "return getEphemeralAttr(ZAttrProvisioning.A_%s, %s).getIntValue(%s);",
                  name, dynamic, defaultValue);
        } else {
          javaBody =
              String.format(
                  "return getIntAttr(ZAttrProvisioning.A_%s, %s, true);", name, defaultValue);
        }
        javaDocReturns = String.format(", or %s if unset", defaultValue);
        break;
      case TYPE_PORT:
        if (defaultValue == null) {
          defaultValue = "-1";
        }
        javaType = "int";
        if (Boolean.TRUE.equals(ai.isEphemeral())) {
          javaBody =
              String.format(
                  "return getEphemeralAttr(ZAttrProvisioning.A_%s, %s).getIntValue(%s);",
                  name, dynamic, defaultValue);
        } else {
          javaBody =
              String.format(
                  "return getIntAttr(ZAttrProvisioning.A_%s, %s, true);", name, defaultValue);
        }
        javaDocReturns = String.format(", or %s if unset", defaultValue);
        asStringDoc = true;
        break;
      case TYPE_ENUM:
        javaType = "ZAttrProvisioning." + enumName(ai);
        if (defaultValue != null) {
          defaultValue = javaType + "." + StringUtil.escapeJavaIdentifier(defaultValue);
        } else {
          defaultValue = "null";
        }
        if (Boolean.TRUE.equals(ai.isEphemeral())) {
          javaBody =
              String.format(
                  "try { String v = getEphemeralAttr(ZAttrProvisioning.A_%s, %s).getValue(); return"
                      + " v == null ? %s : ZAttrProvisioning.%s.fromString(v); }"
                      + " catch(com.zimbra.common.service.ServiceException e) { return %s; }",
                  name, dynamic, defaultValue, enumName(ai), defaultValue);
        } else {
          javaBody =
              String.format(
                  "try { String v = getAttr(ZAttrProvisioning.A_%s, true, true); return v == null ?"
                      + " %s : ZAttrProvisioning.%s.fromString(v); }"
                      + " catch(com.zimbra.common.service.ServiceException e) { return %s; }",
                  name, defaultValue, enumName(ai), defaultValue);
        }
        javaDocReturns = String.format(", or %s if unset and/or has invalid value", defaultValue);
        break;
      case TYPE_LONG:
        if (defaultValue == null) {
          defaultValue = "-1";
        }
        javaType = "long";
        if (Boolean.TRUE.equals(ai.isEphemeral())) {
          javaBody =
              String.format(
                  "return getEphemeralAttr(ZAttrProvisioning.A_%s, %s).getLongValue(%sL);",
                  name, dynamic, defaultValue);
        } else {
          javaBody =
              String.format(
                  "return getLongAttr(ZAttrProvisioning.A_%s, %sL, true);",
                  name, new MemoryUnitUtil(1024).convertToBytes(defaultValue));
        }
        javaDocReturns = String.format(", or %s if unset", defaultValue);
        break;
      case TYPE_DURATION:
        String defaultDurationStrValue;
        if (defaultValue != null) {
          defaultDurationStrValue = " (" + defaultValue + ") ";
          defaultValue = String.valueOf(DateUtil.getTimeInterval(defaultValue, -1));
        } else {
          defaultValue = "-1";
          defaultDurationStrValue = "";
        }
        if (Boolean.TRUE.equals(ai.isEphemeral())) {
          javaBody =
              String.format(
                  "return getEphemeralTimeInterval(ZAttrProvisioning.A_%s, %s, %sL);",
                  name, dynamic, defaultValue);
        } else {
          javaBody =
              String.format(
                  "return getTimeInterval(ZAttrProvisioning.A_%s, %sL, true);", name, defaultValue);
        }
        javaDocReturns =
            String.format(
                " in millseconds, or %s%s if unset", defaultValue, defaultDurationStrValue);
        javaType = "long";
        asStringDoc = true;
        break;
      case TYPE_GENTIME:
        javaType = "Date";
        if (Boolean.TRUE.equals(ai.isEphemeral())) {
          javaBody =
              String.format(
                  "String v = getEphemeralAttr(ZAttrProvisioning.A_%s, %s).getValue(%s); return v"
                      + " == null ? null : LdapDateUtil.parseGeneralizedTime(v);",
                  name, dynamic, defaultValue);
        } else {
          javaBody =
              String.format(
                  "return getGeneralizedTimeAttr(ZAttrProvisioning.A_%s, null, true);", name);
        }
        javaDocReturns = " as Date, null if unset or unable to parse";
        asStringDoc = true;
        break;
      default:
        if (ai.getCardinality() != AttributeCardinality.multi) {
          if (defaultValue != null) {
            defaultValue = "\"" + defaultValue.replace("\"", "\\\"") + "\"";
          } else {
            defaultValue = "null";
          }
          javaType = "String";
          if (Boolean.TRUE.equals(ai.isEphemeral())) {
            javaBody =
                String.format(
                    "return getEphemeralAttr(ZAttrProvisioning.A_%s, %s).getValue(%s);",
                    name, dynamic, defaultValue);
          } else {
            javaBody =
                String.format(
                    "return getAttr(ZAttrProvisioning.A_%s, %s, true);", name, defaultValue);
          }
          javaDocReturns = String.format(", or %s if unset", defaultValue);
        } else {
          if (Boolean.TRUE.equals(ai.isEphemeral())) {
            javaType = "String";
            javaBody =
                String.format(
                    "return getEphemeralAttr(ZAttrProvisioning.A_%s, %s).getValue(%s);",
                    name, dynamic, defaultValue);
          } else {
            javaType = "String[]";
            if (defaultValue == null) {
              javaBody =
                  String.format("return getMultiAttr(ZAttrProvisioning.A_%s, true, true);", name);
            } else {
              javaBody =
                  String.format(
                      "String[] value = getMultiAttr(ZAttrProvisioning.A_%s, true, true); return"
                          + " value.length > 0 ? value : %s;",
                      name, defaultValue);
            }
          }
          javaDocReturns = ", or empty array if unset";
        }
        break;
    }

    result.append("\n    /**\n");
    if (ai.getDescription() != null) {
      result.append(
          FileGenUtil.wrapComments(StringUtil.escapeHtml(ai.getDescription()), 70, "     * "));
      result.append("\n");
    }
    if (ai.getType() == AttributeType.TYPE_ENUM) {
      result.append("     *\n");
      result.append(String.format("     * <p>Valid values: %s%n", ai.getEnumSet().toString()));
    }
    if (asStringDoc) {
      result.append("     *\n");
      result.append(
          String.format("     * <p>Use %sAsString to access value as a string.%n", methodName));
      result.append("     *\n");
      result.append(String.format("     * @see #%sAsString()%n", methodName));
    }
    if (Boolean.TRUE.equals(ai.isEphemeral())) {
      result.append("     *\n");
      result.append("     * Ephemeral attribute - requests routed to EphemeralStore\n");
      result.append("     *\n");
      result.append(
          "     * @throws com.zimbra.common.service.ServiceException if error on accessing"
              + " ephemeral data\n");
    }
    result.append("     *\n");
    result.append(String.format("     * @return %s%s%n", name, javaDocReturns));
    if (ai.getSince() != null) {
      result.append("     *\n");
      result.append(String.format("     * @since ZCS %s%n", versionListAsString(ai.getSince())));
    }
    result.append("     */\n");
    result.append(String.format("    @ZAttr(id=%d)%n", ai.getId()));
    result.append(
        String.format(
            "    public %s %s(%s)",
            javaType,
            methodName,
            Boolean.TRUE.equals(ai.isDynamic()) ? "String dynamicComponent" : ""));
    if (Boolean.TRUE.equals(ai.isEphemeral())) {
      result.append(" throws com.zimbra.common.service.ServiceException");
    }
    result.append(String.format(" {%n        %s%n    }%n", javaBody));
  }

  private static String versionListAsString(List<Version> versions) {
    if (versions == null || versions.isEmpty()) {
      return "";
    } else if (versions.size() == 1) {
      return versions.iterator().next().toString();
    } else {
      StringBuilder sb = new StringBuilder();
      for (Version version : versions) {
        sb.append(version.toString()).append(",");
      }
      sb.setLength(sb.length() - 1);
      return sb.toString();
    }
  }

  private static void generateSetters(
      StringBuilder result, AttributeInfo ai, boolean asString, SetterType setterType) {
    generateSetter(result, ai, asString, setterType, true);
    generateSetter(result, ai, asString, setterType, false);
  }

  @VisibleForTesting
  public static void generateSetter(
      StringBuilder result,
      AttributeInfo attributeInfo,
      boolean asString,
      SetterType setterType,
      boolean noMap) {
    if (Boolean.TRUE.equals(attributeInfo.isEphemeral())) {
      if (!noMap) {
        return; // don't generate any epheemeral setters with the map parameter
      } else if (Boolean.TRUE.equals(attributeInfo.isDynamic())
          && (setterType == SetterType.UNSET || setterType == SetterType.SET)) {
        // don't generate ephemeral setters/unsetters for dynamic ephemeral attributes,
        // since we don't support deleting all values for a key.
        return;
      }
    }
    String javaType;
    String putParam;

    String name = attributeInfo.getName();

    AttributeType type = asString ? AttributeType.TYPE_STRING : attributeInfo.getType();

    String methodName = attributeInfo.getName();
    if (methodName.startsWith("zimbra")) {
      methodName = methodName.substring(6);
    }
    methodName =
        setterType.name().toLowerCase()
            + methodName.substring(0, 1).toUpperCase()
            + methodName.substring(1);
    if (asString) {
      methodName += "AsString";
    }

    switch (type) {
      case TYPE_BOOLEAN:
        javaType = "boolean";
        putParam = String.format("%s ? TRUE : FALSE", name);
        break;
      case TYPE_BINARY:
      case TYPE_CERTIFICATE:
        javaType = "byte[]";
        putParam = String.format("%s==null ? \"\" : ByteUtil.encodeLDAPBase64(%s)", name, name);
        break;
      case TYPE_INTEGER:
      case TYPE_PORT:
        javaType = "int";
        putParam = String.format("Integer.toString(%s)", name);
        break;
      case TYPE_LONG:
        javaType = "long";
        putParam = String.format("Long.toString(%s)", name);
        break;
      case TYPE_GENTIME:
        javaType = "Date";
        putParam =
            String.format("%s==null ? \"\" : LdapDateUtil.toGeneralizedTime(%s)", name, name);
        break;
      case TYPE_ENUM:
        javaType = "ZAttrProvisioning." + enumName(attributeInfo);
        putParam = String.format("%s.toString()", name);
        break;
      default:
        if (attributeInfo.getCardinality() != AttributeCardinality.multi) {
          javaType = "String";
        } else {
          if (setterType == SetterType.SET) {
            javaType = "String[]";
          } else {
            javaType = "String";
          }
        }
        putParam = String.format("%s", name);
        break;
    }

    String mapType = "Map<String,Object>";

    result.append("\n    /**\n");
    if (attributeInfo.getDescription() != null) {
      result.append(
          FileGenUtil.wrapComments(
              StringUtil.escapeHtml(attributeInfo.getDescription()), 70, "     * "));
      result.append("\n");
    }
    if (attributeInfo.getType() == AttributeType.TYPE_ENUM) {
      result.append("     *\n");
      result.append(
          String.format("     * <p>Valid values: %s%n", attributeInfo.getEnumSet().toString()));
    }
    result.append("     *\n");

    StringBuilder paramDoc = new StringBuilder();
    String body = "";
    if (Boolean.TRUE.equals(attributeInfo.isEphemeral())) {
      paramDoc.append("     * Ephemeral attribute - requests routed to EphemeralStore\n");
      paramDoc.append("     *\n");
    }
    String expiry = Boolean.TRUE.equals(attributeInfo.isExpirable()) ? "expiration" : "null";
    String dynamic = Boolean.TRUE.equals(attributeInfo.isDynamic()) ? "dynamicComponent" : "null";
    switch (setterType) {
      case SET:
        if (Boolean.TRUE.equals(attributeInfo.isEphemeral())) {
          body =
              String.format(
                  "        modifyEphemeralAttr(ZAttrProvisioning.A_%s, %s, %s, false, %s);%n",
                  name, dynamic, putParam, expiry);
        } else {
          body = String.format("        attrs.put(ZAttrProvisioning.A_%s, %s);%n", name, putParam);
        }
        paramDoc.append(String.format("     * @param %s new value%n", name));
        break;
      case ADD:
        if (Boolean.TRUE.equals(attributeInfo.isEphemeral())) {
          body =
              String.format(
                  "        modifyEphemeralAttr(ZAttrProvisioning.A_%s, %s, %s, true, %s);%n",
                  name, dynamic, putParam, expiry);
        } else {
          body =
              String.format(
                  "        StringUtil.addToMultiMap(attrs, \"+\"  + ZAttrProvisioning.A_%s, %s);%n",
                  name, name);
        }
        paramDoc.append(String.format("     * @param %s new to add to existing values%n", name));
        break;
      case REMOVE:
        if (Boolean.TRUE.equals(attributeInfo.isEphemeral())) {
          body =
              String.format(
                  "        deleteEphemeralAttr(ZAttrProvisioning.A_%s, %s, %s);%n",
                  name, dynamic, putParam);
        } else {
          body =
              String.format(
                  "        StringUtil.addToMultiMap(attrs, \"-\"  + ZAttrProvisioning.A_%s, %s);%n",
                  name, name);
        }
        paramDoc.append(String.format("     * @param %s existing value to remove%n", name));
        break;
      case UNSET:
        if (Boolean.TRUE.equals(attributeInfo.isEphemeral())) {
          body = String.format("        deleteEphemeralAttr(ZAttrProvisioning.A_%s);%n", name);
        } else {
          body = String.format("        attrs.put(ZAttrProvisioning.A_%s, \"\");%n", name);
        }
        // paramDoc = null;
        break;
      case PURGE:
        body = String.format("        purgeEphemeralAttr(ZAttrProvisioning.A_%s);%n", name);
        break;
      case HAS:
        body =
            String.format(
                "        return hasEphemeralAttr(ZAttrProvisioning.A_%s, %s);%n", name, dynamic);
        break;
      default:
        break;
    }

    result.append(paramDoc);
    if (!noMap) {
      result.append(
          String.format(
              "     * @param attrs existing map to populate, or null to create a new map%n"));
      result.append("     * @return populated map to pass into Provisioning.modifyAttrs\n");
    } else {
      result.append(
          "     * @throws com.zimbra.common.service.ServiceException if error during update\n");
    }
    if (attributeInfo.getSince() != null) {
      result.append("     *\n");
      result.append(
          String.format("     * @since ZCS %s%n", versionListAsString(attributeInfo.getSince())));
    }
    result.append("     */\n");
    result.append(String.format("    @ZAttr(id=%d)%n", attributeInfo.getId()));
    if (noMap) {
      String expiryParam =
          Boolean.TRUE.equals(attributeInfo.isExpirable())
              ? ", com.zimbra.cs.ephemeral.EphemeralInput.Expiration expiration"
              : "";
      if (Boolean.TRUE.equals(attributeInfo.isEphemeral())) {
        switch (setterType) {
          case SET:
            result.append(
                String.format(
                    "    public void %s(%s %s%s) throws com.zimbra.common.service.ServiceException"
                        + " {%n",
                    methodName, javaType, name, expiryParam));
            break;
          case ADD:
            if (Boolean.TRUE.equals(attributeInfo.isDynamic())) {
              result.append(
                  String.format(
                      "    public void %s(String dynamicComponent, %s %s%s) throws"
                          + " com.zimbra.common.service.ServiceException {%n",
                      methodName, javaType, name, expiryParam));
            } else {
              result.append(
                  String.format(
                      "    public void %s(%s %s%s) throws"
                          + " com.zimbra.common.service.ServiceException {%n",
                      methodName, javaType, name, expiryParam));
            }
            break;
          case UNSET:
          case PURGE:
            result.append(
                String.format(
                    "    public void %s() throws com.zimbra.common.service.ServiceException {%n",
                    methodName));
            break;
          case REMOVE:
            if (Boolean.TRUE.equals(attributeInfo.isDynamic())) {
              result.append(
                  String.format(
                      "    public void %s(String dynamicComponent, %s %s) throws"
                          + " com.zimbra.common.service.ServiceException {%n",
                      methodName, javaType, name));
            } else {
              result.append(
                  String.format(
                      "    public void %s(%s %s) throws com.zimbra.common.service.ServiceException"
                          + " {%n",
                      methodName, javaType, name));
            }
            break;
          case HAS:
            if (Boolean.TRUE.equals(attributeInfo.isDynamic())) {
              result.append(
                  String.format(
                      "    public boolean %s(String dynamicComponent) throws"
                          + " com.zimbra.common.service.ServiceException {%n",
                      methodName));
            } else {
              result.append(
                  String.format(
                      "    public boolean %s() throws com.zimbra.common.service.ServiceException"
                          + " {%n",
                      methodName));
            }
            break;
        }
      } else {
        if (setterType != SetterType.UNSET) {
          result.append(
              String.format(
                  "    public void %s(%s %s) throws com.zimbra.common.service.ServiceException {%n",
                  methodName, javaType, name));
        } else {
          result.append(
              String.format(
                  "    public void %s() throws com.zimbra.common.service.ServiceException {%n",
                  methodName));
        }
      }
      if (Boolean.FALSE.equals(attributeInfo.isEphemeral())) {
        result.append(String.format("        HashMap<String,Object> attrs = new HashMap<>();%n"));
      }
      result.append(body);
      if (Boolean.FALSE.equals(attributeInfo.isEphemeral())) {
        result.append(String.format("        getProvisioning().modifyAttrs(this, attrs);%n"));
      }
    } else {
      if (setterType != SetterType.UNSET) {
        result.append(
            String.format(
                "    public %s %s(%s %s, %s attrs) {%n",
                mapType, methodName, javaType, name, mapType));
      } else {
        result.append(
            String.format("    public %s %s(%s attrs) {%n", mapType, methodName, mapType));
      }
      result.append(String.format("        if (attrs == null) attrs = new HashMap<>();%n"));
      result.append(body);
      result.append(String.format("        return attrs;%n"));
    }

    result.append(String.format("    }%n"));
  }

  private static void usage(String errmsg) {
    if (errmsg != null) {
      logger.error(errmsg);
    }
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("AttributeManagerUtil [options] where [options] are one of:", options);
    System.exit((errmsg == null) ? 0 : 1);
  }

  private static CommandLine parseArgs(String[] args) {
    //    StringBuilder gotCL = new StringBuilder("cmdline: ");
    //    for (String arg : args) {
    //      gotCL.append("'").append(arg).append("' ");
    //    }
    // mLog.info(gotCL);

    CommandLineParser parser = new GnuParser();
    CommandLine commandLine = null;
    try {
      commandLine = parser.parse(options, args);
    } catch (ParseException pe) {
      usage(pe.getMessage());
    }
    if (commandLine != null && commandLine.hasOption('h')) {
      usage(null);
    }
    return commandLine;
  }

  public static void main(String[] args) throws IOException, ServiceException {
    CliUtil.toolSetup();
    CommandLine commandLine = parseArgs(args);

    if (commandLine == null) {
      ZimbraLog.misc.error("No command line option specified! Exiting.");
      System.exit(1);
    }

    String actionStr = commandLine.getOptionValue('a');
    if (actionStr == null) {
      usage("no action specified");
    } else {
      Action action = null;
      try {
        action = Action.valueOfIgnoreCase(actionStr);
      } catch (IllegalArgumentException iae) {
        usage("unknown action: " + actionStr);
      }

      AttributeManager am = null;
      if (action != Action.DUMP && action != Action.LIST_ATTRS) {
        if (!commandLine.hasOption('i')) {
          usage("no input attribute xml files specified");
        }
        am = new AttributeManager(commandLine.getOptionValue('i'));
        if (am.hasErrors()) {
          ZimbraLog.misc.warn(am.getErrors());
          System.exit(1);
        }
      }

      OutputStream outputStream = System.out;
      if (commandLine.hasOption('o')) {
        outputStream = new FileOutputStream(commandLine.getOptionValue('o'));
      }

      try (PrintWriter printWriter =
          new PrintWriter(
              new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)))) {
        AttributeManagerUtil attributeManagerUtil = new AttributeManagerUtil(am);
        switch (action) {
          case DUMP:
            LdapProv.getInst().dumpLdapSchema(printWriter);
            break;
          case GENERATE_DEFAULT_COS_LDIF:
            attributeManagerUtil.generateDefaultCOSLdif(printWriter);
            break;
          case GENERATE_DEFAULT_EXTERNAL_COS_LDIF:
            attributeManagerUtil.generateDefaultExternalCOSLdif(printWriter);
            break;
          case GENERATE_GETTERS:
            attributeManagerUtil.generateGetters(
                commandLine.getOptionValue('c'), commandLine.getOptionValue('r'));
            break;
          case GENERATE_GLOBAL_CONFIG_LDIF:
            attributeManagerUtil.generateGlobalConfigLdif(printWriter);
            break;
          case GENERATE_LDAP_SCHEMA:
            if (!commandLine.hasOption('t')) {
              usage("no schema template specified");
            }
            attributeManagerUtil.generateLdapSchema(printWriter, commandLine.getOptionValue('t'));
            break;
          case GENERATE_MESSAGE_PROPERTIES:
            attributeManagerUtil.generateMessageProperties(commandLine.getOptionValue('r'));
            break;
          case GENERATE_PROVISIONING:
            attributeManagerUtil.generateProvisioningConstants(commandLine.getOptionValue('r'));
            break;
          case GENERATE_SCHEMA_LDIF:
            attributeManagerUtil.generateSchemaLdif(printWriter);
            break;
          case LIST_ATTRS:
            attributeManagerUtil.listAttrs(
                commandLine.getOptionValues('c'),
                commandLine.getOptionValues('n'),
                commandLine.getOptionValues('f'));
            break;
        }
      } catch (IOException e) {
        ZimbraLog.misc.error(e.getMessage());
        System.exit(1);
      }
    }
  }

  private Map<String, AttributeInfo> getAttrs() {
    return attrMgr.getAttrs();
  }

  private Map<String, ObjectClassInfo> getOCs() {
    return attrMgr.getOCs();
  }

  private Map<Integer, String> getGroupMap() {
    return attrMgr.getGroupMap();
  }

  private Map<Integer, String> getOCGroupMap() {
    return attrMgr.getOCGroupMap();
  }

  private String doNotModifyDisclaimer() {
    return FileGenUtil.genDoNotModifyDisclaimer(
        "#", AttributeManagerUtil.class.getSimpleName(), CLOptions.buildVersion());
  }

  private void generateDefaultCOSLdif(PrintWriter pw) {
    pw.println(doNotModifyDisclaimer());
    pw.println("#");
    pw.println("# LDAP entry for the default Zimbra COS.");
    pw.println("#");

    String baseDn = CLOptions.getBaseDn("cos");
    String cosName = CLOptions.getEntryName("cos", Provisioning.DEFAULT_COS_NAME);
    String cosId = CLOptions.getEntryId("cos", "e00428a1-0c00-11d9-836a-000d93afea2a");

    pw.println("dn: cn=" + cosName + ",cn=cos," + baseDn);
    pw.println("cn: " + cosName);
    pw.println("objectclass: zimbraCOS");
    pw.println("zimbraId: " + cosId);
    pw.println("description: The " + cosName + " COS");

    List<String> out = new LinkedList<>();
    for (AttributeInfo attr : getAttrs().values()) {
      List<String> gcv = attr.getDefaultCosValues();
      if (gcv != null) {
        for (String v : gcv) {
          out.add(attr.getName() + ": " + v);
        }
      }
    }
    String[] outs = out.toArray(new String[0]);
    Arrays.sort(outs);
    for (String o : outs) {
      pw.println(o);
    }
  }

  private void generateDefaultExternalCOSLdif(PrintWriter pw) {
    pw.println(doNotModifyDisclaimer());
    pw.println("#");
    pw.println("# LDAP entry for default COS for external user accounts.");
    pw.println("#");

    String baseDn = CLOptions.getBaseDn("cos");
    String cosName = CLOptions.getEntryName("cos", Provisioning.DEFAULT_EXTERNAL_COS_NAME);
    String cosId = CLOptions.getEntryId("cos", "f27456a8-0c00-11d9-280a-286d93afea2g");

    pw.println("dn: cn=" + cosName + ",cn=cos," + baseDn);
    pw.println("cn: " + cosName);
    pw.println("objectclass: zimbraCOS");
    pw.println("zimbraId: " + cosId);
    pw.println("description: The default external users COS");

    List<String> out = new LinkedList<>();
    for (AttributeInfo attr : getAttrs().values()) {
      List<String> defaultValues = attr.getDefaultExternalCosValues();
      if (defaultValues != null && !defaultValues.isEmpty()) {
        for (String v : defaultValues) {
          out.add(attr.getName() + ": " + v);
        }
      } else {
        defaultValues = attr.getDefaultCosValues();
        if (defaultValues != null) {
          for (String v : defaultValues) {
            out.add(attr.getName() + ": " + v);
          }
        }
      }
    }
    String[] outs = out.toArray(new String[0]);
    Arrays.sort(outs);
    for (String o : outs) {
      pw.println(o);
    }
  }

  private void generateGlobalConfigLdif(PrintWriter pw) {
    pw.println(doNotModifyDisclaimer());
    pw.println("#");
    pw.println("# LDAP entry that contains initial default Zimbra global config.");
    pw.println("#");

    String baseDn = CLOptions.getBaseDn("config");
    pw.println("dn: cn=config," + baseDn);
    pw.println("objectclass: organizationalRole");
    pw.println("cn: config");
    pw.println("objectclass: zimbraGlobalConfig");

    List<String> out = new LinkedList<>();
    for (AttributeInfo attr : getAttrs().values()) {
      List<String> gcv = attr.getGlobalConfigValues();
      if (gcv != null) {
        for (String v : gcv) {
          out.add(attr.getName() + ": " + v);
        }
      }
    }
    String[] outs = out.toArray(new String[0]);
    Arrays.sort(outs);
    for (String o : outs) {
      pw.println(o);
    }
  }

  private List<AttributeInfo> getAttrList(int groupId) {
    List<AttributeInfo> list = new ArrayList<>(getAttrs().size());
    for (AttributeInfo ai : getAttrs().values()) {
      if (ai.getId() > -1 && ai.getGroupId() == groupId) {
        list.add(ai);
      }
    }
    return list;
  }

  private void sortAttrsByOID(List<AttributeInfo> list) {
    list.sort(Comparator.comparingInt(AttributeInfo::getId));
  }

  private void sortAttrsByName(List<AttributeInfo> list) {
    list.sort(Comparator.comparing(AttributeInfo::getName));
  }

  private List<ObjectClassInfo> getOCList(int groupId) {
    List<ObjectClassInfo> list = new ArrayList<>(getOCs().size());
    for (ObjectClassInfo oci : getOCs().values()) {
      if (oci.getId() > -1 && oci.getGroupId() == groupId) {
        list.add(oci);
      }
    }
    return list;
  }

  private void sortOCsByOID(List<ObjectClassInfo> list) {
    list.sort(Comparator.comparingInt(ObjectClassInfo::getId));
  }

  // escape QQ and QS for rfc4512 dstring(http://www.ietf.org/rfc/rfc4512.txt 4.1.  Schema
  // Definitions)
  private String rfc4512Dstring(String unescaped) {
    return unescaped.replace("\\", "\\5C").replace("'", "\\27");
  }

  private void buildSchemaBanner(StringBuilder banner) {

    banner.append(doNotModifyDisclaimer());
    banner.append("#\n");
    banner.append("# Zimbra LDAP Schema\n");
    banner.append("#\n");
    banner.append("#\n");
    banner.append("#\n");
    banner.append("# our root OID (http://www.iana.org/assignments/enterprise-numbers)\n");
    banner.append("#\n");
    banner.append("#  1.3.6.1.4.1.19348\n");
    banner.append("#  1.3.6.1.4.1.19348.2      LDAP elements\n");
    banner.append("#  1.3.6.1.4.1.19348.2.1    Attribute Types\n");
    banner.append("#  1.3.6.1.4.1.19348.2.2    Object Classes\n");
    banner.append("#");
  }

  private void buildZimbraRootOIDs(StringBuilder zimbraRootOids, String prefix) {
    zimbraRootOids.append(prefix).append("ZimbraRoot 1.3.6.1.4.1.19348\n");
    zimbraRootOids.append(prefix).append("ZimbraLDAP ZimbraRoot:2\n");
  }

  private void buildAttrDef(StringBuilder attributeDefinitions, AttributeInfo ai) {
    String lengthSuffix;

    String syntax;
    String substr = null;
    String equality;
    String ordering = null;

    switch (ai.getType()) {
      case TYPE_BOOLEAN:
        syntax = "1.3.6.1.4.1.1466.115.121.1.7";
        equality = "booleanMatch";
        break;
      case TYPE_BINARY:
        // cannot use the binary syntax because it cannot support adding/deleting individual values
        // in a multi-valued attrs, only replacement(i.e. replace all values) is supported.
        //
        // when a value is added to a multi-valued attr, or when an attempt is made to delete a
        // specific value, will get "no equality matching rule" error from LDAP server, because
        // there is no equality matching for 1.3.6.1.4.1.1466.115.121.1.5
        //
        // Note: 1.3.6.1.4.1.1466.115.121.1.5 attrs, like userSMIMECertificate, when included in
        //       the zimbra schema, are declared as type="binary" in attrs.xml.
        //       Handling for the two (1.3.6.1.4.1.1466.115.121.1.5 and
        // 1.3.6.1.4.1.1466.115.121.1.40)
        //       are *exactly the same* in ZCS.  They are:
        //       - transferred as binary on the wire, by setting the JNDI
        // "java.naming.ldap.attributes.binary"
        //         environment property
        //       - stored as base64 encoded string in ZCS memory
        //       - Entry.getAttr(String name) returns the base64 encoded value
        //       - Entry.getBinaryAttr(String name) returns the base64 decoded value
        //
        /*
        lengthSuffix = "";
        if (ai.getMax() != Long.MAX_VALUE) {
            lengthSuffix = "{" + ai.getMax() + "}";
        }
        syntax = "1.3.6.1.4.1.1466.115.121.1.5" + lengthSuffix;
        break;
        */

        // the same as octet string
        lengthSuffix = "";
        if (ai.getMax() != Long.MAX_VALUE) {
          lengthSuffix = "{" + ai.getMax() + "}";
        }
        syntax = "1.3.6.1.4.1.1466.115.121.1.40" + lengthSuffix;
        equality = "octetStringMatch";
        break;
      case TYPE_CERTIFICATE:
        // This type does have an equality matching rule, so adding/deleting individual values
        // is supported.
        //
        lengthSuffix = "";
        if (ai.getMax() != Long.MAX_VALUE) {
          lengthSuffix = "{" + ai.getMax() + "}";
        }
        syntax = "1.3.6.1.4.1.1466.115.121.1.8" + lengthSuffix;
        equality = "certificateExactMatch";
        break;
      case TYPE_EMAIL:
      case TYPE_EMAILP:
      case TYPE_CS_EMAILP:
        syntax = "1.3.6.1.4.1.1466.115.121.1.26{256}";
        equality = "caseIgnoreIA5Match";
        substr = "caseIgnoreSubstringsMatch";
        break;

      case TYPE_GENTIME:
        syntax = "1.3.6.1.4.1.1466.115.121.1.24";
        equality = "generalizedTimeMatch";
        ordering = "generalizedTimeOrderingMatch ";
        break;

      case TYPE_ID:
        syntax = "1.3.6.1.4.1.1466.115.121.1.15{256}";
        equality = "caseIgnoreMatch";
        substr = "caseIgnoreSubstringsMatch";
        break;

      case TYPE_DURATION:
        syntax = "1.3.6.1.4.1.1466.115.121.1.26{32}";
        equality = "caseIgnoreIA5Match";
        break;

      case TYPE_ENUM:
        int maxLen = Math.max(32, ai.getEnumValueMaxLength());
        syntax = "1.3.6.1.4.1.1466.115.121.1.15{" + maxLen + "}";
        equality = "caseIgnoreMatch";
        substr = "caseIgnoreSubstringsMatch";
        break;

      case TYPE_INTEGER:
      case TYPE_PORT:
      case TYPE_LONG:
        syntax = "1.3.6.1.4.1.1466.115.121.1.27";
        equality = "integerMatch";
        break;

      case TYPE_STRING:
      case TYPE_REGEX:
        lengthSuffix = "";
        if (ai.getMax() != Long.MAX_VALUE) {
          lengthSuffix = "{" + ai.getMax() + "}";
        }
        syntax = "1.3.6.1.4.1.1466.115.121.1.15" + lengthSuffix;
        equality = "caseIgnoreMatch";
        substr = "caseIgnoreSubstringsMatch";
        break;

      case TYPE_ASTRING:
        lengthSuffix = "";
        if (ai.getMax() != Long.MAX_VALUE) {
          lengthSuffix = "{" + ai.getMax() + "}";
        }
        syntax = "1.3.6.1.4.1.1466.115.121.1.26" + lengthSuffix;
        equality = "caseIgnoreIA5Match";
        substr = "caseIgnoreSubstringsMatch";
        break;

      case TYPE_OSTRING:
        lengthSuffix = "";
        if (ai.getMax() != Long.MAX_VALUE) {
          lengthSuffix = "{" + ai.getMax() + "}";
        }
        syntax = "1.3.6.1.4.1.1466.115.121.1.40" + lengthSuffix;
        equality = "octetStringMatch";
        break;

      case TYPE_CSTRING:
        lengthSuffix = "";
        if (ai.getMax() != Long.MAX_VALUE) {
          lengthSuffix = "{" + ai.getMax() + "}";
        }
        syntax = "1.3.6.1.4.1.1466.115.121.1.15" + lengthSuffix;
        equality = "caseExactMatch";
        substr = "caseExactSubstringsMatch";
        break;

      case TYPE_PHONE:
        lengthSuffix = "";
        if (ai.getMax() != Long.MAX_VALUE) {
          lengthSuffix = "{" + ai.getMax() + "}";
        }
        syntax = "1.3.6.1.4.1.1466.115.121.1.50" + lengthSuffix;
        equality = "telephoneNumberMatch";
        substr = "telephoneNumberSubstringsMatch";
        break;

      default:
        throw new RuntimeException("unknown type encountered!");
    }

    attributeDefinitions.append("( ").append(ai.getName()).append("\n");
    attributeDefinitions.append(ML_CONT_PREFIX + "NAME ( '").append(ai.getName()).append("' )\n");
    attributeDefinitions
        .append(ML_CONT_PREFIX + "DESC '")
        .append(rfc4512Dstring(ai.getDescription()))
        .append("'\n");

    attributeDefinitions.append(ML_CONT_PREFIX + "SYNTAX ").append(syntax);

    if (equality != null) {
      attributeDefinitions.append("\n" + ML_CONT_PREFIX + "EQUALITY ").append(equality);
    }

    if (substr != null) {
      attributeDefinitions.append("\n" + ML_CONT_PREFIX + "SUBSTR ").append(substr);
    }

    if (ordering != null) {
      attributeDefinitions.append("\n" + ML_CONT_PREFIX + "ORDERING ").append(ordering);
    } else if (ai.getOrder() != null) {
      attributeDefinitions.append("\n" + ML_CONT_PREFIX + "ORDERING ").append(ai.getOrder());
    }

    if (ai.getCardinality() == AttributeCardinality.single) {
      attributeDefinitions.append("\n" + ML_CONT_PREFIX + "SINGLE-VALUE");
    }

    attributeDefinitions.append(")");
  }

  private void buildObjectClassOIDs(
      StringBuilder ocGroupOids, StringBuilder ocOids, String prefix) {
    for (int i : getOCGroupMap().keySet()) {
      // OC_GROUP_OIDS
      ocGroupOids
          .append(prefix)
          .append(getOCGroupMap().get(i))
          .append(" ZimbraLDAP:")
          .append(i)
          .append("\n");

      // List all ocs which we define and which belong in this group
      List<ObjectClassInfo> list = getOCList(i);

      // OC_OIDS - sorted by OID
      sortOCsByOID(list);

      for (ObjectClassInfo oci : list) {
        ocOids
            .append(prefix)
            .append(oci.getName())
            .append(" ")
            .append(getOCGroupMap().get(i))
            .append(':')
            .append(oci.getId())
            .append("\n");
      }
    }
  }

  /**
   * @param blankLineSeperator whether to seperate each OC with a blank line
   */
  private void buildObjectClassDefs(
      StringBuilder ocDefinitions, String prefix, boolean blankLineSeperator) {
    for (AttributeClass cls : AttributeClass.values()) {

      String ocName = cls.getOCName();
      String ocCanonicalName = ocName.toLowerCase();
      ObjectClassInfo oci = getOCs().get(ocCanonicalName);
      if (oci == null) {
        continue; // oc not defined in xml, skip
      }

      // OC_DEFINITIONS:
      List<String> comment = oci.getComment();
      ocDefinitions.append("#\n");
      for (String line : comment) {
        if (line.length() > 0) {
          ocDefinitions.append("# ").append(line).append("\n");
        } else {
          ocDefinitions.append("#\n");
        }
      }
      ocDefinitions.append("#\n");

      ocDefinitions.append(prefix).append("( ").append(oci.getName()).append("\n");
      ocDefinitions.append(ML_CONT_PREFIX + "NAME '").append(oci.getName()).append("'\n");
      ocDefinitions
          .append(ML_CONT_PREFIX + "DESC '")
          .append(rfc4512Dstring(oci.getDescription()))
          .append("'\n");
      ocDefinitions.append(ML_CONT_PREFIX + "SUP ");
      for (String sup : oci.getSuperOCs()) {
        ocDefinitions.append(sup);
      }
      ocDefinitions.append(" ").append(oci.getType()).append("\n");

      StringBuilder value = new StringBuilder();
      buildObjectClassAttrs(cls, value);

      ocDefinitions.append(value);
      ocDefinitions.append(")\n");

      if (blankLineSeperator) {
        ocDefinitions.append("\n");
      }
    }
  }

  private void buildObjectClassAttrs(AttributeClass cls, StringBuilder value) {
    List<String> must = new LinkedList<>();
    List<String> may = new LinkedList<>();
    for (AttributeInfo ai : getAttrs().values()) {
      if (ai.requiredInClass(cls)) {
        must.add(ai.getName());
      }
      if (ai.optionalInClass(cls)) {
        may.add(ai.getName());
      }
    }
    Collections.sort(must);
    Collections.sort(may);

    if (!must.isEmpty()) {
      value.append(ML_CONT_PREFIX + "MUST (\n");
      Iterator<String> mustIter = must.iterator();
      while (true) {
        value.append(ML_CONT_PREFIX + "  ").append(mustIter.next());
        if (!mustIter.hasNext()) {
          break;
        }
        value.append(" $\n");
      }
      value.append("\n" + ML_CONT_PREFIX + ")\n");
    }
    if (!may.isEmpty()) {
      value.append(ML_CONT_PREFIX + "MAY (\n");
      Iterator<String> mayIter = may.iterator();
      while (true) {
        value.append(ML_CONT_PREFIX + "  ").append(mayIter.next());
        if (!mayIter.hasNext()) {
          break;
        }
        value.append(" $\n");
      }
      value.append("\n" + ML_CONT_PREFIX + ")\n");
    }
    value.append(ML_CONT_PREFIX);
  }

  /** This method uses xml for generating objectclass OIDs and definitions */
  private void generateLdapSchema(PrintWriter pw, String schemaTemplateFile) throws IOException {
    byte[] templateBytes = ByteUtil.getContent(new File(schemaTemplateFile));
    String templateString = new String(templateBytes, StandardCharsets.UTF_8);

    StringBuilder banner = new StringBuilder();
    StringBuilder zimbraRootOids = new StringBuilder();
    StringBuilder groupOids = new StringBuilder();
    StringBuilder attributeOids = new StringBuilder();
    StringBuilder attributeDefinitions = new StringBuilder();
    StringBuilder ocGroupOids = new StringBuilder();
    StringBuilder ocOids = new StringBuilder();
    StringBuilder ocDefinitions = new StringBuilder();

    buildSchemaBanner(banner);
    buildZimbraRootOIDs(zimbraRootOids, "objectIdentifier ");

    for (int i : getGroupMap().keySet()) {
      // GROUP_OIDS
      groupOids
          .append("objectIdentifier ")
          .append(getGroupMap().get(i))
          .append(" ZimbraLDAP:")
          .append(i)
          .append("\n");

      // List all attrs which we define and which belong in this group
      List<AttributeInfo> list = getAttrList(i);

      // ATTRIBUTE_OIDS - sorted by OID
      sortAttrsByOID(list);

      for (AttributeInfo ai : list) {
        String parentOid = ai.getParentOid();
        if (parentOid == null) {
          attributeOids
              .append("objectIdentifier ")
              .append(ai.getName())
              .append(" ")
              .append(getGroupMap().get(i))
              .append(':')
              .append(ai.getId())
              .append("\n");
        } else {
          attributeOids
              .append("objectIdentifier ")
              .append(ai.getName())
              .append(" ")
              .append(parentOid)
              .append(".")
              .append(ai.getId())
              .append("\n");
        }
      }

      // ATTRIBUTE_DEFINITIONS: DESC EQUALITY NAME ORDERING SINGLE-VALUE SUBSTR SYNTAX
      // - sorted by name
      sortAttrsByName(list);

      for (AttributeInfo ai : list) {
        attributeDefinitions.append("attributetype ");
        buildAttrDef(attributeDefinitions, ai);
        attributeDefinitions.append("\n\n");
      }
    }

    // object class OIDs
    buildObjectClassOIDs(ocGroupOids, ocOids, "objectIdentifier ");

    // object class definitions
    buildObjectClassDefs(ocDefinitions, "objectclass ", true);

    Map<String, String> templateFillers = new HashMap<>();
    templateFillers.put("BANNER", banner.toString());
    templateFillers.put("ZIMBRA_ROOT_OIDS", zimbraRootOids.toString());
    templateFillers.put("GROUP_OIDS", groupOids.toString());
    templateFillers.put("ATTRIBUTE_OIDS", attributeOids.toString());
    templateFillers.put("OC_GROUP_OIDS", ocGroupOids.toString());
    templateFillers.put("OC_OIDS", ocOids.toString());
    templateFillers.put("ATTRIBUTE_DEFINITIONS", attributeDefinitions.toString());
    templateFillers.put("OC_DEFINITIONS", ocDefinitions.toString());

    pw.print(StringUtil.fillTemplate(templateString, templateFillers));
  }

  private void generateSchemaLdif(PrintWriter pw) {

    StringBuilder banner = new StringBuilder();
    StringBuilder zimbraRootOids = new StringBuilder();
    StringBuilder attributeGroupOids = new StringBuilder();
    StringBuilder attributeOids = new StringBuilder();
    StringBuilder attributeDefinitions = new StringBuilder();
    StringBuilder ocGroupOids = new StringBuilder();
    StringBuilder ocOids = new StringBuilder();
    StringBuilder ocDefinitions = new StringBuilder();

    buildSchemaBanner(banner);
    buildZimbraRootOIDs(zimbraRootOids, "olcObjectIdentifier: ");

    for (int i : getGroupMap().keySet()) {
      // GROUP_OIDS
      attributeGroupOids
          .append("olcObjectIdentifier: ")
          .append(getGroupMap().get(i))
          .append(" ZimbraLDAP:")
          .append(i)
          .append("\n");

      // List all attrs which we define and which belong in this group
      List<AttributeInfo> list = getAttrList(i);

      // ATTRIBUTE_OIDS - sorted by OID
      sortAttrsByOID(list);

      for (AttributeInfo ai : list) {
        String parentOid = ai.getParentOid();
        if (parentOid == null) {
          attributeOids
              .append("olcObjectIdentifier: ")
              .append(ai.getName())
              .append(" ")
              .append(getGroupMap().get(i))
              .append(':')
              .append(ai.getId())
              .append("\n");
        } else {
          attributeOids
              .append("olcObjectIdentifier: ")
              .append(ai.getName())
              .append(" ")
              .append(parentOid)
              .append(".")
              .append(ai.getId())
              .append("\n");
        }
      }

      // ATTRIBUTE_DEFINITIONS: DESC EQUALITY NAME ORDERING SINGLE-VALUE SUBSTR SYNTAX
      // - sorted by name
      sortAttrsByName(list);

      /* Hack to add the company attribute from Microsoft schema
       * For generateLdapSchema, it is specified in the carbonio.schema-template file
       * We don't use a template file for generateSchemaLdif thus hardcode here.
       * Move to template file if really necessary.
       *
      #### From Microsoft Schema
      olcAttributeTypes ( 1.2.840.113556.1.2.146
              NAME ( 'company' )
              SYNTAX 1.3.6.1.4.1.1466.115.121.1.15{512}
              EQUALITY caseIgnoreMatch
              SUBSTR caseIgnoreSubstringsMatch
              SINGLE-VALUE )
      */

      attributeDefinitions.append("olcAttributeTypes: ( 1.2.840.113556.1.2.146\n");
      attributeDefinitions.append(ML_CONT_PREFIX + "NAME ( 'company' )\n");
      attributeDefinitions.append(ML_CONT_PREFIX + "SYNTAX 1.3.6.1.4.1.1466.115.121.1.15{512}\n");
      attributeDefinitions.append(ML_CONT_PREFIX + "EQUALITY caseIgnoreMatch\n");
      attributeDefinitions.append(ML_CONT_PREFIX + "SUBSTR caseIgnoreSubstringsMatch\n");
      attributeDefinitions.append(ML_CONT_PREFIX + "SINGLE-VALUE )\n");

      for (AttributeInfo ai : list) {
        attributeDefinitions.append("olcAttributeTypes: ");
        buildAttrDef(attributeDefinitions, ai);
        attributeDefinitions.append("\n");
      }
    }

    // objectclass OIDs
    buildObjectClassOIDs(ocGroupOids, ocOids, "olcObjectIdentifier: ");

    // objectclass definitions
    buildObjectClassDefs(ocDefinitions, "olcObjectClasses: ", false);

    pw.println(banner);
    pw.println("dn: cn=zimbra,cn=schema,cn=config");
    pw.println("objectClass: olcSchemaConfig");
    pw.println("cn: zimbra");
    pw.print(zimbraRootOids);
    pw.print(attributeGroupOids);
    pw.print(attributeOids);
    pw.print(ocGroupOids);
    pw.print(ocOids);
    pw.print(attributeDefinitions);
    pw.print(ocDefinitions);
  }

  private void generateMessageProperties(String outFile) throws IOException {
    StringBuilder result = new StringBuilder();

    result.append(doNotModifyDisclaimer());
    result.append("# Zimbra LDAP attributes." + "\n");
    result.append("# \n");

    List<String> attrs = new ArrayList<>(getAttrs().keySet());
    Collections.sort(attrs);

    for (String attr : attrs) {
      AttributeInfo ai = getAttrs().get(attr);
      String desc = ai.getDescription();
      if (desc != null) {
        String text =
            FileGenUtil.wrapComments(desc, 80, "  ", " \\")
                .substring(2); // strip off the 2 spaces on the first line
        result.append(ai.getName()).append(" = ").append(text).append("\n");
      }
    }

    FileGenUtil.replaceFile(outFile, result.toString());
  }

  private void listAttrs(String[] inClass, String[] notInClass, String[] printFlags) {
    if (inClass == null) {
      usage("no class specified");
    }

    Set<String> attrsInClass = new HashSet<>();
    for (String c : inClass) {
      AttributeClass ac = AttributeClass.valueOf(c);
      SetUtil.union(attrsInClass, attrMgr.getAttrsInClass(ac));
    }

    Set<String> attrsNotInClass = new HashSet<>();
    if (notInClass != null) {
      for (String c : notInClass) {
        AttributeClass ac = AttributeClass.valueOf(c);
        SetUtil.union(attrsNotInClass, attrMgr.getAttrsInClass(ac));
      }
    }

    attrsInClass = SetUtil.subtract(attrsInClass, attrsNotInClass);

    List<String> list = new ArrayList<>(attrsInClass);
    Collections.sort(list);

    for (String a : list) {
      StringBuilder flags = new StringBuilder();
      if (printFlags != null) {
        for (String f : printFlags) {
          AttributeFlag af = AttributeFlag.valueOf(f);
          if (attrMgr.hasFlag(af, a)) {
            if (flags.length() > 0) {
              flags.append(", ");
            }
            flags.append(af.name());
          }
        }

        if (flags.length() > 0) {
          flags.insert(0, "(").append(")");
        }
      }
      System.out.println(a + " " + flags);
    }
  }

  /** */
  private void generateGetters(String inClass, String javaFile) throws IOException {
    if (inClass == null) {
      usage("no class specified");
    }

    AttributeClass ac = AttributeClass.valueOf(inClass);
    Set<String> attrsInClass = attrMgr.getAttrsInClass(ac);

    // add in mailRecipient if we need to
    if (ac == AttributeClass.account) {
      SetUtil.union(attrsInClass, attrMgr.getAttrsInClass(AttributeClass.mailRecipient));
    }

    List<String> list = new ArrayList<>(attrsInClass);
    Collections.sort(list);

    StringBuilder result = new StringBuilder();

    for (String a : list) {
      AttributeInfo attributeInfo = getAttrs().get(a.toLowerCase());
      if (attributeInfo == null) {
        continue;
      }

      switch (attributeInfo.getType()) {
        case TYPE_BINARY:
        case TYPE_CERTIFICATE:
        case TYPE_DURATION:
        case TYPE_GENTIME:
        case TYPE_ENUM:
        case TYPE_PORT:
          if (attributeInfo.getCardinality() != AttributeCardinality.multi) {
            generateGetter(result, attributeInfo, false, ac);
          }
          generateGetter(result, attributeInfo, true, ac);
          generateSetters(result, attributeInfo, false, SetterType.SET);
          if (attributeInfo.getType() == AttributeType.TYPE_GENTIME
              || attributeInfo.getType() == AttributeType.TYPE_ENUM
              || attributeInfo.getType() == AttributeType.TYPE_PORT) {
            generateSetters(result, attributeInfo, true, SetterType.SET);
          }
          generateSetters(result, attributeInfo, false, SetterType.UNSET);
          break;
        default:
          generateGetter(
              result, attributeInfo, attributeInfo.getName().equalsIgnoreCase("zimbraLocale"), ac);
          generateSetters(result, attributeInfo, false, SetterType.SET);
          if (attributeInfo.getCardinality() == AttributeCardinality.multi) {
            generateSetters(result, attributeInfo, false, SetterType.ADD);
            generateSetters(result, attributeInfo, false, SetterType.REMOVE);
            if (Boolean.TRUE.equals(attributeInfo.isEphemeral())) {
              generateSetters(result, attributeInfo, false, SetterType.HAS);
            }
            if (Boolean.TRUE.equals(attributeInfo.isExpirable())) {
              generateSetters(result, attributeInfo, false, SetterType.PURGE);
            }
          }
          generateSetters(result, attributeInfo, false, SetterType.UNSET);
          break;
      }
    }
    FileGenUtil.replaceJavaFile(javaFile, result.toString());
  }

  /** */
  private void generateProvisioningConstants(String javaFile) throws IOException {
    List<String> list = new ArrayList<>(getAttrs().keySet());
    Collections.sort(list);

    StringBuilder result = new StringBuilder();

    for (String a : list) {
      AttributeInfo ai = getAttrs().get(a.toLowerCase());
      if (ai == null || ai.getType() != AttributeType.TYPE_ENUM) {
        continue;
      }
      generateEnum(result, ai);
    }

    for (String a : list) {
      AttributeInfo ai = getAttrs().get(a.toLowerCase());
      if (ai == null) {
        continue;
      }

      result.append("\n    /**\n");
      if (ai.getDescription() != null) {
        result.append(
            FileGenUtil.wrapComments(StringUtil.escapeHtml(ai.getDescription()), 70, "     * "));
        result.append("\n");
      }
      if (ai.getSince() != null) {
        result.append("     *\n");
        result.append(String.format("     * @since ZCS %s%n", versionListAsString(ai.getSince())));
      }
      result.append("     */\n");
      result.append(String.format("    @ZAttr(id=%d)%n", ai.getId()));

      result.append(
          String.format(
              "    public static final String A_%s = \"%s\";%n", ai.getName(), ai.getName()));
    }

    FileGenUtil.replaceJavaFile(javaFile, result.toString());
  }

  private enum Action {
    DUMP,
    GENERATE_DEFAULT_COS_LDIF,
    GENERATE_DEFAULT_EXTERNAL_COS_LDIF,
    GENERATE_GETTERS,
    GENERATE_GLOBAL_CONFIG_LDIF,
    GENERATE_LDAP_SCHEMA,
    GENERATE_MESSAGE_PROPERTIES,
    GENERATE_PROVISIONING,
    GENERATE_SCHEMA_LDIF,
    LIST_ATTRS;

    public static Action valueOfIgnoreCase(String value) {
      for (Action action : Action.values()) {
        if (action.name().equalsIgnoreCase(value)
            || action.name().replace("_", "").equalsIgnoreCase(value)) {
          return action;
        }
      }
      throw new IllegalArgumentException("Invalid Action: " + value);
    }
  }

  @VisibleForTesting
  enum SetterType {
    SET,
    ADD,
    UNSET,
    REMOVE, /* these two are for ephemeral attrs */
    PURGE,
    HAS
  }

  static class CLOptions {

    private CLOptions() {
      throw new IllegalStateException("Utility class");
    }

    private static String get(String key, String defaultValue) {
      String value = System.getProperty(key);
      if (value == null) {
        return defaultValue;
      } else {
        return value;
      }
    }

    public static String buildVersion() {
      return get("zimbra.version", "unknown");
    }

    public static String getBaseDn(String entry) {
      return get(entry + ".basedn", "cn=zimbra");
    }

    public static String getEntryName(String entry, String defaultValue) {
      return get(entry + ".name", defaultValue);
    }

    public static String getEntryId(String entry, String defaultValue) {
      return get(entry + ".id", defaultValue);
    }
  }
}
