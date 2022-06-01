package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.DateUtil;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import java.io.PrintStream;
import java.util.Formatter;
import java.util.Objects;
import java.util.regex.Pattern;

class ProxyConfVar {

  public static final String UNKNOWN_HEADER_NAME = "X-Zimbra-Unknown-Header";
  public static final Pattern RE_HEADER = Pattern.compile("^([^:]+):\\s+(.*)$");
  public static Entry configSource = null;
  public static Entry serverSource = null;
  protected static Log mLog = LogFactory.getLog(ProxyConfGen.class);
  protected static Provisioning mProv = Provisioning.getInstance();
  public String mKeyword;
  public String mAttribute;
  public ProxyConfValueType mValueType;
  public Object mDefault;
  public Object mValue;
  public ProxyConfOverride mOverride;
  public String mDescription;

  public ProxyConfVar(
      String keyword,
      String attribute,
      Object defaultValue,
      ProxyConfValueType valueType,
      ProxyConfOverride overrideType,
      String description) {
    mKeyword = keyword;
    mAttribute = attribute;
    mValueType = valueType;
    mDefault = defaultValue;
    mOverride = overrideType;
    mValue = mDefault;
    mDescription = description;
  }

  public String confValue() throws ProxyConfException {
    return format(mValue);
  }

  public Object rawValue() {
    return mValue;
  }

  public void write(PrintStream ps) throws ProxyConfException {
    ps.println("  NGINX Keyword:         " + mKeyword);
    ps.println("  Description:           " + mDescription);
    ps.println("  Value Type:            " + mValueType.toString());
    ps.println("  Controlling Attribute: " + ((mAttribute == null) ? "(none)" : mAttribute));
    ps.println("  Default Value:         " + ((mDefault == null) ? "(none)" : mDefault.toString()));
    ps.println("  Current Value:         " + ((mValue == null) ? "(none)" : mValue.toString()));
    ps.println("  Config Text:           " + ((mValue == null) ? "(none)" : format(mValue)));
    ps.println();
  }

  /* Update internal value depending upon config source and data type */
  public void update() throws ServiceException, ProxyConfException {
    if (mOverride == ProxyConfOverride.NONE) {
      return;
    }

    if (mValueType == ProxyConfValueType.INTEGER) {
      updateInteger();
    } else if (mValueType == ProxyConfValueType.LONG) {
      updateLong();
    } else if (mValueType == ProxyConfValueType.STRING) {
      updateString();
    } else if (mValueType == ProxyConfValueType.BOOLEAN) {
      updateBoolean();
    } else if (mValueType == ProxyConfValueType.ENABLER) {
      updateEnabler();
    } else if (mValueType == ProxyConfValueType.TIME) {
      updateTime();
    } else if (mValueType == ProxyConfValueType.CUSTOM) {

      /* should always use override to define the custom update method */
      throw new ProxyConfException(
          "the custom update of ProxyConfVar with key "
              + mKeyword
              + " has to be implementated by override");
    }
  }

  public String format(Object o) throws ProxyConfException {
    if (mValueType == ProxyConfValueType.INTEGER) {
      return formatInteger(o);
    } else if (mValueType == ProxyConfValueType.LONG) {
      return formatLong(o);
    } else if (mValueType == ProxyConfValueType.STRING) {
      return formatString(o);
    } else if (mValueType == ProxyConfValueType.BOOLEAN) {
      return formatBoolean(o);
    } else if (mValueType == ProxyConfValueType.ENABLER) {
      return formatEnabler(o);
    } else if (mValueType == ProxyConfValueType.TIME) {
      return formatTime(o);
    } else /* if (mValueType == ProxyConfValueType.CUSTOM) */ {
      throw new ProxyConfException(
          "the custom format of ProxyConfVar with key "
              + mKeyword
              + " has to be implemented by override");
    }
  }

  public void updateString() {
    if (mOverride == ProxyConfOverride.CONFIG) {
      mValue = configSource.getAttr(mAttribute, (String) mDefault);
    } else if (mOverride == ProxyConfOverride.LOCALCONFIG) {
      mValue = lcValue(mAttribute, (String) mDefault);
    } else if (mOverride == ProxyConfOverride.SERVER) {
      mValue = serverSource.getAttr(mAttribute, (String) mDefault);
    }
  }

  public String formatString(Object o) {
    Formatter f = new Formatter();
    f.format("%s", o);
    return f.toString();
  }

  public void updateBoolean() {
    if (mOverride == ProxyConfOverride.CONFIG) {
      mValue = configSource.getBooleanAttr(mAttribute, (Boolean) mDefault);
    } else if (mOverride == ProxyConfOverride.LOCALCONFIG) {
      mValue = Boolean.valueOf(lcValue(mAttribute, mDefault.toString()));
    } else if (mOverride == ProxyConfOverride.SERVER) {
      mValue = serverSource.getBooleanAttr(mAttribute, (Boolean) mDefault);
    }
  }

