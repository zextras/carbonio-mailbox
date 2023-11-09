package com.zextras.mailbox.support;

public class ScenarioResponse extends Scenario {

  public ScenarioResponse(String serviceName) {
    super(serviceName);
  }

  /**
   * Creates the GetInfoResponse XML body substituting the input parameters in the related
   * placeholders.
   *
   * @param accountId is a {@link String} representing the identifier of the retrieved account
   * @param accountEmail is a {@link String} representing the email of the retrieved account
   * @param accountDomain is a {@link String} representing the domain of the retrieved account
   * @param accountName is a {@link String} representing the name of the retrieved account
   * @param accountLocale is a {@link String} representing the locale chosen by the retrieved
   *     account
   * @return a {@link String} representing the XML payload response for the GetInfo API.
   */
  public String getInfo_AllSections(
      String accountId,
      String accountEmail,
      String accountDomain,
      String accountName,
      String accountLocale) {
    String getInfoResponse = getContent("getInfo_AllSections");

    return getInfoResponse
        .replaceAll("%ACCOUNT_ID%", accountId)
        .replaceAll("%ACCOUNT_EMAIL%", accountEmail)
        .replaceAll("%ACCOUNT_DOMAIN%", accountDomain)
        .replaceAll("%ACCOUNT_NAME%", accountName)
        .replaceAll("%ACCOUNT_LOCALE%", accountLocale);
  }

  /**
   * Creates the GetInfoResponse XML body substituting the input parameters in the related
   * placeholders.
   *
   * @param accountId is a {@link String} representing the identifier of the retrieved account
   * @param accountEmail is a {@link String} representing the email of the retrieved account
   * @param accountDomain is a {@link String} representing the domain of the retrieved account
   * @param accountName is a {@link String} representing the name of the retrieved account
   * @return a {@link String} representing the XML payload response for the GetInfo API.
   */
  public String getInfo_SomeSections(
      String accountId, String accountEmail, String accountDomain, String accountName) {
    String getInfoResponse = getContent("getInfo_SomeSections");

    return getInfoResponse
        .replaceAll("%ACCOUNT_ID%", accountId)
        .replaceAll("%ACCOUNT_EMAIL%", accountEmail)
        .replaceAll("%ACCOUNT_DOMAIN%", accountDomain)
        .replaceAll("%ACCOUNT_NAME%", accountName);
  }

  /**
   * Creates the GetAccountInfoResponse XML body substituting the input parameters in the related
   * placeholders.
   *
   * @param accountId is a {@link String} representing the identifier of the retrieved account
   * @param accountEmail is a {@link String} representing the email of the retrieved account
   * @param accountDomain is a {@link String} representing the domain of the retrieved account
   * @param accountName is a {@link String} representing the name of the retrieved account
   * @return a {@link String} representing the XML payload response for the GetAccountInfo API.
   */
  public String getAccountInfo_ById(
      String accountId, String accountEmail, String accountDomain, String accountName) {
    return getContent("getAccountInfo_ById")
        .replaceAll("%ACCOUNT_ID%", accountId)
        .replaceAll("%ACCOUNT_EMAIL%", accountEmail)
        .replaceAll("%ACCOUNT_DOMAIN%", accountDomain)
        .replaceAll("%ACCOUNT_NAME%", accountName);
  }

  /**
   * Creates the GetAccountInfoResponse XML body substituting the input parameters in the related
   * placeholders.
   *
   * @param accountId is a {@link String} representing the identifier of the retrieved account
   * @param accountEmail is a {@link String} representing the email of the retrieved account
   * @param accountDomain is a {@link String} representing the domain of the retrieved account
   * @param accountName is a {@link String} representing the name of the retrieved account
   * @return a {@link String} representing the XML payload response for the GetAccountInfo API.
   */
  public String getAccountInfo_ByEmail(
      String accountId, String accountEmail, String accountDomain, String accountName) {
    return getContent("getAccountInfo_ByEmail")
        .replaceAll("%ACCOUNT_ID%", accountId)
        .replaceAll("%ACCOUNT_EMAIL%", accountEmail)
        .replaceAll("%ACCOUNT_DOMAIN%", accountDomain)
        .replaceAll("%ACCOUNT_NAME%", accountName);
  }

  @Override
  protected String getType() {
    return "response";
  }
}
