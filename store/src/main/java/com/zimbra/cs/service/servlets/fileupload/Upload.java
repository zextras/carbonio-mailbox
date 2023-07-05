package com.zimbra.cs.service.servlets.fileupload;

import com.zimbra.common.mime.MimeConstants;
import com.zimbra.common.mime.MimeDetect;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.FileUtil;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.ldap.LdapUtil;
import com.zimbra.cs.store.BlobInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import javax.mail.util.SharedByteArrayInputStream;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;

public final class Upload {

  final String accountId;
  String contentType;
  final String uuid;
  final String name;
  final FileItem file;
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
    uuid = localServer + FileUploadServlet.UPLOAD_PART_DELIMITER + LdapUtil.generateUUID();
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
      FileUploadServlet.mLog.debug("Deleting from disk: id=%s, %s", uuid, file);
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
        + FileUploadServlet.getStoreLocation(file)
        + " }";
  }
}
