package com.zimbra.soap.mail.message;

import com.zimbra.common.soap.MailConstants;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @zm-api-command-auth-required true
 * @zm-api-command-admin-auth-required false
 * @zm-api-command-description API for File preview service. <br /> Provide preview for requested
 * file or artifact ID using the Carbonio previewer service.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = MailConstants.E_GET_PREVIEW_REQUEST)
public class GetPreviewRequest {

  /**
   * @zm-api-field-description Specify item id of the object in the mailbox that you want to get the
   * preview for
   */
  @XmlElement(name = "itemId", required = true)
  private String itemId;

  /**
   * @zm-api-field-description Specify format type of the object in the mailbox that you want to get
   * the preview for
   */
  @XmlElement(name = "part", required = true)
  private String part;

  /**
   * @zm-api-field-description the pdf element
   */
  @XmlElement(name = "pdf", required = false)
  private String pdf;

  /**
   * @zm-api-field-description the image element
   */
  @XmlElement(name = "image", required = false)
  private String image;

  /**
   * @zm-api-field-description crop image preview
   */
  @XmlAttribute(name = "crop", required = false)
  private String crop;

  /**
   * @zm-api-field-description preview type thumbnail or full
   */
  @XmlAttribute(name = "preview_type", required = false)
  private String previewType;

  /**
   * @zm-api-field-description area for image/pdf preview
   */
  @XmlAttribute(name = "area", required = false)
  private String area;

  /**
   * @zm-api-field-description quality of image/pdf preview
   */
  @XmlAttribute(name = "quality", required = false)
  private String quality;

  /**
   * @zm-api-field-description output format for preview
   */
  @XmlAttribute(name = "output_format", required = false)
  private String outputFormat;

  /**
   * @zm-api-field-description first page of pdf preview
   */
  @XmlAttribute(name = "first_page", required = false)
  private String firstPage;

  /**
   * @zm-api-field-description last page of pdf preview
   */
  @XmlAttribute(name = "last_page", required = false)
  private String lastPage;

  public GetPreviewRequest() {
  }
}
