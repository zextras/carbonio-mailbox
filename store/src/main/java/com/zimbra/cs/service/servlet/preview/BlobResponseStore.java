package com.zimbra.cs.service.servlet.preview;

import java.io.InputStream;

/**
 * {@link BlobResponseStore} class is used to store responses we get from preview service It
 * basically act as intermediate object with all metadata we get from attachment and preview
 * service.
 */
class BlobResponseStore {

  private final String filename;
  private final Long size;
  private final String mimeType;
  private final InputStream blobInputStream;
  private final String dispositionType;

  public BlobResponseStore(
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