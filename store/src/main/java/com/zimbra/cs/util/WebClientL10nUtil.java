// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.L10nUtil;
import com.zimbra.common.util.L10nUtil.MatchingPropertiesFilter;
import com.zimbra.common.util.SetUtil;
import com.zimbra.common.util.ZimbraLog;

public class WebClientL10nUtil {

    private static final String LOAD_LOCALES_ON_UI_NODE = "/fromservice/loadlocales";

    /**
     * Return all known locales sorted by their US English display name.
     * @return
     */
    public static Locale[] getAllLocalesSorted() {
        Locale[] locales = Locale.getAvailableLocales();
        Arrays.sort(locales, new LocaleComparatorByDisplayName(Locale.US));
        return locales;
    }

    private static class LocaleComparatorByDisplayName implements Comparator<Locale> {
        private final Locale mInLocale;

        LocaleComparatorByDisplayName(Locale inLocale) {
            mInLocale = inLocale;
        }

        @Override
        public int compare(Locale a, Locale b) {
            String da = a.getDisplayName(mInLocale);
            String db = b.getDisplayName(mInLocale);
            return da.compareTo(db);
        }
    }

    public static synchronized Set<Locale> getAvailableLocales() throws ServiceException {
        if (locales == null) {
            loadBundles();
        }
        return locales;
    }

    enum ClientResource {
        // I18nMsg,  // generated, all locales are there, so we don't count this resource
        AjxMsg,
        ZMsg,
        ZaMsg,
        ZhMsg,
        ZmMsg
    }

    // set of localized(translated) locales
    static Set<Locale> locales = null;

    // we cache the sorted list per display locale to avoid the array copy
    // and sorting each time for a GetLocale request
    static Map<Locale, Locale[]> sortedLocalesMap = null;

    public static void loadBundlesByDiskScan() {
        String msgsDir = LC.localized_client_msgs_directory.value();
        ZimbraLog.misc.info("Scanning installed locales from %s", msgsDir);
        locales = new HashSet<>();

        // the en_US locale is always available
        ZimbraLog.misc.info("Adding locale " + Locale.US.toString() + " (always added)");
        locales.add(Locale.US);
        File dir = new File(msgsDir);
        if (!dir.exists()) {
            ZimbraLog.misc.info("message directory does not exist: %s", msgsDir);
            return;
        }
        if (!dir.isDirectory()) {
            ZimbraLog.misc.info("message directory is not a directory: %s", msgsDir);
            return;
        }

        for (File file : dir.listFiles(new MatchingPropertiesFilter(ClientResource.values()))) {
            ZimbraLog.misc.debug("loadBundlesByDiskScan processing file: %s", file.getName());
            Locale locale = L10nUtil.getLocaleForPropertiesFile(file, true);
            if (locale != null && !locales.contains(locale)) {
                ZimbraLog.misc.info("Adding locale: %s", locale);
                locales.add(locale);
            }
        }
    }

    private static void loadBundles() throws ServiceException {
        ZimbraLog.webclient.debug("Loading locales...");
        locales = new HashSet<Locale>();

        if (WebClientServiceUtil.isServerInSplitMode()) {
            String localesStr = WebClientServiceUtil.sendServiceRequestToOneRandomUiNode(LOAD_LOCALES_ON_UI_NODE);
            for (String str : localesStr.split(",")) {
                String[] parts = str.split("_");
                switch (parts.length) {
                    case 1:
                        locales.add(new Locale(parts[0]));
                        break;
                    case 2:
                        locales.add(new Locale(parts[0], parts[1]));
                        break;
                    case 3:
                        locales.add(new Locale(parts[0], parts[1], parts[2]));
                        break;
                    default:
                        ZimbraLog.misc.warn("unsupported locale %s", str);
                }
            }
        } else {
            loadBundlesByDiskScan();
        }

        /*
         * UI displays locales with country in sub menus.
         *
         * E.g. if there are:
         *      id: "zh_CN", name: "Chinese (China)"
         *      id: "zh_HK", name: "Chinese (Hong Kong)"
         *
         *      then the menu looks like:
         *          Chinese
         *                   Chinese (China)
         *                   Chinese (Hong Kong)
         *
         *      UI relies on the presence of a "language only" entry
         *      for the top level label "Chinese".
         *      i.e. id: "zh", name: "Chinese"
         *
         *      Thus we need to add a "language only" pseudo entry for locales that have
         *      a country part but the "language only" entry is not already there.
         */
        Set<Locale> pseudoLocales = new HashSet<Locale>();
        for (Locale lc : locales) {
            String language = lc.getLanguage();
            Locale lcLang = new Locale(language);
            if (!locales.contains(lcLang) && !pseudoLocales.contains(lcLang)) {
                ZimbraLog.misc.info("Adding locale " + lcLang.toString() + " (pseudo)");
                pseudoLocales.add(lcLang);
            }
        }
        if (pseudoLocales.size() > 0) {
            locales = SetUtil.union(locales, pseudoLocales);
        }
        ZimbraLog.webclient.debug("Locale loading complete.");
    }

    public synchronized static Locale[] getLocales(Locale inLocale) throws ServiceException {
        if (locales == null) {
            loadBundles();
        }

        Locale[] sortedLocales = null;
        if (sortedLocalesMap == null) {
            sortedLocalesMap = new HashMap<Locale, Locale[]>();
        } else {
            sortedLocales = sortedLocalesMap.get(inLocale);
        }

        if (sortedLocales == null) {
            // cache the sorted list per display locale
            sortedLocales = locales.toArray(new Locale[0]);
            Arrays.sort(sortedLocales, new LocaleComparatorByDisplayName(inLocale));
            sortedLocalesMap.put(inLocale, sortedLocales);
        }
        return sortedLocales;
    }

    public synchronized static void flushCache() throws ServiceException {
        ZimbraLog.misc.debug("WebClientL10nUtil: flushing locale cache");
        locales = null;
        sortedLocalesMap = null;
    }
}