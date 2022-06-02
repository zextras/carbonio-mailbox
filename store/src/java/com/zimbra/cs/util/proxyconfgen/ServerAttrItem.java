package com.zimbra.cs.util.proxyconfgen;

/**
 * Simplified container object for a server
 *
 * @author Davide Baldo
 */
class ServerAttrItem {

  String zimbraId;
  String hostname;
  String[] services;

  public ServerAttrItem(String zimbraId, String hostname, String[] services) {
    this.zimbraId = zimbraId;
    this.hostname = hostname;
    this.services = services;
  }

  public boolean hasService(String service) {
    for (String current : services) {
      if (service.equals(current)) {
        return true;
      }
    }
    return false;
  }
}
