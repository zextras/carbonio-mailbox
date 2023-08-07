package com.zimbra.cs.util.proxyconfgen;

/**
 * Represents a domain attribute exception item, which is a specialized form of {@link
 * DomainAttrItem} that includes an associated exception.
 *
 * <p>The visit of {@link com.zimbra.cs.account.ldap.LdapProvisioning} can't throw the exception
 * out. Therefore, uses this special item to indicate exception.
 *
 * @author jiankuan, Keshav Bhatt
 */
class DomainAttrExceptionItem extends DomainAttrItem {

  private final ProxyConfException exception;

  /**
   * Constructs a new {@link DomainAttrExceptionItem} instance.
   *
   * @param builder The builder used to construct the parent DomainAttrItem instance.
   * @param exception The exception associated with this DomainAttrExceptionItem.
   */
  private DomainAttrExceptionItem(DomainAttrItem.Builder builder, ProxyConfException exception) {
    super(builder);
    this.exception = exception;
  }

  /**
   * Get the associated exception.
   *
   * @return The associated {@link ProxyConfException}.
   */
  public ProxyConfException getException() {
    return exception;
  }

  /** Builder class for constructing {@link DomainAttrExceptionItem} instances. */
  public static class Builder extends DomainAttrItem.Builder {

    /**
     * Creates a new Builder instance for constructing {@link DomainAttrExceptionItem} instances.
     *
     * @param domainName The domain name.
     * @param virtualHostname The virtual hostname.
     * @param virtualIPAddress The virtual IP address.
     */
    public Builder(String domainName, String virtualHostname, String virtualIPAddress) {
      super(domainName, virtualHostname, virtualIPAddress);
    }

    /**
     * Build a {@link DomainAttrExceptionItem} instance with an associated exception.
     *
     * @param exception The exception to associate with the item.
     * @return A new {@link DomainAttrExceptionItem} instance.
     */
    public DomainAttrExceptionItem buildWithException(ProxyConfException exception) {
      return new DomainAttrExceptionItem(this, exception);
    }
  }
}
