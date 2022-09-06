// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.json.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.zimbra.soap.type.ZmBoolean;
import java.io.IOException;

/**
 * For Zimbra SOAP, Historically Booleans have been represented as "0" for false and "1" for true in
 * XML. This is valid but differs from the default values JAXB marshals to - "true" and "false".
 *
 * <p>Some legacy client code cannot accept the values "true" and "false", so the ZmBoolean class
 * has been introduced whose values will always marshal to either "0" or "1".
 *
 * <p>However, for JSON SOAP, the values true and false need to be used. This serializer is
 * responsible for ensuring that happens.
 */
public class ZmBooleanSerializer extends JsonSerializer<ZmBoolean> {

  public ZmBooleanSerializer() {
    super();
  }

  @Override
  public void serialize(ZmBoolean value, JsonGenerator jgen, SerializerProvider provider)
      throws IOException, JsonProcessingException {
    if (value == null) {
      return;
    }
    jgen.writeBoolean(ZmBoolean.toBool(value));
  }
}
