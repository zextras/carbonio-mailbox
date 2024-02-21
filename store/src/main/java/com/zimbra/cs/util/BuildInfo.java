// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.zimbra.common.localconfig.LC;
import com.zimbra.cs.db.Versions;

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

    public static final String UNKNOWN = "unknown";
    public static final String ZMLICENSE_PATH = "/opt/zextras/bin/zmlicense";

    static {
        String version = UNKNOWN;
        String type = getType();
        String release = UNKNOWN;
        String date = UNKNOWN;
        String host = UNKNOWN;
        String majorversion = UNKNOWN;
        String minorversion = UNKNOWN;
        String microversion = UNKNOWN;
        String platform = getPlatform();
        String buildnum = "buildnum";
        try {
            Class<?> clz = Class.forName("com.zimbra.cs.util.BuildInfoGenerated");
            version = (String) clz.getField("VERSION").get(null);
            release = (String) clz.getField("RELEASE").get(null);
            date = (String) clz.getField("DATE").get(null);
            host = (String) clz.getField("HOST").get(null);
            majorversion = (String) clz.getField("MAJORVERSION").get(null);
            minorversion = (String) clz.getField("MINORVERSION").get(null);
            microversion = (String) clz.getField("MICROVERSION").get(null);
            buildnum = (String) clz.getField("BUILDNUM").get(null);
        }catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            System.out.println("build information not available: " + e);
        }

        VERSION = version;
        TYPE = type;
        RELEASE = release;
        DATE = date;
        HOST = host;
        PLATFORM = platform;
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

    /**
     * Returns the first line in {@code /opt/zextras/.platform}, or {@code unknown}
     * if the platform cannot be determined.
     */
    private static String getPlatform() {
        String platform = UNKNOWN;
        File platformFile = new File(LC.zimbra_home.value(), ".platform");
        if (platformFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(platformFile))){
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.length() > 0) {
                        platform = line;
                        break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Unable to determine platform.");
                e.printStackTrace(System.err);
            }
        } else {
            System.err.format("Unable to determine platform because %s does not exist.%n", platformFile);
        }
        return platform;
    }

    public static void main(String[] args) {
        System.out.println("Version: " + VERSION);
        System.out.println("Release: " + RELEASE);
        System.out.println("Build Date: " + DATE);
        System.out.println("Build Host: " + HOST);
        System.out.println("Full Version: " + FULL_VERSION);
        System.out.println("DB Version: " + Versions.DB_VERSION);
        System.out.println("Index Version: " + Versions.INDEX_VERSION);
    }
}
