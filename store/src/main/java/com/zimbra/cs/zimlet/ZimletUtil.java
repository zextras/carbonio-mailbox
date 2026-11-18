// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.zimlet;

import com.zimbra.common.localconfig.DebugConfig;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.W3cDomUtil;
import com.zimbra.common.util.Pair;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Zimlet;
import com.zimbra.cs.zimlet.ZimletPresence.Presence;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jylee
 *
 */
public class ZimletUtil {

    public static final String ZIMLET_BASE = "/service/zimlet";
    public static final String ZIMLET_DEV_DIR = "_dev";
    public static final String ZIMLET_NAME_REGEX = "^[\\w.-]+$";

    private static boolean sZimletsLoaded = false;
    private static Map<String,ZimletFile> sZimlets = new HashMap<>();

    private static Map<String, Class> sZimletHandlers = new HashMap<>();

    public static void migrateUserPrefIfNecessary(Account acct) throws ServiceException {
        if (!DebugConfig.enableMigrateUserZimletPrefs) {
            return;
        }

        Set<String> wanted = acct.getMultiAttrSet(Provisioning.A_zimbraPrefZimlets);
        Set<String> unwanted = acct.getMultiAttrSet(Provisioning.A_zimbraPrefDisabledZimlets);

        // needs upgrade only if wanted is not empty and unwanted is empty, because
        // if wanted is empty: means use whatever zimbraZimletAvailableZimlets/zimbraZimletDomainAvailableZimlets says
        // if unwanted is not empty: already migrated or user has change it.
        boolean needsMigrate = (!wanted.isEmpty()) && unwanted.isEmpty();

        if (!needsMigrate) {
            return;
        }

        ZimletPresence availZimlets = getAvailableZimlets(acct);
        Map<String, Object> attrs = new HashMap<>();
        StringBuilder disabledZimletNamesForLogging = new StringBuilder();

        for (String zimletName : availZimlets.getZimletNames()) {
            Presence presence = availZimlets.getPresence(zimletName);

            if (presence == Presence.enabled && !wanted.contains(zimletName)) {
                disabledZimletNamesForLogging.append(zimletName).append(", ");
                StringUtil.addToMultiMap(attrs, Provisioning.A_zimbraPrefDisabledZimlets, zimletName);
            }
        }

        // in the odd case when someone has all available zimlets set in his zimbraPrefZimlets,
        // attrs will be empty => we've got nothing to do.
        if (attrs.isEmpty()) {
            ZimbraLog.account.info("Not migrating zimbraPrefDisabledZimlets for account " + acct.getName() + " because all zimlets are enabled");
            return;
        }

        ZimbraLog.account.info("Migrating zimbraPrefDisabledZimlets for account " + acct.getName() + " to " + disabledZimletNamesForLogging);

        Provisioning prov = Provisioning.getInstance();
        prov.modifyAttrs(acct, attrs);
    }

    public static ZimletPresence getUserZimlets(Account acct) throws ServiceException {
        ZimletPresence userZimlets = getAvailableZimlets(acct);

        // userZimlets now contains all allowed zimlets for the user
        // process user pref, which overrides cos/domain enabled/disabled by default
        // user pref cannot override cos/domain mandatory zimlets

        String[] userPrefEnabledZimlets = acct.getMultiAttr(Provisioning.A_zimbraPrefZimlets);
        for (String zimletName : userPrefEnabledZimlets) {
            Presence presence = userZimlets.getPresence(zimletName);
            if (presence != null &&               // zimlet is allowed
                presence != Presence.mandatory && // can't override mandatory
                presence != Presence.enabled) {   // disabled by default, but user specifically enabled it
                userZimlets.put(zimletName, Presence.enabled);
            }
        }

        String[] userPrefDisabledZimlets = acct.getMultiAttr(Provisioning.A_zimbraPrefDisabledZimlets);
        for (String zimletName : userPrefDisabledZimlets) {
            Presence presence = userZimlets.getPresence(zimletName);
            if (presence != null &&               // zimlet is allowed
                presence != Presence.mandatory && // can't override mandatory
                presence != Presence.disabled) {  // enabled by default, but user specifically disabled it
                userZimlets.put(zimletName, Presence.disabled);
            }
        }

        return userZimlets;
    }

