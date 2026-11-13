package com.zextras.mailbox.feature;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import org.jooq.JSONB;

final class AttributesJson {

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final TypeReference<Map<String, List<String>>> ATTRIBUTE_MAP =
      new TypeReference<>() {};

  private AttributesJson() {}

  static JSONB toJson(Map<String, List<String>> attributes) {
    try {
      return JSONB.valueOf(JSON.writeValueAsString(attributes));
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
  }

  static Map<String, List<String>> fromJson(JSONB attributes) {
    try {
      return JSON.readValue(attributes.data(), ATTRIBUTE_MAP);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
  }
}
