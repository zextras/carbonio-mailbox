package com.zimbra.common.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.spi.LoggerContext;

/**
 * Utility class for managing {@link Log}.
 *
 * @author Davide Polonio
 * @since 23.7.0
 * @see LogFactory in order to create Logs
 */
public class LogManager {

  private LogManager() {
    throw new java.lang.UnsupportedOperationException("Utility class and cannot be instantiated");
  }

  private static final Map<String, Log> globalLogMapper = new ConcurrentHashMap<>();

  /**
   * @return {@code true} if a logger with the given name exists.
   */
  public static boolean logExists(final String name) {
    return getContext().hasLogger(name);
  }

  /**
   * @return all account loggers that have been created since the last server start, or an empty
   *     {@link List}.
   */
  public static List<AccountLogger> getAllAccountLoggers() {
    return globalLogMapper.values().stream()
        .map(Log::getAccountLoggers)
        .filter(Objects::nonNull)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  /**
   * @return all the loggers that have been created with {@link LogFactory#getLog(Class)} or {@link
   *     LogFactory#getLog(String)}. The {@link Collection} does not include account loggers.
   */
  public static Collection<Log> getAllLoggers() {
    return globalLogMapper.values();
  }

  /**
   * @return the global {@link Map} with the associated name/{@link Log}
   */
  static Map<String, Log> getGlobalLogMapper() {
    return globalLogMapper;
  }

  /**
   * As you can imagine from this method name, it sets the log you pass and the root level {@link
   * Logger} to the desired {@link Level}
   *
   * @param logger the logger you want to change the level
   * @param level the new level the passed logger and the root logger will have
   */
  public static void setThisLogAndRootToLevel(final Logger logger, final Level level) {
    Configurator.setRootLevel(level);
    Configurator.setLevel(logger, level);
  }

  /**
   * Gets the logger context.
   *
   * <p>Note: if currentContext false the LoggerContext appropriate for the caller of this method is
   * returned. For example, in a web application if the caller is a class in WEB-INF/lib then one
   * LoggerContext may be returned and if the caller is a class in the container's classpath then a
   * different LoggerContext may be returned. If true then only a single LoggerContext will be
   * returned.
   *
   * @return {@link org.apache.logging.log4j.spi.LoggerContext}
   */
  public static LoggerContext getContext() {
    return org.apache.logging.log4j.LogManager.getContext(false);
  }
}
