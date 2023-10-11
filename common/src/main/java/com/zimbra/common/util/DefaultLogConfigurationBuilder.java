// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.common.util;

import java.util.Objects;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.LoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

public class DefaultLogConfigurationBuilder {

  public static final String LOG_CONFIG_NAME = "CLI";
  public static final String DEFAULT_APPENDER_NAME = "Stdout";
  public static final String DEFAULT_APPENDER_TYPE = "CONSOLE";
  public static final String DEFAULT_APPENDER_TARGET = "target";
  public static final String APPENDER_NAME = "rolling";
  public static final String APPENDER_TYPE = "RollingFile";
  public static final String LAYOUT_TYPE = "PatternLayout";
  public static final String LAYOUT_PATTERN = "pattern";
  public static final String LAYOUT_PATTERN_VALUE_WITH_THREADS =
      "%d [%t] %-5level: %msg%n%throwable";
  public static final String LAYOUT_PATTERN_VALUE = "%d [%t] %-5level: %msg%n";
  public static final String POLICY_TYPE = "Policies";
  public static final String POLICY_TIME_TYPE = "TimeBasedTriggeringPolicy";
  public static final String POLICY_TIME_TYPE_INTERVAL = "interval";
  public static final String POLICY_TIME_TYPE_INTERVAL_VALUE = "1";
  public static final String APPENDER_FILE_NAME = "fileName";
  public static final String APPENDER_FILE_PATTERN = "filePattern";
  public static final String LOGGER_NAME = "TestLogger";
  public static final String LOGGER_ADDITIVITY = "additivity";

  public DefaultLogConfigurationBuilder setShowThreads(boolean showThreads) {
    this.showThreads = showThreads;
    return this;
  }

  public DefaultLogConfigurationBuilder setLogFile(String logFile) {
    this.logFile = logFile;
    return this;
  }

  public DefaultLogConfigurationBuilder setLevel(Level level) {
    this.level = level;
    return this;
  }

  private boolean showThreads;
  private String logFile;
  private Level level;

  public Configuration createConfiguration() {
    ConfigurationBuilder<BuiltConfiguration> builder =
        ConfigurationBuilderFactory.newConfigurationBuilder();
    builder.setStatusLevel(Level.INFO);
    builder.setConfigurationName(LOG_CONFIG_NAME);
    AppenderComponentBuilder appenderBuilder =
        builder
            .newAppender(DEFAULT_APPENDER_NAME, DEFAULT_APPENDER_TYPE)
            .addAttribute(DEFAULT_APPENDER_TARGET, ConsoleAppender.Target.SYSTEM_OUT);
    LayoutComponentBuilder layoutBuilder = builder.newLayout(LAYOUT_TYPE);
    if (this.showThreads) {
      layoutBuilder.addAttribute(LAYOUT_PATTERN, LAYOUT_PATTERN_VALUE_WITH_THREADS);
    } else {
      layoutBuilder.addAttribute(LAYOUT_PATTERN, LAYOUT_PATTERN_VALUE);
    }
    builder.add(appenderBuilder.add(layoutBuilder));

    if (null != logFile) {
      ComponentBuilder policy =
          builder
              .newComponent(POLICY_TYPE)
              .addComponent(
                  builder
                      .newComponent(POLICY_TIME_TYPE)
                      .addAttribute(POLICY_TIME_TYPE_INTERVAL, POLICY_TIME_TYPE_INTERVAL_VALUE));
      appenderBuilder =
          builder
              .newAppender(APPENDER_NAME, APPENDER_TYPE)
              .addAttribute(APPENDER_FILE_NAME, logFile)
              .addAttribute(APPENDER_FILE_PATTERN, logFile + "-%d{yyyy-MM-dd}")
              .add(layoutBuilder)
              .addComponent(policy);
      builder.add(appenderBuilder);

      LoggerComponentBuilder newLogger;
      RootLoggerComponentBuilder rootLogger;
      if (Objects.isNull(this.level)) {
        newLogger = builder.newLogger(LOGGER_NAME);
      } else {
        newLogger = builder.newLogger(LOGGER_NAME, level);
      }

      // create the new logger
      builder.add(
          newLogger
              .add(builder.newAppenderRef(APPENDER_NAME))
              .addAttribute(LOGGER_ADDITIVITY, false));
    }
    RootLoggerComponentBuilder rootLogger;
    if (Objects.isNull(this.level)) {
      rootLogger = builder.newRootLogger();
    } else {
      rootLogger = builder.newRootLogger(level);
    }
    builder.add(rootLogger.add(builder.newAppenderRef(DEFAULT_APPENDER_NAME)));
    return builder.build();
  }
}
