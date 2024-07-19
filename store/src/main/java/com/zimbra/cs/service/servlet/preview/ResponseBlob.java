package com.zimbra.cs.service.servlet.preview;

import java.io.InputStream;

/**
 * Represents a response containing a blob (binary large object).
 * <p>
 * This class encapsulates the details of a blob, including its size,
 * MIME type, filename, disposition type, and input stream. It is used
 * to facilitate the transfer of binary data, such as attachment or response
 * from preview service.
 * </p>
 */
class ResponseBlob {

  private final String filename;
  private final Long size;
  private final String mimeType;
  private final InputStream blobInputStream;
  private final String dispositionType;

  public ResponseBlob(
      InputStream blobInputStream, String filename, Long size, String mimeType, String disposition) {
    this.blobInputStream = blobInputStream;
    this.filename = filename;
    this.size = size;
    this.mimeType = mimeType;
    this.dispositionType = disposition;
  }

  public String getDispositionType() {
    return dispositionType;
  }

  public InputStream getBlobInputStream() {
    return blobInputStream;
  }

  public String getFilename() {
    return filename;
  }

  public Long getSize() {
    return size;
  }

  public String getMimeType() {
    return mimeType;
  }
}