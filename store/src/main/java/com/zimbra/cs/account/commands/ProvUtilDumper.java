package com.zimbra.cs.account.commands;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.FileUtil;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Alias;
import com.zimbra.cs.account.AttributeInfo;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.CalendarResource;
import com.zimbra.cs.account.Console;
import com.zimbra.cs.account.Cos;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.DynamicGroup;
import com.zimbra.cs.account.GalContact;
import com.zimbra.cs.account.Group;
import com.zimbra.cs.account.Identity;
import com.zimbra.cs.account.ProvUtilDumperOptions;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.AttrRight;
import com.zimbra.cs.account.accesscontrol.ComboRight;
import com.zimbra.cs.account.accesscontrol.Help;
import com.zimbra.cs.account.accesscontrol.Right;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

class ProvUtilDumper {

  private final Console console;
  private final ProvUtilDumperOptions options;

  public ProvUtilDumper(Console console, ProvUtilDumperOptions options) {
    this.console = console;
    this.options = options;
  }

  public void dumpCos(Cos cos, Set<String> attrNames) throws ServiceException {
    console.println("# name " + cos.getName());
    Map<String, Object> attrs = cos.getAttrs();
    dumpAttrs(attrs, attrNames);
    console.println();
  }

  public void dumpDomain(Domain domain, Set<String> attrNames) throws ServiceException {
    dumpDomain(domain, true, attrNames);
  }

  public void dumpDomain(Domain domain, boolean expandConfig, Set<String> attrNames)
          throws ServiceException {
    console.println("# name " + domain.getName());
    Map<String, Object> attrs = domain.getAttrs(expandConfig);
    dumpAttrs(attrs, attrNames);
    console.println();
  }

  public void dumpGroup(Group group, Set<String> attrNames) throws ServiceException {

    String[] members;
    if (group instanceof DynamicGroup) {
      members = ((DynamicGroup) group).getAllMembers(true);
    } else {
      members = group.getAllMembers();
    }

    int count = members == null ? 0 : members.length;
    console.println("# distributionList " + group.getName() + " memberCount=" + count);
    Map<String, Object> attrs = group.getAttrs();
    dumpAttrs(attrs, attrNames);
    console.println();
    console.println("members");
    for (String member : members) {
      console.println(member);
    }
  }

  public void dumpAlias(Alias alias) throws ServiceException {
    console.println("# alias " + alias.getName());
    Map<String, Object> attrs = alias.getAttrs();
    dumpAttrs(attrs, null);
  }

  public void dumpRight(Right right, boolean expandComboRight) {
    String tab = "    ";
    String indent = tab;
    String indent2 = indent + indent;

    console.println();
    console.println("------------------------------");

    console.println(right.getName());
    console.println(indent + "      description: " + right.getDesc());
    console.println(indent + "       right type: " + right.getRightType().name());

    String targetType = right.getTargetTypeStr();
    console.println(indent + "   target type(s): " + (targetType == null ? "" : targetType));

    String grantTargetType = right.getGrantTargetTypeStr();
    console.println(
            indent + "grant target type: " + (grantTargetType == null ? "(default)" : grantTargetType));

    console.println(indent + "      right class: " + right.getRightClass().name());

    if (right.isAttrRight()) {
      AttrRight attrRight = (AttrRight) right;
      console.println();
      console.println(indent + "attributes:");
      if (attrRight.allAttrs()) {
        console.println(indent2 + "all attributes");
      } else {
        for (String attrName : attrRight.getAttrs()) {
          console.println(indent2 + attrName);
        }
      }
    } else if (right.isComboRight()) {
      ComboRight comboRight = (ComboRight) right;
      console.println();
      console.println(indent + "rights:");
      dumpComboRight(comboRight, expandComboRight, indent, new HashSet<>());
    }
    console.println();

    Help help = right.getHelp();
    if (help != null) {
      console.println(help.getDesc());
      List<String> helpItems = help.getItems();
      for (String helpItem : helpItems) {
        console.println("- " + helpItem.trim());
        console.println();
      }
    }
    console.println();
  }

