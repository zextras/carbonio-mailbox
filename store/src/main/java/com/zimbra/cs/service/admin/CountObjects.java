// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.admin;

import com.google.common.collect.Lists;
import com.zimbra.common.account.Key;
import com.zimbra.common.localconfig.DebugConfig;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.AccountServiceException;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.UCService;
import com.zimbra.cs.account.accesscontrol.AdminRight;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.RightCommand;
import com.zimbra.cs.account.accesscontrol.Rights.Admin;
import com.zimbra.cs.account.accesscontrol.TargetType;
import com.zimbra.soap.ZimbraSoapContext;
import com.zimbra.soap.admin.message.CountObjectsRequest;
import com.zimbra.soap.admin.message.CountObjectsResponse;
import com.zimbra.soap.admin.type.CountObjectsType;
import com.zimbra.soap.admin.type.DomainSelector;
import com.zimbra.soap.admin.type.DomainSelector.DomainBy;
import com.zimbra.soap.admin.type.GranteeSelector;
import com.zimbra.soap.admin.type.UCServiceSelector;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class CountObjects extends AdminDocumentHandler {

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {

    ZimbraSoapContext zsc = getZimbraSoapContext(context);
    CountObjectsRequest req = zsc.elementToJaxb(request);
    CountObjectsType countObjectsType = req.getType();

    if (countObjectsType == null) {
      throw ServiceException.INVALID_REQUEST("No type specified", null);
    }
    Provisioning prov = Provisioning.getInstance();

    UCService ucService = null;
    UCServiceSelector ucserviceSelector = req.getUcService();
    if (null != ucserviceSelector) {
      if (!countObjectsType.allowsUCService()) {
        throw ServiceException.INVALID_REQUEST(
            "UCService cannot be specified for type: " + countObjectsType.name(), null);
      }
      String value = ucserviceSelector.getKey();
      ucService = prov.get(Key.UCServiceBy.fromString(ucserviceSelector.getBy().name()), value);
      if (ucService == null) {
        throw AccountServiceException.NO_SUCH_UC_SERVICE(value);
      }
    }

    List<DomainSelector> specifiedDomains = req.getDomains();
    if (!countObjectsType.allowsDomain() && !specifiedDomains.isEmpty()) {
      throw ServiceException.INVALID_REQUEST(
          "domain cannot be specified for type: " + countObjectsType.name(), null);
    }

    long count = 0;
    if (specifiedDomains.isEmpty()
        && !zsc.getAuthToken().isAdmin()
        && countObjectsType.allowsDomain()
        && !countObjectsType.equals(CountObjectsType.domain)) {
      // if a delegated admin is trying to count objects that exist within
      // a domain, count only within this admin's domains
      List<Domain> domains = prov.getAllDomains();
      AdminAccessControl aac = AdminAccessControl.getAdminAccessControl(zsc);
      AdminRight associatedRight = getAssociatedRight(countObjectsType);
      for (Iterator<Domain> it = domains.iterator(); it.hasNext(); ) {
        Domain domain = it.next();
        if (!aac.hasRight(domain, associatedRight)) {
          it.remove();
        }
      }
      count = 0;
      int threshold = DebugConfig.minimumDomainsToUseThreadsForDomainAdminCountObjects;
      if (threshold > 0 && domains.size() >= threshold) {
        // For a large number of domains, counting can be slow.  Do the LDAP queries in parallel.
        // As they all use different bases, they don't interfere with each other much.
        AtomicLong atomicCount = new AtomicLong(0);
        List<Thread> threads = Lists.newArrayList();
        final int chunkSize =
            (domains.size() / DebugConfig.numberOfThreadsToUseForDomainAdminCountObjects) + 1;
        int lastIndex = domains.size() - 1;
        int begin = 0;
        int end = (lastIndex < chunkSize) ? lastIndex : chunkSize - 1;
        while (end <= lastIndex) {
          threads.add(
              new Thread(
                  new GetDomainCountsThread(
                      atomicCount,
                      prov,
                      domains.subList(begin, end + 1),
                      countObjectsType,
                      ucService),
                  String.format(
                      "%s-CountsForDomains-%d", Thread.currentThread().getName(), threads.size())));
          if (end >= lastIndex) {
            break;
          }
          begin += chunkSize;
          end += chunkSize;
          if (end > lastIndex) {
            end = lastIndex;
          }
        }
        for (Thread thread : threads) {
          thread.start();
        }
        for (Thread thread : threads) {
          try {
            thread.join();
          } catch (InterruptedException e) {
            ZimbraLog.search.debug("Unexpected exception counting for domain", e);
          }
        }
        count = atomicCount.get();
      } else {
        for (Domain domain : domains) {
          count += prov.countObjects(countObjectsType, domain, ucService);
        }
      }
    } else if (!specifiedDomains.isEmpty() && countObjectsType.allowsDomain()) {
      // count objects within specified domains
      for (DomainSelector specifiedDomain : specifiedDomains) {
        DomainBy by = specifiedDomain.getBy();
        String domValue = specifiedDomain.getKey();
        Domain domain = prov.get(Key.DomainBy.fromString(by.name()), domValue);
        if (domain == null) {
          throw AccountServiceException.NO_SUCH_DOMAIN(domValue);
        }
        checkDomainRight(zsc, domain, getAssociatedRight(countObjectsType));
        count += prov.countObjects(countObjectsType, domain, ucService);
      }
    } else if (countObjectsType.equals(CountObjectsType.domain)
        && (zsc.getAuthToken().isDelegatedAdmin() || zsc.getAuthToken().isDomainAdmin())
        && req.getOnlyRelated()) {
      RightCommand.Grants grants =
          prov.getGrants(
              null,
              null,
              null,
              GranteeType.GT_USER.getCode(),
              GranteeSelector.GranteeBy.id,
              zsc.getAuthtokenAccountId(),
              false);
      if (grants != null) {
        Set<RightCommand.ACE> acEs = grants.getACEs();
        Set<String> domainIds = new HashSet<String>();
        for (RightCommand.ACE acE : acEs) {
          if (acE.targetType().equals(TargetType.domain.getCode())
              && !domainIds.contains(acE.targetId())) {
            count++;
            domainIds.add(acE.targetId());
          }
        }
      }

    } else {
      // count objects globally
      this.checkRight(zsc, context, null, getAssociatedRight(countObjectsType));
      count += prov.countObjects(countObjectsType, null, ucService);
    }

    return zsc.jaxbToElement(new CountObjectsResponse(count, countObjectsType.name()));
  }

  private class GetDomainCountsThread implements Runnable {

    private final AtomicLong atomicCount;
    private final Provisioning prov;
    private final List<Domain> domains;
    private final CountObjectsType countObjectsType;
    private final UCService ucService;

    private GetDomainCountsThread(
        AtomicLong atomicCount,
        Provisioning prov,
        List<Domain> domains,
        CountObjectsType countObjectsType,
        UCService ucService) {
      this.atomicCount = atomicCount;
      this.prov = prov;
      this.domains = domains;
      this.countObjectsType = countObjectsType;
      this.ucService = ucService;
    }

    @Override
    public void run() {
      for (Domain domain : domains) {
        try {
          atomicCount.addAndGet(prov.countObjects(countObjectsType, domain, ucService));
        } catch (ServiceException e) {
          ZimbraLog.search.debug("Problem counting %s for domain %s", countObjectsType, domain, e);
        }
      }
    }
  }

  @Override
  public void docRights(List<AdminRight> relatedRights, List<String> notes) {
    relatedRights.add(Admin.R_countAccount);
    relatedRights.add(Admin.R_countAlias);
    relatedRights.add(Admin.R_countDomain);
    relatedRights.add(Admin.R_countDistributionList);
    relatedRights.add(Admin.R_countCos);
    relatedRights.add(Admin.R_countServer);
    relatedRights.add(Admin.R_countCalendarResource);
  }

  private AdminRight getAssociatedRight(CountObjectsType countObjectsType) {
    switch (countObjectsType) {
      case account:
      case accountOnUCService:
      case internalArchivingAccount:
      case internalUserAccount:
      case userAccount:
        return Admin.R_countAccount;
      case alias:
        return Admin.R_countAlias;
      case calresource:
        return Admin.R_countCalendarResource;
      case cos:
      case cosOnUCService:
        return Admin.R_countCos;
      case dl:
        return Admin.R_countDistributionList;
      case domain:
      case domainOnUCService:
        return Admin.R_countDomain;
      case server:
        return Admin.R_countServer;
    }
    return null; // shouldn't be possible
  }
}
