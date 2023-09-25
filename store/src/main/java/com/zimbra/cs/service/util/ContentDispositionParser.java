package com.zimbra.cs.service.util;

import com.zimbra.common.util.StringUtil;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for parsing Content-Disposition headers following RFC-6266 specifications. This
 * class provides methods to extract filenames from Content-Disposition header values.
 */
public class ContentDispositionParser {

  /** Private constructor to prevent instantiation. This class should be used as a utility class. */
  private ContentDispositionParser() {}

  /**
   * RFC-6266 compliant filename extraction helper method. Returns the filename from the given
   * Content-Disposition header value following RFC-6266 specifications. Preference to the extended
   * version of the filename {@code filename*=UTF-8''utf8EncodedFile} is given. If the extended
   * filename is not specified in the content-disposition header, then the ASCII valued filename is
   * returned. If that is also not specified, an empty string is returned.
   *
   * @param contentDisposition A {@link String} value of the Content-Disposition header.
   * @return The filename from the given Content-Disposition header or an empty string if not found.
   */
  public static String getFileNameFromContentDisposition(final String contentDisposition) {
    String fileName = "";
    if (!StringUtil.isNullOrEmpty(contentDisposition)) {
      final String dispositionItemsDelimiter = ";";
      for (String dispositionItem : contentDisposition.split(dispositionItemsDelimiter)) {
        fileName = getAsciiFileNameFromDispositionItem(fileName, dispositionItem);
        fileName = getExtendedFilenameFromDispositionItem(fileName, dispositionItem);
      }
    }
    return fileName;
  }

  /**
   * Returns the filename from the given disposition item.
   *
   * @param defaultValue The fallback filename if the filename is not specified in the disposition
   *     item.
   * @param dispositionItem The disposition item.
   * @return The filename.
   */
  private static String getAsciiFileNameFromDispositionItem(
      final String defaultValue, final String dispositionItem) {
    String newFileName = defaultValue;

    if (dispositionItem.trim().toLowerCase().startsWith("filename=")) {
      final String[] keyValue = dispositionItem.split("=");
      if (keyValue.length >= 2) {
        newFileName = keyValue[1].trim().replace("\"", "");
      }
    }
    return newFileName;
  }

  /**
   * Returns the extended filename from the given disposition item.
   *
   * @param defaultValue The fallback filename if the filename is not specified in the disposition
   *     item.
   * @param dispositionItem The disposition item.
   * @return The filename.
   */
  private static String getExtendedFilenameFromDispositionItem(
      final String defaultValue, final String dispositionItem) {
    String newFileName = defaultValue;
    if (dispositionItem.trim().toLowerCase().startsWith("filename*=")) {
      final String[] keyValue = dispositionItem.split("=");
      final String value = keyValue[1].trim();
      final String delimiter = "utf-8''";
      if (value.toLowerCase().startsWith(delimiter)) {
        newFileName =
            URLDecoder.decode(value.substring(delimiter.length()), StandardCharsets.UTF_8);
      }
    }
    return newFileName;
  }
}
