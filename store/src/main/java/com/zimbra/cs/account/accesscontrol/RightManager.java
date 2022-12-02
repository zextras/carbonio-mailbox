// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.zimbra.common.localconfig.DebugConfig;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.W3cDomUtil;
import com.zimbra.common.soap.XmlParseException;
import com.zimbra.common.util.CliUtil;
import com.zimbra.common.util.SetUtil;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.AttributeClass;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.FileGenUtil;
import com.zimbra.cs.account.accesscontrol.Right.RightType;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;


public class RightManager {

    private static final String E_A            = "a";
    private static final String E_ATTRS        = "attrs";
    private static final String E_DEFAULT      = "default";
    private static final String E_DESC         = "desc";
    private static final String E_HELP         = "help";
    private static final String E_INCLUDE      = "include";
    private static final String E_ITEM         = "item";
    private static final String E_R            = "r";
    private static final String E_RIGHTS       = "rights";
    private static final String E_RIGHT        = "right";
    private static final String E_ROOT         = "root";
    private static final String E_UI           = "ui";

    private static final String A_CACHE        = "cache";
    private static final String A_FALLBACK     = "fallback";
    private static final String A_FILE         = "file";
    private static final String A_GRANT_TARGET_TYPE  = "grantTargetType";
    private static final String A_LIMIT        = "l";
    private static final String A_N            = "n";
    private static final String A_NAME         = "name";
    private static final String A_TARGET_TYPE  = "targetType";
    private static final String A_TYPE         = "type";
    private static final String A_USER_RIGHT   = "userRight";

    private static final String TARGET_TYPE_DELIMITER   = ",";

    private static RightManager mInstance;

    // keep the map sorted so "zmmailbox lp" can display in alphabetical order
    private final Map<String, UserRight> sUserRights = new TreeMap<String, UserRight>();
    private final Map<String, AdminRight> sAdminRights = new TreeMap<String, AdminRight>();
    private final Map<String, Help> sHelp = new TreeMap<String, Help>();
    private final Map<String, UI> sUI = new TreeMap<String, UI>();

    static private class CoreRightDefFiles {
        private static final HashSet<String> sCoreRightDefFiles = new HashSet<String>();

        static void init(boolean unittest) {
            sCoreRightDefFiles.add("rights.xml");
            sCoreRightDefFiles.add("user-rights.xml");

            if (unittest || DebugConfig.running_unittest) {
                sCoreRightDefFiles.add("rights-unittest.xml");
            }
        }

        static boolean isCoreRightFile(File file) {
            return sCoreRightDefFiles.contains(file.getName());
        }

        static String listCoreDefFiles() {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (String file : sCoreRightDefFiles) {
                if (!first)
                    sb.append(", ");
                else
                    first = false;
                sb.append(file);
            }
            return sb.toString();
        }
    }

    public static synchronized RightManager getInstance() throws ServiceException {
        return getInstance(false);
    }

    public static synchronized RightManager getInstance(boolean unittest)
    throws ServiceException {
        return getInstance(LC.zimbra_rights_directory.value(), unittest);
    }

    private static synchronized RightManager getInstance(String dir, boolean unittest)
    throws ServiceException {
        if (mInstance != null) {
            return mInstance;
        }

        mInstance = new RightManager(dir, unittest);

        try {
            Right.init(mInstance);
        } catch (ServiceException e) {
            ZimbraLog.acl.error("failed to initialize known right from: " + dir, e);
            throw e;
        }
        return mInstance;
    }

    private RightManager(String dir, boolean unittest) throws ServiceException {
        CoreRightDefFiles.init(unittest);

        File fdir = new File(dir);
        if (!fdir.exists()) {
            throw ServiceException.FAILURE("rights directory does not exists: " + dir, null);
        }
        if (!fdir.isDirectory()) {
            throw ServiceException.FAILURE("rights directory is not a directory: " + dir, null);
        }

        ZimbraLog.acl.debug("Loading rights from %s", fdir.getAbsolutePath());

        File[] files = fdir.listFiles();
        List<File> yetToProcess = new ArrayList<File>(Arrays.asList(files));
        List<File> processed = new ArrayList<File>();

        while (!yetToProcess.isEmpty()) {
            File file = yetToProcess.get(0);

            if (!file.getPath().endsWith(".xml") || !file.isFile()) {
                ZimbraLog.acl.warn("while loading rights, ignoring none .xml file or sub folder: %s", file);
                yetToProcess.remove(file);
                continue;
            }

            try {
                boolean done = loadSystemRights(file, processed, files);
                if (done) {
                    processed.add(file);
                    yetToProcess.remove(file);
                } else {
                    // move this file to the end
                    yetToProcess.remove(file);
                    yetToProcess.add(file);
                }
            } catch (XmlParseException | FileNotFoundException e) {
                throw ServiceException.PARSE_ERROR("error loading rights file: " + file, e);
            }
        }
    }

