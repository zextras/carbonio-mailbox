package com.zimbra.cs.account;

import com.zimbra.common.service.ServiceException;
import java.util.Map;

/**
 * Defines the provisioning layer for operations on accounts, domain, COS and servers.
 *
 * @since 23.4.0
 * @author davidefrison
 */
public interface IProvisioning {

  /**
   * Get server where account resides.
   *
   * @param acct account
   * @return
   * @throws ServiceException
   */
  Server getServer(Account acct) throws ServiceException;

  /**
   * Get the account COS.
   *
   * @param acct account
   * @return class of service
   * @throws ServiceException
   */
  Cos getCOS(Account acct) throws ServiceException;

  /**
   * Get Default COS for a domain.
   *
   * @param domain
   * @return
   * @throws ServiceException
   */
  Cos getDefaultCOS(Domain domain) throws ServiceException;

  /**
   * Get email address from domain alias.
   *
   * @param domainAlias
   * @return email address
   * @throws ServiceException
   */
  String getEmailAddrByDomainAlias(String domainAlias) throws ServiceException;

  /**
   * Creates the specified account. The A_uid attribute is automatically created and should not be
   * passed in.
   *
   * <p>If A_zimbraId is passed in the attrs list, createAccount honors it if it is a valid uuid per
   * RFC 4122. It is caller's responsibility to ensure the uuid passed in is unique in the
   * namespace. createAccount does not check for uniqueness of the uuid passed in as an argument.
   *
   * <p>For example:
   *
   * <pre>
   * HashMap attrs  = new HashMap();
   * attrs.put(Provisioning.A_sn, "Schemers");
   * attrs.put(Provisioning.A_cn, "Roland Schemers");
   * attrs.put(Provisioning.A_zimbraMailStatus, Provisioning.MAIL_STATUS_ENABLED);
   * attrs.put(Provisioning.A_zimbraMailHost, "server1");
   * attrs.put(Provisioning.A_zimbraMailDeliveryAddress, "roland@tiiq.net");
   * prov.createAccount("roland@tiiq.net", "dsferulz", attrs);
   * </pre>
   *
   * @param emailAddress email address (domain must already exist) of account being created.
   * @param password password of account being created, or null. Account's without passwords can't
   *     be logged into.
   * @param attrs other initial attributes or <code>null</code>
   * @return
   * @throws ServiceException
   */
  Account createAccount(String emailAddress, String password, Map<String, Object> attrs)
      throws ServiceException;

  /**
   * Get a {@link Domain} by its name.
   *
   * @param name domain name in format domain.exmaple.com
   * @return domain
   * @throws ServiceException
   */
  Domain getDomainByName(String name) throws ServiceException;

  /**
   * Get domain by its id.
   *
   * @param id domain id
   * @return
   * @throws ServiceException
   */
  Domain getDomainById(String id) throws ServiceException;

  /**
   * Delete domain by id.
   *
   * @param id domain id
   * @throws ServiceException
   */
  void deleteDomain(String id) throws ServiceException;
}
