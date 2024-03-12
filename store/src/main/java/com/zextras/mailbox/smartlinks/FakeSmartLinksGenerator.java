package com.zextras.mailbox.smartlinks;

import com.zextras.mailbox.AuthenticationInfo;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class FakeSmartLinksGenerator implements SmartLinksGenerator {

  @Override
  public List<SmartLink> smartLinksFrom(List<Attachment> attachments, AuthenticationInfo authenticationInfo) {
    return attachments
        .stream()
        .map(attachment -> new SmartLink(publicUrlOf(attachment)))
        .collect(toList());
  }

  private static String publicUrlOf(Attachment attachment) {
    return "http://fake-public-link.local?draftId=" + attachment.getDraftId() + "&partName=" + attachment.getPartName();
  }
}
