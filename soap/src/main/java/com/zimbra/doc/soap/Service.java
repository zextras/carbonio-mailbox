// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

public class Service implements java.io.Serializable {

    private static final long serialVersionUID = 1L;

    private List<Command> commands = Lists.newLinkedList();

    private String className = null;
    private String namespace = null;
    private String name = null;
    public  String description = null;

    Service(String className, String namespace) {
        this.className = className;
        this.namespace = namespace;
        this.name = namespace.replaceFirst("urn:", "");
    }

    public String getName() {
        return this.name;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getClassName() {
        return this.className;
    }

    public String getDescription() {
        return (this.description == null) ? "" : this.description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public Command getCommand(String namespace, String name) {
        for (Command cmd : commands) {
            if ( (name.equals(cmd.getName())) && (namespace.equals(cmd.getNamespace())) ) {
                return cmd;
            }
        }
        return null;
    }

    public List<Command> getCommands() {
        List<Command> allCommands = Lists.newLinkedList();

      allCommands.addAll(this.commands);

      Collections.sort(allCommands, new Command.CommandComparator());

        return allCommands;
    }

    public Command addCommand(Command cmd) {
        if (cmd == null) {
            return null;
        }

        if (DocExcludedServices.isExclude(cmd.getName())) {
            return cmd;
        }

        this.commands.add(cmd);
        return cmd;
    }

    /**
     * Dumps the contents to <code>System.out.println</code>
     */
    public void dump(boolean dumpCommands) {

        System.out.println("Dump service...");
        System.out.println(this);

        if (dumpCommands) {
            System.out.println("Dump commands...");
            Iterator<Command> it = this.commands.iterator();
            while (it.hasNext()) {
                Command c = it.next();

                c.dump();
            }
        }
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append("[service;hashCode=").append(hashCode());
        buf.append(";name=").append(this.getName());
        buf.append(";description=").append(this.getDescription());
        buf.append(";commandCount=").append(this.commands.size());
        buf.append("]");
        return buf.toString();
    }

    public static class ServiceComparator implements java.util.Comparator<Service> {
        @Override
        public int compare(Service o1, Service o2) {
            String n1 = o1.getName();
            String n2 = o2.getName();

            return    n1.compareTo(n2);
        }
    }

}
