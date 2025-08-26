// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.certmanager;

import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.ZimbraHttpConnectionManager;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.extension.ExtensionHttpHandler;
import com.zimbra.cs.rmgmt.RemoteManager;
import com.zimbra.cs.rmgmt.RemoteResult;
import com.zimbra.cs.servlet.ZimbraServlet;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;

public class DownloadCSRHandler extends ExtensionHttpHandler {
    public static final String HANDLER_PATH_NAME = "downloadcsr";
    public static final String CSR_FILE_NAME = LC.zimbra_home.value() + "/ssl/carbonio/commercial/commercial.csr"; // this path is hardcoded in /opt/zextras/bin/zmcertmgr
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        AuthToken authToken = ZimbraServlet.getAdminAuthTokenFromCookie(req, resp);
        if (authToken == null) {
            ZimbraLog.extensions.error("Missing authtoken");
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        String serverId = req.getParameter(AdminConstants.A_SERVER);
        Provisioning prov = Provisioning.getInstance();
        try {
            Server server;
            if (serverId == null) {
                server = prov.getLocalServer();
            } else {
                server = prov.getServerById(serverId);
            }
            if (server == null) {
                ZimbraLog.extensions.error("Cannot find server with ID %s", serverId);
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            try {
                checkRight(authToken, server, Admin.R_getCSR);
                if (server.isLocalServer()) {
                    // send CSR file
                    getCSRFile(resp);
                } else if (server.hasMailClientService()) {
                    // forward request to target server
                    proxyRequestWithAuth(authToken, server, resp);
                } else {
                    downloadViaRemoteMgr(server, resp);
                }
            } catch (ServiceException | HttpException e) {
                ZimbraLog.extensions.error("Admin user %s does not have permission %s to download CSR from server %s",
                        authToken.getAccount().getName(), Admin.R_getCSR.toString(), serverId);
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } catch (ServiceException e) {
            resp.setHeader(ZimbraServlet.ZIMBRA_FAULT_CODE_HEADER, e.getCode());
            resp.setHeader(ZimbraServlet.ZIMBRA_FAULT_MESSAGE_HEADER, e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static void getCSRFile(HttpServletResponse resp) throws FileNotFoundException,
            IOException {
        setRespHeaders(resp);
        InputStream in = null;
        try {
            OutputStream out = resp.getOutputStream();
            in = new BufferedInputStream(new FileInputStream(CSR_FILE_NAME));
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buf)) != -1) {
                out.write(buf, 0, bytesRead);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private void proxyRequestWithAuth(AuthToken authToken, Server server, HttpServletResponse resp)
            throws ServiceException, IOException, HttpException {
        HttpClientBuilder httpClientBuilder = ZimbraHttpConnectionManager.getInternalHttpConnMgr().getDefaultHttpClient();
        BasicCookieStore cookieStore = HttpClientUtil.newHttpState(authToken.toZAuthToken(), server.getServiceHostname(), true);
        String destURL = String.format("https://%s:%s/service/extension/%s/%s",
                server.getServiceHostname(), server.getAdminPortAsString(), ZimbraCertMgrExt.EXTENSION_NAME_CERTMGR,
 HANDLER_PATH_NAME);
        HttpGet method = new HttpGet(destURL);
        httpClientBuilder.setDefaultCookieStore(cookieStore);
        ZimbraLog.extensions.debug("Proxying CSR download request to %s", destURL);
        HttpResponse httpResponse = HttpClientUtil.executeMethod(httpClientBuilder.build(), method);
        InputStream responseBody = httpResponse.getEntity().getContent();
        resp.setStatus(httpResponse.getStatusLine().getStatusCode());
        for (Header h : httpResponse.getAllHeaders()) {
            resp.addHeader(h.getName(), h.getValue());
        }
        if (responseBody != null) {
            ByteUtil.copy(responseBody, true, resp.getOutputStream(), true);
        }
    }

    private static void setRespHeaders(HttpServletResponse resp) {
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Expires", "0");
        resp.setContentType("application/x-download");
        resp.setHeader("Content-Disposition", "attachment; filename=commercial.csr");
    }

    private static void downloadViaRemoteMgr(Server server, HttpServletResponse resp)
            throws ServiceException, IOException {
        RemoteManager rmgr = RemoteManager.getRemoteManager(server);
        ZimbraLog.security.debug("***** Executing the cmd = %s", ZimbraCertMgrExt.DOWNLOAD_CSR_CMD);
        RemoteResult rr = null;
        rr = rmgr.execute(ZimbraCertMgrExt.DOWNLOAD_CSR_CMD);
        if (rr.getMExitStatus() == 0) {
            byte[] content = rr.getMStdout();
            setRespHeaders(resp);
            ByteUtil.copy(new ByteArrayInputStream(OutputParser.cleanCSROutput(content).getBytes()), true,
                    resp.getOutputStream(), true);
        } else {
            String stderr = (rr.getMStderr() != null) ? new String(rr.getMStderr(), Charset.forName("UTF-8")) : null;
            String errmsg = String.format("Command \"%s\" failed; exit code=%d; stderr=\n%s",
                    ZimbraCertMgrExt.DOWNLOAD_CSR_CMD, rr.getMExitStatus(), stderr);
            ZimbraLog.security.error(errmsg);
            throw ServiceException.FAILURE(errmsg, null);
        }
    }

    @Override
    public String getPath() {
        return super.getPath() + "/" + HANDLER_PATH_NAME;
    }
}