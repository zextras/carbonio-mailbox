package com.zextras.mailbox.smartlinks;

public class Attachment {
  private final String draftId;
  private final String partName;

  public Attachment(String draftId, String partName) {
    this.draftId = draftId;
    this.partName = partName;
  }

  public String getDraftId() {
    return draftId;
  }

  public String getPartName() {
    return partName;
  }
}
