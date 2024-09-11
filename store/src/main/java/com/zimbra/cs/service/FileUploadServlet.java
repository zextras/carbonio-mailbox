// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.zimbra.client.ZMailbox;
import com.zimbra.common.account.Key;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.httpclient.HttpClientUtil;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.mime.ContentDisposition;
import com.zimbra.common.mime.ContentType;
import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.mime.MimeDetect;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.service.ServiceException.Argument;
import com.zimbra.common.service.ServiceException.InternalArgument;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.util.ByteUtil;
import com.zimbra.common.util.Constants;
import com.zimbra.common.util.FileUtil;
import com.zimbra.common.util.Log;
import com.zimbra.common.util.LogFactory;
import com.zimbra.common.util.MapUtil;
import com.zimbra.common.util.StringUtil;
import com.zimbra.common.util.ZimbraHttpConnectionManager;
import com.zimbra.common.util.ZimbraLog;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.Server;
import com.zimbra.cs.ldap.LdapUtil;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.service.util.ContentDispositionParser;
import com.zimbra.cs.service.util.JWTUtil;
import com.zimbra.cs.servlet.CsrfFilter;
import com.zimbra.cs.servlet.ZimbraServlet;
import com.zimbra.cs.servlet.util.CsrfUtil;
import com.zimbra.cs.store.BlobInputStream;
import com.zimbra.cs.util.AccountUtil;
import com.zimbra.cs.util.Zimbra;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.mail.util.SharedByteArrayInputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.DefaultFileItem;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public class FileUploadServlet extends ZimbraServlet {

  /** The character separating upload IDs in a list */
  public static final String UPLOAD_DELIMITER = ",";

  // If this query param is present in the URI, upload size is limited by zimbraFileUploadMaxSizePerFile,
  // This allows end user to upload large attachments ignoring the zimbraMtaMaxMessageSize.
  protected static final String PARAM_LIMIT_BY_FILE_UPLOAD_MAX_SIZE_PER_FILE = "lbfums";

  protected static final String PARAM_CSRF_TOKEN = "csrfToken";
  static final long DEFAULT_MAX_SIZE = 10L * 1024 * 1024;

  /** Uploads time out after 15 minutes. */
  static final long UPLOAD_TIMEOUT_MSEC = 15 * Constants.MILLIS_PER_MINUTE;

  static final HashMap<String, Upload> mPending = new HashMap<>(100);
  private static final long serialVersionUID = -3156986245375108467L;

  /** The character separating server ID from upload ID */
  private static final String UPLOAD_PART_DELIMITER = ":";

  /** Purge uploads once every minute. */
  private static final long REAPER_INTERVAL_MSEC = Constants.MILLIS_PER_MINUTE;

  static Map<String, String> mProxiedUploadIds = MapUtil.newLruMap(100);
  static Log mLog = LogFactory.getLog(FileUploadServlet.class);
  private static String sUploadDir;

  /**
   * Returns the zimbra id of the server the specified upload resides on.
   *
   * @param uploadId The id of the upload.
   * @throws ServiceException if the upload id is malformed.
   */
  static String getUploadServerId(String uploadId) throws ServiceException {
    if (uploadId == null || uploadId.split(UPLOAD_PART_DELIMITER).length != 2) {
      throw ServiceException.INVALID_REQUEST("invalid upload ID: " + uploadId, null);
    }

    return uploadId.split(UPLOAD_PART_DELIMITER)[0];
  }

  /**
   * Returns whether the specified upload resides on this server.
   *
   * @param uploadId The id of the upload.
   * @throws ServiceException if the upload id is malformed or if there is an error accessing LDAP.
   */
  static boolean isLocalUpload(String uploadId) throws ServiceException {
    String serverId = getUploadServerId(uploadId);
    return Provisioning.getInstance().getLocalServer().getId().equals(serverId);
  }

  public static Upload fetchUpload(String accountId, String uploadId, AuthToken authtoken)
      throws ServiceException {
    mLog.debug("Fetching upload %s for account %s", uploadId, accountId);
    String context = "accountId=" + accountId + ", uploadId=" + uploadId;
    if (accountId == null || uploadId == null) {
      throw ServiceException.FAILURE("fetchUploads(): missing parameter: " + context, null);
    }

    // if the upload is remote, fetch it from the other server
    if (!isLocalUpload(uploadId)) {
      return fetchRemoteUpload(accountId, uploadId, authtoken);
    }

    // the upload is local, so get it from the cache
    synchronized (mPending) {
      Upload up = mPending.get(uploadId);
      if (up == null) {
        mLog.warn("upload not found: " + context);
        throw MailServiceException.NO_SUCH_UPLOAD(uploadId);
      }
      if (!accountId.equals(up.accountId)) {
        mLog.warn("mismatched accountId for upload: " + up + "; expected: " + context);
        throw MailServiceException.NO_SUCH_UPLOAD(uploadId);
      }
      up.time = System.currentTimeMillis();
      mLog.debug("fetchUpload() returning %s", up);
      return up;
    }
  }

  private static Upload fetchRemoteUpload(String accountId, String uploadId, AuthToken authtoken)
      throws ServiceException {
    // check if we have fetched the Upload from the remote server previously
    String localUploadId = null;
    synchronized (mProxiedUploadIds) {
      localUploadId = mProxiedUploadIds.get(uploadId);
    }
    if (localUploadId != null) {
      synchronized (mPending) {
        Upload up = mPending.get(localUploadId);
        if (up != null) {
          return up;
        }
      }
    }
    // the first half of the upload id is the server id where it lives
    Server server = Provisioning.getInstance().get(Key.ServerBy.id, getUploadServerId(uploadId));
    String url = AccountUtil.getBaseUri(server);
    if (url == null) {
      return null;
    }
    String hostname = server.getServiceHostname();
    url +=
        ContentServlet.SERVLET_PATH
            + ContentServlet.PREFIX_PROXY
            + '?'
            + ContentServlet.PARAM_UPLOAD_ID
            + '='
            + uploadId
            + '&'
            + ContentServlet.PARAM_EXPUNGE
            + "=true";

    // create an HTTP client with auth cookie to fetch the file from the remote ContentServlet
    HttpClientBuilder clientBuilder =
        ZimbraHttpConnectionManager.getInternalHttpConnMgr().newHttpClient();
    HttpGet get = new HttpGet(url);

    authtoken.encode(clientBuilder, get, false, hostname);
    HttpClient client = clientBuilder.build();
    try {
      // fetch the remote item
      HttpResponse httpResp = HttpClientUtil.executeMethod(client, get);
      int statusCode = httpResp.getStatusLine().getStatusCode();
      if (statusCode != HttpStatus.SC_OK) {
        return null;
      }

      // metadata is encoded in the response's HTTP headers
      Header ctHeader = httpResp.getFirstHeader("Content-Type");
      String contentType = ctHeader == null ? "text/plain" : ctHeader.getValue();
      Header cdispHeader = httpResp.getFirstHeader("Content-Disposition");
      String filename =
          cdispHeader == null
              ? "unknown"
              : new ContentDisposition(cdispHeader.getValue()).getParameter("filename");

      // store the fetched upload along with original uploadId
      Upload up = saveUpload(httpResp.getEntity().getContent(), filename, contentType, accountId);
      synchronized (mProxiedUploadIds) {
        mProxiedUploadIds.put(uploadId, up.uuid);
      }
      return up;
    } catch (HttpException e) {
      throw ServiceException.PROXY_ERROR(e, url);
    } catch (IOException e) {
      throw ServiceException.RESOURCE_UNREACHABLE(
          "can't fetch remote upload",
          e,
          new InternalArgument(ServiceException.URL, url, Argument.Type.STR));
    } finally {
      get.releaseConnection();
    }
  }

  public static Upload saveUpload(
      InputStream is,
      String filename,
      String contentType,
      String accountId,
      boolean limitByFileUploadMaxSize)
      throws ServiceException, IOException {
    return saveUpload(
        is, filename, contentType, accountId, getFileUploadMaxSize(limitByFileUploadMaxSize));
  }

  private static Upload saveUpload(
      InputStream is, String filename, String contentType, String accountId, long limit)
      throws ServiceException, IOException {
    FileItem fi = null;
    boolean success = false;
    try {
      // store the fetched file as a normal upload
      ServletFileUpload upload = getUploader(limit);
      long sizeMax = upload.getSizeMax();
      fi = upload.getFileItemFactory().createItem("upload", contentType, false, filename);
      // sizeMax=-1 means "no limit"
      long size =
          ByteUtil.copy(is, true, fi.getOutputStream(), true, sizeMax < 0 ? sizeMax : sizeMax + 1);
      if (upload.getSizeMax() >= 0 && size > upload.getSizeMax()) {
        mLog.warn("Exceeded maximum upload size of %s bytes", upload.getSizeMax());
        throw MailServiceException.UPLOAD_TOO_LARGE(filename, "upload too large");
      }

      Upload up = new Upload(accountId, fi);
      mLog.info("saveUpload(): received %s", up);
      synchronized (mPending) {
        mPending.put(up.uuid, up);
      }
      success = true;
      return up;
    } finally {
      if (!success && fi != null) {
        mLog.debug("saveUpload(): unsuccessful attempt.  Deleting %s", fi);
        fi.delete();
      }
    }
  }

  public static Upload saveUploadForSmartLink(
      InputStream is, String filename, String contentType, String accountId)
      throws ServiceException, IOException {
    return saveUpload(is, filename, contentType, accountId, true);
  }

  public static Upload saveUpload(
      InputStream is, String filename, String contentType, String accountId)
      throws ServiceException, IOException {
    return saveUpload(is, filename, contentType, accountId, false);
  }

  static File getStoreLocation(FileItem fi) {
    if (fi.isInMemory() || !(fi instanceof DiskFileItem)) {
      return null;
    }
    return ((DiskFileItem) fi).getStoreLocation();
  }

  public static void deleteUploads(Collection<Upload> uploads) {
    if (uploads != null && !uploads.isEmpty()) {
      for (Upload up : uploads) {
        deleteUpload(up);
      }
    }
  }

  public static void deleteUpload(Upload upload) {
    if (upload == null) {
      return;
    }
    Upload up;
    synchronized (mPending) {
      mLog.debug("deleteUpload(): removing %s", upload);
      up = mPending.remove(upload.uuid);
      if (up != null) {
        up.markDeleted();
      }
    }
    if (up == upload) {
      up.purge();
    }
  }

  protected static String getUploadDir() {
    if (sUploadDir == null) {
      sUploadDir = LC.zimbra_tmp_directory.value() + "/upload";
    }
    return sUploadDir;
  }

  private static void cleanupLeftoverTempFiles() {
    File[] files = new File(getUploadDir()).listFiles(new TempFileFilter());
    if (files == null || files.length < 1) {
      return;
    }

    mLog.info("deleting %d temporary upload files left over from last time", files.length);
    for (File file : files) {
      String path = file.getAbsolutePath();
      if (file.delete()) {
        mLog.info("deleted leftover upload file %s", path);
      } else {
        mLog.error("unable to delete leftover upload file %s", path);
      }
    }
  }

  public static void sendResponse(
      HttpServletResponse resp,
      int status,
      String fmt,
      String reqId,
      List<Upload> uploads,
      List<FileItem> items)
      throws IOException {
    boolean raw = false, extended = false;
    if (fmt != null && !fmt.trim().equals("")) {
      // parse out the comma-separated "fmt" options
      for (String foption : fmt.toLowerCase().split(",")) {
        raw |= ContentServlet.FORMAT_RAW.equals(foption);
        extended |= "extended".equals(foption);
      }
    }

    StringBuffer results = new StringBuffer();
    results
        .append(status)
        .append(",'")
        .append(reqId != null ? StringUtil.jsEncode(reqId) : "null")
        .append('\'');
    if (status == HttpServletResponse.SC_OK) {
      boolean first = true;
      if (extended) {
        // serialize as a list of JSON objects, one per upload
        results.append(",[");
        for (Upload up : uploads) {
          Element.JSONElement elt = new Element.JSONElement("ignored");
          elt.addAttribute(MailConstants.A_ATTACHMENT_ID, up.uuid);
          elt.addAttribute(MailConstants.A_CONTENT_TYPE, up.getContentType());
          elt.addAttribute(MailConstants.A_CONTENT_FILENAME, up.name);
          elt.addAttribute(MailConstants.A_SIZE, up.getSize());
          results.append(first ? "" : ",").append(elt);
          first = false;
        }
        results.append(']');
      } else {
        // serialize as a string containing the comma-separated upload IDs
        results.append(",'");
        for (Upload up : uploads) {
          results.append(first ? "" : UPLOAD_DELIMITER).append(up.uuid);
          first = false;
        }
        results.append('\'');
      }
    }

    resp.setContentType("text/html; charset=utf-8");
    PrintWriter out = resp.getWriter();

    if (raw) {
      out.println(results);
    } else {
      out.println(
          "<html><head><script language='javascript'>\n"
              + "function doit() { window.parent._uploadManager.loaded("
              + results
              + "); }\n</script>"
              + "</head><body onload='doit()'></body></html>\n");
    }
    out.close();

    // handle failure by cleaning up the failed upload
    if (status != HttpServletResponse.SC_OK && items != null && items.size() > 0) {
      for (FileItem fi : items) {
        mLog.debug("sendResponse(): deleting %s", fi);
        fi.delete();
      }
    }
  }

  /**
   * Reads the end of the client request when an error occurs, to avoid cases where the client
   * blocks when writing the HTTP request.
   */
  public static void drainRequestStream(HttpServletRequest req) {
    try {
      InputStream in = req.getInputStream();
      byte[] buf = new byte[1024];
      int numRead = 0;
      int totalRead = 0;
      mLog.debug("Draining request input stream");
      while ((numRead = in.read(buf)) >= 0) {
        totalRead += numRead;
      }
      mLog.debug("Drained %d bytes", totalRead);
    } catch (IOException e) {
      mLog.info("Ignoring error that occurred while reading the end of the client request: " + e);
    }
  }

  private static long getFileUploadMaxSize(boolean limitByFileUploadMaxSize) {
    return getFileUploadMaxSize(null, limitByFileUploadMaxSize);
  }

  private static long getFileUploadMaxSize(Account account, boolean limitByFileUploadMaxSize) {
    // look up the maximum file size for uploads
    long maxSize = DEFAULT_MAX_SIZE;
    try {
      if (limitByFileUploadMaxSize) {
        if(account != null){
          maxSize = account.getFileUploadMaxSizePerFile();
        }else{
          maxSize = Provisioning.getInstance().getConfig().getFileUploadMaxSizePerFile();
        }
      } else {
        maxSize = Provisioning.getInstance().getConfig().getMtaMaxMessageSize();
      }
      if (maxSize == 0) {
        /* zimbraFileUploadMaxSizePerFile=0 & zimbraMtaMaxMessageSize=0 means "no limit".  The return value from this function gets used
         * by FileUploadBase "sizeMax" where "-1" means "no limit"
         */
        maxSize = -1;
      }
    } catch (ServiceException e) {
      mLog.error(
          "Unable to read "
              + ((limitByFileUploadMaxSize)
                  ? ZAttrProvisioning.A_zimbraFileUploadMaxSizePerFile
                  : ZAttrProvisioning.A_zimbraMtaMaxMessageSize)
              + " attribute",
          e);
    }
    return maxSize;
  }

  private static ServletFileUpload getUploader(Account account, boolean limitByFileUploadMaxSize) {
    return getUploader(getFileUploadMaxSize(account, limitByFileUploadMaxSize));
  }

  public static ServletFileUpload getUploader(long maxSize) {
    DiskFileItemFactory dfif = new DiskFileItemFactory();
    dfif.setSizeThreshold(32 * 1024);
    dfif.setRepository(new File(getUploadDir()));
    ServletFileUpload upload = new ServletFileUpload(dfif);
    upload.setSizeMax(maxSize);
    upload.setHeaderEncoding("utf-8");
    return upload;
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, ServletException {
    ZimbraLog.clearContext();
    addRemoteIpToLoggingContext(req);

    String fmt = req.getParameter(ContentServlet.PARAM_FORMAT);

    ZimbraLog.addUserAgentToContext(req.getHeader("User-Agent"));

    // file upload requires authentication
    boolean isAdminRequest = false;
    try {
      isAdminRequest = isAdminRequest(req);
    } catch (ServiceException e) {
      drainRequestStream(req);
      throw new ServletException(e);
    }

    AuthToken at =
        isAdminRequest
            ? getAdminAuthTokenFromCookie(req, resp, true)
            : getAuthTokenFromCookie(req, resp, true);
    if (at == null) {
      mLog.info(
          "Auth token not present.  Returning %d response.", HttpServletResponse.SC_UNAUTHORIZED);
      drainRequestStream(req);
      sendResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, fmt, null, null, null);
      return;
    }

    boolean doCsrfCheck = false;
    boolean csrfCheckComplete = false;
    if (req.getAttribute(CsrfFilter.CSRF_TOKEN_CHECK) != null) {
      doCsrfCheck = (Boolean) req.getAttribute(CsrfFilter.CSRF_TOKEN_CHECK);
    }

    if (JWTUtil.isJWT(at)) {
      doCsrfCheck = false;
    }

    if (doCsrfCheck) {
      String csrfToken = req.getHeader(Constants.CSRF_TOKEN);

      // Bug: 96344
      if (!StringUtil.isNullOrEmpty(csrfToken)) {
        if (!CsrfUtil.isValidCsrfToken(csrfToken, at)) {

          drainRequestStream(req);
          mLog.info(
              "CSRF token validation failed for account: %s"
                  + ", Auth token is CSRF enabled: %s"
                  + ". CSRF token is: %s",
              at, at.isCsrfTokenEnabled(), csrfToken);
          sendResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, fmt, null, null, null);
          return;
        }
        csrfCheckComplete = true;
      } else {
        if (at.isCsrfTokenEnabled()) {
          csrfCheckComplete = false;
          mLog.debug(
              "CSRF token was not found in the header. Auth token is %s, it is CSRF enabled:  %s,"
                  + " will check if sent in form field.",
              at, at.isCsrfTokenEnabled());
        }
      }
    } else {
      csrfCheckComplete = true;
    }

    try {
      Provisioning prov = Provisioning.getInstance();
      Account acct = AuthProvider.validateAuthToken(prov, at, true);
      if (!isAdminRequest) {
        // fetching the mailbox will except if it's in maintenance mode
        if (Provisioning.getInstance().onLocalServer(acct)) {
          Mailbox mbox = MailboxManager.getInstance().getMailboxByAccount(acct, false);
          if (mbox != null) {
            ZimbraLog.addMboxToContext(mbox.getId());
          }
        }
      }

      boolean limitByFileUploadMaxSizePerFile =
          req.getParameter(PARAM_LIMIT_BY_FILE_UPLOAD_MAX_SIZE_PER_FILE) != null;

      // file upload requires multipart enctype
      if (ServletFileUpload.isMultipartContent(req)) {
        handleMultipartUpload(
            req, resp, fmt, acct, limitByFileUploadMaxSizePerFile, at, csrfCheckComplete);
      } else {
        if (!csrfCheckComplete) {
          drainRequestStream(req);
          mLog.info("CSRF token validation failed for account: %s.No csrf token recd.", acct);
          sendResponse(resp, HttpServletResponse.SC_UNAUTHORIZED, fmt, null, null, null);
        } else {
          handlePlainUpload(req, resp, fmt, acct, limitByFileUploadMaxSizePerFile);
        }
      }
    } catch (ServiceException e) {
      mLog.info("File upload failed", e);
      drainRequestStream(req);
      returnError(resp, e);
    } catch (IOException e) {
      mLog.info("File upload failed", e);
      drainRequestStream(req);
      returnError(
          resp, ServiceException.FAILURE("An IO exception occurred while processing file upload."));
    }
  }

  /**
   * Handles HTTP POST request which contains multipart upload items.
   *
   * @param request {@link HttpServletRequest} instance
   * @param response {@link HttpServletResponse} instance
   * @param format {@link String} format
   * @param account {@link Account} account
   * @param limitByFileUploadMaxSize whether to limit the file upload by max size
   * @param authToken {@link AuthToken} auth token
   * @param csrfCheckComplete whether to check the CSRF token
   * @return {@link List} of {@link Upload} items
   * @throws IOException if an I/O error occurs
   * @throws ServiceException if an error occurs processing upload request
   */
  List<Upload> handleMultipartUpload(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final String format,
      final Account account,
      final boolean limitByFileUploadMaxSize,
      final AuthToken authToken,
      final boolean csrfCheckComplete)
      throws IOException, ServiceException {

    final List<FileItem> fileItems =
        getFileItemsFromMultipartUploadRequest(
            request,
            response,
            format,
            account,
            limitByFileUploadMaxSize,
            authToken,
            csrfCheckComplete);

    if (fileItems.isEmpty()) {
      mLog.info("No data in upload for reqId: %s", request);
      sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, format, null, null, fileItems);
    } else {
      final String requestId = extractRequestIdFromFormField(fileItems);
      final Map<FileItem, String> fileNames = extractFileNamesFromFormFields(fileItems);
      final List<Upload> uploads = cacheUploadedFiles(fileItems, account, fileNames);
      sendResponse(response, HttpServletResponse.SC_OK, format, requestId, uploads, fileItems);
      return uploads;
    }
    return Collections.emptyList();
  }

  /**
   * Extract the requestId form field if present in one of the {@link FileItem}, return null if not
   * present.
   *
   * @param fileItems {@link List} of {@link FileItem}
   * @return {@link String} requestId
   */
  private String extractRequestIdFromFormField(final List<FileItem> fileItems) {
    for (FileItem fileItem : fileItems) {
      if (fileItem == null) {
        continue;
      }
      if (fileItem.isFormField() && "requestId".equals(fileItem.getFieldName())) {
        return fileItem.getString();
      }
    }
    return null;
  }

  /**
   * Extract file names from form fields if present and map them to the corresponding {@link
   * FileItem}, the extraction supports: explicitly provide filenames in form fields and file name
   * provided in Content-Disposition header.
   *
   * @param fileItems {@link List} of {@link FileItem} to be used for extracting file names
   * @return {@link Map} of file names to corresponding {@link FileItem}
   * @throws UnsupportedEncodingException if specified encoding using <code>_charset_</code> form
   *     field is unsupported
   */
  private Map<FileItem, String> extractFileNamesFromFormFields(final List<FileItem> fileItems)
      throws UnsupportedEncodingException {

    String suggestedCharset = StandardCharsets.UTF_8.toString();
    final LinkedList<String> names = new LinkedList<>();
    final HashMap<FileItem, String> fileNames = new HashMap<>();

    final Iterator<FileItem> fileItemIterator = fileItems.iterator();
    while (fileItemIterator.hasNext()) {
      final FileItem fileItem = fileItemIterator.next();
      if (fileItem == null) {
        continue;
      }

      if (fileItem.isFormField()) {
        if ("_charset_".equals(fileItem.getFieldName()) && !fileItem.getString().isEmpty()) {
          suggestedCharset = fileItem.getString();
        } else if (fileItem.getFieldName().startsWith("filename")) {
          names.clear();
          final String value = fileItem.getString(suggestedCharset);
          if (!Strings.isNullOrEmpty(value)) {
            Collections.addAll(
                names, Arrays.stream(value.split("\n")).map(String::trim).toArray(String[]::new));
          }
        }
        fileItemIterator.remove();
      } else {
        if (fileItem.getName() == null || fileItem.getName().trim().equals("")) {
          fileItemIterator.remove();
        } else if (!names.isEmpty()) {
          fileNames.put(fileItem, names.remove());
        } else {
          fileNames.put(
              fileItem,
              ContentDispositionParser.getFileNameFromContentDisposition(
                  fileItem.getHeaders().getHeader("content-disposition")));
        }
        names.clear();
      }
    }
    return fileNames;
  }

  /**
   * Parses {@link List} of {@link FileItem} from HTTP multipart upload request.
   *
   * @param request {@link HttpServletRequest} instance
   * @param response {@link HttpServletResponse} instance
   * @param format {@link String} format
   * @param account {@link Account} account
   * @param limitByFileUploadMaxSize whether to limit the file upload by max size
   * @param authToken {@link AuthToken} auth token
   * @param csrfCheckComplete whether to check the CSRF token
   * @return {@link List} of {@link FileItem}
   * @throws IOException if an I/O error occurs while parsing upload request
   */
  List<FileItem> getFileItemsFromMultipartUploadRequest(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final String format,
      final Account account,
      final boolean limitByFileUploadMaxSize,
      final AuthToken authToken,
      final boolean csrfCheckComplete)
      throws IOException {

    Preconditions.checkNotNull(account, "Account must not be null");
    List<FileItem> fileItems = new ArrayList<>();

    final ServletFileUpload upload = getUploader(account, limitByFileUploadMaxSize);
    try {
      fileItems = upload.parseRequest(request);
      if (!csrfCheckComplete && !CsrfUtil.checkCsrfInMultipartFileUpload(fileItems, authToken)) {
        mLog.info(
            "CSRF token validation failed for account: %s, Auth token is CSRF enabled",
            account.getName());
        sendResponse(response, HttpServletResponse.SC_UNAUTHORIZED, format, null, null, fileItems);
      }
    } catch (FileUploadBase.SizeLimitExceededException sizeLimitExceededException) {
      mLog.info(
          "Exceeded maximum upload size of "
              + upload.getSizeMax()
              + " bytes: "
              + sizeLimitExceededException);
      drainRequestStream(request);
      sendResponse(
          response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, format, null, null, null);
    } catch (FileUploadBase.InvalidContentTypeException invalidContentType) {
      mLog.info("File upload failed", invalidContentType);
      drainRequestStream(request);
      sendResponse(
          response, HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, format, null, null, null);
    } catch (FileUploadException fileUploadException) {
      mLog.info("File upload failed", fileUploadException);
      drainRequestStream(request);
      sendResponse(
          response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, format, null, null, null);
    }

    return fileItems;
  }

  /**
   * @param fileItems {@link List} of {@link FileItem} to be cached
   * @param account {@link Account} account to be used
   * @param fileNames {@link Map} {@link FileItem} objects with their names
   * @return {@link List} of {@link Upload} instances
   * @throws ServiceException if an error occurs
   */
  private List<Upload> cacheUploadedFiles(
      final List<FileItem> fileItems, final Account account, final Map<FileItem, String> fileNames)
      throws ServiceException {
    final List<Upload> uploads = new ArrayList<>(fileItems.size());
    for (FileItem fileItem : fileItems) {
      final String fileName = fileNames.get(fileItem);
      final Upload upload = new Upload(account.getId(), fileItem, fileName);
      mLog.info("Received multipart: %s", upload);
      synchronized (mPending) {
        mPending.put(upload.uuid, upload);
      }
      uploads.add(upload);
    }
    return uploads;
  }

  /**
   * This is used when handling a POST request generated by {@link ZMailbox#uploadContentAsStream}
   *
   * @param req the HTTP request
   * @param response the HTTP response
   * @param format the format of the upload
   * @param account the account associated with the upload; must not be null
   * @param limitByFileUploadMaxSize flag indicating whether to limit by file upload max size
   * @return a list of uploaded files
   *
   * @throws IOException if an I/O error occurs
   * @throws ServiceException if a service error occurs
   */
  List<Upload> handlePlainUpload(
      HttpServletRequest req,
      HttpServletResponse response,
      String format,
      Account account,
      boolean limitByFileUploadMaxSize)
      throws IOException, ServiceException {

    Preconditions.checkNotNull(account, "Account must not be null");

    // metadata is encoded in the response's HTTP headers
    ContentType ctype = new ContentType(req.getContentType());
    String contentType = ctype.getContentType();
    String filename = ctype.getParameter("name");
    if (filename == null) {
      filename =
          new ContentDisposition(req.getHeader("Content-Disposition")).getParameter("filename");
    }

    if (filename == null || "".equals(filename.trim())) {
      mLog.info("Rejecting upload with no name.");
      drainRequestStream(req);
      sendResponse(response, HttpServletResponse.SC_NO_CONTENT, format, null, null, null);
      return Collections.emptyList();
    }

    // Unescape the filename so it actually displays correctly
    filename = StringEscapeUtils.unescapeHtml(filename);

    // store the fetched file as a normal upload
    ServletFileUpload upload = getUploader(account, limitByFileUploadMaxSize);
    FileItem fi = upload.getFileItemFactory().createItem("upload", contentType, false, filename);
    try {
      // write the upload to disk, but make sure not to exceed the permitted max upload size
      long size =
          ByteUtil.copy(
              req.getInputStream(), false, fi.getOutputStream(), true, upload.getSizeMax() * 3);
      if ((upload.getSizeMax() >= 0 /* -1 would mean "no limit" */)
          && (size > upload.getSizeMax())) {
        mLog.debug("handlePlainUpload(): deleting %s", fi);
        fi.delete();
        mLog.info(
            "Exceeded maximum upload size of " + upload.getSizeMax() + " bytes: " + account.getId());
        drainRequestStream(req);
        sendResponse(response, HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, format, null, null, null);
        return Collections.emptyList();
      }
    } catch (IOException ioe) {
      mLog.warn("Unable to store upload.  Deleting %s", fi, ioe);
      fi.delete();
      drainRequestStream(req);
      sendResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, format, null, null, null);
      return Collections.emptyList();
    }
    List<FileItem> items = new ArrayList<>(1);
    items.add(fi);
    Upload up = new Upload(account.getId(), fi, filename);

    if (filename.endsWith(".har")) {
      File file = ((DiskFileItem) fi).getStoreLocation();
      try {
        String mimeType = MimeDetect.getMimeDetect().detect(file);
        if (mimeType != null) {
          up.contentType = mimeType;
        }
      } catch (IOException e) {
        mLog.warn("Failed to detect file content type");
      }
    }
    final String finalMimeType = up.contentType;
    String contentTypeBlacklist = LC.zimbra_file_content_type_blacklist.value();
    List<String> blacklist = new ArrayList<>();
    if (!StringUtil.isNullOrEmpty(contentTypeBlacklist)) {
      blacklist.addAll(Arrays.asList(contentTypeBlacklist.trim().split(",")));
    }
    if (blacklist.stream()
        .anyMatch(
            (blacklistedContentType) -> {
              Pattern p = Pattern.compile(blacklistedContentType);
              Matcher m = p.matcher(finalMimeType);
              return m.find();
            })) {
      mLog.debug("handlePlainUpload(): deleting %s", fi);
      fi.delete();
      mLog.info("File content type is blacklisted : %s", finalMimeType);
      drainRequestStream(req);
      sendResponse(response, HttpServletResponse.SC_FORBIDDEN, format, null, null, null);
      return Collections.emptyList();
    }

    mLog.info("Received plain: %s", up);
    synchronized (mPending) {
      mPending.put(up.uuid, up);
    }

    List<Upload> uploads = Arrays.asList(up);
    sendResponse(response, HttpServletResponse.SC_OK, format, null, uploads, items);
    return uploads;
  }

  @Override
  public void init() throws ServletException {
    String name = getServletName();
    mLog.info("Servlet %s starting up", name);
    super.init();

    File tempDir = new File(getUploadDir());
    if (!tempDir.exists()) {
      if (!tempDir.mkdirs()) {
        String msg = "Unable to create temporary upload directory " + tempDir;
        mLog.error(msg);
        throw new ServletException(msg);
      }
    }
    cleanupLeftoverTempFiles();

    Zimbra.sTimer.schedule(new MapReaperTask(), REAPER_INTERVAL_MSEC, REAPER_INTERVAL_MSEC);
  }

  @Override
  public void destroy() {
    String name = getServletName();
    mLog.info("Servlet %s shutting down", name);
    super.destroy();
  }

  public static final class Upload {

    final String accountId;
    final String uuid;
    final String name;
    final FileItem file;
    String contentType;
    long time;
    boolean deleted = false;
    BlobInputStream blobInputStream;

    Upload(String acctId, FileItem attachment) throws ServiceException {
      this(acctId, attachment, attachment.getName());
    }

    Upload(String acctId, FileItem attachment, String filename) throws ServiceException {
      assert (attachment != null); // TODO: Remove null checks in mainline.

      String localServer = Provisioning.getInstance().getLocalServer().getId();
      accountId = acctId;
      time = System.currentTimeMillis();
      uuid = localServer + UPLOAD_PART_DELIMITER + LdapUtil.generateUUID();
      name = FileUtil.trimFilename(filename);
      file = attachment;
      if (file == null) {
        contentType = MimeConstants.CT_TEXT_PLAIN;
      } else {
        // use content based detection.  we can't use magic based
        // detection alone because it defaults to application/xml
        // when it sees xml magic <?xml.  that's incompatible
        // with WebDAV handlers as the content type needs to be
        // text/xml instead.

        // 1. detect by magic
        try {
          contentType = MimeDetect.getMimeDetect().detect(file.getInputStream());
        } catch (Exception e) {
          contentType = null;
        }

        // 2. detect by file extension
        // .xls and .docx files can contain beginning characters
        // resembling to x-ole-storage/zip. Hence,
        // check by file extension
        if (contentType == null
            || contentType.equals("application/x-ole-storage")
            || contentType.equals("application/zip")) {
          contentType = MimeDetect.getMimeDetect().detect(name);
        }

        // 3. special-case text/xml to avoid detection
        if (contentType == null && file.getContentType() != null) {
          if (file.getContentType().equals("text/xml")) {
            contentType = file.getContentType();
          }
        }

        // 4. try the browser-specified content type
        if (contentType == null || contentType.equals(MimeConstants.CT_APPLICATION_OCTET_STREAM)) {
          contentType = file.getContentType();
        }

        // 5. when all else fails, use application/octet-stream
        if (contentType == null) {
          contentType = file.getContentType();
        }
        if (contentType == null) {
          contentType = MimeConstants.CT_APPLICATION_OCTET_STREAM;
        }
      }
    }

    public String getName() {
      return name;
    }

    public String getId() {
      return uuid;
    }

    public String getContentType() {
      return contentType;
    }

    public long getSize() {
      return file == null ? 0 : file.getSize();
    }

    public BlobInputStream getBlobInputStream() {
      return blobInputStream;
    }

    public InputStream getInputStream() throws IOException {
      if (wasDeleted()) {
        throw new IOException("Cannot get content for upload " + uuid + " because it was deleted.");
      }
      if (file == null) {
        return new SharedByteArrayInputStream(new byte[0]);
      }
      if (!file.isInMemory() && file instanceof DiskFileItem) {
        // If it's backed by a File, return a BlobInputStream so that any use by JavaMail
        // will avoid loading the whole thing in memory.
        File f = ((DiskFileItem) file).getStoreLocation();
        blobInputStream = new BlobInputStream(f, f.length());
        return blobInputStream;
      } else {
        return file.getInputStream();
      }
    }

    boolean accessedAfter(long checkpoint) {
      return time > checkpoint;
    }

    void purge() {
      if (file != null) {
        mLog.debug("Deleting from disk: id=%s, %s", uuid, file);
        file.delete();
      }
      if (blobInputStream != null) {
        blobInputStream.closeFile();
      }
    }

    synchronized void markDeleted() {
      deleted = true;
    }

    public synchronized boolean wasDeleted() {
      return deleted;
    }

    @Override
    public String toString() {
      return "Upload: { accountId="
          + accountId
          + ", time="
          + new Date(time)
          + ", size="
          + getSize()
          + ", uploadId="
          + uuid
          + ", name="
          + name
          + ", path="
          + getStoreLocation(file)
          + " }";
    }
  }

  private static class TempFileFilter implements FileFilter {

    private final long mNow = System.currentTimeMillis();

    TempFileFilter() {}

    /**
     * Returns <code>true</code> if the specified <code>File</code> follows the {@link
     * DefaultFileItem} naming convention (<code>upload_*.tmp</code>) and is older than {@link
     * FileUploadServlet#UPLOAD_TIMEOUT_MSEC}.
     */
    @Override
    public boolean accept(File pathname) {
      // upload_ XYZ .tmp
      if (pathname == null) {
        return false;
      }
      String name = pathname.getName();
      // file naming convention used by DefaultFileItem class
      return name.startsWith("upload_")
          && name.endsWith(".tmp")
          && mNow - pathname.lastModified() > UPLOAD_TIMEOUT_MSEC;
    }
  }

  private static final class MapReaperTask extends TimerTask {

    MapReaperTask() {}

    @Override
    public void run() {
      try {
        ArrayList<Upload> reaped = new ArrayList<>();
        int sizeBefore;
        int sizeAfter;
        synchronized (mPending) {
          sizeBefore = mPending.size();
          long cutoffTime = System.currentTimeMillis() - UPLOAD_TIMEOUT_MSEC;
          for (Iterator<Upload> it = mPending.values().iterator(); it.hasNext(); ) {
            Upload up = it.next();
            if (!up.accessedAfter(cutoffTime)) {
              mLog.debug("Purging cached upload: %s", up);
              it.remove();
              reaped.add(up);
              up.markDeleted();
              assert (mPending.get(up.uuid) == null);
            }
          }
          sizeAfter = mPending.size();
        }

        int removed = sizeBefore - sizeAfter;
        if (removed > 0) {
          mLog.info("Removed %d expired file uploads; %d pending file uploads", removed, sizeAfter);
        } else if (sizeAfter > 0) {
          mLog.info("%d pending file uploads", sizeAfter);
        }

        for (Upload up : reaped) {
          up.purge();
        }
      } catch (Throwable e) { // don't let exceptions kill the timer
        if (e instanceof OutOfMemoryError) {
          Zimbra.halt("Caught out of memory error", e);
        }
        ZimbraLog.system.warn("Caught exception in FileUploadServlet timer", e);
      }
    }
  }
}
