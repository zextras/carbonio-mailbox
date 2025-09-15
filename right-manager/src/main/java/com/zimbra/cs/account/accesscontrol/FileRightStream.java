package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.service.ServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

class FileRightStream implements RightStream {

  final String baseDirectory;

  FileRightStream(String baseDirectory) {
    this.baseDirectory = baseDirectory;
  }

  @Override
  public InputStream open(String rightFileName) throws ServiceException {
    final File file = new File(baseDirectory, rightFileName);
    try {
      return new FileInputStream(file);
    } catch (FileNotFoundException e) {
      throw ServiceException.FAILURE("Cannot read file " + file.getAbsolutePath(), e);
    }
  }
}
