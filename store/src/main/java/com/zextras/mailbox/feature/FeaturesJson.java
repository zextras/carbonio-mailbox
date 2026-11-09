package com.zextras.mailbox.feature;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import org.jooq.JSONB;

final class FeaturesJson {

  private static final ObjectMapper JSON = new ObjectMapper();
  private static final TypeReference<Map<String, List<String>>> FEATURE_MAP =
      new TypeReference<>() {};

  private FeaturesJson() {}

  static JSONB toJson(Map<String, List<String>> features) {
    try {
      return JSONB.valueOf(JSON.writeValueAsString(features));
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
  }

  static Map<String, List<String>> fromJson(JSONB features) {
    try {
      return JSON.readValue(features.data(), FEATURE_MAP);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
  }
}
