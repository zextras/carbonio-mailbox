package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_PREVIEW_RESPONSE)
public class GetPreviewResponse {

  /**
   * @zm-api-field-description The previewServiceStatus element specifies whether the Carbonio
   * preview service is available or not. It provides status-code(int) and status-message(string)
   */
  @XmlElement(name = MailConstants.E_P_PREVIEW_SERVICE_STATUS, required = true)
  private String previewServiceStatus;

  /**
   * @zm-api-field-description Preview data stream returned from the Carbonio Previewer service
   * is base64 encoded stream of data representing the preview
   */
  @XmlElement(name = MailConstants.E_P_PREVIEW_DATA_STREAM, required = false)
  private String previewDataStream;

  /**
   * @zm-api-field-description Error element provides the error message that the previewer service
   * experienced while generating preview
   */
  @XmlElement(name = MailConstants.E_P_ERROR, required = false)
  private String error;

  /**
   * @zm-api-field-tag file-name
   * @zm-api-field-description Original Name of the file whose preview is being returned
   */
  @XmlAttribute(name = MailConstants.A_P_FILE_NAME, required = false)
  private String fileName;

  /**
   * @zm-api-field-tag status-code
   * @zm-api-field-description Status code returned while checking health/live status of Carbonio
   * Previewer service
   */
  @XmlAttribute(name = MailConstants.A_P_STATUS_CODE, required = false)
  private String statusCode;

  /**
   * @zm-api-field-tag status-message
   * @zm-api-field-description Status message returned while checking health/live status of Carbonio
   * Previewer service
   */
  @XmlAttribute(name = MailConstants.A_P_STATUS_MESSAGE, required = false)
  private String statusMessage;


  private GetPreviewResponse() {
  }

  public String getStatusCode() {
    return statusCode;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  public String getPreviewServiceStatus() {
    return previewServiceStatus;
  }

  public void setPreviewServiceStatus(String previewServiceStatus) {
    this.previewServiceStatus = previewServiceStatus;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getPreviewDataStream() {
    return previewDataStream;
  }

  public void setPreviewDataStream(String previewDataStream) {
    this.previewDataStream = previewDataStream;
  }
}
