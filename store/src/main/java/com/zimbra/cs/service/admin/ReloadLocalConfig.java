// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.localconfig.ConfigException;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.ReloadLocalConfigResponse;
import java.util.Map;
import org.dom4j.DocumentException;

/**
 * Reload the local config file on the fly.
 * <p>
 * After successfully reloading a new local config file, subsequent
 * {@link LC#get(String)} calls should receive new value. However, if you store/
 * cache those values (e.g. keep them as class member or instance member), new
 * values are of course not reflected.
 *
 * @author ysasaki
 */
public final class ReloadLocalConfig extends AdminDocumentHandler {

    @Override
    public Element handle(Element request, Map<String, Object> context)
            throws ServiceException {
        try {
            LC.reload();
        } catch (DocumentException e) {
            ZimbraLog.misc.error("Failed to reload LocalConfig", e);
            throw AdminServiceException.FAILURE("Failed to reload LocalConfig", e);
        } catch (ConfigException e) {
            ZimbraLog.misc.error("Failed to reload LocalConfig", e);
            throw AdminServiceException.FAILURE("Failed to reload LocalConfig", e);
        }
        ZimbraLog.misc.info("LocalConfig reloaded");
        ZimbraSoapContext zsc = getZimbraSoapContext(context);
        return zsc.jaxbToElement(new ReloadLocalConfigResponse());
    }

}
