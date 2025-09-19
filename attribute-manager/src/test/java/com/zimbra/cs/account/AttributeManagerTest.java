package com.zimbra.cs.account;


import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.service.ServiceException;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AttributeManagerTest {

  private static final String BASE_ATTRIBUTES_DIRECTORY_PATH = "src/main/resources/conf/attrs";

  @AfterEach
  void cleanup() {
    AttributeManager.destroy();
  }

  @Test
  void shouldLoadAllAttributesWhenUsingSingleton() throws ServiceException {
    final AttributeManager attributeManager = AttributeManager.getInstance();

    assertLoadedAllAttributes(attributeManager);
  }

  @Test
  void shouldLoadAllAttributesFromResourceByDefaultWhenUsingSingleton()
      throws ServiceException {
    final AttributeManager attributeManager = AttributeManager.getInstance();
    assertLoadedAllAttributes(attributeManager);
  }

  @Test
  void shouldLoadAllAttributesFromResources() throws ServiceException {
    final AttributeManager attributeManager = AttributeManager.fromResource();
    assertLoadedAllAttributes(attributeManager);
  }

  @Test
  void shouldLoadAllAttributesFromAttrsFile() throws ServiceException {
    final AttributeManager attributeManager = AttributeManager.fromFileSystem(
        BASE_ATTRIBUTES_DIRECTORY_PATH);

    assertLoadedAllAttributes(attributeManager);
  }

  private void assertLoadedAllAttributes(AttributeManager attributeManager) {
    final Map<String, AttributeInfo> allAttrs = attributeManager.getAttrs();
    assertEquals(1866, allAttrs.size());
  }
}