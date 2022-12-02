// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest.prov.soap;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.cs.account.soap.SoapProvisioning;
import qa.unittest.prov.ProvTestUtil;

public class SoapProvTestUtil extends ProvTestUtil {
    
    SoapProvTestUtil() throws Exception {
        super(SoapProvisioning.getAdminInstance());
        SoapProvisioning prov = getProv();
        prov.soapSetHttpTransportDebugListener(new SoapDebugListener());
    }
    
    SoapProvisioning getProv() {
        return (SoapProvisioning) prov;
    }
    
    static SoapProvisioning getSoapProvisioning(String userName, String password) 
    throws ServiceException {
        SoapProvisioning sp = new SoapProvisioning();
        sp.soapSetHttpTransportDebugListener(new SoapDebugListener());
        sp.soapSetURI("https://localhost:7071" + AdminConstants.ADMIN_SERVICE_URI);
        sp.soapAdminAuthenticate(userName, password);
        return sp;
    }
}