    private boolean getBoolean(String value) throws ServiceException {
        if ("1".equals(value))
            return true;
        else if ("0".equals(value))
            return false;
        else
            throw ServiceException.PARSE_ERROR("invalid value:" + value, null);
    }

    private boolean getBooleanAttr(Element elem, String attr) throws ServiceException {
        String value = elem.attributeValue(attr);
        if (value == null)
            throw ServiceException.PARSE_ERROR("missing required attribute: " + attr, null);
        return getBoolean(value);
    }

    private boolean getBooleanAttr(Element elem, String attr, boolean defaultValue)
    throws ServiceException {
        String value = elem.attributeValue(attr);
        if (value == null)
            return defaultValue;
        return getBoolean(value);
    }

    private void parseDesc(Element eDesc, Right right) throws ServiceException {
        if (right.getDesc() != null)
            throw ServiceException.PARSE_ERROR("multiple " + E_DESC, null);
        right.setDesc(eDesc.getText());
    }

    private void parseHelp(Element eHelp, Right right) throws ServiceException {
        if (right.getHelp() != null) {
            throw ServiceException.PARSE_ERROR("multiple " + E_HELP, null);
        }

        String helpName = eHelp.attributeValue(A_NAME);
        if (helpName == null) {
            throw ServiceException.PARSE_ERROR("missing help name", null);
        }

        Help help = sHelp.get(helpName);

        if (help == null) {
            throw ServiceException.PARSE_ERROR("no such help: " + helpName, null);
        }

        right.setHelp(help);
    }

    private void parseUI(Element eUI, Right right) throws ServiceException {
        if (right.getUI() != null) {
            throw ServiceException.PARSE_ERROR("multiple " + E_UI + " for right " + right.getName(), null);
        }

        String uiName = eUI.attributeValue(A_NAME);
        if (uiName == null) {
            throw ServiceException.PARSE_ERROR("missing ui name", null);
        }

        UI ui = sUI.get(uiName);

        if (ui == null) {
            throw ServiceException.PARSE_ERROR("no such ui: " + uiName, null);
        }

        right.setUI(ui);
    }

    private void parseDefault(Element eDefault, Right right) throws ServiceException {
        String defaultValue = eDefault.getText();
        if ("allow".equalsIgnoreCase(defaultValue))
            right.setDefault(Boolean.TRUE);
        else if ("deny".equalsIgnoreCase(defaultValue))
            right.setDefault(Boolean.FALSE);
        else
            throw ServiceException.PARSE_ERROR("invalid default value: " + defaultValue, null);
    }

    private void parseAttr(Element eAttr, AttrRight right) throws ServiceException {
        String attrName = eAttr.attributeValue(A_N);
        if (attrName == null)
            throw ServiceException.PARSE_ERROR("missing attr name", null);

        right.validateAttr(attrName);
        right.addAttr(attrName);
    }

    private void parseAttrs(Element eAttrs, Right right) throws ServiceException {
        if (!(right instanceof AttrRight))
            throw ServiceException.PARSE_ERROR(
                    E_ATTRS + " is only allowed for admin getAttrs or setAttrs right", null);

        AttrRight attrRight = (AttrRight)right;
        for (Iterator elemIter = eAttrs.elementIterator(); elemIter.hasNext();) {
            Element elem = (Element)elemIter.next();
            if (elem.getName().equals(E_A))
                parseAttr(elem, attrRight);
            else
                throw ServiceException.PARSE_ERROR("invalid element: " + elem.getName(), null);
        }
    }

    private void parseRight(Element eAttr, ComboRight right) throws ServiceException {
        String rightName = eAttr.attributeValue(A_N);
        if (rightName == null)
            throw ServiceException.PARSE_ERROR("missing right name", null);

        Right r = getRight(rightName); // getRight will throw if the right does not exist.
        // combo right can only contain admin rights
        if (r.isUserRight())
            throw ServiceException.PARSE_ERROR(r.getName() + " is an user right, combo right " +
                    "can only contain admin rights.", null);

        right.addRight(r);
    }

