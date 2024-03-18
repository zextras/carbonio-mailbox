// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.rmgmt;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.rmgmt.RemoteMailQueue.QueueAttr;

/** 
 * Parse a list of simple key_string=value_string\n maps from standard input.
 * Maps are seperated any lines that do not contain a = seperator.
 */
public class RemoteResultParser {
    
    public interface Visitor {
        public void handle(int lineNo, Map<String, String> map) throws IOException;
    }
    
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^([^=]+)=(.*)$");
    private static final int KEY_GROUP = 1;
    private static final int VALUE_GROUP = 2;
    
    public static void parse(Reader reader, Visitor visitor) throws IOException {
        BufferedReader in = new BufferedReader(reader);
        String line;
        int lineNumber = 0;
        
        Map<String,String> current = new HashMap<>();
        int currentMapStartLineNumber = 1;
        
        while ((line = in.readLine()) != null) {
            lineNumber++;
            Matcher matcher = KEY_VALUE_PATTERN.matcher(line);
            if (ZimbraLog.rmgmt.isDebugEnabled()) ZimbraLog.rmgmt.debug("Scanning mail queues. Read line: " + line);
            if (!matcher.find()) {
            	if (ZimbraLog.rmgmt.isDebugEnabled()) ZimbraLog.rmgmt.debug("Scanning mail queues. Matcher did not find any mathces.");
            	String id = current.get(QueueAttr.id.toString());
            	if (id == null) 
            		continue;
            	
            	visitor.handle(currentMapStartLineNumber, current);
                current = new HashMap<>();
                currentMapStartLineNumber = lineNumber + 1;
            } else {
                current.put(matcher.group(KEY_GROUP), matcher.group(VALUE_GROUP));
            }
        }
        if (!current.isEmpty()) {
            visitor.handle(currentMapStartLineNumber, current);
        }
    }
    
    public static void parse(InputStream is, Visitor v) throws IOException {
        parse(new InputStreamReader(is), v);
    }
    
    public static void parse(RemoteResult rr, Visitor v) throws IOException {
        parse(new InputStreamReader(new ByteArrayInputStream(rr.mStdout)), v);
    }
    
    private static class SingleVisitor implements Visitor {
        private Map<String,String> mValue;
        
        public void handle(int lineNo, Map<String, String> map) {
            mValue = map;
        }
    }
    
    public static Map<String,String> parseSingleMap(Reader reader) throws IOException {
        SingleVisitor v = new SingleVisitor();
        parse(reader, v);
        return v.mValue;
    }
    
    public static Map<String,String> parseSingleMap(InputStream is) throws IOException {
        return parseSingleMap(new InputStreamReader(is));
    }
    
    public static void main(String[] args) throws IOException {
        InputStream is;
        if (args.length == 0) {
            is = System.in;
        } else {
            is = new FileInputStream(args[0]);
        }
        
        RemoteResultParser.parse(new InputStreamReader(is), new Visitor() {
            
            public void handle(int lineNumber, Map<String, String> map) {
                for (String key : map.keySet()) {
                    System.out.print(key);
                    System.out.print("=");
                    System.out.println(map.get(key));
                }
                System.out.println();
            }
        });
    }

    public static Map<String, String> parseSingleMap(RemoteResult rr) throws IOException {
        return parseSingleMap(new InputStreamReader(new ByteArrayInputStream(rr.mStdout)));
    }
}
