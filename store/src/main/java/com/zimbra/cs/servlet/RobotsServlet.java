// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.cs.account.Provisioning;

public class RobotsServlet extends HttpServlet {
    private static final long serialVersionUID = 1058982623987983L;
    
    @Override protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        boolean keepOutCrawlers = false;
        try {
            keepOutCrawlers = Provisioning.getInstance().getLocalServer().isMailKeepOutWebCrawlers();
        } catch (ServiceException e) {
        }
      try (ServletOutputStream out = response.getOutputStream()) {
        out.println("User-agent: *");
        if (keepOutCrawlers) {
          out.println("Disallow: /");
        } else {
          out.println("Allow: /");
        }
        String extra = LC.robots_txt.value();
        File extraFile = new File(extra);
        if (extraFile.exists()) {
          FileInputStream in = new FileInputStream(extraFile);
          ByteUtil.copy(in, true, out, false);
        }
        out.flush();
      }
    }
}
