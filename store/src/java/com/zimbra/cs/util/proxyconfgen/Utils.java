package com.zimbra.cs.util.proxyconfgen;

import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.cs.account.Server;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
}
