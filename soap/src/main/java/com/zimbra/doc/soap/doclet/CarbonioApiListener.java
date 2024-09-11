// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap.doclet;

import java.util.Map;
import java.util.HashMap;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ElementKind;

import jdk.javadoc.doclet.DocletEnvironment;

import com.zimbra.doc.soap.ApiClassDocumentation;
import com.zimbra.doc.soap.ZmApiTags;
import jdk.javadoc.doclet.Reporter;

/**
 * This class listens to and processes the doclet API to extract annotations and tags.
 */
public class CarbonioApiListener {

    /**
     * Maps class names to related documentation
     */
    private Map<String, ApiClassDocumentation> docMap = new HashMap<>();
    private Reporter reporter;

    CarbonioApiListener(Reporter reporter) {
        this.reporter = reporter;
    }

    /**
     * Processes the Javadoc results using the new Doclet API
     */
    public void processJavadocResults(DocletEnvironment docEnv) {
        for (Element element : docEnv.getIncludedElements()) {
            System.out.println("CarbonioApiListener element: " + element.getSimpleName());
            if (element.getKind() == ElementKind.CLASS) {
                processClass((TypeElement) element);
            }
        }
    }

    /**
     * Processes a class and its fields and methods.
     */
    private void processClass(TypeElement classElement) {
        ApiClassDocumentation doc = new ApiClassDocumentation();

        // Process class-level tags (annotations or comments)
        processClassTags(doc, classElement);

        // Process fields
        for (Element enclosedElement : classElement.getEnclosedElements()) {
            if (enclosedElement.getKind() == ElementKind.FIELD) {
                processFieldTags(doc, (VariableElement) enclosedElement);
            } else if (enclosedElement.getKind() == ElementKind.METHOD) {
                processMethodTags(doc, (ExecutableElement) enclosedElement);
            }
        }

        if (doc.hasDocumentation()) {
            docMap.put(classElement.getQualifiedName().toString(), doc);
        }
    }

    private void processClassTags(ApiClassDocumentation doc, TypeElement classElement) {
        for (AnnotationMirror annotationMirror : classElement.getAnnotationMirrors()) {
            String annotationName = annotationMirror.getAnnotationType().toString();
            // Process the annotation based on its name
            if (ZmApiTags.TAG_COMMAND_DESCRIPTION.equals(annotationName)) {
                doc.setCommandDescription(getAnnotationValue(annotationMirror));
            } else if (ZmApiTags.TAG_COMMAND_REQUEST_DESCRIPTION.equals(annotationName)) {
                doc.setClassDescription(getAnnotationValue(annotationMirror));
            } else if (ZmApiTags.TAG_COMMAND_NETWORK_ONLY.equals(annotationName)) {
                doc.setNetworkEdition(true);
            } else if (ZmApiTags.TAG_COMMAND_DEPRECATION_INFO.equals(annotationName)) {
                doc.setDeprecationDescription(getAnnotationValue(annotationMirror));
            } else if (ZmApiTags.TAG_COMMAND_AUTH_REQUIRED.equals(annotationName)) {
                doc.setAuthRequiredDescription(getAnnotationValue(annotationMirror));
            } else if (ZmApiTags.TAG_COMMAND_ADMIN_AUTH_REQUIRED.equals(annotationName)) {
                doc.setAdminAuthRequiredDescription(getAnnotationValue(annotationMirror));
            } else if (ZmApiTags.TAG_COMMAND_RESPONSE_DESCRIPTION.equals(annotationName)) {
                doc.setClassDescription(getAnnotationValue(annotationMirror));
            }
        }
    }


    /**
     * Process class-level tags (represented as comments or annotations).
     */
    private void processFieldTags(ApiClassDocumentation doc, VariableElement fieldElement) {
        for (AnnotationMirror annotationMirror : fieldElement.getAnnotationMirrors()) {
            String annotationName = annotationMirror.getAnnotationType().toString();
            // Process the annotation based on its name
            if (ZmApiTags.TAG_FIELD_DESCRIPTION.equals(annotationName)) {
                doc.addFieldDescription(fieldElement.getSimpleName().toString(), getAnnotationValue(annotationMirror));
            } else if (ZmApiTags.TAG_FIELD_TAG.equals(annotationName)) {
                doc.addFieldTag(fieldElement.getSimpleName().toString(), getAnnotationValue(annotationMirror));
            }
        }
    }

    private void processMethodTags(ApiClassDocumentation doc, ExecutableElement methodElement) {
        String fieldName = guessFieldNameFromGetterOrSetter(methodElement.getSimpleName().toString());
        if (fieldName == null) {
            return;
        }

        for (AnnotationMirror annotationMirror : methodElement.getAnnotationMirrors()) {
            String annotationName = annotationMirror.getAnnotationType().toString();
            if (ZmApiTags.TAG_FIELD_DESCRIPTION.equals(annotationName)) {
                doc.addFieldDescription(fieldName, getAnnotationValue(annotationMirror));
            } else if (ZmApiTags.TAG_FIELD_TAG.equals(annotationName)) {
                doc.addFieldTag(fieldName, getAnnotationValue(annotationMirror));
            }
        }
    }

    private String getAnnotationValue(AnnotationMirror annotationMirror) {
        // You can extract values from AnnotationMirror if necessary
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                annotationMirror.getElementValues().entrySet()) {
            String elementName = entry.getKey().getSimpleName().toString();
            Object value = entry.getValue().getValue();
            return value != null ? value.toString() : "";
        }
        return "";
    }

    /**
     * Guesses the field name from a getter or setter method.
     */
    private String guessFieldNameFromGetterOrSetter(String methodName) {
        String fieldName = null;
        if (methodName.startsWith("set") || methodName.startsWith("get")) {
            fieldName = methodName.substring(3, 4).toLowerCase() + methodName.substring(4);
        } else if (methodName.startsWith("is")) {
            fieldName = methodName.substring(2, 3).toLowerCase() + methodName.substring(3);
        }
        return fieldName;
    }

    public Map<String, ApiClassDocumentation> getDocMap() {
        return docMap;
    }

}
