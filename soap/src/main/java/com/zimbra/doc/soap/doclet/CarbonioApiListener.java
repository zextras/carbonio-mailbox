// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap.doclet;

import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ElementKind;

import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.DocTree;
import com.sun.source.doctree.UnknownBlockTagTree;
import com.sun.source.util.DocTrees;
import jdk.javadoc.doclet.DocletEnvironment;

import com.zimbra.doc.soap.ApiClassDocumentation;
import com.zimbra.doc.soap.ZmApiTags;

/**
 * This class listens to and processes the doclet API to extract annotations and tags.
 */
public class CarbonioApiListener {

    /**
     * Maps class names to related documentation
     */
    private final Map<String, ApiClassDocumentation> docMap = new HashMap<>();
    private final DocTrees docTrees;

    CarbonioApiListener(DocTrees docTrees) {
        this.docTrees = docTrees;
    }

    /**
     * Processes the Javadoc results using the new Doclet API
     */
    public void processJavadocResults(DocletEnvironment docEnv) {
        for (Element elementSpecified : docEnv.getSpecifiedElements()) {
            for (Element element : elementSpecified.getEnclosedElements()) {
                if (element.getKind() == ElementKind.CLASS) {
                    processClass((TypeElement) element);
                }
            }
        }
    }

    /**
     * Processes a class and its fields and methods.
     */
    private void processClass(TypeElement classElement) {
        ApiClassDocumentation doc = new ApiClassDocumentation();

        processClassTags(doc, classElement);

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
        DocCommentTree docCommentTree = docTrees.getDocCommentTree(classElement);
        if (docCommentTree != null) {
            for (DocTree tag : docCommentTree.getBlockTags()) {
                if (tag instanceof UnknownBlockTagTree customTag) {
                    String tagName = customTag.getTagName();
                    String tagContent = getContent(customTag);
                    switch (tagName) {
                        case ZmApiTags.TAG_COMMAND_DESCRIPTION:
                            doc.setCommandDescription(tagContent);
                            break;
                        case ZmApiTags.TAG_COMMAND_REQUEST_DESCRIPTION, ZmApiTags.TAG_COMMAND_RESPONSE_DESCRIPTION:
                            doc.setClassDescription(tagContent);
                            break;
                        case ZmApiTags.TAG_COMMAND_NETWORK_ONLY:
                            doc.setNetworkEdition(true);
                            break;
                        case ZmApiTags.TAG_COMMAND_DEPRECATION_INFO:
                            doc.setDeprecationDescription(tagContent);
                            break;
                        case ZmApiTags.TAG_COMMAND_AUTH_REQUIRED:
                            doc.setAuthRequiredDescription(tagContent);
                            break;
                        case ZmApiTags.TAG_COMMAND_ADMIN_AUTH_REQUIRED:
                            doc.setAdminAuthRequiredDescription(tagContent);
                            break;
                        default:
                            // Handle any other cases if needed
                            break;
                    }
                }
            }
        }

    }

    private String getContent(UnknownBlockTagTree customTag) {
        return customTag.getContent().stream()
                .map(DocTree::toString)
                .collect(Collectors.joining());
    }

    /**
     * Process class-level tags (represented as comments or annotations).
     */
    private void processFieldTags(ApiClassDocumentation doc, VariableElement fieldElement) {
        DocCommentTree docCommentTree = docTrees.getDocCommentTree(fieldElement);
        if (docCommentTree != null) {
            for (DocTree tag : docCommentTree.getBlockTags()) {
                if (tag instanceof UnknownBlockTagTree customTag) {
                    String tagName = customTag.getTagName();
                    String tagContent = getContent(customTag);
                    if (ZmApiTags.TAG_FIELD_DESCRIPTION.equals(tagName)) {
                        doc.addFieldDescription(fieldElement.getSimpleName().toString(), tagContent);
                    } else if (ZmApiTags.TAG_FIELD_TAG.equals(tagName)) {
                        doc.addFieldTag(fieldElement.getSimpleName().toString(), tagContent);
                    }
                }
            }

        }
    }

    private void processMethodTags(ApiClassDocumentation doc, ExecutableElement methodElement) {
        String fieldName = guessFieldNameFromGetterOrSetter(methodElement.getSimpleName().toString());
        if (fieldName == null) {
            return;
        }

        DocCommentTree docCommentTree = docTrees.getDocCommentTree(methodElement);
        if (docCommentTree != null) {
            for (DocTree tag : docCommentTree.getBlockTags()) {
                if (tag instanceof UnknownBlockTagTree customTag) {
                    String tagName = customTag.getTagName();
                    String tagContent = getContent(customTag);
                    if (ZmApiTags.TAG_FIELD_DESCRIPTION.equals(tagName)) {
                        doc.addFieldDescription(fieldName, tagContent);
                    } else if (ZmApiTags.TAG_FIELD_TAG.equals(tagName)) {
                        doc.addFieldTag(fieldName, tagContent);
                    }
                }
            }
        }
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
