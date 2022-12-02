// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.util;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.header.HeaderContext;

/**
 */
public class Jaxb2Xsds {
    private static final Logger LOG = Logger.getLogger(Jaxb2Xsds.class);

    private static final String ARG_DIR = "--dir";

    private static String dir = null;

    static {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        LOG.setLevel(Level.INFO);
    }
    /**
     * Main
     */
    public static void main(String[] args) throws Exception {
        readArguments(args);
        createXsds();
    }

    /**
     * Reads the command line arguments.
     */
    private static void readArguments(String[] args) {
        int    argPos = 0;

        while (argPos < args.length) {
            if (args[argPos].equals(ARG_DIR)) {
                dir = args[++argPos];
            }
            argPos++;
        }
        if (dir == null) {
            throw new RuntimeException(String.format("Missing %s argument", ARG_DIR));
        }
    }

    private static class ZimbraSchemaOutputResolver extends SchemaOutputResolver {
        String parentDir;

        ZimbraSchemaOutputResolver(String dir) {
            this.parentDir = dir;
        }

        @Override
        public Result createOutput(String namespaceURI, String suggestedFileName) throws IOException {
            if (!Strings.isNullOrEmpty(namespaceURI)) {
                suggestedFileName = namespaceURI.substring(namespaceURI.indexOf(':') + 1) + ".xsd";
            }
            File file = new File(parentDir, suggestedFileName);
            StreamResult result = new StreamResult(file);
            result.setSystemId(file.toURI().toURL().toString());
            return result;
        }
    }

    /**
     * Create XSDs for all reachable objects from either the requests and responses or the HeaderContext
     */
    public static void createXsds() {
        List<Class<?>> classList = Lists.newArrayList();
        classList.addAll(JaxbUtil.getJaxbRequestAndResponseClasses());
        classList.add(HeaderContext.class);
        JAXBContext jaxbContext;
        try {
            jaxbContext = JAXBContext.newInstance(classList.toArray(new Class[classList.size()]));
        } catch (JAXBException e) {
            throw new RuntimeException(String.format("Problem creating JAXBContext", ARG_DIR), e);
        }

        SchemaOutputResolver sor = new ZimbraSchemaOutputResolver(dir);
        try {
            jaxbContext.generateSchema(sor);
        } catch (IOException e) {
            throw new RuntimeException(String.format("Problem generating schemas", ARG_DIR), e);
        }
    }
}
