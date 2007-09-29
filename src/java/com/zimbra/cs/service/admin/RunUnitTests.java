/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2005, 2006, 2007 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */

package com.zimbra.cs.service.admin;

import java.io.ByteArrayOutputStream;
import java.util.Map;

import junit.framework.TestResult;

import com.zimbra.qa.unittest.ZimbraSuite;
import com.zimbra.soap.Element;
import com.zimbra.soap.ZimbraSoapContext;

/**
 * @author bburtin
 */
public class RunUnitTests extends AdminDocumentHandler {
    
	public Element handle(Element request, Map<String, Object> context) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        TestResult result = ZimbraSuite.runTestSuite(os);
        
        ZimbraSoapContext lc = getZimbraSoapContext(context);
        Element response = lc.createElement(AdminService.RUN_UNIT_TESTS_RESPONSE);
        response.addAttribute(AdminService.A_NUM_EXECUTED, Integer.toString(result.runCount()));
        response.addAttribute(AdminService.A_NUM_FAILED,
            Integer.toString(result.failureCount() + result.errorCount()));
        response.addAttribute(AdminService.A_OUTPUT, os.toString());
    	return response;
	}
}