  public String formatBoolean(Object o) {
    if ((Boolean) o) {
      return "on";
    }
    return "off";
  }

  public void updateEnabler() {
    updateBoolean();
  }

  public String formatEnabler(Object o) {
    if ((Boolean) o) {
      return "";
    }
    return "#";
  }

  public void updateTime() {
    if (mOverride == ProxyConfOverride.CONFIG) {
      mValue = configSource.getTimeInterval(mAttribute, (Long) mDefault);
    } else if (mOverride == ProxyConfOverride.LOCALCONFIG) {
      mValue = DateUtil.getTimeInterval(lcValue(mAttribute, mDefault.toString()), (Long) mDefault);
    } else if (mOverride == ProxyConfOverride.SERVER) {
      mValue = serverSource.getTimeInterval(mAttribute, (Long) mDefault);
    }
  }

  public String formatTime(Object o) {
    Formatter f = new Formatter();
    f.format("%dms", (Long) o);
    return f.toString();
  }

  public void updateInteger() {
    if (mOverride == ProxyConfOverride.CONFIG) {
      mValue = configSource.getIntAttr(mAttribute, (Integer) mDefault);
    } else if (mOverride == ProxyConfOverride.LOCALCONFIG) {
      mValue = Integer.valueOf(lcValue(mAttribute, mDefault.toString()));
    } else if (mOverride == ProxyConfOverride.SERVER) {
      mValue = serverSource.getIntAttr(mAttribute, (Integer) mDefault);
    }
  }

  public String formatInteger(Object o) {
    Formatter f = new Formatter();
    f.format("%d", (Integer) o);
    return f.toString();
  }

  public void updateLong() {
    if (mOverride == ProxyConfOverride.CONFIG) {
      mValue = configSource.getLongAttr(mAttribute, (Long) mDefault);
    } else if (mOverride == ProxyConfOverride.LOCALCONFIG) {
      mValue = Long.valueOf(lcValue(mAttribute, mDefault.toString()));
    } else if (mOverride == ProxyConfOverride.SERVER) {
      mValue = serverSource.getLongAttr(mAttribute, (Long) mDefault);
    }
  }

  public String formatLong(Object o) {
    Formatter f = new Formatter();
    Long l = (Long) o;

    if (l % (1024 * 1024) == 0) {
      f.format("%dm", l / (1024 * 1024));
    } else if (l % 1024 == 0) {
      f.format("%dk", l / 1024);
    } else {
      f.format("%d", l);
    }
    return f.toString();
  }

  private String lcValue(String key, String def) {
    String val = LC.get(key);

    return val.length() == 0 ? def : val;
  }

  boolean isValidUpstream(Server server, String serverName) {
    boolean isTarget = server.getBooleanAttr(Provisioning.A_zimbraReverseProxyLookupTarget, false);
    if (!isTarget) {
      return false;
    }

    String mode = server.getAttr(Provisioning.A_zimbraMailMode, "");
    if (mode.equalsIgnoreCase(Provisioning.MailMode.http.toString())
        || mode.equalsIgnoreCase(Provisioning.MailMode.mixed.toString())
        || mode.equalsIgnoreCase(Provisioning.MailMode.both.toString())
        || mode.equalsIgnoreCase(Provisioning.MailMode.redirect.toString())
        || mode.equalsIgnoreCase(Provisioning.MailMode.https.toString())) {
      return true;
    } else {
      mLog.warn(
          "Upstream: Ignoring server: "
              + serverName
              + " ,because its mail mode is: "
              + (mode.equals("") ? "EMPTY" : mode));
      return false;
    }
  }

  String generateServerDirective(Server server, String serverName, String portName) {
    int serverPort = server.getIntAttr(portName, 0);
    return generateServerDirective(server, serverName, serverPort);
  }

  String generateServerDirective(Server server, String serverName, int serverPort) {
    int timeout = server.getIntAttr(Provisioning.A_zimbraMailProxyReconnectTimeout, 60);
    String version = server.getAttr(Provisioning.A_zimbraServerVersion, "");
    int maxFails = server.getIntAttr(Provisioning.A_zimbraMailProxyMaxFails, 1);
    if (maxFails != 1 && !Objects.equals(version, "")) {
      return String.format(
          "%s:%d fail_timeout=%ds max_fails=%d version=%s",
          serverName, serverPort, timeout, maxFails, version);
    } else if (maxFails != 1) {
      return String.format(
          "%s:%d fail_timeout=%ds max_fails=%d", serverName, serverPort, timeout, maxFails);
    } else if (!Objects.equals(version, "")) {
      return String.format(
          "%s:%d fail_timeout=%ds version=%s", serverName, serverPort, timeout, version);
    } else {
      return String.format("%s:%d fail_timeout=%ds", serverName, serverPort, timeout);
    }
  }

  public static final class KeyValue {

    public final String key;
    public final String value;

    public KeyValue(String value) {
      this(ProxyConfVar.UNKNOWN_HEADER_NAME, value);
    }

    public KeyValue(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }
}
