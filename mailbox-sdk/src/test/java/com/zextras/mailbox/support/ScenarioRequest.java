package com.zextras.mailbox.support;

public class ScenarioRequest extends Scenario {

  public ScenarioRequest(String serviceName) {
    super(serviceName);
  }

  /**
   * Creates the GetInfoRequest XML body substituting the auth token in the related placeholders.
   *
   * @param authToken is a {@links String} representing the auth token of the requester
   * @return a {@link String} representing the XML body request for the GetInfo API.
   */
  public String getInfo_AllSections(String authToken) {
    return getContent("getInfo_AllSections").replaceAll("%AUTH_TOKEN%", authToken);
  }

  /**
   * Creates the GetInfoRequest XML body substituting the auth token in the related placeholders.
   *
   * @param authToken is a {@links String} representing the auth token of the requester
   * @return a {@link String} representing the XML body request for the GetInfo API.
   */
  public String getInfo_SomeSections(String authToken) {
    return getContent("getInfo_SomeSections").replaceAll("%AUTH_TOKEN%", authToken);
  }

  /**
   * Creates the GetAccountInfoRequest XML body substituting the auth token and the account id in
   * the related placeholders.
   *
   * @param authToken is a {@links String} representing the auth token of the requester
   * @param accountId is a {@link String} representing the identifier of the account to retrieve
   * @return a {@link String} representing the XML body request for the GetAccountInfo API.
   */
  public String getAccountInfo_ById(String authToken, String accountId) {
    return getContent("getAccountInfo_ById")
        .replaceAll("%AUTH_TOKEN%", authToken)
        .replaceAll("%ACCOUNT_ID%", accountId);
  }

  /**
   * Creates the GetAccountInfoRequest XML body substituting the auth token and the account id in
   * the related placeholders.
   *
   * @param authToken is a {@links String} representing the auth token of the requester
   * @param accountEmail is a {@link String} representing the email of the account to retrieve
   * @return a {@link String} representing the XML body request for the GetAccountInfo API.
   */
  public String getAccountInfo_ByEmail(String authToken, String accountEmail) {
    return getContent("getAccountInfo_ByEmail")
        .replaceAll("%AUTH_TOKEN%", authToken)
        .replaceAll("%ACCOUNT_EMAIL%", accountEmail);
  }

  @Override
  protected String getType() {
    return "request";
  }
}
