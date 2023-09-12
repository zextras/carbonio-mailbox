/*
 * SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: CC0-1.0
 */

package com.zextras.mailbox.servlet;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceServletContextListener;
import java.util.Collection;
import java.util.Set;

/**
 * This class contains the entrypoint for the {@link Injector} creation. It is called at Carbonio
 * startup
 *
 * @author Davide Frison
 * @since 23.3
 */
public class GuiceMailboxServletConfig extends GuiceServletContextListener {

  /**
   * This method allows to properly bootstrap Carbonio, returning all the modules necessary for the
   * {@link Injector} in order to resolve possible dependencies at runtime
   *
   * @return a {@link Collection} of {@link AbstractModule} implementations, containing definitions
   *     on how to solve possible dependencies
   */
  private static Collection<AbstractModule> getInjectionModules() {
    return Set.of(new MailboxServletModule());
  }

  /**
   * Main entrypoint for the whole dependency injection system
   *
   * @return a {@link Injector} that contains the whole dependencies graph of Carbonio (note only
   *     the classes that support DI are listed)
   */
  @Override
  protected Injector getInjector() {
    // TODO we should create the injector accordingly. When testing it should be Stage.DEVELOPMENT
    return Guice.createInjector(Stage.PRODUCTION, getInjectionModules());
  }
}