    private void parseRights(Element eAttrs, Right right) throws ServiceException {
        if (!(right instanceof ComboRight))
            throw ServiceException.PARSE_ERROR(E_RIGHTS + " is only allowed for admin combo right", null);

        ComboRight comboRight = (ComboRight)right;

        for (Iterator elemIter = eAttrs.elementIterator(); elemIter.hasNext();) {
            Element elem = (Element)elemIter.next();
            if (elem.getName().equals(E_R))
                parseRight(elem, comboRight);
            else
                throw ServiceException.PARSE_ERROR("invalid element: " + elem.getName(), null);
        }
    }

    private Right parseRight(Element eRight) throws ServiceException {
        String name = eRight.attributeValue(A_NAME);

        // system define rights cannot contain a ".".  "." is the separator for inline attr right
        if (name.contains("."))
            throw ServiceException.PARSE_ERROR("righ name cannot contain dot(.): " + name, null);

        boolean userRight = getBooleanAttr(eRight, A_USER_RIGHT, false);

        // System.out.println("Parsing right " + "(" +  (userRight?"user":"admin") + ") " + name);
        Right right;

        AdminRight.RightType rightType = null;
        String targetTypeStr = eRight.attributeValue(A_TARGET_TYPE, null);

        if (userRight) {
            TargetType targetType;
            if (targetTypeStr != null) {
                targetType = TargetType.fromCode(targetTypeStr);
            } else {
                targetType = TargetType.account;  // default target type for user right is account
            }

            right = new UserRight(name);
            right.setTargetType(targetType);

            String fallback = eRight.attributeValue(A_FALLBACK, null);
            if (fallback != null) {
                CheckRightFallback fb = loadFallback(fallback, right);
                right.setFallback(fb);
            }

        } else {
            String rt = eRight.attributeValue(A_TYPE);
            if (rt == null) {
                throw ServiceException.PARSE_ERROR("missing attribute [" + A_TYPE + "]", null);
            }
            rightType = AdminRight.RightType.fromString(rt);

            right = AdminRight.newAdminSystemRight(name, rightType);
            if (targetTypeStr != null) {
                String taregtTypes[] = targetTypeStr.split(TARGET_TYPE_DELIMITER);
                for (String tt : taregtTypes) {
                    TargetType targetType = TargetType.fromCode(tt);
                    right.setTargetType(targetType);
                }
            }
        }

        String grantTargetTypeStr = eRight.attributeValue(A_GRANT_TARGET_TYPE, null);
        if (grantTargetTypeStr != null) {
            TargetType grantTargetType = TargetType.fromCode(grantTargetTypeStr);
            right.setGrantTargetType(grantTargetType);
        }

        boolean cache = getBooleanAttr(eRight, A_CACHE, false);
        if (cache) {
            right.setCacheable();
        }

        for (Iterator elemIter = eRight.elementIterator(); elemIter.hasNext();) {
            Element elem = (Element)elemIter.next();
            if (elem.getName().equals(E_DESC)) {
                parseDesc(elem, right);
            } else if (elem.getName().equals(E_HELP)) {
                parseHelp(elem, right);
            } else if (elem.getName().equals(E_UI)) {
                parseUI(elem, right);
            } else if (elem.getName().equals(E_DEFAULT)) {
                parseDefault(elem, right);
            } else if (elem.getName().equals(E_ATTRS)) {
                parseAttrs(elem, right);
            } else if (elem.getName().equals(E_RIGHTS)) {
                parseRights(elem, right);
            } else {
                throw ServiceException.PARSE_ERROR("invalid element: " + elem.getName(), null);
            }
        }

        // verify that all required fields are set and populate internal data
        right.completeRight();

        return right;
    }

    private static CheckRightFallback loadFallback(String clazz, Right right) {
        CheckRightFallback cb = null;
        if (clazz == null)
            return null;
        if (clazz.indexOf('.') == -1)
            clazz = "com.zimbra.cs.account.accesscontrol.fallback." + clazz;
        try {
            cb = (CheckRightFallback) Class.forName(clazz).newInstance();
            if (cb != null)
                cb.setRight(right);
        } catch (Exception e) {
            ZimbraLog.acl.warn("loadFallback " + clazz + " for right " + right.getName() +  " caught exception", e);
        }
        return cb;
    }

