// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.lmtpserver.utils;

import java.util.Map;
import java.util.Iterator;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import com.zimbra.cs.lmtpserver.LmtpAddress;

public class LmtpAddressTester {
	
	public static void main(String[] args) throws IOException {
		BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
		String line;
		int i = 0;
		while ((line = in.readLine()) != null) {
			i++;
			
			if (line.startsWith("#") || line.length() == 0) {
				continue;
			}
			
			int colon = line.indexOf(':');
			if (colon < 0) {
				System.err.println("missing colon:line " + i + ":" + line);
				continue;
			}
			String result = line.substring(0, colon);
			String input = line.substring(colon + 1);
			
			boolean validity;
			if ("+".equals(result)) {
				validity = true;
			} else if ("-".equals(result)) {
				validity = false;
			} else {
				System.err.println("missing result:line " + i + ":" + line);
				continue;
			}
			
            if (args.length == 0) {
                args = new String[] { "BODY", "SIZE" };
            }
			if (test(input, args) != validity) {
				System.err.println("incorrect result:line " + i + ":" + line);
			}
		}
	}
	
	private static boolean test(String line, String[] allowedParams) {
		System.out.println("==>" + line + "<==");
		LmtpAddress addr = new LmtpAddress(line, allowedParams, "+");
		System.out.println("  valid=" + addr.isValid());
		if (addr.isValid()) {
			System.out.println("  local-part=/" + addr.getLocalPart() + "/");
            System.out.println("  normalized-local=/" + addr.getNormalizedLocalPart() + "/");
			System.out.println("  domain-part=/" + addr.getDomainPart() + "/");
			Map params = addr.getParameters();
			int i = 0;
      for (Object o : params.entrySet()) {
        Map.Entry e = (Map.Entry) o;
        String key = (String) e.getKey();
        String val = (String) e.getValue();
        System.out.println("  [" + i + "] key=/" + key + "/ val=/" + val + "/");
        i++;
      }
		}
		return addr.isValid();
	}
}
