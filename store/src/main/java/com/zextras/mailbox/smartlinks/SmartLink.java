package com.zextras.mailbox.smartlinks;

public class SmartLink {
  private final String publicUrl;

  public SmartLink(String publicUrl) {
    this.publicUrl = publicUrl;
  }

  public String getPublicUrl() {
    return publicUrl;
  }
}
