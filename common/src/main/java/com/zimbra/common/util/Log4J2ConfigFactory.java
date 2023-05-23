package com.zimbra.common.util;

import com.zimbra.common.localconfig.KnownKey;
import com.zimbra.common.localconfig.LC;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Order;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.plugins.Plugin;

/**
 * This plugin for Log4j2 allows to load settings from the Carbonio Localconfig (represented by
 * {@link LC} class)
 *
 * @author Davide
 * @since 23.7
 */
@Plugin(name = "CarbonioConfigurationFactory", category = ConfigurationFactory.CATEGORY)
@Order(50)
public class Log4J2ConfigFactory extends ConfigurationFactory {

  @Override
  protected String[] getSupportedTypes() {
    return new String[] {"*"};
  }

  @Override
  public Configuration getConfiguration(LoggerContext loggerContext, ConfigurationSource source) {
    final ConfigurationBuilder<BuiltConfiguration> builder = newConfigurationBuilder();
    final KnownKey zimbraLog4jProperties = LC.zimbra_log4j_properties;

    builder.addProperty(zimbraLog4jProperties.key(), zimbraLog4jProperties.value());
    if (source != null) {
      builder.setConfigurationSource(source);
    }

    final BuiltConfiguration builtConfiguration = builder.build();

    if (loggerContext != null) {
      loggerContext.setConfiguration(builtConfiguration);
    }

    return builtConfiguration;
  }
}
