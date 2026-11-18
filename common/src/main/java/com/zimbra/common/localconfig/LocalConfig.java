// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.localconfig;

import com.zimbra.common.soap.W3cDomUtil;
import com.zimbra.common.soap.XmlParseException;
import com.zimbra.common.util.FileUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

public class LocalConfig {

  static final String E_LOCALCONFIG = "localconfig";
  static final String E_KEY = "key";
  static final String A_NAME = "name";
  static final String E_VALUE = "value";

  private String mConfigFile;

  private String defaultConfigFile() {
    String zimbra_config = System.getProperty("zimbra.config");
    if (zimbra_config == null) {
      final String FS = File.separator;
      zimbra_config = FS + "opt" + FS + "zextras" + FS + "conf" + FS + "localconfig.xml";
    }
    return zimbra_config;
  }

  public String getConfigFile() {
    return mConfigFile;
  }

  private final Map<String, String> mConfiguredKeys = new HashMap<>();
  private final Map<String, String> mExpanded = new HashMap<>();

  public void set(String key, String value) {
    mConfiguredKeys.put(key, value);
  }

  String getRaw(String key) {
    if (mConfiguredKeys.containsKey(key)) return mConfiguredKeys.get(key);

    if (KnownKey.isKnown(key)) return KnownKey.getDefaultValue(key);

    return null;
  }

  String findKey(String value) {
    int begin = value.indexOf("${");
    if (begin == -1) {
      return null;
    }

    int end = value.indexOf("}", begin);
    if (end == -1) {
      return null;
    }

    return value.substring(begin + 2, end);
  }

  private String expandDeep(String key, Set<String> seenKeys) throws ConfigException {
    if (seenKeys.contains(key)) {
      StringBuilder sb = new StringBuilder();
      sb.append("recursive expansion of key '").append(key).append("':");
      for (String seen : seenKeys) sb.append(" ").append(seen);
      throw new ConfigException(sb.toString());
    }

    seenKeys.add(key);

    String replacement = getRaw(key);
    if (replacement == null) throw new ConfigException("null valued key '" + key + "' referenced");

    String nestedKey = null;
    while ((nestedKey = findKey(replacement)) != null) {
      String expanded = expandDeep(nestedKey, seenKeys);
      String target = "${" + nestedKey + "}";
      replacement = replacement.replace(target, expanded);
    }

    seenKeys.remove(key);
    return replacement;
  }

  String expand(String key, String rawValue) throws ConfigException {
    if (rawValue == null) return null;

    return expandDeep(key, new HashSet<>());
  }

  String get(String key) throws ConfigException {
    if (mExpanded.containsKey(key)) {
      return mExpanded.get(key);
    }
    if (KnownKey.isKnown(key)) {
      return KnownKey.getValue(key);
    }
    return null;
  }

  //
  // Load & save
  //
  public void save() throws IOException, ConfigException {
    Document doc = DocumentHelper.createDocument();
    Element root = doc.addElement(E_LOCALCONFIG);
    for (String key : mConfiguredKeys.keySet()) {
      String value = getRaw(key);
      if (value != null) {
        root.addElement(E_KEY).addAttribute(A_NAME, key).addElement(E_VALUE).addText(value);
      }
    }

    File configFile = new File(mConfigFile);
    File directory = configFile.getCanonicalFile().getParentFile();
    File tempFile = File.createTempFile("localconfig.xml.", "", directory);
    try (FileWriter fileWriter = new FileWriter(tempFile)) {
      XMLWriter xmlWriter = new XMLWriter(fileWriter, OutputFormat.createPrettyPrint());
      xmlWriter.write(doc);
      xmlWriter.flush();
    }
    configFile.delete();
    tempFile.renameTo(configFile);
  }

  void backup(String suffix) throws IOException {
    FileUtil.copy(new File(mConfigFile), new File(mConfigFile + suffix), true);
  }

  public LocalConfig(String file) throws DocumentException, ConfigException {
    mConfigFile = file;
    if (mConfigFile == null) {
      mConfigFile = defaultConfigFile();
    }

    File cf = new File(mConfigFile);
    if (cf.exists() && cf.canRead()) {
      try (FileInputStream fis = new FileInputStream(cf)) {
        Document document = W3cDomUtil.parseXMLToDom4jDocUsingSecureProcessing(fis);
        Element root = document.getRootElement();

        if (!root.getName().equals(E_LOCALCONFIG))
          throw new DocumentException(
              "config file " + mConfigFile + " root tag is not " + E_LOCALCONFIG);

        for (Iterator<?> iter = root.elementIterator(E_KEY); iter.hasNext(); ) {
          Element ekey = (Element) iter.next();
          String key = ekey.attributeValue(A_NAME);
          String value = ekey.elementText(E_VALUE);
          set(key, value);
        }
      } catch (IOException | XmlParseException e) {
        Logging.warn(String.format("Problem parsing local config file '%s'", cf), e);
        throw new DocumentException(String.format("Problem parsing local config file '%s'", cf));
      }
    } else {
      Logging.warn("local config file `" + cf + "' is not readable");
    }
    expandAll();
  }

  public boolean isSet(String key) {
    return mConfiguredKeys.containsKey(key) || KnownKey.isKnown(key);
  }

  public void remove(String key) {
    mConfiguredKeys.remove(key);
  }

  /*
   * Return all keys - known or otherwise.
   */
  String[] allKeys() {
    Set<String> union = new HashSet<>();

    // Add known keys.
    String[] knownKeys = KnownKey.getAll();
    union.addAll(Arrays.asList(knownKeys));

    // Add set keys (this might contain unknown keys)
    union.addAll(mConfiguredKeys.keySet());

    return union.toArray(new String[0]);
  }

  //
  // Expand and cache all keys
  //
  private void expandAll() throws ConfigException {
    KnownKey.expandAll(this);
    for (String key : mConfiguredKeys.keySet()) {
      mExpanded.put(key, expand(key, mConfiguredKeys.get(key)));
    }
  }

  /**
   * The singleton instance. This is a volatile variable, so that we can reload the config file on
   * the fly without locking.
   */
  private static volatile LocalConfig mLocalConfig;

  static LocalConfig getInstance() {
    return mLocalConfig;
  }

  /**
   * Loads the config file.
   *
   * @param path config file path or null to use the default path
   * @throws DocumentException if the config file was syntactically invalid
   * @throws ConfigException if the config file was semantically invalid
   */
  static synchronized void load(String path) throws DocumentException, ConfigException {
    mLocalConfig = new LocalConfig(path);
  }

  static {
    try {
      load(null);
    } catch (DocumentException | ConfigException de) {
      throw new RuntimeException(de);
    }
  }
}
