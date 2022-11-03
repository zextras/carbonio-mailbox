// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/** This class represents the root data model for the SOAP API. */
public class Root {

  private List<Service> services = Lists.newLinkedList();

  Root() {}

  public Service addService(Service service) {
    this.services.add(service);
    return service;
  }

  public Service getServiceForNamespace(String namespace) {
    for (Service svc : services) {
      if (svc.getNamespace().equals(namespace)) {
        return svc;
      }
    }
    return null;
  }

  public List<Service> getServices() {
    Collections.sort(this.services, new Service.ServiceComparator());
    return Collections.unmodifiableList(this.services);
  }

  /** Gets a list of all commands in all services. */
  public List<Command> getAllCommands() {
    List<Command> allCommands = Lists.newLinkedList();

    Iterator<Service> sit = this.getServices().iterator();
    while (sit.hasNext()) {
      Service s = sit.next();
      Iterator<Command> cit = s.getCommands().iterator();
      while (cit.hasNext()) {
        Command c = cit.next();
        allCommands.add(c);
      }
    }

    Collections.sort(allCommands, new Command.CommandComparator());
    return allCommands;
  }

  public void dump() {
    this.dump(false);
  }

  public void dump(boolean dumpCommands) {
    System.out.println("Dump doc root...");
    System.out.println(this);

    System.out.println("Dump services...");
    Iterator it = this.services.iterator();
    while (it.hasNext()) {
      Service s = (Service) it.next();
      s.dump(dumpCommands);
    }
  }

  @Override
  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append("[docroot;hashCode=").append(hashCode());
    buf.append(";serviceCount=").append(this.services.size());
    buf.append("]");
    return buf.toString();
  }
} // end Root class
