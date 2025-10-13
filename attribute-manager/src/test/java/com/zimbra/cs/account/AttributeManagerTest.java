package com.zimbra.cs.account;


import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class AttributeManagerTest {

  @AfterEach
  void cleanup() {
    AttributeManager.destroy();
  }

  @Test
  void shouldLoadAllAttributesWhenUsingSingleton() throws AttributeManagerException {
    final AttributeManager attributeManager = AttributeManager.getInstance();

    assertLoadedAllAttributes(attributeManager);
  }

  @Test
  void shouldLoadAllAttributesFromResourceByDefaultWhenUsingSingleton()
      throws AttributeManagerException {
    final AttributeManager attributeManager = AttributeManager.getInstance();
    assertLoadedAllAttributes(attributeManager);
  }

  @Test
  void shouldLoadAllAttributesFromResources() throws AttributeManagerException {
    final AttributeManager attributeManager = AttributeManager.fromResource();
    assertLoadedAllAttributes(attributeManager);
  }

  private void assertLoadedAllAttributes(AttributeManager attributeManager) {
    final Map<String, AttributeInfo> allAttrs = attributeManager.getAttrs();
    assertEquals(1866, allAttrs.size());
  }
}