// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.ArrayUtil;
import com.zimbra.common.util.ListUtil;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraHttpConnectionManager;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.soap.ZimbraSoapContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

/**
 * @author bburtin
 */
public class CheckSpelling extends MailDocumentHandler {

  private Log log = ZimbraLog.misc;

  private class ServerResponse {
    int statusCode;
    String content;
  }

  @Override
  public Element handle(Element request, Map<String, Object> context) throws ServiceException {
    ZimbraSoapContext zc = getZimbraSoapContext(context);
    Element response = zc.createElement(MailConstants.CHECK_SPELLING_RESPONSE);

    // Make sure a spell check server is specified
    Provisioning prov = Provisioning.getInstance();
    Server localServer = prov.getLocalServer();
    String[] urls = localServer.getMultiAttr(Provisioning.A_zimbraSpellCheckURL);
    if (ArrayUtil.isEmpty(urls)) {
      log.warn(
          "Unable to check spelling.  No value specified for %s",
          Provisioning.A_zimbraSpellCheckURL);
      return unavailable(response);
    }

    // Handle no content
    String text = request.getTextTrim();
    if (StringUtil.isNullOrEmpty(text)) {
      log.debug("<CheckSpellingRequest> was empty");
      response.addAttribute(MailConstants.A_AVAILABLE, true);
      return response;
    }

    // Get the dictionary name from either (1) the request,
    // (2) zimbraPrefSpellDictionary or (3) the account's locale.
    String dictionary = request.getAttribute(MailConstants.A_DICTIONARY, null);
    Account account = getRequestedAccount(zc);
    if (dictionary == null) {
      dictionary = account.getPrefSpellDictionary();
      if (dictionary == null) {
        dictionary = account.getLocale().toString();
      }
    }

    // Assemble the list of words to ignore from the account, domain, and COS.
    List<String> ignoreWords = new ArrayList<String>();
    addToList(ignoreWords, account.getPrefSpellIgnoreWord());
    addToList(ignoreWords, prov.getDomain(account).getPrefSpellIgnoreWord());
    addToList(ignoreWords, prov.getCOS(account).getPrefSpellIgnoreWord());

    String ignore = request.getAttribute(MailConstants.A_IGNORE, null);
    if (ignore != null) {
      addToList(ignoreWords, ignore.split("[\\s,]+"));
    }

    String ignorePattern = account.getPrefSpellIgnorePattern();

    // Get word list from one of the spell servers.
    ServerResponse spellResponse = null;
    for (int i = 0; i < urls.length; i++) {
      String url = urls[i];
      try {
        boolean ignoreAllCaps = account.isPrefSpellIgnoreAllCaps();
        log.debug(
            "Checking spelling: url=%s, dictionary=%s, text=%s, ignore=%s, ignoreAllCaps=%b",
            url, dictionary, text, ignoreWords, ignoreAllCaps);
        spellResponse = checkSpelling(url, dictionary, ignoreWords, text, ignoreAllCaps);
        if (spellResponse.statusCode == 200) {
          break; // Successful request.  No need to check the other servers.
        }
      } catch (IOException | HttpException ex) {
        ZimbraLog.mailbox.warn("An error occurred while contacting " + url, ex);
      }
    }

    // Check for errors
    if (spellResponse == null) {
      return unavailable(response);
    }
    if (spellResponse.statusCode != 200) {
      throw ServiceException.FAILURE("Spell check failed: " + spellResponse.content, null);
    }

    // Parse spell server response, assemble SOAP response
    if (spellResponse.content != null) {
      BufferedReader reader = new BufferedReader(new StringReader(spellResponse.content));
      String line = null;
      int numLines = 0;
      int numMisspelled = 0;
      try {
        while ((line = reader.readLine()) != null) {
          // Each line in the response has the format "werd:word,ward,weird"
          line = line.trim();
          numLines++;
          int colonPos = line.indexOf(':');

          if (colonPos > 0) {
            String word = line.substring(0, colonPos);
            String suggestions = line.substring(colonPos + 1, line.length());
            if (ignorePattern != null && word.matches(ignorePattern)) {
              continue;
            }
            Element wordEl = response.addElement(MailConstants.E_MISSPELLED);
            wordEl.addAttribute(MailConstants.A_WORD, word);
            wordEl.addAttribute(MailConstants.A_SUGGESTIONS, suggestions);
            numMisspelled++;
          }
        }
      } catch (IOException e) {
        log.warn("IOException checking spelling", e);
        return unavailable(response);
      }
      log.debug("CheckSpelling: found %d misspelled words in %d lines", numMisspelled, numLines);
    }

    response.addAttribute(MailConstants.A_AVAILABLE, true);
    return response;
  }

  private void addToList(List<String> list, String[] elements) {
    if (elements == null) {
      return;
    }
    for (String element : elements) {
      if (!StringUtil.isNullOrEmpty(element)) {
        list.add(element);
      }
    }
  }

  private ServerResponse checkSpelling(
      String url, String dictionary, List<String> ignoreWords, String text, boolean ignoreAllCaps)
      throws IOException, HttpException {
    HttpPost post = new HttpPost(url);
    post.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
    List<NameValuePair> nvps = new ArrayList<NameValuePair>();

    if (dictionary != null) {
      nvps.add(new BasicNameValuePair("dictionary", dictionary));
    }
    if (text != null) {
      nvps.add(new BasicNameValuePair("text", text));
    }
    if (!ListUtil.isEmpty(ignoreWords)) {
      nvps.add(new BasicNameValuePair("ignore", StringUtil.join(",", ignoreWords)));
    }
    if (ignoreAllCaps) {
      nvps.add(new BasicNameValuePair("ignoreAllCaps", "on"));
    }
    post.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
    HttpClient http = ZimbraHttpConnectionManager.getExternalHttpConnMgr().newHttpClient().build();
    ServerResponse response = new ServerResponse();
    HttpResponse httpResp = null;
    try {
      httpResp = HttpClientUtil.executeMethod(http, post);
      response.statusCode = httpResp.getStatusLine().getStatusCode();
      response.content = EntityUtils.toString(httpResp.getEntity());
    } finally {
      post.releaseConnection();
    }
    return response;
  }

  private Element unavailable(Element response) {
    response.addAttribute(MailConstants.A_AVAILABLE, false);
    return response;
  }
}
