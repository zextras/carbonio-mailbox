// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapFaultException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.DataSource;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.datasource.DataSourceManager;
import com.zimbra.cs.datasource.ImportStatus;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.List;
import java.util.Map;

public class GetImportStatus extends MailDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context)
      throws ServiceException, SoapFaultException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    Account account = getRequestedAccount(zsc);
    Provisioning prov = Provisioning.getInstance();

    List<ImportStatus> statusList = DataSourceManager.getImportStatus(account);
    Element response = zsc.createElement(MailConstants.GET_IMPORT_STATUS_RESPONSE);

    for (ImportStatus status : statusList) {
      DataSource ds = prov.get(account, Key.DataSourceBy.id, status.getDataSourceId());
      Element eDataSource = response.addElement(ds.getType().name());
      eDataSource.addAttribute(MailConstants.A_ID, status.getDataSourceId());
      eDataSource.addAttribute(MailConstants.A_DS_IS_RUNNING, status.isRunning());

      if (status.hasRun() && !status.isRunning()) { // Has finished at least one run
        eDataSource.addAttribute(MailConstants.A_DS_SUCCESS, status.getSuccess());
        if (!status.getSuccess() && status.getError() != null) { // Failed, error available
          eDataSource.addAttribute(MailConstants.A_DS_ERROR, status.getError());
        }
      }
    }
    return response;
  }
}
