// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.service.FileUploadServlet.Upload;

public abstract class UploadScanner {

    public static final class Result {
        private String mDesc;
        
        Result(String desc) {
            mDesc = desc;
        }
        
        public String toString() {
            return mDesc;
        }
    }
    
    public static final Result ACCEPT = new Result("ACCEPT");
    public static final Result REJECT = new Result("REJECT");
    public static final Result ERROR = new Result("ERROR");
    
    private static List<UploadScanner> sRegisteredScanners = new LinkedList<UploadScanner>();
    
    public static void registerScanner(UploadScanner scanner) {
    	sRegisteredScanners.add(scanner);
    }
    
    public static void unregisterScanner(UploadScanner scanner) {
    	sRegisteredScanners.remove(scanner);
    }
    
    public static Result accept(Upload up, StringBuffer info) {

        InputStream is = null;
        try {
            is = up.getInputStream();
        } catch (IOException ioe) {
            ZimbraLog.misc.error("exception getting input stream for scanning", ioe);
            info.append(" ").append(ioe);
            return ERROR;
        }
      for (UploadScanner scanner : sRegisteredScanners) {
        if (!scanner.isEnabled()) {
          continue;
        }

        Result result;
        try {
          result = scanner.accept(is, info);
        } finally {
          ByteUtil.closeStream(is);
        }
        if (result == REJECT || result == ERROR) {
          // Fail on the first scanner that says it was bad,
          // or first error we encounter. Is bailing on first error
          // too harsh, should we continue to try other scanners?
          return result;
        }
      }
        return ACCEPT;
    }

    public static Result acceptStream(InputStream is, StringBuffer info) {

      for (UploadScanner scanner : sRegisteredScanners) {

        if (!scanner.isEnabled()) {
          continue;
        }

        Result result;
        try {
          result = scanner.accept(is, info);
        } finally {
          ByteUtil.closeStream(is);
        }
        if (result == REJECT || result == ERROR) {
          // Fail on the first scanner that says it was bad,
          // or first error we encounter. Is bailing on first error
          // too harsh, should we continue to try other scanners?
          return result;
        }
      }
        return ACCEPT;
    }

    protected abstract Result accept(InputStream is, StringBuffer info);

    protected abstract Result accept(byte[] data, StringBuffer info);

    public abstract void setURL(String string) throws MalformedURLException;
    
    public abstract boolean isEnabled();
}
