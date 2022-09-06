// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ClassLoaderUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SkinUtil {

  private static final String LOAD_SKINS_ON_UI_NODE = "/fromservice/loadskins";
  private static final String FLUSH_SKINS_ON_UI_NODE = "/fromservice/flushskins";

  private static String[] sSkins = null;

  // returns all installed skins
  private static synchronized String[] getAllInstalledSkinsSorted() throws ServiceException {
    if (sSkins == null) {
      sSkins = loadSkins();
    }
    return sSkins;
  }

  public static synchronized void flushCache() throws ServiceException {
    sSkins = null;
    if (WebClientServiceUtil.isServerInSplitMode()) {
      WebClientServiceUtil.sendServiceRequestToEveryUiNode(FLUSH_SKINS_ON_UI_NODE);
    }
  }

  private static String[] loadSkins() throws ServiceException {
    ZimbraLog.webclient.debug("Loading skins...");
    List<String> skins = new ArrayList<String>();
    if (WebClientServiceUtil.isServerInSplitMode()) {
      String resp = WebClientServiceUtil.sendServiceRequestToOneRandomUiNode(LOAD_SKINS_ON_UI_NODE);
      Collections.addAll(skins, resp.split(","));
    } else {
      loadSkinsByDiskScan(skins);
    }
    String[] sortedSkins = skins.toArray(new String[skins.size()]);
    Arrays.sort(sortedSkins);
    ZimbraLog.webclient.debug("Skin loading complete.");
    return sortedSkins;
  }

  public static void loadSkinsByDiskScan(List<String> skins) throws ServiceException {
    String skinsDir = LC.skins_directory.value();
    ClassLoader classLoader = ClassLoaderUtil.getClassLoaderByDirectory(skinsDir);
    if (classLoader == null) {
      throw ServiceException.FAILURE(
          "unable to get class loader for directory "
              + skinsDir
              + " configured in localconfig key "
              + LC.skins_directory.key(),
          null);
    }
    File dir = new File(skinsDir);
    File[] files = dir.listFiles();

    if (files != null) {
      for (File f : files) {
        String fname = f.getName();
        if (!fname.startsWith("_") && new File(f, "manifest.xml").exists()) {
          skins.add(fname);
        }
      }
    }
  }

  public static String[] getSkins(Account acct) throws ServiceException {
    String[] installedSkins = getAllInstalledSkinsSorted();
    Set<String> allowedSkins = getAvailableSkins(acct);

    String[] availSkins = null;
    if (allowedSkins.size() == 0) {
      availSkins = installedSkins;
    } else {
      List<String> skins = new ArrayList<String>();
      // take intersection of the two, loop thru installedSkins because it is sorted
      for (String skin : installedSkins) {
        if (allowedSkins.contains(skin)) {
          skins.add(skin);
        }
      }
      availSkins = skins.toArray(new String[skins.size()]);
    }
    return availSkins;
  }

  public static String[] getAllSkins() throws ServiceException {
    return getAllInstalledSkinsSorted();
  }

  private static Set<String> getAvailableSkins(Account acct) throws ServiceException {
    return acct.getMultiAttrSet(Provisioning.A_zimbraAvailableSkin);
  }

  public static String chooseSkin(Account acct, String requestedSkin) throws ServiceException {
    String[] installedSkins = getAllInstalledSkinsSorted();

    // If the requested skin is installed and allowed, return it.
    Set<String> allowedSkins = getAvailableSkins(acct);
    if (checkSkin(requestedSkin, installedSkins, allowedSkins)) {
      ZimbraLog.webclient.debug("Loading requested skin " + requestedSkin);
      return requestedSkin;
    }

    // If the account/cos's pref skin is installed and allowed, return it.
    String prefSkin = acct.getAttr(Provisioning.A_zimbraPrefSkin);
    if (checkSkin(prefSkin, installedSkins, allowedSkins)) {
      ZimbraLog.webclient.debug("Loading account skin " + prefSkin);
      return prefSkin;
    }

    // Nothing in ldap has a valid skin.
    // fallback to zextras if selected skin not available
    String usuallyAvailableSkin = "zextras";
    if (prefSkin != usuallyAvailableSkin) {
      if (checkSkin(usuallyAvailableSkin, installedSkins, allowedSkins)) {
        ZimbraLog.webclient.debug("Loading default skin " + usuallyAvailableSkin);
        return usuallyAvailableSkin;
      }
    }

    // Return some installed skin.
    if (installedSkins.length > 0) {
      ZimbraLog.webclient.debug("Returning first known skin " + installedSkins[0]);
      return installedSkins[0];
    }

    // Didn't find an acceptable skin. Return null and hope the client doesn't need it.
    return null;
  }

  private static boolean checkSkin(
      String requestedSkin, String[] installedSkins, Set<String> allowedSkins) {
    if (requestedSkin != null && requestedSkin.length() > 0) {
      for (String skin : installedSkins) {
        if (requestedSkin.equals(skin)) {
          if (allowedSkins.size() == 0 || allowedSkins.contains(skin)) {
            return true;
          }
        }
      }
    }
    return false;
  }
}
