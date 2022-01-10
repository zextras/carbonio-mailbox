// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.doc.soap.apidesc;

import java.util.List;

import com.google.common.collect.Lists;
import com.zimbra.doc.soap.ChoiceNode;
import com.zimbra.doc.soap.DescriptionNode;
import com.zimbra.doc.soap.XmlElementDescription;

public class SoapApiElementChoiceInfo
implements SoapApiElementArtifact {
    private final boolean singleChild;
    private final List<SoapApiElementArtifact> subElements = Lists.newArrayList();

    /* no-argument constructor needed for deserialization */
    @SuppressWarnings("unused")
    private SoapApiElementChoiceInfo() {
        singleChild = true;
    }

    public SoapApiElementChoiceInfo(ChoiceNode descNode) {
        singleChild = descNode.isSingleChild();
        for (DescriptionNode child : descNode.getChildren()) {
            if (child instanceof XmlElementDescription) {
                XmlElementDescription elemChild = (XmlElementDescription) child;
                if (elemChild.isWrapper()) {
                    subElements.add(new SoapApiWrapperElement(elemChild));
                } else if (elemChild.isJaxbType()) {
                    subElements.add(new SoapApiElement(elemChild));
                } else {
                    subElements.add(new SoapApiSimpleElement(elemChild));
                }
            } else if (child instanceof ChoiceNode) {
                subElements.add(new SoapApiElementChoiceInfo((ChoiceNode)child));
            }
        }
    }

    public boolean isSingleChild() { return singleChild; }
    public List<SoapApiElementArtifact> getSubElements() { return subElements; }
}
