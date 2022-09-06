// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.store.triton;

import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.ZimbraHttpConnectionManager;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.store.Blob;
import com.zimbra.cs.store.BlobBuilder;
import com.zimbra.cs.store.external.ExternalResumableIncomingBlob;
import com.zimbra.cs.store.external.ExternalResumableOutputStream;
import com.zimbra.cs.store.triton.TritonBlobStoreManager.HashType;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.util.EntityUtils;

/**
 * IncomingBlob implementation which streams data directly to Triton using
 * TritonIncomingOutputStream
 */
public class TritonIncomingBlob extends ExternalResumableIncomingBlob {
  private final String baseUrl;
  private final MozyServerToken serverToken;
  private final TritonUploadUrl uploadUrl;
  private final HashType hashType;
  private final MessageDigest digest;
  private TritonIncomingOutputStream outStream;
  private AtomicLong written;

  public TritonIncomingBlob(
      String id,
      String baseUrl,
      BlobBuilder blobBuilder,
      Object ctx,
      MessageDigest digest,
      HashType hashType)
      throws ServiceException, IOException {
    super(id, blobBuilder, ctx);
    this.digest = digest;
    this.hashType = hashType;
    this.baseUrl = baseUrl;
    serverToken = new MozyServerToken();
    uploadUrl = new TritonUploadUrl();
    written = new AtomicLong(0);
  }

  @Override
  protected ExternalResumableOutputStream getAppendingOutputStream(BlobBuilder blobBuilder) {
    lastAccessTime = System.currentTimeMillis();
    outStream =
        new TritonIncomingOutputStream(
            blobBuilder, digest, hashType, baseUrl, uploadUrl, serverToken, written);
    return outStream;
  }

  @Override
  public Blob getBlob() throws IOException, ServiceException {
    if (outStream != null) {
      outStream.close();
    }
    String locator = Hex.encodeHexString((digest.digest()));
    return new TritonBlob(blobBuilder.finish(), locator, uploadUrl.toString(), serverToken);
  }

  @Override
  protected long getRemoteSize() throws IOException {
    outStream.flush();
    HttpClient client =
        ZimbraHttpConnectionManager.getInternalHttpConnMgr().newHttpClient().build();
    HttpHead head = new HttpHead(baseUrl + uploadUrl);
    ZimbraLog.store.info("heading %s", head.getURI());
    try {
      head.addHeader(TritonHeaders.SERVER_TOKEN, serverToken.getToken());
      HttpResponse httpResp = HttpClientUtil.executeMethod(client, head);
      int statusCode = httpResp.getStatusLine().getStatusCode();
      if (statusCode == HttpStatus.SC_OK) {
        String contentLength = httpResp.getFirstHeader(TritonHeaders.CONTENT_LENGTH).getValue();
        long remoteSize = -1;
        try {
          remoteSize = Long.valueOf(contentLength);
        } catch (NumberFormatException nfe) {
          throw new IOException("Content length can't be parsed to Long", nfe);
        }
        return remoteSize;
      } else {
        ZimbraLog.store.error(
            "failed with code %d response: %s",
            statusCode, EntityUtils.toString(httpResp.getEntity()));
        throw new IOException(
            "unable to head blob " + statusCode + ":" + httpResp.getStatusLine().getReasonPhrase(),
            null);
      }
    } catch (HttpException e) {
      throw new IOException("unexpected error during getremotesize() operation: " + e.getMessage());
    } finally {
      head.releaseConnection();
    }
  }
}
