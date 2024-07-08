package com.zextras.mailbox.soap.utils;

import com.zimbra.cs.account.Account;
import com.zimbra.soap.mail.message.CreateMountpointRequest;
import com.zimbra.soap.mail.type.NewMountpointSpec;

public class CreateMountpoint {

  private final Account owner;
  private final Integer remoteId;

  public CreateMountpoint(Account owner, Integer remoteId) {
    this.owner = owner;
    this.remoteId = remoteId;
  }

  public CreateMountpointRequest createCalendarMountpoint() {
    final NewMountpointSpec newMountpointSpec = new NewMountpointSpec("test shared calendar");
    newMountpointSpec.setDefaultView("appointment");
    newMountpointSpec.setRemoteId(remoteId);
    newMountpointSpec.setOwnerId(owner.getId());
    newMountpointSpec.setFolderId("1");
    return new CreateMountpointRequest(newMountpointSpec);
  }

}