    public static ZimletPresence getAvailableZimlets(Account acct) throws ServiceException {
        ZimletPresence availZimlets = new ZimletPresence();

        // process domain settings first, because if domain and cos conflict we honor the cos setting
        Domain domain = Provisioning.getInstance().getDomain(acct);
        if (domain != null) {
            String[] domainZimlets = domain.getMultiAttr(Provisioning.A_zimbraZimletDomainAvailableZimlets);
            for (String zimletWithPrefix : domainZimlets) {
                availZimlets.put(zimletWithPrefix);
            }
        }

        String[] acctCosZimlets = acct.getMultiAttr(Provisioning.A_zimbraZimletAvailableZimlets);
        for (String zimletWithPrefix : acctCosZimlets) {
            availZimlets.put(zimletWithPrefix);
        }

        return availZimlets;
    }

    public static List<Zimlet> orderZimletsByPriority(List<Zimlet> zimlets) {
        // create a sortable collection, sort, then return List<Zimlet> in the
        // sorted order.  version is not comparable in String format.
        List<Pair<Version,Zimlet>> plist = new ArrayList<>();
        for (Zimlet z : zimlets) {
            String pstring = z.getPriority();
            if (pstring == null) {
                // no priority.  put it at the end of priority list
                pstring = Integer.toString(Integer.MAX_VALUE);
            }
            Version v = new Version(pstring);
            plist.add(new Pair<>(v, z));
        }
        plist.sort((first, second) -> first.getFirst().compareTo(second.getFirst()));

        List<Zimlet> ret = new ArrayList<>();
        for (Pair<Version,Zimlet> p : plist) {
            ret.add(p.getSecond());
        }
        return ret;
    }

    public static List<Zimlet> orderZimletsByPriority(String[] zimlets) {
        Provisioning prov = Provisioning.getInstance();
        List<Zimlet> zlist = new ArrayList<>();
      for (String zimlet : zimlets) {
        try {
          Zimlet z = prov.getZimlet(zimlet);
          if (z != null) {
            zlist.add(z);
          }
        } catch (ServiceException se) {
          // ignore error and continue on
          ZimbraLog.zimlet.warn("unable to get zimlet " + zimlet, se);
        }
      }
        return orderZimletsByPriority(zlist);
    }

    public static List<Zimlet> orderZimletsByPriority() throws ServiceException {
        Provisioning prov = Provisioning.getInstance();
        List<Zimlet> allzimlets = prov.listAllZimlets();
        return orderZimletsByPriority(allzimlets);
    }

    /**
     * Loads all the Zimlets, locates the server side ZimletHandler for each Zimlets,
     * loads the class and instantiate the object, then returns the instance.
     *
     * @param name of the Zimlet
     * @return ZimletHandler object
     */
    public static ZimletHandler getHandler(String name) {
        loadZimlets();
        Class zh = sZimletHandlers.get(name);
        if (zh == null) {
            ZimletFile zf = sZimlets.get(name);
            if (zf == null) {
                return null;
            }
            URLClassLoader cl = null;
            try {
                String clazz = zf.getZimletDescription().getServerExtensionClass();
                if (clazz != null) {
                    URL[] urls = { zf.toURL() };
                    cl = new URLClassLoader(urls, ZimletUtil.class.getClassLoader());
                    zh = cl.loadClass(clazz);
                    ZimbraLog.zimlet.info("Loaded class "+zh.getName());
                    sZimletHandlers.put(name, zh);
                }
            } catch (Exception e) {
                ZimbraLog.zimlet.warn("Unable to load zimlet handler for %s", name, e);
                return null;
            } finally {
                if (cl != null) {
                    try {
                        cl.close();
                    } catch (IOException e) {
                        ZimbraLog.zimlet.warn("failed to close URLClassLoader", e);
                    }
                }
            }
        }
        try {
            if (zh != null) {
                return (ZimletHandler) zh.newInstance();
            }
        } catch (Exception e) {
            ZimbraLog.zimlet.warn("Unable to instantiate zimlet handler for "+name, e);
        }
        return null;
    }

    /**
     *
     * Load all the installed Zimlets.
     *
     */
    public static synchronized Map<String,ZimletFile> loadZimlets() {
        if (!sZimletsLoaded) {
            loadZimletsFromDir(sZimlets, LC.zimlet_directory.value());
            sZimletsLoaded = true;
        }
        // NOTE: Was added for consistency with loadDevZimlets
        return sZimlets;
    }

    /**
     *
     * Load all the Zimlets in the dev test directory.
     *
     */
    public static Map<String,ZimletFile> loadDevZimlets() {
        Map<String,ZimletFile> zimletMap = new HashMap<>();
        loadZimletsFromDir(zimletMap, LC.zimlet_directory.value() + File.separator + ZIMLET_DEV_DIR);
        return zimletMap;
    }

