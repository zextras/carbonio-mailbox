// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.XMLConstants;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zimbra.common.soap.AccountConstants;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.AdminExtConstants;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.ReplicationConstants;
import com.zimbra.common.soap.SyncConstants;
import com.zimbra.common.soap.VoiceConstants;
import com.zimbra.common.soap.ZimbraNamespace;
import com.zimbra.soap.JaxbUtil;

/**
 * This class represents a utility to generate the top level WSDL files for
 * Zimbra SOAP interfaces
 *
 * @author gren
 *
 */
public class WsdlGenerator {

    private static final String ARG_OUTPUT_DIR = "-output.dir";
    private static String outputDir = null;

    private static final String svcPrefix = "svc"; // Namespace prefix used for references to targetNamespace
    private static final Namespace nsSoap = new Namespace("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
    private static final Namespace nsXsd = new Namespace( "xsd", XMLConstants.W3C_XML_SCHEMA_NS_URI);
    private static final Namespace nsWsdl = new Namespace("wsdl", "http://schemas.xmlsoap.org/wsdl/");
    private static final Namespace nsZimbra = new Namespace("zimbra", ZimbraNamespace.ZIMBRA_STR);
    private static final String targetNsBase = "https://www.zextras.com/wsdl/";
    private static final QName xsdSchema = QName.get("schema", nsXsd);
    private static final QName xsdImport = QName.get("import", nsXsd);
    private static final QName soapBinding = QName.get("binding", nsSoap);
    private static final QName wsdlMessage = QName.get("message", nsWsdl);
    private static final QName wsdlBinding = QName.get("binding", nsWsdl);
    private static final QName wsdlOperation = QName.get("operation", nsWsdl);
    private static final QName soapOperation = QName.get("operation", nsSoap);
    private static final QName portType = QName.get("portType", nsWsdl);
    private static final QName part = QName.get("part", nsWsdl);
    private static final QName input = QName.get("input", nsWsdl);
    private static final QName output = QName.get("output", nsWsdl);
    private static final QName body = QName.get("body", nsSoap);
    private static final QName header = QName.get("header", nsSoap);
    private static final QName service = QName.get("service", nsWsdl);
    private static final QName port = QName.get("port", nsWsdl);
    private static final QName address = QName.get("address", nsSoap);

    /**
     * Reads the command line arguments.
     *
     * @param    args        the arguments
     */
    private static void readArguments(String[] args) {
        int    argPos = 0;

        if (args[argPos].equals(ARG_OUTPUT_DIR)) {
            outputDir = args[++argPos];
            argPos++;
        }
    }

    public static Document makeWsdlDoc(List<WsdlInfoForNamespace> nsInfos, String serviceName, String targetNamespace) {
        Namespace nsSvc = new Namespace(svcPrefix, targetNamespace);

        final QName svcTypes = QName.get("types", nsWsdl);

        Document document = DocumentHelper.createDocument();
        Map<WsdlServiceInfo, Element> bindElems = Maps.newTreeMap();
        Map<WsdlServiceInfo, Element> portTypeElems = Maps.newTreeMap();
        Element root = document.addElement(QName.get("definitions", nsWsdl));
        root.add(nsSvc);
        for (WsdlInfoForNamespace wsdlNsInfo : nsInfos) {
            root.add(wsdlNsInfo.getXsdNamespace());
        }
        root.add(nsZimbra);
        root.add(nsSoap);
        root.add(nsXsd);
        root.add(nsWsdl);
        root.addAttribute("targetNamespace", targetNamespace);
        root.addAttribute("name", serviceName);
        addWsdlTypesElement(root, svcTypes, nsInfos);

        for (WsdlInfoForNamespace wsdlNsInfo : nsInfos) {
            WsdlServiceInfo svcInfo = wsdlNsInfo.getSvcInfo();
            if (!portTypeElems.containsKey(svcInfo)) {
                // wsdl:definitions/wsdl:portType
                Element portTypeElem = DocumentHelper.createElement(portType);
                portTypeElem.addAttribute("name", svcInfo.getPortTypeName());
                portTypeElems.put(svcInfo, portTypeElem);
            }
            if (!bindElems.containsKey(svcInfo)) {
                // wsdl:definitions/wsdl:binding
                Element bindingElem = DocumentHelper.createElement(wsdlBinding);
                bindingElem.addAttribute("name", svcInfo.getBindingName());
                bindingElem.addAttribute("type", svcPrefix + ":" + svcInfo.getPortTypeName());
                // wsdl:definitions/wsdl:binding/soap:binding
                Element soapBindElem = bindingElem.addElement(soapBinding);
                soapBindElem.addAttribute("transport", "http://schemas.xmlsoap.org/soap/http");
                soapBindElem.addAttribute("style", "document");

                bindElems.put(svcInfo, bindingElem);
            }
        }

        for (WsdlInfoForNamespace wsdlNsInfo : nsInfos) {
            WsdlServiceInfo svcInfo = wsdlNsInfo.getSvcInfo();
            for (String requestName : wsdlNsInfo.getRequests() ) {
                String rootName = requestName.substring(0, requestName.length() -7);
                String responseName = rootName + "Response";
                String reqOpName = requestName.substring(0, 1).toLowerCase() + requestName.substring(1);
                String reqMsgName = wsdlNsInfo.getTag() + requestName + "Message";
                String respMsgName = wsdlNsInfo.getTag() + responseName + "Message";

                addWsdlRequestAndResponseMessageElements(root, wsdlNsInfo,
                        reqMsgName, respMsgName, requestName, responseName);

                addWsdlPortTypeOperationElements(portTypeElems.get(svcInfo), reqMsgName, respMsgName, reqOpName);

                addWsdlBindingOperationElements(bindElems.get(svcInfo), wsdlNsInfo, reqOpName, rootName);
            }
        }
        addWsdlSoapHdrContextMessageElement(root);

        for (Entry<WsdlServiceInfo, Element> entry : portTypeElems.entrySet()) {
            root.add(entry.getValue());
        }

        for (Entry<WsdlServiceInfo, Element> entry : bindElems.entrySet()) {
            root.add(entry.getValue());
        }

        Set<WsdlServiceInfo> svcSet = Sets.newHashSet();
        for (WsdlInfoForNamespace wsdlNsInfo : nsInfos) {
            WsdlServiceInfo svcInfo  = wsdlNsInfo.getSvcInfo();
            if (!svcSet.contains(svcInfo)) {
                svcSet.add(svcInfo);
                addWsdlServiceElement(root, svcInfo);
            }
        }
        return document;
    }

    private static void addWsdlTypesElement(Element root, QName svcTypes, List<WsdlInfoForNamespace> nsInfos) {
        // wsdl:definitions/svc:types
        Element typesElem = root.addElement(svcTypes);
        // wsdl:definitions/svc:types/xsd:schema
        Element schemaElem = typesElem.addElement(xsdSchema);
        // wsdl:definitions/svc:types/xsd:schema/xsd:import
        Element importZimbraElem = schemaElem.addElement(xsdImport);
        importZimbraElem.addAttribute("namespace", ZimbraNamespace.ZIMBRA_STR);
        importZimbraElem.addAttribute("schemaLocation", "zimbra.xsd");

        for (WsdlInfoForNamespace nsInfo : nsInfos) {
            Element importTnsElem = schemaElem.addElement(xsdImport);
            importTnsElem.addAttribute("namespace", nsInfo.getXsdNamespaceString());
            importTnsElem.addAttribute("schemaLocation", nsInfo.getXsdFilename());
        }
    }

    private static void addWsdlRequestAndResponseMessageElements(Element root, WsdlInfoForNamespace nsInfo,
            String reqMsgName, String respMsgName, String requestName, String responseName) {
            // wsdl:definitions/wsdl:message - for request
            String xsdPrefix = nsInfo.getXsdPrefix();
            Element msgElem = root.addElement(wsdlMessage);
            msgElem.addAttribute("name", reqMsgName);
            // wsdl:definitions/wsdl:message/wsdl:part
            Element partElem = msgElem.addElement(part);
            /**
             * Bug 79898 - changed "name" from "parameters" to "params".
             * http://msdn.microsoft.com/en-us/magazine/cc188906.aspx
             *    Side Effects of "Parameters"
             *    Both wsdl.exe and the WebMethod infrastructure exercise special behavior when a special part name,
             *    "parameters," is used in a WSDL definition. The exact behavior is undocumented, but it's critical
             *    to understand the convention if you happen to use it.
             */
            partElem.addAttribute("name", "params");
            partElem.addAttribute("element", xsdPrefix + ":" + requestName);
            // wsdl:definitions/wsdl:message - for response
            msgElem = root.addElement(wsdlMessage);
            msgElem.addAttribute("name", respMsgName);
            // wsdl:definitions/wsdl:message/wsdl:part
            partElem = msgElem.addElement(part);
            partElem.addAttribute("name", "params");
            partElem.addAttribute("element", xsdPrefix + ":" + responseName);
    }

    private static void addWsdlPortTypeOperationElements(Element portTypeElem,
            String reqMsgName, String respMsgName, String reqOpName) {
            // wsdl:definitions/wsdl:portType/wsdl:operation
            Element opElem = portTypeElem.addElement(wsdlOperation);
            opElem.addAttribute("name", reqOpName);
            // wsdl:definitions/wsdl:portType/wsdl:operation/wsdl:input
            Element inElem = opElem.addElement(input);
            inElem.addAttribute("message", svcPrefix + ":" + reqMsgName);
            // wsdl:definitions/wsdl:portType/wsdl:operation/wsdl:output
            Element outElem = opElem.addElement(output);
            outElem.addAttribute("message", svcPrefix + ":" + respMsgName);
    }

    private static void addWsdlBindingOperationElements(Element bindingElem, WsdlInfoForNamespace nsInfo,
            String reqOpName, String rootName) {
            // wsdl:definitions/wsdl:binding/wsdl:operation
            Element boElem = bindingElem.addElement(wsdlOperation);
            boElem.addAttribute("name", reqOpName);
            // wsdl:definitions/wsdl:binding/wsdl:operation/soap:operation
            Element soapOpElem = boElem.addElement(soapOperation);
            soapOpElem.addAttribute("soapAction", nsInfo.getXsdNamespaceString() + "/" + rootName);
            soapOpElem.addAttribute("style", "document");
            // wsdl:definitions/wsdl:binding/wsdl:operation/wsdl:input
            Element boInElem = boElem.addElement(input);
            // wsdl:definitions/wsdl:binding/wsdl:operation/wsdl:input/soap:body
            Element inSoapBodyElem = boInElem.addElement(body);
            inSoapBodyElem.addAttribute("use", "literal");
            // wsdl:definitions/wsdl:binding/wsdl:operation/wsdl:input/soap:header
            Element inSoapHdrElem = boInElem.addElement(header);
            inSoapHdrElem.addAttribute("message", svcPrefix + ":soapHdrContext");
            inSoapHdrElem.addAttribute("part", "context");
            inSoapHdrElem.addAttribute("use", "literal");
            // wsdl:definitions/wsdl:binding/wsdl:operation/wsdl:output
            Element boOutElem = boElem.addElement(output);
            // wsdl:definitions/wsdl:binding/wsdl:operation/wsdl:output/soap:body
            Element outSoapBodyElem = boOutElem.addElement(body);
            outSoapBodyElem.addAttribute("use", "literal");
    }

    private static void addWsdlSoapHdrContextMessageElement(Element root) {
        // For Header Context
        // wsdl:definitions/wsdl:message
        Element hdrCntxtMsgElem = root.addElement(wsdlMessage);
        hdrCntxtMsgElem.addAttribute("name", "soapHdrContext");
        // wsdl:definitions/wsdl:message/wsdl:part
        Element partElem = hdrCntxtMsgElem.addElement(part);
        partElem.addAttribute("name", "context");
        partElem.addAttribute("element", "zimbra:context");
    }

    private static void addWsdlServiceElement(Element root, WsdlServiceInfo svcInfo) {
        // wsdl:definitions/wsdl:service
        Element svcElem = root.addElement(service);
        svcElem.addAttribute("name", svcInfo.getServiceName());
        // wsdl:definitions/wsdl:service/wsdl:port
        Element svcPortElem = svcElem.addElement(port);
        svcPortElem.addAttribute("name", svcInfo.getServiceName() + "Port");
        svcPortElem.addAttribute("binding", svcPrefix + ":" + svcInfo.getBindingName());
        // wsdl:definitions/wsdl:service/wsdl:port/soap:address
        Element svcPortAddrElem = svcPortElem.addElement(address);
        svcPortAddrElem.addAttribute("location", svcInfo.getSoapAddressURL());
    }

    public static void writeWsdl(OutputStream xmlOut, String targetNamespace, String serviceName,
            List<WsdlInfoForNamespace> nsInfos)
    throws IOException {
        Document wsdlDoc = makeWsdlDoc(nsInfos, serviceName, targetNamespace);
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter writer = new XMLWriter( xmlOut, format );
        writer.write(wsdlDoc);
        writer.close();
    }

    public static void createWsdlFile(File wsdlFile, String serviceName, List<WsdlInfoForNamespace> nsInfos)
    throws IOException {
        String targetNamespace = targetNsBase + wsdlFile.getName();
        if (wsdlFile.exists())
            wsdlFile.delete();
        OutputStream xmlOut = new FileOutputStream(wsdlFile);
        writeWsdl(xmlOut, targetNamespace, serviceName, nsInfos);
    }

    public static void createWsdlFile(String wsdlFileName, String serviceName, List<WsdlInfoForNamespace> nsInfos)
    throws IOException {
        File wsdlFile = new File(outputDir, wsdlFileName);
        createWsdlFile(wsdlFile, serviceName, nsInfos);
    }

    /**
     * Create a map whose key is a package name.  The value is the list of requests in that package.
     */
    private static Map<String,List<String>> getPackageToRequestListMap() {
        Map<String,List<String>> packageToRequestListsMap = Maps.newHashMap();
        for (Class<?> currClass : JaxbUtil.getJaxbRequestAndResponseClasses()) {
            String requestName = currClass.getSimpleName();
            if (!requestName.endsWith("Request"))
                continue;
            String pkgName = currClass.getPackage().getName();
            List<String> reqList;
            if (packageToRequestListsMap.containsKey(pkgName)) {
                reqList = packageToRequestListsMap.get(pkgName);
            } else {
                reqList = Lists.newArrayList();
                packageToRequestListsMap.put(pkgName, reqList);
            }
            reqList.add(requestName);
        }
        for (Entry<String, List<String>> entry : packageToRequestListsMap.entrySet()) {
            Collections.sort(entry.getValue());
        }
        return packageToRequestListsMap;
    }

    public enum WsdlDefinition {
        ALL("ZimbraService.wsdl", "ZimbraService", targetNsBase + "ZimbraService.wsdl"),
        ADMIN("ZimbraAdminService.wsdl", "ZimbraAdminService", targetNsBase + "ZimbraAdminService.wsdl"),
        USER("ZimbraUserService.wsdl", "ZimbraUserService", targetNsBase + "ZimbraAdminService.wsdl");

        private final String fileName;
        private final String serviceName;
        private final String targetNamespace;
        private WsdlDefinition(String fileName, String serviceName, String targetNamespace) {
            this.fileName = fileName;
            this.serviceName = serviceName;
            this.targetNamespace = targetNamespace;
        }
        public String getFileName() { return fileName; }
        public String getServiceName() { return serviceName; }
        public String getTargetNamespace() { return targetNamespace; }
    }

    public static boolean handleRequestForWsdl(String fileName, OutputStream out, String soapUrl, String adminSoapUrl)
    throws IOException {
        if (WsdlDefinition.ALL.getFileName().equals(fileName)) {
            createZimbraServiceWsdl(out, soapUrl, adminSoapUrl);
            return true;
        } else if (WsdlDefinition.ADMIN.getFileName().equals(fileName)) {
            createZimbraAdminServiceWsdl(out, adminSoapUrl);
            return true;
        } else if (WsdlDefinition.USER.getFileName().equals(fileName)) {
            createZimbraUserServiceWsdl(out, soapUrl);
            return true;
        }
        return false;
    }

    public static void createZimbraServiceWsdl(OutputStream out, String soapUrl, String adminSoapUrl)
    throws IOException {
        List<WsdlInfoForNamespace> nsInfoList = Lists.newArrayList();
        Map<String,List<String>> packageToRequestListMap = getPackageToRequestListMap();
        WsdlServiceInfo zcsService = WsdlServiceInfo.createForSoap(soapUrl);
        WsdlServiceInfo zcsAdminService = WsdlServiceInfo.createForAdmin(adminSoapUrl);
        addUserNamespaceInfo(nsInfoList, zcsService, packageToRequestListMap);
        addAdminNamespaceInfo(nsInfoList, zcsAdminService, packageToRequestListMap);
        writeWsdl(out, WsdlDefinition.ALL.getTargetNamespace(), WsdlDefinition.ALL.getServiceName(), nsInfoList);
    }

    public static void createZimbraAdminServiceWsdl(OutputStream out, String adminSoapUrl)
    throws IOException {
        List<WsdlInfoForNamespace> nsInfoList = Lists.newArrayList();
        Map<String,List<String>> packageToRequestListMap = getPackageToRequestListMap();
        WsdlServiceInfo zcsAdminService = WsdlServiceInfo.createForAdmin(adminSoapUrl);
        addAdminNamespaceInfo(nsInfoList, zcsAdminService, packageToRequestListMap);
        writeWsdl(out, WsdlDefinition.ALL.getTargetNamespace(), WsdlDefinition.ALL.getServiceName(), nsInfoList);
    }

    public static void createZimbraUserServiceWsdl(OutputStream out, String soapUrl)
    throws IOException {
        List<WsdlInfoForNamespace> nsInfoList = Lists.newArrayList();
        Map<String,List<String>> packageToRequestListMap = getPackageToRequestListMap();
        WsdlServiceInfo zcsService = WsdlServiceInfo.createForSoap(soapUrl);
        addUserNamespaceInfo(nsInfoList, zcsService, packageToRequestListMap);
        writeWsdl(out, WsdlDefinition.ALL.getTargetNamespace(), WsdlDefinition.ALL.getServiceName(), nsInfoList);
    }

    private static void addAdminNamespaceInfo(List<WsdlInfoForNamespace> nsInfoList, WsdlServiceInfo zcsAdminService,
            Map<String,List<String>> packageToRequestListMap) {
        nsInfoList.add(WsdlInfoForNamespace.create(AdminConstants.NAMESPACE_STR, zcsAdminService,
                packageToRequestListMap.get("com.zimbra.soap.admin.message")));
        nsInfoList.add(WsdlInfoForNamespace.create(AdminExtConstants.NAMESPACE_STR, zcsAdminService,
                packageToRequestListMap.get("com.zimbra.soap.adminext.message")));
    }

    private static void addUserNamespaceInfo(List<WsdlInfoForNamespace> nsInfoList, WsdlServiceInfo zcsService,
            Map<String,List<String>> packageToRequestListMap) {
        nsInfoList.add(WsdlInfoForNamespace.create(AccountConstants.NAMESPACE_STR, zcsService,
                packageToRequestListMap.get("com.zimbra.soap.account.message")));
        nsInfoList.add(WsdlInfoForNamespace.create(MailConstants.NAMESPACE_STR, zcsService,
                packageToRequestListMap.get("com.zimbra.soap.mail.message")));
        nsInfoList.add(WsdlInfoForNamespace.create(ReplicationConstants.NAMESPACE_STR, zcsService,
                packageToRequestListMap.get("com.zimbra.soap.replication.message")));
        nsInfoList.add(WsdlInfoForNamespace.create(SyncConstants.NAMESPACE_STR, zcsService,
                packageToRequestListMap.get("com.zimbra.soap.sync.message")));
        nsInfoList.add(WsdlInfoForNamespace.create(VoiceConstants.NAMESPACE_STR, zcsService,
                packageToRequestListMap.get("com.zimbra.soap.voice.message")));
    }

    /**
     * Main
     *
     * @param args the utility arguments
     */
    public static void main(String[] args) throws Exception {
        List<WsdlInfoForNamespace> nsInfoList = Lists.newArrayList();
        readArguments(args);
        Map<String,List<String>> packageToRequestListMap = getPackageToRequestListMap();
        addUserNamespaceInfo(nsInfoList, WsdlServiceInfo.zcsService, packageToRequestListMap);
        addAdminNamespaceInfo(nsInfoList, WsdlServiceInfo.zcsAdminService, packageToRequestListMap);
        createWsdlFile("ZimbraService.wsdl", "ZimbraService", nsInfoList);
    }
} // end WsdlGenerator class
