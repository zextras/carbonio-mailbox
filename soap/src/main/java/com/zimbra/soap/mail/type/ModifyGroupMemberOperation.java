// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.mail.type;

import java.util.Arrays;
import java.util.Map;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

import com.google.common.collect.Maps;
import com.zimbra.common.service.ServiceException;

@XmlEnum
public enum ModifyGroupMemberOperation {
    @XmlEnumValue("+") ADD("+"),
    @XmlEnumValue("-") REMOVE("-"),
    @XmlEnumValue("reset") RESET("reset") ;
    
        private static Map<String, ModifyGroupMemberOperation> nameToView = Maps.newHashMap();

        static {
            for (ModifyGroupMemberOperation v : ModifyGroupMemberOperation.values()) {
                nameToView.put(v.toString(), v);
            }
        }

        private String name;

        private ModifyGroupMemberOperation(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        public static ModifyGroupMemberOperation fromString(String name)
        throws ServiceException {
            ModifyGroupMemberOperation op = nameToView.get(name);
            if (op == null) {
               throw ServiceException.INVALID_REQUEST("unknown Operation: " + name + ", valid values: " +
                       Arrays.asList(ModifyGroupMemberOperation.values()), null);
            }
            return op;
        }

}
