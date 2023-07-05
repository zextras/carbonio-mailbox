package com.zimbra.cs.service.servlets.fileupload;

import java.io.File;
import java.io.FileFilter;
import org.apache.commons.fileupload.DefaultFileItem;

class TempFileFilter implements FileFilter {

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
        && mNow - pathname.lastModified() > FileUploadServlet.UPLOAD_TIMEOUT_MSEC;
  }
}
