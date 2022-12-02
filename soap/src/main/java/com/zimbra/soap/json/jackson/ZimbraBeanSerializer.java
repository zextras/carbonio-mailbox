// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.json.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanSerializer;
import com.zimbra.common.soap.Element;

/**
 * Zimbra specific BeanSerializer
 */
public class ZimbraBeanSerializer extends BeanSerializer {
    /**
     * 
     */
    private static final long serialVersionUID = 52939088950238993L;




    public ZimbraBeanSerializer(BeanSerializer src) {
        super(src);
    }

    /**
     * Based on {@code BeanSerializer.serialize} but allows the addition of Zimbra's namespace property "_jsns"
     * to the list of properties serialized.
     */
    public final void serializeWithNamespace(Object bean, JsonGenerator jgen, SerializerProvider provider,
            String namespace)
    throws IOException, JsonGenerationException {
        jgen.writeStartObject();
        if (_propertyFilterId != null) {
            serializeFieldsFiltered(bean, jgen, provider);
        } else {
            serializeFields(bean, jgen, provider);
        }
        if (namespace != null) {
            jgen.writeStringField(Element.JSONElement.A_NAMESPACE /* _jsns */, namespace);
        }
        jgen.writeEndObject();
    }
}
