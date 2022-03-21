package com.zimbra.soap.mail.message;

import com.google.common.base.MoreObjects;
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
   * @zm-api-field-description Specify item ID of the attachment in the mailbox that you want to get
   * the preview for
   */
  @XmlElement(name = "itemId", required = true)
  private String itemId;

  /**
   * @zm-api-field-description Specify part number of the attachment in the mailbox that you want to
   * get the preview for
   */
  @XmlElement(name = "part", required = true)
  private String part;

  /**
   * @zm-api-field-description Element to define specification of preview for PDF
   */
  @XmlElement(name = "pdf", required = false)
  private String pdf;

  /**
   * @zm-api-field-description Element to define specification of preview for Image
   */
  @XmlElement(name = "image", required = false)
  private String image;

  /**
   * @zm-api-field-description True will crop the picture starting from the borders. This option
   * will lose information, leaving it False will scale and have borders to fill the requested size.
   * Default: false
   */
  @XmlAttribute(name = "crop", required = false)
  private String crop;

  /**
   * @zm-api-field-description Preview type thumbnail or full<br> Values: full/thumbnail<br>Default:
   * "full"
   */
  @XmlAttribute(name = "preview_type", required = false)
  private String previewType;

  /**
   * @zm-api-field-description Area for image/pdf preview. Width of the output image (>=0) x height
   * of the output image (>=0), width x height => 100x200. The first is width, the latter height,
   * the order is important! <br>Area is required in case of preview_type="thumbnail"
   */
  @XmlAttribute(name = "area", required = false)
  private String area;

  /**
   * @zm-api-field-description Quality of image/pdf preview quality of the output (the higher you go
   * the slower the process) Values: lowest, low, medium, high, highest DEfault: ""
   */
  @XmlAttribute(name = "quality", required = false)
  private String quality;

  /**
   * @zm-api-field-description Format of the output image
   */
  @XmlAttribute(name = "output_format", required = false)
  private String outputFormat;

  /**
   * @zm-api-field-description  Integer value of first page to preview (n>=1)
   */
  @XmlAttribute(name = "first_page", required = false)
  private String firstPage;

  /**
   * @zm-api-field-description Integer value of last page to preview (0 = last of the pdf)
   */
  @XmlAttribute(name = "last_page", required = false)
  private String lastPage;

  public GetPreviewRequest() {
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper;
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
