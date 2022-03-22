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
  @XmlElement(name = MailConstants.E_P_ITEM_ID, required = true)
  private String itemId;
  /**
   * @zm-api-field-description Specify part number of the attachment in the mailbox that you want to
   * get the preview for
   */
  @XmlElement(name = MailConstants.E_P_PART, required = true)
  private String part;

  /**
   * @zm-api-field-description Element to define specification of preview for PDF
   */
  @XmlElement(name = MailConstants.E_P_PDF, required = false)
  private String pdf;
  /**
   * @zm-api-field-description Element to define specification of preview for Image
   */
  @XmlElement(name = MailConstants.E_P_IMAGE, required = false)
  private String image;
  /**
   * @zm-api-field-description True will crop the picture starting from the borders. This option
   * will lose information, leaving it False will scale and have borders to fill the requested size.
   * Default: false
   */
  @XmlAttribute(name = MailConstants.A_P_CROP, required = false)
  private String crop;
  /**
   * @zm-api-field-description Preview type thumbnail or full<br> Values: full/thumbnail<br>Default:
   * "full"
   */
  @XmlAttribute(name = MailConstants.A_P_PREVIEW_TYPE, required = false)
  private String previewType;
  /**
   * @zm-api-field-description Area for image/pdf preview. Width of the output image (>=0) x height
   * of the output image (>=0), width x height => 100x200. The first is width, the latter height,
   * the order is important! <br>Area is required in case of preview_type="thumbnail"
   */
  @XmlAttribute(name = MailConstants.A_P_AREA, required = false)
  private String area;
  /**
   * @zm-api-field-description Quality of image/pdf preview quality of the output (the higher you go
   * the slower the process) Values: lowest, low, medium, high, highest DEfault: ""
   */
  @XmlAttribute(name = MailConstants.A_P_QUALITY, required = false)
  private String quality;
  /**
   * @zm-api-field-description Format of the output image
   */
  @XmlAttribute(name = MailConstants.A_P_OUTPUT_FORMAT, required = false)
  private String outputFormat;
  /**
   * @zm-api-field-description Integer value of first page to preview (n>=1)
   */
  @XmlAttribute(name = MailConstants.A_P_FIRST_PAGE, required = false)
  private String firstPage;
  /**
   * @zm-api-field-description Integer value of last page to preview (0 = last of the pdf)
   */
  @XmlAttribute(name = MailConstants.A_P_LAST_PAGE, required = false)
  private String lastPage;

  public GetPreviewRequest() {
  }

  public String getItemId() {
    return itemId;
  }

  public String getPart() {
    return part;
  }

  public String getPdf() {
    return pdf;
  }

  public String getImage() {
    return image;
  }

  public String getCrop() {
    return crop;
  }

  public String getPreviewType() {
    return previewType;
  }

  public String getArea() {
    return area;
  }

  public String getQuality() {
    return quality;
  }

  public String getOutputFormat() {
    return outputFormat;
  }

  public String getFirstPage() {
    return firstPage;
  }

  public String getLastPage() {
    return lastPage;
  }

  public MoreObjects.ToStringHelper addToStringInfo(MoreObjects.ToStringHelper helper) {
    return helper;
  }

  @Override
  public String toString() {
    return addToStringInfo(MoreObjects.toStringHelper(this)).toString();
  }
}
