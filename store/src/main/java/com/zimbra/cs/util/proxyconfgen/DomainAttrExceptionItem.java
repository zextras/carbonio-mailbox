package com.zimbra.cs.util.proxyconfgen;

/**
 * Represents a domain attribute exception item, which is a specialized form of {@link
 * DomainAttrItem} that includes an associated exception.
 *
 * <p>The visit of {@link com.zimbra.cs.account.ldap.LdapProvisioning} can't throw the exception
 * out. Therefore, uses this special item to indicate exception.
 *
 * @author jiankuan
 */
class DomainAttrExceptionItem extends DomainAttrItem {

  private final ProxyConfException exception;

  /**
   * Constructs a new {@link DomainAttrExceptionItem} instance.
   *
   * @param exception The exception associated with this DomainAttrExceptionItem.
   */
  public DomainAttrExceptionItem(ProxyConfException exception) {
    super(null, null, null);
    this.exception = exception;
  }

  ProxyConfException getException() {
    return exception;
  }
}
