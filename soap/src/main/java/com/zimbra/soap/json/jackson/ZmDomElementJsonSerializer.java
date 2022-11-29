// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.json.jackson;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zimbra.common.soap.Element.JSONElement;

public class ZmDomElementJsonSerializer
extends StdSerializer<Element>
{


    /**
     * 
     */
    private static final long serialVersionUID = -2141574675978397686L;


    public ZmDomElementJsonSerializer() {
        super(Element.class);
    }
    
    /* (non-Javadoc)
     * @see com.fasterxml.jackson.databind.ser.std.StdSerializer#serialize(java.lang.Object, com.fasterxml.jackson.core.JsonGenerator, com.fasterxml.jackson.databind.SerializerProvider)
     */
    @Override
    public void serialize(Element value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException {
        jgen.writeStartArray();
        serializeInner(value, jgen, provider, null /* parent namespaceURI */);
        jgen.writeEndArray();
    }

    private void serializeInner(Element value, JsonGenerator jgen, SerializerProvider provider, String parentNs)
    throws IOException, JsonGenerationException {
        String namespaceURI = value.getNamespaceURI();
        jgen.writeStartObject();
        NamedNodeMap attributes = value.getAttributes();
        if (attributes != null && attributes.getLength() > 0) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr) attributes.item(i);
                if ("xmlns".equals(attribute.getName())) {
                    if (!attribute.getValue().startsWith("urn:zimbra")) {
                        jgen.writeStringField(attribute.getName(), attribute.getValue());
                    }
                } else {
                    jgen.writeStringField(attribute.getName(), attribute.getValue());
                }
                // Not supporting attributes in different namespaces
            }
        }

        Map<String,List<Element>> elemMap = Maps.newTreeMap();
        NodeList children = value.getChildNodes();
        if (children != null && children.getLength() > 0) {
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                switch (child.getNodeType()) {
                    case Node.CDATA_SECTION_NODE:
                    case Node.TEXT_NODE:
                        String txt = child.getNodeValue();
                        if ((txt == null) || ((txt.startsWith("\n")) && (txt.trim().length() == 0))) {
                            break;  // Almost certainly only formatting text
                        }
                        jgen.writeStringField(JSONElement.A_CONTENT /* "_content" */, txt);
                        break;
                    case Node.ELEMENT_NODE:
                        Element elem = (Element) child;
                        String eleName = elem.getLocalName();
                        List<Element> elems = elemMap.get(eleName);
                        if (elems == null) {
                            elems = Lists.newArrayList();
                            elemMap.put(eleName, elems);
                        }
                        elems.add(elem);
                        break;
                }
            }
            for (Entry<String, List<Element>> entry : elemMap.entrySet()) {
                jgen.writeArrayFieldStart(entry.getKey());
                for (Element elem : entry.getValue()) {
                    serializeInner(elem, jgen, provider, namespaceURI);
                }
                jgen.writeEndArray();
            }
        }

        if ((namespaceURI != null) && (!namespaceURI.equals(parentNs))) {
            if (!namespaceURI.startsWith("urn:zimbra")) {
                jgen.writeStringField(JSONElement.A_NAMESPACE, namespaceURI);
            }
        }
        jgen.writeEndObject();
    }
}
