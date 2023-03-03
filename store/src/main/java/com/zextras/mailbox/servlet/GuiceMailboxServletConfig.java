package com.zextras.mailbox.servlet;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.zimbra.cs.util.Zimbra;

/**
 * Guice Injector for the Mailbox.
 *
 * @since 23.3.0
 * @author davidefrison
 */
public class GuiceMailboxServletConfig extends GuiceServletContextListener {

  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new Zimbra(), new MailboxModule());
  }
}