    private boolean loadSystemRights(File file, List<File> processedFiles, File[] allFiles)
    throws ServiceException, FileNotFoundException {
        Document doc;
        try (FileInputStream fis = new FileInputStream(file)) {
            doc = W3cDomUtil.parseXMLToDom4jDocUsingSecureProcessing(fis);
        } catch (FileNotFoundException e) {
            throw e;
        } catch (IOException e) {
            ZimbraLog.acl.debug("Problem parsing file %s", file, e);
            throw ServiceException.PARSE_ERROR("Problem parsing file for system rights", null);
        }
        Element root = doc.getRootElement();
        if (!root.getName().equals(E_RIGHTS)) {
            throw ServiceException.PARSE_ERROR("root tag is not " + E_RIGHTS, null);
        }

        // preset rights can only be defined in our core right definition file
        boolean allowPresetRight = CoreRightDefFiles.isCoreRightFile(file);

        boolean seenRight = false;
        for (Iterator iter = root.elementIterator(); iter.hasNext();) {
            Element elem = (Element) iter.next();

            // see if all include files are processed already
            if (elem.getName().equals(E_INCLUDE)) {
                // all <include>'s have to appear before <right>'s
                if (seenRight) {
                    throw ServiceException.PARSE_ERROR(
                            E_INCLUDE + " cannot appear after any right definition: " +
                            elem.getName(), null);
                }

                String includeFile = elem.attributeValue(A_FILE);

                // make sure the include file exists
                boolean foundFile = false;
                for (File f : allFiles) {
                    if (f.getName().equals(includeFile)) {
                        foundFile = true;
                        break;
                    }
                }
                if (!foundFile) {
                    throw ServiceException.PARSE_ERROR("cannot find include file " + includeFile, null);
                }

                boolean processed = false;
                for (File f : processedFiles) {
                    if (f.getName().equals(includeFile)) {
                        processed = true;
                        break;
                    }
                }
                if (!processed) {
                    return false;
                } else {
                    continue;
                }
            } else if (elem.getName().equals(E_RIGHT)) {
                if (!seenRight) {
                    seenRight = true;
                    ZimbraLog.acl.debug("Loading %s", file.getAbsolutePath());
                }
                loadRight(elem, file, allowPresetRight);
            } else if (elem.getName().equals(E_HELP)) {
                loadHelp(elem, file);
            } else if (elem.getName().equals(E_UI)) {
                loadUI(elem, file);
            } else {
                throw ServiceException.PARSE_ERROR("unknown element: " + elem.getName(), null);
            }
        }

        return true;
    }

    private void loadRight(Element eRight, File file, boolean allowPresetRight)
    throws ServiceException {
        String name = eRight.attributeValue(A_NAME);
        if (name == null) {
            throw ServiceException.PARSE_ERROR("no name specified", null);
        }

        checkName(name);

        try {
            Right right = parseRight(eRight);
            if (!allowPresetRight && RightType.preset == right.getRightType()) {
                throw ServiceException.PARSE_ERROR(
                        "Encountered preset right " + name + " in " + file.getName() +
                        ", preset right can only be defined in one of the core right definition files: " +
                        CoreRightDefFiles.listCoreDefFiles(),
                        null);
            }

            if (right instanceof UserRight) {
                sUserRights.put(name, (UserRight)right);
            } else {
                sAdminRights.put(name, (AdminRight)right);
            }
        } catch (ServiceException e) {
            throw ServiceException.PARSE_ERROR("unable to parse right: [" + name + "]", e);
        }
    }

    private void loadHelp(Element eHelp, File file) throws ServiceException {
        String name = eHelp.attributeValue(A_NAME);
        if (name == null) {
            throw ServiceException.PARSE_ERROR("no name specified", null);
        }

        checkName(name);

        Help help = new Help(name);

        for (Iterator elemIter = eHelp.elementIterator(); elemIter.hasNext();) {
            Element elem = (Element)elemIter.next();
            if (elem.getName().equals(E_DESC)) {
                if (help.getDesc() != null) {
                    throw ServiceException.PARSE_ERROR("desc for help " + name + " already set", null);
                }
                help.setDesc(elem.getText());
            } else if (elem.getName().equals(E_ITEM)) {
                help.addItem(elem.getText());
            } else {
                throw ServiceException.PARSE_ERROR("invalid element: " + elem.getName(), null);
            }
        }

        // make all required bits are set in the help
        help.validate();

        sHelp.put(name, help);
    }

