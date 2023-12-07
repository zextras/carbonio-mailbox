// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.mailbox.RetentionPolicyManager;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.CreateSystemRetentionPolicyRequest;
import com.zimbra.soap.admin.message.CreateSystemRetentionPolicyResponse;
import com.zimbra.soap.admin.type.CosSelector;
import com.zimbra.soap.mail.type.Policy;
import java.util.List;
import java.util.Map;

public class CreateSystemRetentionPolicy extends AdminDocumentHandler {

  static final String SYSTEM_RETENTION_POLICY_ATTR = Provisioning.A_zimbraMailPurgeSystemPolicy;

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);

    CreateSystemRetentionPolicyRequest req = zsc.elementToJaxb(request);

    Provisioning prov = Provisioning.getInstance();

    // assume default retention policy to be set in globalConfig (for backward compatibility)
    Entry entry = prov.getConfig();

    // check if cos is specified
    CosSelector cosSelector = req.getCos();

    if (cosSelector != null) {
      entry = prov.get(Key.CosBy.fromString(cosSelector.getBy().name()), cosSelector.getKey());
      if (entry == null) throw AccountServiceException.NO_SUCH_COS(cosSelector.getKey());
    }

    // check right
    checkSetRight(entry, zsc, context, this);

    Policy purge = req.getPurgePolicy();
    if (purge == null) {
      throw ServiceException.INVALID_REQUEST("No purge policy specified.", null);
    }

    Policy newPolicy =
        RetentionPolicyManager.getInstance()
            .createSystemPurgePolicy(entry, purge.getName(), purge.getLifetime());
    CreateSystemRetentionPolicyResponse res = new CreateSystemRetentionPolicyResponse(newPolicy);

    return zsc.jaxbToElement(res);
  }

  static void checkSetRight(
      Entry entry, ZimbraSoapContext zsc, Map<String, Object> context, AdminDocumentHandler handler)
      throws ServiceException {
    AdminAccessControl.SetAttrsRight sar = new AdminAccessControl.SetAttrsRight();
    sar.addAttr(CreateSystemRetentionPolicy.SYSTEM_RETENTION_POLICY_ATTR);
    handler.checkRight(zsc, context, entry, sar);
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    notes.add("Need set attr right on attribute " + SYSTEM_RETENTION_POLICY_ATTR);
  }
}