  private void dumpComboRight(
          ComboRight comboRight, boolean expandComboRight, String indent, Set<String> seen) {
    // safety check, should not happen,
    // detect circular combo rights
    if (seen.contains(comboRight.getName())) {
      console.println("Circular combo right: " + comboRight.getName() + " !!");
      return;
    }

    String indent2 = indent + indent;

    for (Right r : comboRight.getRights()) {
      String tt = r.getTargetTypeStr();
      tt = tt == null ? "" : " (" + tt + ")";
      console.print(String.format("%s %s: %s %s\n", indent2, r.getRightType().name(), r.getName(), tt));

      seen.add(comboRight.getName());

      if (r.isComboRight() && expandComboRight) {
        dumpComboRight((ComboRight) r, expandComboRight, indent2, seen);
      }

      seen.clear();
    }
  }

  public void dumpServer(Server server, boolean expandConfig, Set<String> attrNames)
          throws ServiceException {
    console.println("# name " + server.getName());
    Map<String, Object> attrs = server.getAttrs(expandConfig);
    dumpAttrs(attrs, attrNames);
    console.println();
  }

  public void dumpAccount(Account account, boolean expandCos, Set<String> attrNames)
          throws ServiceException {
    console.println("# name " + account.getName());
    Map<String, Object> attrs = account.getAttrs(expandCos);
    dumpAttrs(attrs, attrNames);
    console.println();
  }

  public void dumpCalendarResource(
          CalendarResource resource, boolean expandCos, Set<String> attrNames) throws ServiceException {
    console.println("# name " + resource.getName());
    Map<String, Object> attrs = resource.getAttrs(expandCos);
    dumpAttrs(attrs, attrNames);
    console.println();
  }

  public void dumpContact(GalContact contact) throws ServiceException {
    console.println("# name " + contact.getId());
    Map<String, Object> attrs = contact.getAttrs();
    dumpAttrs(attrs, null);
    console.println();
  }

  public void dumpIdentity(Identity identity, Set<String> attrNameSet) throws ServiceException {
    console.println("# name " + identity.getName());
    Map<String, Object> attrs = identity.getAttrs();
    dumpAttrs(attrs, attrNameSet);
    console.println();
  }

  public void dumpAttrs(Map<String, Object> attrsIn, Set<String> specificAttrs)
          throws ServiceException {
    TreeMap<String, Object> attrs = new TreeMap<>(attrsIn);

    Map<String, Set<String>> specificAttrValues = null;

    if (specificAttrs != null) {
      specificAttrValues = new HashMap<>();
      for (String specificAttr : specificAttrs) {
        int colonAt = specificAttr.indexOf("=");
        String attrName = null;
        String attrValue = null;
        if (colonAt == -1) {
          attrName = specificAttr;
        } else {
          attrName = specificAttr.substring(0, colonAt);
          attrValue = specificAttr.substring(colonAt + 1);
          if (attrValue.length() < 1) {
            throw ServiceException.INVALID_REQUEST("missing value for " + specificAttr, null);
          }
        }

        attrName = attrName.toLowerCase();
        Set<String> values = specificAttrValues.get(attrName);
        if (values == null) { // haven't seen the attr yet
          values = new HashSet<>();
        }
        if (attrValue != null) {
          values.add(attrValue);
        }
        specificAttrValues.put(attrName, values);
      }
    }

    AttributeManager attrMgr = AttributeManager.getInstance();

    SimpleDateFormat dateFmt = new SimpleDateFormat("yyyyMMddHHmmss");
    String timestamp = dateFmt.format(new Date());

    for (Map.Entry<String, Object> entry : attrs.entrySet()) {
      String name = entry.getKey();

      boolean isBinary = needsBinaryIO(attrMgr, name);

      Set<String> specificValues = null;
      if (specificAttrValues != null) {
        specificValues = specificAttrValues.get(name.toLowerCase());
      }
      if (specificAttrValues == null || specificAttrValues.containsKey(name.toLowerCase())) {

        Object value = entry.getValue();

        if (value instanceof String[] sv) {
          for (int i = 0; i < sv.length; i++) {
            String aSv = sv[i];
            // don't print permission denied attr
            if (this.options.getForceDisplayAttrValue()
                    || aSv.length() > 0
                    && (specificValues == null
                    || specificValues.isEmpty()
                    || specificValues.contains(aSv))) {
              printAttr(name, aSv, i, isBinary, timestamp);
            }
          }
        } else if (value instanceof String string) {
          // don't print permission denied attr
          if (this.options.getForceDisplayAttrValue()
                  || string.length() > 0
                  && (specificValues == null
                  || specificValues.isEmpty()
                  || specificValues.contains(value))) {
            printAttr(name, (String) value, null, isBinary, timestamp);
          }
        }
      }
    }

    // force display empty value attribute
    if (this.options.getForceDisplayAttrValue()) {
      for (String attr : specificAttrs) {
        if (!attrs.containsKey(attr)) {
          AttributeInfo ai = attrMgr.getAttributeInfo(attr);
          if (ai != null) {
            printAttr(attr, "", null, false, timestamp);
          }
        }
      }
    }
  }