    private void loadUI(Element eUI, File file) throws ServiceException {
        String name = eUI.attributeValue(A_NAME);
        if (name == null) {
            throw ServiceException.PARSE_ERROR("no name specified", null);
        }

        checkName(name);

        UI ui = new UI(name);

        String desc = eUI.getText();
        ui.setDesc(desc);

        // make all required bits are set in the ui
        ui.validate();

        sUI.put(name, ui);
    }

    private void checkName(String name) throws ServiceException {

        // help name and ui name cannot be the same as any of the right names
        // because name is the key to the generated ZsMsgRights.properties file.
        //
        // Though currently we don't generate helps/UIs in ZsMsgRights.properties, because
        // all the formatting will be lost and it won't look good in admin console anyway.
        // Enforce uniqueness to keep the option open.
        //
        if (sUserRights.containsKey(name) || sAdminRights.containsKey(name) ||
                sHelp.containsKey(name) || sUI.containsKey(name)) {
            throw ServiceException.PARSE_ERROR("right or help or ui " + name + " is already defined", null);
        }
    }

    //
    // getters
    //

    public UserRight getUserRight(String right) throws ServiceException {
        UserRight r = sUserRights.get(right);
        if (r == null) {
            throw ServiceException.FAILURE("invalid right " + right, null);
        }
        return r;
    }

    public AdminRight getAdminRight(String right) throws ServiceException {
        AdminRight r = sAdminRights.get(right);
        if (r == null) {
            throw ServiceException.FAILURE("invalid right " + right, null);
        }
        return r;
    }

    public Right getRight(String right) throws ServiceException {
        if (InlineAttrRight.looksLikeOne(right)) {
            return InlineAttrRight.newInlineAttrRight(right);
        } else {
            return getRightInternal(right, true);
        }
    }

    private Right getRightInternal(String right, boolean mustFind) throws ServiceException {
        Right r = sUserRights.get(right);
        if (r == null) {
            r = sAdminRights.get(right);
        }

        if (mustFind && r == null) {
            throw AccountServiceException.NO_SUCH_RIGHT("invalid right " + right);
        }

        return r;
    }

    public Map<String, UserRight> getAllUserRights() {
        return sUserRights;
    }

    public Map<String, AdminRight> getAllAdminRights() {
        return sAdminRights;
    }

    private String dump(StringBuilder sb) {
        if (sb == null) {
            sb = new StringBuilder();
        }

        sb.append("============\n");
        sb.append("user rights:\n");
        sb.append("============\n");
        for (Map.Entry<String, UserRight> ur : getAllUserRights().entrySet()) {
            sb.append("\n------------------------------\n");
            ur.getValue().dump(sb);
        }

        sb.append("\n");
        sb.append("\n");
        sb.append("=============\n");
        sb.append("admin rights:\n");
        sb.append("=============\n");
        for (Map.Entry<String, AdminRight> ar : getAllAdminRights().entrySet()) {
            sb.append("\n------------------------------\n");
            ar.getValue().dump(sb);
        }

        return sb.toString();
    }

    void genRightConst(Right r, StringBuilder sb) {
        sb.append("\n    /**\n");
        if (r.getDesc() != null) {
            sb.append(FileGenUtil.wrapComments(StringUtil.escapeHtml(r.getDesc()), 70, "     * "));
            sb.append("\n");
        }
        sb.append("     */\n");
        sb.append("    public static final String RT_" + r.getName() + " = \"" + r.getName() + "\";" + "\n");
    }

    private String genRightConsts() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n\n");
        sb.append("    /*\n");
        sb.append("    ============\n");
        sb.append("    user rights:\n");
        sb.append("    ============\n");
        sb.append("    */\n\n");
        for (Map.Entry<String, UserRight> ur : getAllUserRights().entrySet()) {
            genRightConst(ur.getValue(), sb);
        }

        sb.append("\n\n");
        sb.append("    /*\n");
        sb.append("    =============\n");
        sb.append("    admin rights:\n");
        sb.append("    =============\n");
        sb.append("    */\n\n");
        for (Map.Entry<String, AdminRight> ar : getAllAdminRights().entrySet()) {
            genRightConst(ar.getValue(), sb);
        }

