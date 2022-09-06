// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.SystemUtil;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.mailbox.RetentionPolicyManager;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.ModifySystemRetentionPolicyRequest;
import com.zimbra.soap.admin.message.ModifySystemRetentionPolicyResponse;
import com.zimbra.soap.admin.type.CosSelector;
import com.zimbra.soap.mail.type.Policy;
import java.util.List;
import java.util.Map;

public class ModifySystemRetentionPolicy extends AdminDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zsc = getZimbraSoapContext(context);

    ModifySystemRetentionPolicyRequest req = zsc.elementToJaxb(request);
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
    CreateSystemRetentionPolicy.checkSetRight(entry, zsc, context, this);

    Policy p = req.getPolicy();
    if (p == null) {
      throw ServiceException.INVALID_REQUEST("policy not specified", null);
    }
    if (p.getId() == null) {
      throw ServiceException.INVALID_REQUEST("id not specified for policy", null);
    }

    RetentionPolicyManager mgr = RetentionPolicyManager.getInstance();
    String id = p.getId();
    Policy current = mgr.getPolicyById(entry, id);
    if (current == null) {
      throw ServiceException.INVALID_REQUEST(
          "Could not find system retention policy with id " + id, null);
    }
    String name = SystemUtil.coalesce(p.getName(), current.getName());
    String lifetime = SystemUtil.coalesce(p.getLifetime(), current.getLifetime());
    Policy latest = mgr.modifySystemPolicy(entry, id, name, lifetime);
    ModifySystemRetentionPolicyResponse res = new ModifySystemRetentionPolicyResponse(latest);
    return zsc.jaxbToElement(res);
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    notes.add(
        "Need set attr right on attribute "
            + CreateSystemRetentionPolicy.SYSTEM_RETENTION_POLICY_ATTR);
  }
}
