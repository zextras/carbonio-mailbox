// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.certmanager;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraLog;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OutputParser {
    private static final String ERROR_PREFIX = "XXXXX ERROR:";
    private static final Pattern GET_CERT_OUT_PATTERN = Pattern.compile("^([^=]+)=(.*)$");

    // parse the output of the zmcertmgr cmd
    public static HashMap<String, String> parseOuput(byte[] in) throws IOException, ServiceException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(in)));
        String line;
        HashMap<String, String> hash = new HashMap();
        Matcher matcher;
        String key;
        String value;
        while ((line = br.readLine()) != null) {
            if (line.startsWith("STARTCMD:") || line.startsWith("ENDCMD:")) {
                continue;
            } else if (line.startsWith(ERROR_PREFIX)) {
                throw ServiceException.FAILURE(line, null);
            } else {
                ZimbraLog.security.debug("DEBUG: Current Line = " + line);
                // line = line.replaceFirst(OUTPUT_PREFIX, "").trim(); //remove the OUTPUT_PREFIX
                // for GetCert
                matcher = GET_CERT_OUT_PATTERN.matcher(line);
                if (matcher.matches()) {
                    key = matcher.group(1);
                    value = matcher.group(2);
                    // System.out.println("Key = " + key + "; value="+ value);
                    hash.put(key, value);
                } else {
                    continue;
                }
            }
        }

        return hash;
    }

    // parse the output of the zmcertmgr cmd
    public static String cleanCSROutput(byte[] in) throws IOException, ServiceException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(in)));
        String line;
        StringBuilder csrContent = new StringBuilder();
        while ((line = br.readLine()) != null) {
            if (line.startsWith("STARTCMD:") || line.startsWith("ENDCMD:")) {
                continue;
            } else if (line.startsWith(ERROR_PREFIX)) {
                throw ServiceException.FAILURE(line, null);
            } else {
                csrContent.append(line).append("\n");
            }
        }
        return csrContent.toString();
    }

    // Example:
    // subject=/C=US/ST=CA/L=San Mateo/O=Zimbra/OU=Zimbra Collaboration Suite/CN=admindev.zimbra.com

    public static HashMap<String, String> parseSubject(String subject) {
        HashMap<String, String> hash = new HashMap<String, String>();
        // this will cause issue when the subject contains /
        // String [] dsn = subject.split("/");
        Matcher matcher;
        String key;
        String value;
        Pattern key_pattern = Pattern.compile("^\\/(C|ST|L|O|OU|CN)=(.*)$");
        Pattern value_pattern = Pattern.compile("^(.*?)(\\/(C|ST|L|O|OU|CN)=.*)$");
        String parsing_literal = subject.trim();
        matcher = key_pattern.matcher(parsing_literal);
        while (matcher.matches()) {
            key = matcher.group(1);
            parsing_literal = matcher.group(2);
            matcher = value_pattern.matcher(parsing_literal);

            if (matcher.matches()) {
                value = matcher.group(1);
                parsing_literal = matcher.group(2);
            } else {
                value = parsing_literal;
            }
            hash.put(key, value);
            matcher = key_pattern.matcher(parsing_literal);
        }
        return hash;
    }

    // SubjectAltNames=DNS:admindev.zimbra.com, DNS:test1.zimbra.com, DNS:test2.zimbra.com
    public static Vector<String> parseSubjectAltName(String subjectAltNames) {
        // ZimbraLog.security.info(subjectAltNames);
        Vector<String> vec = new Vector<String>();
        String[] dns = subjectAltNames.split(",");
        for (int i = 0; i < dns.length; i++) {
            vec.add(dns[i].trim());
        }
        /*
         * zmcertmgr remove the DNS.* already Matcher matcher; String value; for (int i=0; i < dns.length; i++) { matcher = GET_SUBJECT_ALT_NAME_PATTERN.matcher(dns[i]); if (matcher.matches()) { value
         * = matcher.group(1); //ZimbraLog.security.info("Host " + i + " = " + value); vec.add(value); } }
         */
        return vec;
    }

    // parse verification result
    public static boolean parseVerifyResult(byte[] in) throws IOException, ServiceException {
        BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(in)));
        String line;
        String ERROR_CERT_OUTPUT = "error:";
        while ((line = br.readLine()) != null) {
            if (line.startsWith("STARTCMD:") || line.startsWith("ENDCMD:")) {
                continue;
            } else if (line.startsWith(ERROR_PREFIX) || line.contains(ERROR_PREFIX) || line.contains(ERROR_CERT_OUTPUT)) {
                // throw ServiceException.FAILURE(line, null);
                return false;
            }
        }

        return true;
    }
}