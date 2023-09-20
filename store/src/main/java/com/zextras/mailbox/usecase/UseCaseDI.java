package com.zextras.mailbox.usecase;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.zextras.mailbox.usecase.factory.ItemIdFactory;
import com.zextras.mailbox.usecase.factory.OperationContextFactory;

/** Provides classes for use case modules. */
public class UseCaseDI extends AbstractModule {
  @Override
  protected void configure() {
    install(new FactoryModuleBuilder().build(ItemIdFactory.class));
    install(new FactoryModuleBuilder().build(OperationContextFactory.class));
  }
}
