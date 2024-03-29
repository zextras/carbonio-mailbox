// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.zimbra.common.account.Key;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.AdminConstants;
import com.zimbra.common.soap.Element;
import com.zimbra.cs.account.AttributeFlag;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Entry;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GetDomainInfo extends AdminDocumentHandler {

  private static final Set<String> bootstrapInfoAttrs =
      Set.of(
          ZAttrProvisioning.A_zimbraSkinLogoURL,
          ZAttrProvisioning.A_zimbraSkinLogoAppBanner,
          ZAttrProvisioning.A_zimbraSkinLogoLoginBanner,
          ZAttrProvisioning.A_zimbraAdminConsoleLoginURL,
          ZAttrProvisioning.A_zimbraWebClientLoginURL,
          ZAttrProvisioning.A_zimbraWebClientLoginURLAllowedUA,
          ZAttrProvisioning.A_zimbraWebClientLoginURLAllowedIP,
          ZAttrProvisioning.A_zimbraWebClientLogoutURL,
          ZAttrProvisioning.A_zimbraWebClientLogoutURLAllowedUA,
          ZAttrProvisioning.A_zimbraWebClientLogoutURLAllowedIP,
          ZAttrProvisioning.A_zimbraWebClientMaxInputBufferLength,
          ZAttrProvisioning.A_zimbraWebClientStaySignedInDisabled,
          ZAttrProvisioning.A_zimbraSkinBackgroundColor,
          ZAttrProvisioning.A_zimbraSkinForegroundColor,
          ZAttrProvisioning.A_zimbraSkinSecondaryColor,
          ZAttrProvisioning.A_zimbraSkinSelectionColor,
          ZAttrProvisioning.A_zimbraSkinFavicon,
          ZAttrProvisioning.A_zimbraFeatureResetPasswordStatus,
          ZAttrProvisioning.A_carbonioWebUiDarkMode,
          ZAttrProvisioning.A_carbonioWebUiLoginLogo,
          ZAttrProvisioning.A_carbonioWebUiDarkLoginLogo,
          ZAttrProvisioning.A_carbonioWebUiLoginBackground,
          ZAttrProvisioning.A_carbonioWebUiDarkLoginBackground,
          ZAttrProvisioning.A_carbonioWebUiAppLogo,
          ZAttrProvisioning.A_carbonioWebUiDarkAppLogo,
          ZAttrProvisioning.A_carbonioWebUiFavicon,
          ZAttrProvisioning.A_carbonioWebUiTitle,
          ZAttrProvisioning.A_carbonioWebUiDescription,
          ZAttrProvisioning.A_carbonioAdminUiLoginLogo,
          ZAttrProvisioning.A_carbonioAdminUiDarkLoginLogo,
          ZAttrProvisioning.A_carbonioAdminUiAppLogo,
          ZAttrProvisioning.A_carbonioAdminUiDarkAppLogo,
          ZAttrProvisioning.A_carbonioAdminUiBackground,
          ZAttrProvisioning.A_carbonioAdminUiDarkBackground,
          ZAttrProvisioning.A_carbonioAdminUiFavicon,
          ZAttrProvisioning.A_carbonioAdminUiTitle,
          ZAttrProvisioning.A_carbonioAdminUiDescription);

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext lc = getZimbraSoapContext(context);
    Provisioning prov = Provisioning.getInstance();

    AuthToken at = lc.getAuthToken();
    boolean hasAuth = at != null;

    boolean applyConfig = request.getAttributeBool(AdminConstants.A_APPLY_CONFIG, true);
    Element d = request.getElement(AdminConstants.E_DOMAIN);
    String key = d.getAttribute(AdminConstants.A_BY);
    String value = d.getText();

    Key.DomainBy domainBy = Key.DomainBy.fromString(key);
    Domain domain = prov.getDomain(domainBy, value, true);
    Element response = lc.createElement(AdminConstants.GET_DOMAIN_INFO_RESPONSE);

    if (domain == null
        && domainBy != Key.DomainBy.name
        && domainBy != Key.DomainBy.virtualHostname) {
      // domain not found, and we don't have info for walking up sub domains
      // return attributes on global config
      toXML(response, prov.getConfig(), applyConfig, hasAuth);
    } else {
      /*
       * for all the attrs we can return (like login/logout URL), start stripping off
       * subdomains and checking the parent domain's settings. i.e., if a.b.com lookup fails,
       * try "b.com" and see if it has any settings defined; ultimately falling back to global.
       *
       * see if we can still find a domain.  We do this by
       *
       * 1. if by virtualHostname, see if we can find a domain by the name.  If we can, use that doamin.
       *
       * 2. if get(DomainBy.name) returns null using the supplied value, we walk up the sub-domains
       *    and see if we can find a domain by the name.
       *    e.g  if x.y.z.com was passed in, we check y.z.com, then z.com.
       *
       * 3. If still no domain found, return attributes on global config
       *
       */

      if (domain == null) {
        if (domainBy == Key.DomainBy.virtualHostname)
          domain = prov.getDomain(Key.DomainBy.name, value, true);

        if (domain == null) domain = findDomain(prov, value);
      }

      if (domain != null) toXML(response, domain, applyConfig, hasAuth);
      else toXML(response, prov.getConfig(), applyConfig, hasAuth);
    }

    return response;
  }

  private void addAttrElementIfNotNull(Entry entry, Element element, String key)
      throws ServiceException {
    if (AttributeManager.getInstance().isMultiValued(key)) {
      for (String value : entry.getMultiAttr(key)) {
        element
            .addNonUniqueElement(AdminConstants.E_A)
            .addAttribute(AdminConstants.A_N, key)
            .setText(value);
      }
    } else {
      String value = entry.getAttr(key, null);
      if (value != null) {
        element
            .addNonUniqueElement(AdminConstants.E_A)
            .addAttribute(AdminConstants.A_N, key)
            .setText(value);
      }
    }
  }

  private void toXML(Element e, Entry entry, boolean applyConfig, boolean hasAuth)
      throws ServiceException {
    Element domain = e.addElement(AdminConstants.E_DOMAIN);
    if (!hasAuth) {
      domain.addAttribute(AdminConstants.A_NAME, "VALUE-BLOCKED");
      domain.addAttribute(AdminConstants.A_ID, "VALUE-BLOCKED");
      if (entry != null) {
        for (String attr : bootstrapInfoAttrs) {
          addAttrElementIfNotNull(entry, domain, attr);
        }
      }
      return;
    }
    if (entry instanceof Domain) {
      Domain d = (Domain) entry;
      domain.addAttribute(AdminConstants.A_NAME, d.getUnicodeName());
      domain.addAttribute(AdminConstants.A_ID, d.getId());
    } else {
      // weird, need to populate name and id because client expects them to construct a Domain
      // object (but don't really use it)
      domain.addAttribute(AdminConstants.A_NAME, "globalconfig");
      domain.addAttribute(AdminConstants.A_ID, "globalconfig-dummy-id");
    }
    Set<String> attrList =
        AttributeManager.getInstance().getAttrsWithFlag(AttributeFlag.domainInfo);
    Map<String, Object> attrsMap = entry.getUnicodeAttrs(applyConfig);

    for (String name : attrList) {
      Object value = attrsMap.get(name);

      if (value instanceof String[]) {
        String[] sv = (String[]) value;
        for (String s : sv)
          domain.addElement(AdminConstants.E_A).addAttribute(AdminConstants.A_N, name).setText(s);
      } else if (value instanceof String)
        domain
            .addElement(AdminConstants.E_A)
            .addAttribute(AdminConstants.A_N, name)
            .setText((String) value);
    }
  }

  private static Domain findDomain(Provisioning prov, String value) throws ServiceException {
    Domain domain = null;

    int firstDotAt = value.indexOf('.');
    int secondDotAt = firstDotAt == -1 ? -1 : value.indexOf('.', firstDotAt + 1);

    // will do the get only if the remaining has at least two segments.
    // e.g will do z.com
    //     will not do com
    while (secondDotAt != -1) {
      domain = prov.getDomain(Key.DomainBy.name, value.substring(firstDotAt + 1), true);
      if (domain != null) break;
      else {
        firstDotAt = secondDotAt;
        secondDotAt = value.indexOf('.', firstDotAt + 1);
      }
    }

    return domain;
  }

  @Override
  public boolean needsAuth(Map<String, Object> context) {
    return false;
  }

  @Override
  public boolean needsAdminAuth(Map<String, Object> context) {
    // note that this handler does not require admin auth, but returns a limited subset of
    // information if auth is not present
    return false;
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    notes.add(AdminRightCheckPoint.Notes.ALLOW_ALL_ADMINS);
  }

  public static void main(String[] args) throws ServiceException {
    findDomain(Provisioning.getInstance(), "x.y.z.a.b.c");
    System.out.println();
  }
}
