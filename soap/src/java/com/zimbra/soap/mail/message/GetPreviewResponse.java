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
   * preview service is available or not.
   */
  @XmlElement(name = "previewServiceStatus", required = true)
  private String previewServiceStatus;
  /**
   * @zm-api-field-description preview data stream returned from the Carbonio previewer service
   */
  @XmlElement(name = "previewDataStream", required = false)
  private String previewDataStream;
  /**
   * @zm-api-field-tag file-name
   * @zm-api-field-description provide the file name
   */
  @XmlAttribute(name = "file-name", required = false)
  private String fileName;
  /**
   * @zm-api-field-description preview data stream returned from the Carbonio previewer service
   */
  @XmlElement(name = "error", required = false)
  private String error;

  private GetPreviewResponse() {
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