    /**
     *
     * Throw away the cached Zimlet, and reloads from the file system.
     *
     * @param zimlet
     * @throws ZimletException
     */
    public static void reloadZimlet(String zimlet) throws ZimletException {
        ZimletFile zf;
        try {
            zf = new ZimletFile(LC.zimlet_directory.value() + File.separator + zimlet);
        } catch (IOException ioe) {
            ZimbraLog.zimlet.warn(ioe.getMessage());
            return;
        }
        synchronized (sZimlets) {
            sZimlets.remove(zimlet);
            sZimlets.put(zimlet, zf);
        }
    }

    /**
     *
     * Load all the Zimlets found in the directory.
     *
     * @param zimlets - Zimlet cache
     * @param dir - directory
     */
    private static void loadZimletsFromDir(Map<String,ZimletFile> zimlets, String dir) {
        File zimletRootDir = new File(dir);
        if (zimletRootDir == null || !zimletRootDir.exists() || !zimletRootDir.isDirectory()) {
            return;
        }

        ZimbraLog.zimlet.debug("Loading zimlets from " + zimletRootDir.getPath());

        synchronized (zimlets) {
            zimlets.clear();
            String[] zimletNames = zimletRootDir.list();
            assert(zimletNames != null);
          for (String zimletName : zimletNames) {
            if (zimletName.equals(ZIMLET_DEV_DIR)) {
              continue;
            }
            try {
              zimlets.put(zimletName, new ZimletFile(zimletRootDir, zimletName));
            } catch (Exception e) {
              ZimbraLog.zimlet.warn("error loading zimlet " + zimletName, e);
            }
          }
        }
    }

    /**
     *
     * List the Zimlet description as XML or JSON Element.
     *
     * @param elem - Parent Element node
     * @param zimlet
     */
    public static void listZimlet(Element elem, Zimlet zimlet, int priority, Presence presence) {
        loadZimlets();
        ZimletFile zf = sZimlets.get(zimlet.getName());
        if (zf == null) {
            ZimbraLog.zimlet.warn("cannot find zimlet "+zimlet.getName());
            return;
        }
        String zimletBase = ZIMLET_BASE + "/" + zimlet.getName() + "/";
        Element entry = elem.addElement(AccountConstants.E_ZIMLET);
        Element zimletContext = entry.addElement(AccountConstants.E_ZIMLET_CONTEXT);
        zimletContext.addAttribute(AccountConstants.A_ZIMLET_BASE_URL, zimletBase);
        if (priority >= 0) {
            zimletContext.addAttribute(AccountConstants.A_ZIMLET_PRIORITY, priority);
        }

        if (presence == null) {
            presence = Presence.enabled;
        }
        zimletContext.addAttribute(AccountConstants.A_ZIMLET_PRESENCE, presence.toString());

        try {
            zf.getZimletDescription().addToElement(entry);
            String config = zimlet.getHandlerConfig();
            if (config != null) {
                entry.addElement(W3cDomUtil.parseXML(config, elem.getFactory()));
            }
        } catch (Exception e) {
            ZimbraLog.zimlet.warn("error loading zimlet "+zimlet, e);
        }
    }

    /**
     *
     * List the description of all the dev test Zimlets as Element.
     *
     * @param elem - Parent element node
     */
    public static void listDevZimlets(Element elem) {
        for (ZimletFile zim : loadDevZimlets().values()) {
            String zimletBase = ZIMLET_BASE + "/" + ZIMLET_DEV_DIR + "/" + zim.getZimletName() + "/";
            Element entry = elem.addElement(AccountConstants.E_ZIMLET);
            Element zimletContext = entry.addElement(AccountConstants.E_ZIMLET_CONTEXT);
            zimletContext.addAttribute(AccountConstants.A_ZIMLET_BASE_URL, zimletBase);

            // dev zimlets are all enabled
            zimletContext.addAttribute(AccountConstants.A_ZIMLET_PRESENCE, Presence.enabled.toString());

            try {
                zim.getZimletDescription().addToElement(entry);
                if (zim.hasZimletConfig()) {
                    zim.getZimletConfig().addToElement(entry);
                }
            } catch (Exception e) {
                ZimbraLog.zimlet.warn("error loading dev zimlet: "+zim.getName(), e);
            }
        }
    }

}