        return sb.toString();
    }

    private String genAdminRights() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n\n");
        for (AdminRight r : getAllAdminRights().values()) {
            sb.append("    public static AdminRight R_" + r.getName() + ";" + "\n");
        }

        sb.append("\n\n");
        sb.append("    public static void init(RightManager rm) throws ServiceException {\n");
        for (AdminRight r : getAllAdminRights().values()) {
            String s = String.format("        R_%-36s = rm.getAdminRight(Right.RT_%s);\n",
                    r.getName(), r.getName());
            sb.append(s);
        }
        sb.append("    }\n");
        return sb.toString();
    }

    private String genUserRights() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n\n");
        for (UserRight r : getAllUserRights().values()) {
            sb.append("    public static UserRight R_" + r.getName() + ";" + "\n");
        }

        sb.append("\n\n");
        sb.append("    public static void init(RightManager rm) throws ServiceException {\n");
        for (UserRight r : getAllUserRights().values()) {
            String s = String.format("        R_%-36s = rm.getUserRight(Right.RT_%s);\n",
                    r.getName(), r.getName());
            sb.append(s);
        }
        sb.append("    }\n");
        return sb.toString();
    }

    private String genMessageProperties() throws ServiceException {
        StringBuilder result = new StringBuilder();

        result.append(FileGenUtil.genDoNotModifyDisclaimer("#", RightManager.class.getSimpleName()));
        result.append("# Zimbra rights");
        result.append("\n\n");

        genMessageProperties(result, getAllUserRights());
        result.append("\n\n");
        genMessageProperties(result, getAllAdminRights());

        return result.toString();
    }

    private void genMessageProperties(StringBuilder result, Map<String, ? extends  Right> rights)
    throws ServiceException {
        List<String> sortedRights = new ArrayList<String>(rights.keySet());
        Collections.sort(sortedRights);

        for (String right : sortedRights) {
            Right r = getRight(right);
            // strip off the 2 spaces on the first line
            String text = FileGenUtil.wrapComments(r.getDesc(), 80, "  ", " \\").substring(2);
            result.append(r.getName() + " = " + text + "\n");
        }
    }

    /**
     * generates two files in the output directory
     *
     * {right}-expanded.xml: the root combo right fully expanded
     * {right}-ui.xml: all UI covered by the root combo right
     *
     * @param outputDir
     * @throws ServiceException
     * @throws IOException
     */
    private void genAdminDocs(String outputDir) throws ServiceException, IOException {
        if (!outputDir.endsWith("/")) {
            outputDir = outputDir + "/";
        }

        List<AdminRight> rootRights = ImmutableList.of(
                Admin.R_adminConsoleRights);

        for (AdminRight right : rootRights) {
            Multimap<UI, Right> uiMap = TreeMultimap.create();

            /*
             * output the rights XML.  This XML has the root combo right expanded
             * down to each atom(preset or attrs) right
             */
            Document document = DocumentHelper.createDocument();

            Element rightsRoot = document.addElement(E_ROOT);
            genAdminDocByRight(rightsRoot, right, uiMap);
            writeXML(outputDir + right.getName() + "-expanded.xml", document);

            /*
             * output the UI XML.  This XML contains one entry for each UI, sorted by
             * the description of the UI.
             */
            document = DocumentHelper.createDocument();
            Element uiRoot = document.addElement(E_ROOT);
            genAdminDocByUI(uiRoot, uiMap);
            writeXML(outputDir + right.getName() + "-ui.xml", document);
        }

    }

    private void writeXML(String fileName, Document document) throws IOException {
        // Pretty print the document to output file
        OutputFormat format = OutputFormat.createPrettyPrint();

        BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
        XMLWriter xmlWriter = new XMLWriter(writer, format);
        xmlWriter.write(document);
        writer.close();
    }

    private void genAdminDocByRight(Element parent, Right right, Multimap<UI, Right> uiMap)
    throws ServiceException {
        Element eRight = parent.addElement(E_RIGHT).
                addAttribute(A_NAME, right.getName()).
                addAttribute(A_TYPE, right.getRightType().name());
        eRight.addElement(E_DESC).setText(right.getDesc());

        UI ui = right.getUI();
        if (ui != null) {
            eRight.addElement(E_UI).setText(ui.getDesc());
            uiMap.put(ui, right);
        }

        if (right.isComboRight()) {
            ComboRight comboRight = (ComboRight)right;
            for (Right childRight : comboRight.getRights()) {
                genAdminDocByRight(eRight, childRight, uiMap);
            }
        } else if (right.isPresetRight()) {
            eRight.addAttribute(A_TARGET_TYPE, right.getTargetTypeStr());
        } else if (right.isAttrRight()) {
            eRight.addAttribute(A_TARGET_TYPE, right.getTargetTypeStr());

            AttrRight attrRight = (AttrRight)right;
            if (!attrRight.allAttrs()) {
                Element eAttrs = eRight.addElement(E_ATTRS);
                for (String attr : attrRight.getAttrs()) {
                    eAttrs.addElement(E_A).addAttribute(A_N, attr);
                }
            }
        }
    }

    private void genAdminDocByUI(Element parent, Multimap<UI, Right> uiMap) {
        for (Map.Entry<UI, Right> entry : uiMap.entries()) {
            UI ui = entry.getKey();
            Right right = entry.getValue();

            Element eUI = parent.addElement(E_UI);
            eUI.addAttribute(E_DESC, ui.getDesc());
            eUI.addAttribute(E_RIGHT, right.getName());
        }
    }

    private static class CL {
        private static Options sOptions = new Options();

        static {
            sOptions.addOption("h", "help", false,
                    "display this usage info");
            sOptions.addOption("a", "action", true,
                    "action, one of genRightConsts, genAdminRights, genUserRights, genMessagePrperties");
            sOptions.addOption("i", "input", true,
                    "rights definition xml input directory");
            sOptions.addOption("o", "output", true,
                    "output directory");
            sOptions.addOption("r", "regenerateFile", true,
                    "file to regenerate");
            sOptions.addOption("t", "templateFile", true,
                    "template file");
        }

        private enum Action {
            genRightConsts(true, true, false),
            genAdminRights(true, true, false),
            genUserRights(true, true, false),
            genDomainAdminSetAttrsRights(true, false, false),
            genMessageProperties(true, true, false),
            genAdminDocs(false, true, true),
            validate(false, true, false);

            boolean regenFileRequred;
            boolean inputDirRequired;
            boolean outputDirRequired;

            private Action(boolean regenFileRequred, boolean inputDirRequired, boolean outputDirRequired) {
                this.regenFileRequred = regenFileRequred;
                this.inputDirRequired = inputDirRequired;
                this.outputDirRequired = outputDirRequired;
            }

            private boolean regenFileRequred() {
                return regenFileRequred;
            }

            private boolean inputDirRequired() {
                return inputDirRequired;
            }

            private boolean outputDirRequired() {
                return outputDirRequired;
            }

            private static Action fromString(String str) throws ServiceException {
                try {
                    return Action.valueOf(str);
                } catch (IllegalArgumentException e) {
                    throw ServiceException.INVALID_REQUEST("unknown RightManager CLI action: " + str, e);
                }
            }
        }

        private static void check() throws ServiceException  {
            ZimbraLog.toolSetupLog4j("DEBUG", "/Users/pshao/sandbox/conf/log4j.properties.phoebe");

            RightManager rm = new RightManager("/Users/pshao/p4/main/ZimbraServer/conf/rights", false);
            System.out.println(rm.dump(null));
        }

        private static void genDomainAdminSetAttrsRights(String outFile, String templateFile)
        throws Exception {
            Set<String> acctAttrs = getDomainAdminModifiableAttrs(AttributeClass.account);
            Set<String> crAttrs = getDomainAdminModifiableAttrs(AttributeClass.calendarResource);
            Set<String> dlAttrs = getDomainAdminModifiableAttrs(AttributeClass.distributionList);
            Set<String> domainAttrs = getDomainAdminModifiableAttrs(AttributeClass.domain);

            Set<String> acctAndCrAttrs = SetUtil.intersect(acctAttrs, crAttrs);
            Set<String> acctOnlyAttrs = SetUtil.subtract(acctAttrs, crAttrs);
            Set<String> crOnlyAttrs = SetUtil.subtract(crAttrs, acctAttrs);

            // sanity check, since we are not generating it, make sure it is indeed empty
            if (acctOnlyAttrs.size() != 0)
                throw ServiceException.FAILURE("account only attrs is not empty???", null);

            String acctAndCrAttrsFiller = genAttrs(acctAndCrAttrs);
            String crOnlyAttrsFiller = genAttrs(crOnlyAttrs);
            String dlAttrsFiller = genAttrs(dlAttrs);
            String domainAttrsFiller = genAttrs(domainAttrs);

            Map<String,String> templateFillers = new HashMap<String,String>();
            templateFillers.put("ACCOUNT_AND_CALENDAR_RESOURCE_ATTRS", acctAndCrAttrsFiller);
            templateFillers.put("CALENDAR_RESOURCE_ATTRS", crOnlyAttrsFiller);
            templateFillers.put("DISTRIBUTION_LIST_ATTRS", dlAttrsFiller);
            templateFillers.put("DOMAIN_ATTRS", domainAttrsFiller);

            FileGenUtil.replaceFile(outFile, templateFile, templateFillers);
        }

        private static Set<String> getDomainAdminModifiableAttrs(AttributeClass klass)
        throws ServiceException {
            AttributeManager am = AttributeManager.getInstance();
            Set<String> allAttrs = am.getAllAttrsInClass(klass);

            Set<String> domainAdminModifiableAttrs = new HashSet<String>();
            for (String attr : allAttrs) {
                if (am.isDomainAdminModifiable(attr, klass)) {
                    domainAdminModifiableAttrs.add(attr);
                }
            }
            return domainAdminModifiableAttrs;
        }

        private static String genAttrs(Set<String> attrs) {
            // sort it
            Set<String> sortedAttrs = new TreeSet<String>(attrs);

            StringBuilder sb = new StringBuilder();
            for (String attr : sortedAttrs) {
                sb.append("    <a n=\"" + attr + "\"/>\n");
            }
            return sb.toString();
        }

        private static void usage(String errmsg) {
            if (errmsg != null) {
                System.out.println(errmsg);
            }
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("AttributeManager [options] where [options] are one of:", sOptions);
            System.exit((errmsg == null) ? 0 : 1);
        }

        private static CommandLine parseArgs(String args[]) {
            StringBuffer gotCL = new StringBuffer("cmdline: ");
            for (int i = 0; i < args.length; i++) {
                gotCL.append("'").append(args[i]).append("' ");
            }
            System.out.println(gotCL);

            CommandLineParser parser = new GnuParser();
            CommandLine cl = null;
            try {
                cl = parser.parse(sOptions, args);
            } catch (ParseException pe) {
                usage(pe.getMessage());
            }
            if (cl.hasOption('h')) {
                usage(null);
            }
            return cl;
        }

        private static void main(String[] args) throws Exception {
            CliUtil.toolSetup();
            CommandLine cl = parseArgs(args);

            if (!cl.hasOption('a')) {
                usage("no action specified");
            }
            Action action = Action.fromString(cl.getOptionValue('a'));

            if (action.regenFileRequred()) {
                if (!cl.hasOption('r')) {
                    usage("no regenerate file specified");
                }
            }

            String regenFile = cl.getOptionValue('r');

            String inputDir = null;
            RightManager rm = null;
            if (action.inputDirRequired()) {
                if (!cl.hasOption('i')) {
                    usage("no input dir specified");
                }
                inputDir = cl.getOptionValue('i');
                rm = RightManager.getInstance(inputDir, false);
            }

            String outputDir = null;
            if (action.outputDirRequired()) {
                if (!cl.hasOption('o')) {
                    usage("no output dir specified");
                }
                outputDir = cl.getOptionValue('o');
            }

            switch (action) {
                case genRightConsts:
                    FileGenUtil.replaceJavaFile(regenFile, rm.genRightConsts());
                    break;
                case genAdminRights:
                    FileGenUtil.replaceJavaFile(regenFile, rm.genAdminRights());
                    break;
                case genUserRights:
                    FileGenUtil.replaceJavaFile(regenFile, rm.genUserRights());
                    break;
                case genDomainAdminSetAttrsRights:
                    String templateFile = cl.getOptionValue('t');
                    genDomainAdminSetAttrsRights(regenFile, templateFile);
                    break;
                case genMessageProperties:
                    FileGenUtil.replaceFile(regenFile, rm.genMessageProperties());
                    break;
                case genAdminDocs:
                    // zmjava com.zimbra.cs.account.accesscontrol.RightManager -a genAdminDocs -i /Users/pshao/p4/main/ZimbraServer/conf/rights -o /Users/pshao/temp
                    rm.genAdminDocs(outputDir);
                    break;
                case validate:
                    // do nothing, all we need is that new RightManager(inputDir) works,
                    // which is done above.
                    break;
                default:
                    usage("invalid action");
            }
        }
    }
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        CL.main(args);

        // CL.check();
    }

}
