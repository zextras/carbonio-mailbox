// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap;

import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;

public class ApiClassDocumentation {

    private String commandDescription;
    private boolean networkEdition;
    private String classDescription;
    private String deprecationDescription;
    private String authRequiredDescription;
    private String adminAuthRequiredDescription;
    private Map <String, String> fieldDescriptions = Maps.newHashMap();
    private Map <String, String> fieldTags = Maps.newHashMap();

    public ApiClassDocumentation() {
    }

    public boolean hasDocumentation() {
        if (commandDescription != null) {
            return true;
        }
        if (classDescription != null) {
            return true;
        }
        if (!fieldDescriptions.isEmpty()) {
            return true;
        }
        return false;
    }

    public void setCommandDescription(String commandDescription) {
        this.commandDescription = commandDescription;
    }

    public String getCommandDescription() {
        return commandDescription;
    }

    public void setClassDescription(String classDescription) {
        this.classDescription = classDescription;
    }

    public String getClassDescription() {
        return classDescription;
    }

    public void addFieldDescription(String field, String description) {
        this.fieldDescriptions.put(field, description);
    }

    public Map <String, String> getFieldDescription() {
        return fieldDescriptions;
    }

    public void addFieldTag(String field, String description) {
        this.fieldTags.put(field, description);
    }

    public Map <String, String> getFieldTag() {
        return fieldTags;
    }

    public void setNetworkEdition(boolean networkEdition) {
        this.networkEdition = networkEdition;
    }

    public boolean isNetworkEdition() {
        return networkEdition;
    }

    public String getDeprecationDescription() {
        return deprecationDescription;
    }

    public void setDeprecationDescription(String deprecationDescription) {
        this.deprecationDescription = deprecationDescription;
    }

    public String getAuthRequiredDescription() {
        return authRequiredDescription;
    }

    public void setAuthRequiredDescription(String authRequiredDescription) {
        this.authRequiredDescription = authRequiredDescription;
    }

    public String getAdminAuthRequiredDescription() {
        return adminAuthRequiredDescription;
    }

    public void setAdminAuthRequiredDescription(
            String adminAuthRequiredDescription) {
        this.adminAuthRequiredDescription = adminAuthRequiredDescription;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (networkEdition) {
            buf.append("[NetworkEdition:TRUE]");
        }
        if (commandDescription != null) {
            buf.append("[CMD:").append(commandDescription).append("]");
        }
        if (classDescription != null) {
            buf.append("[CLASS:").append(classDescription).append("]");
        }
        if (deprecationDescription != null) {
            buf.append("[DEPRECATION:").append(deprecationDescription).append("]");
        }
        if (authRequiredDescription != null) {
            buf.append("[AUTH_REQUIRED:").append(authRequiredDescription).append("]");
        }
        if (adminAuthRequiredDescription != null) {
            buf.append("[ADMIN_AUTH_REQUIRED:").append(adminAuthRequiredDescription).append("]");
        }
        for (Entry<String, String> entry : fieldDescriptions.entrySet()) {
            buf.append("[FIELD:").append(entry.getKey()).append("=").append(entry.getValue()).append("]");
        }
        for (Entry<String, String> entry : fieldTags.entrySet()) {
            buf.append("[FIELD-TAG:").append(entry.getKey()).append("=").append(entry.getValue()).append("]");
        }
        return buf.toString();
    }
}
