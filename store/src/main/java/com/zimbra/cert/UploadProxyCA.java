// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cert;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.CertMgrConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.service.FileUploadServlet;
import com.zimbra.cs.service.FileUploadServlet.Upload;
import com.zimbra.cs.service.admin.AdminDocumentHandler;
import com.zimbra.soap.ZimbraSoapContext;
import java.io.IOException;
import java.util.Map;

public class UploadProxyCA extends AdminDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context) throws ServiceException {
        ZimbraSoapContext lc = getZimbraSoapContext(context);
        Element response = lc.createElement(CertMgrConstants.UPLOAD_PROXYCA_RESPONSE);

        String attachId = null;
        String filename = null;
        Upload up = null ;

        try {
            attachId = request.getAttribute(CertMgrConstants.A_CERT_AID);
            filename = request.getAttribute(CertMgrConstants.A_CERT_NAME);
            ZimbraLog.security.debug("Found certificate Filename  = " + filename + "; attid = " + attachId );

            up = FileUploadServlet.fetchUpload(lc.getAuthtokenAccountId(), attachId, lc.getAuthToken());
            if (up == null)
                throw ServiceException.FAILURE("Uploaded file " + filename + " with " + attachId + " was not found.", null);

            byte [] blob = ByteUtil.getContent(up.getInputStream(),-1) ;
            if(blob.length > 0)
                response.addAttribute(CertMgrConstants.A_cert_content, new String(blob));
        }catch (IOException ioe) {
            throw ServiceException.FAILURE("Can not get uploaded certificate content", ioe);
        }finally {
            FileUploadServlet.deleteUpload(up);
        }

        return response;
    }
}