  private static boolean needsBinaryIO(AttributeManager attrMgr, String attr) {
    return attrMgr.containsBinaryData(attr);
  }

  private void printAttr(
          String attrName, String value, Integer idx, boolean isBinary, String timestamp)
          throws ServiceException {
    if (isBinary) {
      byte[] binary = ByteUtil.decodeLDAPBase64(value);
      if (options.outputBinaryToFile()) {
        outputBinaryAttrToFile(attrName, idx, binary, timestamp);
      } else {
        // print base64 encoded content
        // follow ldapsearch notion of using two colons when printing base64 encoded data
        // re-encode into 76 character blocks
        String based64Chunked = new String(Base64.encodeBase64Chunked(binary));
        // strip off the \n at the end
        if (based64Chunked.charAt(based64Chunked.length() - 1) == '\n') {
          based64Chunked = based64Chunked.substring(0, based64Chunked.length() - 1);
        }
        console.printOutput(attrName + ":: " + based64Chunked);
      }
    } else {
      console.printOutput(attrName + ": " + value);
    }
  }

  /**
   * Output binary attribute to file.
   *
   * <p>value is written to:
   * {LC.zmprov_tmp_directory}/{attr-name}[_{index-if-multi-valued}]{timestamp}
   *
   * <p>e.g. /opt/zextras/data/tmp/zmprov/zimbraFoo_20110202161621
   * /opt/zextras/data/tmp/zmprov/zimbraBar_0_20110202161507
   * /opt/zextras/data/tmp/zmprov/zimbraBar_1_20110202161507
   */
  private void outputBinaryAttrToFile(String attrName, Integer idx, byte[] value, String timestamp)
          throws ServiceException {
    StringBuilder sb = new StringBuilder(LC.zmprov_tmp_directory.value());
    sb.append(File.separator).append(attrName);
    if (idx != null) {
      sb.append("_").append(idx);
    }
    sb.append("_").append(timestamp);

    File file = new File(sb.toString());
    if (file.exists()) {
      file.delete();
    }

    try {
      FileUtil.ensureDirExists(file.getParentFile());
    } catch (IOException e) {
      throw ServiceException.FAILURE(
              "Unable to create directory " + file.getParentFile().getAbsolutePath(), e);
    }

    try {
      ByteUtil.putContent(file.getAbsolutePath(), value);
    } catch (IOException e) {
      throw ServiceException.FAILURE("Unable to write to file " + file.getAbsolutePath(), e);
    }
  }
}
