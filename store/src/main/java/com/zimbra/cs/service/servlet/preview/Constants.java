package com.zimbra.cs.service.servlet.preview;

import java.util.regex.Pattern;


/**
 * A utility class that holds various constants used throughout preview servlet package.
 */
class Constants {

  @SuppressWarnings("squid:S1075")
  public static final String SERVLET_PATH = "/preview";
  public static final String PART_NUMBER_REGEXP = "([0-9.]+(?:\\.[0-9.]+)?)";
  public static final String MESSAGE_ID_REGEXP = "([a-zA-Z\\-:0-9]+|[0-9]+)/";
  public static final String THUMBNAIL_REGEXP =
      MESSAGE_ID_REGEXP + PART_NUMBER_REGEXP + "/([0-9]*x[0-9]*)/thumbnail/?\\??(.*)";
  public static final String PDF_THUMBNAIL_REGEX = SERVLET_PATH + "/pdf/" + THUMBNAIL_REGEXP;
  public static final String IMG_THUMBNAIL_REGEX = SERVLET_PATH + "/image/" + THUMBNAIL_REGEXP;
  public static final String DOC_THUMBNAIL_REGEX = SERVLET_PATH + "/document/" + THUMBNAIL_REGEXP;
  public static final int STATUS_UNPROCESSABLE_ENTITY = 422;
  public static final Pattern REQUIRED_QUERY_PARAMETERS_PATTERN = Pattern.compile(
      SERVLET_PATH + "/([a-zA-Z]+)/" + MESSAGE_ID_REGEXP + PART_NUMBER_REGEXP);

  public static final String PDF_PREVIEW_REGEX =
      SERVLET_PATH
          + "/pdf/"
          + MESSAGE_ID_REGEXP
          + PART_NUMBER_REGEXP
          + "/?((?=(?!thumbnail))(?=([^/ ]*)))";
  public static final String IMG_PREVIEW_REGEX =
      SERVLET_PATH
          + "/image/"
          + MESSAGE_ID_REGEXP
          + PART_NUMBER_REGEXP
          + "/([0-9]*x[0-9]*)/?((?=(?!thumbnail))(?=([^/"
          + " ]*)))";
  public static final String DOC_PREVIEW_REGEX =
      SERVLET_PATH
          + "/document/"
          + MESSAGE_ID_REGEXP
          + PART_NUMBER_REGEXP
          + "/?((?=(?!thumbnail))(?=([^/ ]*)))";
  public static final String REQUEST_ID_KEY = "tRequestId";
  public static final String REQUEST_PARAM_DISP = "disp";

  private Constants() {
    // Prevent instantiation
  }
}
