package com.zimbra.cs.account;

import com.zimbra.common.service.ServiceException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileAttributeStream implements AttributeStream {

  private final String baseDirectory;

  public FileAttributeStream(String baseDirectory) {
    this.baseDirectory = baseDirectory;
  }

  @Override
  public InputStream open(String attributesFileName) throws ServiceException {
    final File file = new File(baseDirectory, attributesFileName);
    try {
      return new FileInputStream(file);
    } catch (FileNotFoundException e) {
      throw ServiceException.FAILURE("Cannot read file " + file.getAbsolutePath(), e);
    }
  }
}
