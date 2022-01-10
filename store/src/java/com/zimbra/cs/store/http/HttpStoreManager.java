// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.http;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.common.httpclient.InputStreamRequestHttpRetryHandler;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.ZimbraHttpConnectionManager;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.service.UserServlet;
import com.zimbra.cs.store.external.ExternalStoreManager;

public abstract class HttpStoreManager extends ExternalStoreManager {

    protected abstract String getPostUrl(Mailbox mbox);
    protected abstract String getGetUrl(Mailbox mbox, String locator);
    protected abstract String getDeleteUrl(Mailbox mbox, String locator);
    protected abstract String getLocator(HttpPost post, String postDigest, long postSize, Mailbox mbox, HttpResponse httpResp) throws ServiceException, IOException;

    @Override
    public String writeStreamToStore(InputStream in, long actualSize, Mailbox mbox) throws IOException,
                    ServiceException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw ServiceException.FAILURE("SHA-256 digest not found", e);
        }
        ByteUtil.PositionInputStream pin = new ByteUtil.PositionInputStream(new DigestInputStream(in, digest));

        HttpClientBuilder clientBuilder = ZimbraHttpConnectionManager.getInternalHttpConnMgr().newHttpClient();
        clientBuilder.setRetryHandler(new InputStreamRequestHttpRetryHandler());
        HttpClient client = clientBuilder.build();

        HttpPost post = new HttpPost(getPostUrl(mbox));
        try {

            InputStreamEntity entity = new InputStreamEntity(pin, actualSize, ContentType.APPLICATION_OCTET_STREAM);
            post.setEntity(entity);
            HttpResponse httpResp = HttpClientUtil.executeMethod(client, post);
            int statusCode = httpResp.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_NO_CONTENT) {
                return getLocator(post, ByteUtil.encodeFSSafeBase64(digest.digest()), pin.getPosition(), mbox, httpResp);
            } else {
                throw ServiceException.FAILURE("error POSTing blob: " + httpResp.getStatusLine().getReasonPhrase(), null);
            }
        } catch (HttpException e) {
            throw new IOException("error POSTing blob: " + e.getMessage());
        } finally {
            post.releaseConnection();
        }
    }

    @Override
    public InputStream readStreamFromStore(String locator, Mailbox mbox)
                    throws IOException {
        HttpClient client = ZimbraHttpConnectionManager.getInternalHttpConnMgr().newHttpClient().build();
        HttpGet get = new HttpGet(getGetUrl(mbox, locator));
        HttpResponse httpResp;
        try {
            httpResp = HttpClientUtil.executeMethod(client, get);
            int statusCode = httpResp.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                return new UserServlet.HttpInputStream(httpResp);
            } else {
                get.releaseConnection();
                throw new IOException("unexpected return code during blob GET: " + httpResp.getStatusLine().getReasonPhrase());
            }
        } catch (HttpException e) {
             throw new IOException("unexpected return code during blob GET: " + e.getMessage());
        }

    }

    @Override
    public boolean deleteFromStore(String locator, Mailbox mbox)
                    throws IOException {
        HttpClient client = ZimbraHttpConnectionManager.getInternalHttpConnMgr().newHttpClient().build();
        HttpDelete delete = new HttpDelete(getDeleteUrl(mbox, locator));
        try {
            HttpResponse httpResp = HttpClientUtil.executeMethod(client, delete);
            int statusCode = httpResp.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_NO_CONTENT) {
                return true;
            } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                return false;
            } else {
                throw new IOException("unexpected return code during blob DELETE: " + httpResp.getStatusLine().getReasonPhrase());
            }
        } catch (HttpException e) {
            throw new IOException("unexpected return code during blob GET: " + e.getMessage());
        }finally {
            delete.releaseConnection();
        }
    }
}
