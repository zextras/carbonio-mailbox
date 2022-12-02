// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap.doclet;

import java.util.Map;

import com.google.common.collect.Maps;

import com.zimbra.doc.soap.ApiClassDocumentation;
import com.zimbra.doc.soap.ZmApiTags;
import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.RootDoc;
import com.sun.javadoc.Tag;

public class DocletApiListener {

    /**
     * Maps Class names to related documentation
     */
    private Map<String,ApiClassDocumentation> docMap = Maps.newHashMap();

    public DocletApiListener() {
    }

    public void processJavadocResults(RootDoc rootDoc) {
        for (ClassDoc classDoc : rootDoc.classes()) {
            processClass(classDoc);
        }
    }

    private void processClass(ClassDoc classDoc) {
        ApiClassDocumentation doc = new ApiClassDocumentation();
        processClassTags(doc, classDoc.tags());
        for (FieldDoc fieldDoc : classDoc.fields()) {
            processFieldTags(doc, fieldDoc);
        }
        for (MethodDoc methodDoc : classDoc.methods()) {
            processMethodTags(doc, methodDoc);
        }
        if (doc.hasDocumentation()) {
            docMap.put(classDoc.toString(), doc);
        }
    }

    private void processClassTags(ApiClassDocumentation doc, Tag[] tags) {
        for (Tag tag: tags) {
            if (ZmApiTags.TAG_COMMAND_DESCRIPTION.equals(tag.name())) {
                doc.setCommandDescription(tag.text());
            } else if (ZmApiTags.TAG_COMMAND_REQUEST_DESCRIPTION.equals(tag.name())) {
                doc.setClassDescription(tag.text());
            } else if (ZmApiTags.TAG_COMMAND_NETWORK_ONLY.equals(tag.name())) {
                doc.setNetworkEdition(true);
            } else if (ZmApiTags.TAG_COMMAND_DEPRECATION_INFO.equals(tag.name())) {
                doc.setDeprecationDescription(tag.text());
            } else if (ZmApiTags.TAG_COMMAND_AUTH_REQUIRED.equals(tag.name())) {
                doc.setAuthRequiredDescription(tag.text());
            } else if (ZmApiTags.TAG_COMMAND_ADMIN_AUTH_REQUIRED.equals(tag.name())) {
                doc.setAdminAuthRequiredDescription(tag.text());
            } else if (ZmApiTags.TAG_COMMAND_RESPONSE_DESCRIPTION.equals(tag.name())) {
                doc.setClassDescription(tag.text());
            }
        }
    }

    private void processFieldTags(ApiClassDocumentation doc, FieldDoc fieldDoc) {
        for (Tag tag: fieldDoc.tags()) {
            if (ZmApiTags.TAG_FIELD_DESCRIPTION.equals(tag.name())) {
                doc.addFieldDescription(fieldDoc.name(), tag.text());
            } else if (ZmApiTags.TAG_FIELD_TAG.equals(tag.name())) {
                doc.addFieldTag(fieldDoc.name(), tag.text());
            }
        }
    }

    private void processMethodTags(ApiClassDocumentation doc, MethodDoc methodDoc) {
        for (Tag tag: methodDoc.tags()) {
            String fieldName = guessFieldNameFromGetterOrSetter(methodDoc.name());
            if (fieldName == null) {
                continue;
            }
            if (ZmApiTags.TAG_FIELD_DESCRIPTION.equals(tag.name())) {
                doc.addFieldDescription(fieldName, tag.text());
            } else if (ZmApiTags.TAG_FIELD_TAG.equals(tag.name())) {
                doc.addFieldTag(fieldName, tag.text());
            }
        }
    }

    private String guessFieldNameFromGetterOrSetter(String methodName) {
        String fieldName = null;
        if ((methodName.startsWith("set")) || (methodName.startsWith("get"))) {
            fieldName = methodName.substring(3,4).toLowerCase() + methodName.substring(4);
        } else if (methodName.startsWith("is")) {
            fieldName = methodName.substring(2,3).toLowerCase() + methodName.substring(3);
        }
        return fieldName;
    }

    public Map<String,ApiClassDocumentation> getDocMap() {
        return docMap;
    }

}
