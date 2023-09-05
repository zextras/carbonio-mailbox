package com.zextras.mailbox.usecase;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.zextras.mailbox.usecase.factory.ItemIdFactory;

public class UseCaseDI extends AbstractModule {
  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(ItemIdFactory.class));
  }
}
