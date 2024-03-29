// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.accesscontrol;

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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.dom4j.Element;


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
    private static final String E_UI           = "ui";

    private static final String A_CACHE        = "cache";
    private static final String A_FALLBACK     = "fallback";
    private static final String A_FILE         = "file";
    private static final String A_GRANT_TARGET_TYPE  = "grantTargetType";
    private static final String A_N            = "n";
    private static final String A_NAME         = "name";
    private static final String A_TARGET_TYPE  = "targetType";
    private static final String A_TYPE         = "type";
    private static final String A_USER_RIGHT   = "userRight";
    private static final List<String> RIGHTS_FILES = List.of(
        "adminconsole-ui.xml",
        "rights.xml",
        "rights-roles.xml",
        "user-rights.xml",
        "rights-adminconsole.xml",
        "rights-adminconsole-domainadmin.xml",
        "rights-domainadmin.xml"
    );

    private static final String RIGHT_RESOURCE_PATH = "/conf/rights/";

    private static final String TARGET_TYPE_DELIMITER   = ",";

    private static RightManager mInstance;
    private final AttributeManager attributeManager;

    // keep the map sorted so "zmmailbox lp" can display in alphabetical order
    private final Map<String, UserRight> sUserRights = new TreeMap<>();
    private final Map<String, AdminRight> sAdminRights = new TreeMap<>();
    private final Map<String, Help> sHelp = new TreeMap<>();
    private final Map<String, UI> sUI = new TreeMap<>();

  private static class CoreRightDefFiles {
        private static final HashSet<String> sCoreRightDefFiles = new HashSet<>();

        static void init() {
            sCoreRightDefFiles.add("rights.xml");
            sCoreRightDefFiles.add("user-rights.xml");
        }

        private static boolean isCoreRightFile(String filename) {
            return sCoreRightDefFiles.contains(filename);
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

    public static synchronized void destroy() {
        mInstance = null;
    }

    public static synchronized RightManager getInstance() throws ServiceException {
        return getInstance(LC.zimbra_rights_directory.value());
    }

    private static synchronized RightManager getInstance(String rightsDirectoryPath)
    throws ServiceException {
        if (mInstance != null) {
            return mInstance;
        }
        final AttributeManager attributeManager = AttributeManager.getInstance();

        if (Objects.isNull(rightsDirectoryPath) || Objects.equals("", rightsDirectoryPath)) {
            mInstance = RightManager.fromResources(attributeManager);
        } else {
            mInstance = RightManager.fromFileSystem(rightsDirectoryPath, attributeManager);
        }

        try {
            Right.init(mInstance, attributeManager);
        } catch (ServiceException e) {
            ZimbraLog.acl.error("failed to initialize known right from: " + rightsDirectoryPath, e);
            throw e;
        }
        return mInstance;
    }

    public static RightManager fromResources(AttributeManager attributeManager) throws ServiceException {
      final RightStream rightStream = new ResourceRightStream(RIGHT_RESOURCE_PATH);
      return new RightManager(rightStream, attributeManager);
    }

    private RightManager(RightStream rightStream, AttributeManager attributeManager) throws ServiceException {
        this.attributeManager = attributeManager;
        CoreRightDefFiles.init();

        final List<String> yetToProcessFileNames = new ArrayList<>(RIGHTS_FILES);
        final List<String> processedFileNames = new ArrayList<>();

        while (!yetToProcessFileNames.isEmpty()) {
            String currentFilename = yetToProcessFileNames.get(0);
            try (InputStream inputStream = rightStream.open(currentFilename)) {
                ZimbraLog.acl.debug("Parsing file %s", currentFilename);

                boolean done = loadSystemRights(currentFilename, inputStream, processedFileNames);
                if (done) {
                    processedFileNames.add(currentFilename);
                    yetToProcessFileNames.remove(currentFilename);
                } else {
                    // move this file to the end
                    yetToProcessFileNames.remove(currentFilename);
                    yetToProcessFileNames.add(currentFilename);
                }
            } catch (IOException | XmlParseException e) {
                ZimbraLog.acl.debug("Problem parsing file %s", currentFilename, e);
                throw ServiceException.PARSE_ERROR("Problem parsing file for system rights", null);
            }
        }
    }

    public static RightManager fromFileSystem(String baseRightsDirectoryPath, AttributeManager attributeManager) throws ServiceException {
        File baseRightsDirectory = new File(baseRightsDirectoryPath);
        if (!baseRightsDirectory.exists()) {
            throw ServiceException.FAILURE("rights directory does not exist: " + baseRightsDirectoryPath, null);
        }
        if (!baseRightsDirectory.isDirectory()) {
            throw ServiceException.FAILURE("rights directory is not a directory: " + baseRightsDirectoryPath, null);
        }
        ZimbraLog.acl.debug("Loading rights from %s", baseRightsDirectory.getAbsolutePath());
        RightStream rightStream = new FileRightStream(baseRightsDirectoryPath);
        return new RightManager(rightStream, attributeManager);

    }

    private boolean getBoolean(String value) throws ServiceException {
        if ("1".equals(value))
            return true;
        else if ("0".equals(value))
            return false;
        else
            throw ServiceException.PARSE_ERROR("invalid value:" + value, null);
    }

    private boolean getBooleanAttr(Element elem, String attr)
    throws ServiceException {
        String value = elem.attributeValue(attr);
        if (value == null) {
            return false;
        }
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
        for (Iterator<Element> elemIter = eAttrs.elementIterator(); elemIter.hasNext();) {
            Element elem = elemIter.next();
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

        for (Iterator<Element> elemIter = eAttrs.elementIterator(); elemIter.hasNext();) {
            Element elem = elemIter.next();
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

        boolean userRight = getBooleanAttr(eRight, A_USER_RIGHT);

        // System.out.println("Parsing right " + "(" +  (userRight?"user":"admin") + ") " + name);
        Right right;

        AdminRight.RightType rightType;
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
            rightType = Right.RightType.fromString(rt);

            right = AdminRight.newAdminSystemRight(name, rightType, attributeManager);
            if (targetTypeStr != null) {
                String[] targetTypes = targetTypeStr.split(TARGET_TYPE_DELIMITER);
                for (String tt : targetTypes) {
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

        boolean cache = getBooleanAttr(eRight, A_CACHE);
        if (cache) {
            right.setCacheable();
        }

        for (Iterator<Element> elemIter = eRight.elementIterator(); elemIter.hasNext();) {
            Element elem = elemIter.next();
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

    private boolean loadSystemRights(String fileName, InputStream fileContent,
        List<String> processedFiles)
    throws ServiceException {
        Document doc = W3cDomUtil.parseXMLToDom4jDocUsingSecureProcessing(fileContent);
        Element root = doc.getRootElement();
        if (!root.getName().equals(E_RIGHTS)) {
            throw ServiceException.PARSE_ERROR("root tag is not " + E_RIGHTS, null);
        }

        // preset rights can only be defined in our core right definition file
        boolean allowPresetRight = CoreRightDefFiles.isCoreRightFile(fileName);

        boolean seenRight = false;
        for (Iterator<Element> iter = root.elementIterator(); iter.hasNext();) {
            Element elem = iter.next();

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
                for (String rightFileName : RightManager.RIGHTS_FILES) {
                    if (rightFileName.equals(includeFile)) {
                        foundFile = true;
                        break;
                    }
                }
                if (!foundFile) {
                    throw ServiceException.PARSE_ERROR("cannot find include file " + includeFile, null);
                }

                boolean processed = false;
                for (String alreadyProcessed : processedFiles) {
                    if (alreadyProcessed.equals(includeFile)) {
                        processed = true;
                        break;
                    }
                }
                if (!processed) {
                    return false;
                }
            } else if (elem.getName().equals(E_RIGHT)) {
                if (!seenRight) {
                    seenRight = true;
                    ZimbraLog.acl.debug("Loading %s", fileName);
                }
                loadRight(elem, fileName, allowPresetRight);
            } else if (elem.getName().equals(E_HELP)) {
                loadHelp(elem);
            } else if (elem.getName().equals(E_UI)) {
                loadUI(elem);
            } else {
                throw ServiceException.PARSE_ERROR("unknown element: " + elem.getName(), null);
            }
        }

        return true;
    }

    private void loadRight(Element eRight, String fileName, boolean allowPresetRight)
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
                        "Encountered preset right " + name + " in " + fileName +
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

    private void loadHelp(Element eHelp) throws ServiceException {
        String name = eHelp.attributeValue(A_NAME);
        if (name == null) {
            throw ServiceException.PARSE_ERROR("no name specified", null);
        }

        checkName(name);

        Help help = new Help(name);

        for (Iterator<Element> elemIter = eHelp.elementIterator(); elemIter.hasNext();) {
            Element elem = elemIter.next();
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

    private void loadUI(Element eUI) throws ServiceException {
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
        // all the formatting will be lost, and it won't look good in admin console anyway.
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
            return InlineAttrRight.newInlineAttrRight(right, attributeManager);
        } else {
            return getRightInternal(right);
        }
    }

    private Right getRightInternal(String right) throws ServiceException {
        Right r = sUserRights.get(right);
        if (r == null) {
            r = sAdminRights.get(right);
        }

        if (r == null) {
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

    void genRightConst(Right r, StringBuilder sb) {
        sb.append("\n    /**\n");
        if (r.getDesc() != null) {
            sb.append(FileGenUtil.wrapComments(StringUtil.escapeHtml(r.getDesc()), 70, "     * "));
            sb.append("\n");
        }
        sb.append("     */\n");
        sb.append("    public static final String RT_").append(r.getName()).append(" = \"")
            .append(r.getName()).append("\";").append("\n");
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
            sb.append("    public static AdminRight R_").append(r.getName()).append(";")
                .append("\n");
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
            sb.append("    public static UserRight R_").append(r.getName()).append(";")
                .append("\n");
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
        List<String> sortedRights = new ArrayList<>(rights.keySet());
        Collections.sort(sortedRights);

        for (String right : sortedRights) {
            Right r = getRight(right);
            // strip off the 2 spaces on the first line
            String text = FileGenUtil.wrapComments(r.getDesc(), 80, "  ", " \\").substring(2);
            result.append(r.getName()).append(" = ").append(text).append("\n");
        }
    }

    private static class CL {
        private static final Options sOptions = new Options();

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
            genRightConsts(true, true),
            genAdminRights(true, true),
            genUserRights(true, true),
            genDomainAdminSetAttrsRights(true, false),
            genMessageProperties(true, true),
            validate(false, true);

            final boolean regenFileRequired;
            final boolean inputDirRequired;

            Action(boolean regenFileRequired, boolean inputDirRequired) {
                this.regenFileRequired = regenFileRequired;
                this.inputDirRequired = inputDirRequired;
            }

            private boolean regenFileRequired() {
                return regenFileRequired;
            }

            private boolean inputDirRequired() {
                return inputDirRequired;
            }

            private static Action fromString(String str) throws ServiceException {
                try {
                    return Action.valueOf(str);
                } catch (IllegalArgumentException e) {
                    throw ServiceException.INVALID_REQUEST("unknown RightManager CLI action: " + str, e);
                }
            }
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
            if (!acctOnlyAttrs.isEmpty())
                throw ServiceException.FAILURE("account only attrs is not empty???", null);

            String acctAndCrAttrsFiller = genAttrs(acctAndCrAttrs);
            String crOnlyAttrsFiller = genAttrs(crOnlyAttrs);
            String dlAttrsFiller = genAttrs(dlAttrs);
            String domainAttrsFiller = genAttrs(domainAttrs);

            Map<String,String> templateFillers = new HashMap<>();
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

            Set<String> domainAdminModifiableAttrs = new HashSet<>();
            for (String attr : allAttrs) {
                if (am.isDomainAdminModifiable(attr, klass)) {
                    domainAdminModifiableAttrs.add(attr);
                }
            }
            return domainAdminModifiableAttrs;
        }

        private static String genAttrs(Set<String> attrs) {
            // sort it
            Set<String> sortedAttrs = new TreeSet<>(attrs);

            StringBuilder sb = new StringBuilder();
            for (String attr : sortedAttrs) {
                sb.append("    <a n=\"").append(attr).append("\"/>\n");
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

        private static CommandLine parseArgs(String[] args) {
            StringBuffer gotCL = new StringBuffer("cmdline: ");
          for (final String arg : args) {
            gotCL.append("'").append(arg).append("' ");
          }
            System.out.println(gotCL);

            CommandLineParser parser = new GnuParser();
            CommandLine commandLine = null;
            try {
                commandLine = parser.parse(sOptions, args);
                if (commandLine.hasOption('h')) {
                    usage(null);
                }
            } catch (ParseException pe) {
                usage(pe.getMessage());
            }
            return commandLine;
        }

        private static void generateRights(String[] args) throws Exception {
            CliUtil.toolSetup();
            CommandLine cl = parseArgs(args);

            if (!cl.hasOption('a')) {
                usage("no action specified");
            }
            Action action = Action.fromString(cl.getOptionValue('a'));

            if (action.regenFileRequired()) {
                if (!cl.hasOption('r')) {
                    usage("no regenerate file specified");
                }
            }

            String regenFile = cl.getOptionValue('r');

            String inputDir = null;
            if (action.inputDirRequired()) {
                if (!cl.hasOption('i')) {
                    usage("no input dir specified");
                }
                inputDir = cl.getOptionValue('i');
            }
            RightManager rightManager = RightManager.getInstance(inputDir);

            switch (action) {
                case genRightConsts:
                    FileGenUtil.replaceJavaFile(regenFile, rightManager.genRightConsts());
                    break;
                case genAdminRights:
                    FileGenUtil.replaceJavaFile(regenFile, rightManager.genAdminRights());
                    break;
                case genUserRights:
                    FileGenUtil.replaceJavaFile(regenFile, rightManager.genUserRights());
                    break;
                case genDomainAdminSetAttrsRights:
                    String templateFile = cl.getOptionValue('t');
                    genDomainAdminSetAttrsRights(regenFile, templateFile);
                    break;
                case genMessageProperties:
                    FileGenUtil.replaceFile(regenFile, rightManager.genMessageProperties());
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

    public static void main(String[] args) throws Exception {
        CL.generateRights(args);
    }
}
