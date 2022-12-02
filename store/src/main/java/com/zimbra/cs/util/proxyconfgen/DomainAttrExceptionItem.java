package com.zimbra.cs.util.proxyconfgen;

/**
 * The visit of LdapProvisioning can't throw the exception out. Therefore uses this special item to
 * indicate exception.
 *
 * @author jiankuan
 */
class DomainAttrExceptionItem extends DomainAttrItem {

  ProxyConfException exception;

  public DomainAttrExceptionItem(ProxyConfException e) {
    super(null, null, null, null, null, null, null, null);
    this.exception = e;
  }
}
