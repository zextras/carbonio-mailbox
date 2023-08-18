package com.zextras.mailbox.domain.usecase;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.zimbra.cs.account.Provisioning;
import javax.inject.Named;

public class UseCaseDI extends AbstractModule {

  @Provides
  @Named("defaultProvisioning")
  Provisioning getProvisioning() {
    return Provisioning.getInstance();
  }
}
