// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.service;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.common.mime.ContentDisposition;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraHttpConnectionManager;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.httpclient.HttpProxyUtil;
import com.zimbra.cs.util.AccountUtil;
import io.vavr.control.Try;
import java.io.InputStream;
import javax.activation.DataHandler;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimePart;
import javax.mail.util.ByteArrayDataSource;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

/** Gets an attachment from Mailbox SOAP */
public class MailboxSOAPAttachmentService implements AttachmentService {

  // TODO: add mailbox client as field

  @Override
  public Try<MimePart> getAttachment(
      String accountId, AuthToken token, int messageId, String part) {
    return Try.of(
        () -> {
          HttpClientBuilder clientBuilder =
              ZimbraHttpConnectionManager.getInternalHttpConnMgr().newHttpClient();
          HttpProxyUtil.configureProxy(clientBuilder);
          HttpGet getRequest =
              new HttpGet(getContentServletResourceUrl(token, String.valueOf(messageId), part));
          HttpClient client = encodeClientBuilderRequest(token, clientBuilder, getRequest).get();
          HttpResponse httpResp = HttpClientUtil.executeMethod(client, getRequest);
          int statusCode = httpResp.getStatusLine().getStatusCode();
          if (statusCode != org.apache.http.HttpStatus.SC_OK) {
            throw ServiceException.FAILURE("Cannot get attachment", null);
          } else {
            Header cdHeader = httpResp.getFirstHeader("Content-Disposition");
            String filename =
                cdHeader == null
                    ? "unknown"
                    : new ContentDisposition(cdHeader.getValue()).getParameter("filename");

            final MimePart attachment = new MimeBodyPart();
            final InputStream content = httpResp.getEntity().getContent();
            ByteArrayDataSource bds = new ByteArrayDataSource(content, filename);
            attachment.setDataHandler(new DataHandler(bds));
            return attachment;
          }
        });
  }

  /**
   * Return the rest resource Url for content servlet
   *
   * @param authToken the {@link AuthToken} passed in request
   * @param messageId {@link String} the messageId that we want to get attachment from
   * @param part {@link String} the part number of the attachment in email
   * @return Try of attachment's URL {@link String} for content servlet
   */
  static String getContentServletResourceUrl(AuthToken authToken, String messageId, String part) {

    return Try.of(
            () -> {
              final Account account = authToken.getAccount();
              final String baseUrl = AccountUtil.getBaseUri(authToken.getAccount());
              final String restUrl =
                  UserServlet.getRestUrl(account) + "?auth=co&id=" + messageId + "&part=" + part;
              return baseUrl == null
                  ? restUrl
                  : baseUrl + UserServlet.SERVLET_PATH + restUrl.split(UserServlet.SERVLET_PATH)[1];
            })
        .get();
  }

  /**
   * Adds request configuration to client builder
   *
   * @param authToken the {@link AuthToken} passed in request
   * @param clientBuilder the {@link HttpClientBuilder} object
   * @param getRequest the {@link HttpServletRequest} that has to be encoded
   * @return Try of encoded {@link CloseableHttpClient} object
   */
  private Try<CloseableHttpClient> encodeClientBuilderRequest(
      AuthToken authToken, HttpClientBuilder clientBuilder, HttpGet getRequest) {

    return Try.of(
        () -> {
          authToken.encode(clientBuilder, getRequest, false, getMailHostUrl(authToken));
          return clientBuilder.build();
        });
  }

  /**
   * @param authToken {@link AuthToken} authToken object
   * @return the mailHostUrl {@link String}
   */
  private String getMailHostUrl(AuthToken authToken) {
    return Try.of(authToken::getAccount)
        .mapTry(account -> account.getAttr(ZAttrProvisioning.A_zimbraMailHost))
        .get();
  }
}
