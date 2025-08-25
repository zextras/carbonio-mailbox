// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util;

import com.zimbra.cs.db.Versions;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public final class BuildInfo {

  public static final String TYPE_NETWORK = "NETWORK";
  public static final String TYPE_FOSS = "FOSS";

  public static final String TYPE;     /* whether this is a FOSS or NETWORK installation */
  public static final String VERSION;
  public static final String RELEASE;
  public static final String DATE;
  public static final String HOST;
  public static final String PLATFORM;
  public static final String MAJORVERSION;
  public static final String MINORVERSION;
  public static final String MICROVERSION;
  public static final String BUILDNUM;

  public static final String FULL_VERSION;

  private static final String UNKNOWN = "unknown";
  private static final String ZMLICENSE_PATH = "/opt/zextras/bin/zmlicense";

  static {
    String version = UNKNOWN;
    String type = getType();
    String release = UNKNOWN;
    String date = UNKNOWN;
    String host = UNKNOWN;
    String majorversion = UNKNOWN;
    String minorversion = UNKNOWN;
    String microversion = UNKNOWN;
		String buildnum = "buildnum";
    try {
      final Properties properties = new Properties();
      properties.load(BuildInfo.class.getResourceAsStream("buildInfo.properties"));
      version = properties.getProperty("VERSION");
      release = properties.getProperty("RELEASE");
      date = properties.getProperty("DATE");
      host = properties.getProperty("HOST");
      majorversion = properties.getProperty("MAJORVERSION");
      minorversion = properties.getProperty("MINORVERSION");
      microversion = properties.getProperty("MICROVERSION");
      buildnum = properties.getProperty("BUILDNUM");
    } catch (IOException e) {
      System.out.println("build information not available: " + e);
    }

    VERSION = version;
    TYPE = type;
    RELEASE = release;
    DATE = date;
    HOST = host;
    PLATFORM = UNKNOWN;
    MAJORVERSION = majorversion;
    MINORVERSION = minorversion;
    MICROVERSION = microversion;
    BUILDNUM = buildnum;
    if (TYPE != null && TYPE.length() > 0) {
      // e.g. 6.0.0_BETA2_1542.RHEL4_64 20090529191053 20090529-1912 NETWORK
      FULL_VERSION = VERSION + " " + RELEASE + " " + DATE + " " + TYPE;
    } else {
      FULL_VERSION = VERSION + " " + RELEASE + " " + DATE;
    }
  }

  private static String getType() {
    File licenseBin = new File(ZMLICENSE_PATH);
    return licenseBin.exists() ? TYPE_NETWORK : TYPE_FOSS;
  }


  public record BuildInfoData(String version,String release,String date,String host,
                                     String fullVersion, int dbVersion, int indexVersion) {

  }

  public static BuildInfoData getVersion() {
    return new BuildInfoData(VERSION, RELEASE, DATE, HOST, FULL_VERSION, Versions.DB_VERSION, Versions.INDEX_VERSION);
  }

  public static void main(String[] args) {
    final BuildInfoData version = getVersion();
    System.out.println("Version: " + version.version());
    System.out.println("Release: " + version.release());
    System.out.println("Build Date: " + version.release());
    System.out.println("Build Host: " + version.host());
    System.out.println("Full Version: " + version.fullVersion());
    System.out.println("DB Version: " + version.dbVersion());
    System.out.println("Index Version: " + version.indexVersion());
  }
}
