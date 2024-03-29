package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Server;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Utils {

  private Utils() {
    throw new java.lang.UnsupportedOperationException("Utility class and cannot be instantiated");
  }

  /**
   * Get Unique list of servers, removes duplicate servers from passed server list using A_zimbraId
   * as unique identifier
   *
   * @param servers List of Servers
   * @return return List of filtered servers
   * @author Keshav Bhatt
   * @since 22.7.1
   */
  public static List<Server> getUniqueServersList(List<Server> servers) {
    return servers.stream()
        .collect(
            Collectors.collectingAndThen(
                Collectors.toCollection(
                    () ->
                        new TreeSet<>(
                            Comparator.comparing(
                                server -> server.getAttr(ZAttrProvisioning.A_zimbraId)))),
                ArrayList::new));
  }

  /**
   * Creates a folder
   *
   * @param folderPath the path to create
   * @throws ProxyConfException if something goes wrong
   * @author Davide Polonio and Yuliya Aheeva
   */
  public static void createFolder(String folderPath) throws ProxyConfException {
    File directory = new File(folderPath);
    if (!directory.exists() && !directory.mkdirs()) {
      throw new ProxyConfException("Unable to create folder in " + folderPath);
    }
  }

  /**
   * Delete a file specified by the given path
   *
   * @param filePath path of file to delete
   * @author Keshav Bhatt
   * @since 22.12.0
   */
  public static void deleteFileIfExists(final String filePath) throws ProxyConfException {
    File file = new File(filePath);
    if (file.exists()) {
      try {
        Files.delete(file.toPath());
      } catch (final IOException ie) {
        throw new ProxyConfException("Unable to delete file " + filePath);
      }
    }
  }

  /**
   * Get file paths from the given directory path Note: This will only return path of regular
   * 'files' in the given directory path limiting the scan to depth -> 1
   *
   * @param directoryPath path to get directory
   * @return String List of file paths
   * @author Keshav Bhatt
   * @since 22.12.0
   */
  public static List<String> getFilesPathInDirectory(final String directoryPath) {
    try (Stream<Path> paths = Files.walk(Paths.get(directoryPath), 1)) {
      return paths
          .filter(Files::isRegularFile)
          .map(Path::toString)
          .collect(Collectors.toCollection(ArrayList::new));
    } catch (IOException ignored) {
      // ignore
    }
    return new ArrayList<>();
  }

  /**
   * Get subdirectories names from the given directory path.
   *
   * @param directoryPath path to get subdirectories names
   * @return list of directories names
   * @author Yuliya Aheeva
   * @since 23.7.0
   */
  public static List<String> getSubdirectoriesNames(final String directoryPath) {
    try (Stream<Path> paths = Files.list(Path.of(directoryPath))) {
      return paths
          .filter(Files::isDirectory)
          .map(Path::getFileName)
          .map(Path::toString)
          .collect(Collectors.toCollection(ArrayList::new));
    } catch (IOException ignored) {
      // ignore
    }
    return new ArrayList<>();
  }
}
