package com.zimbra.cs.account;


import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AttributeManagerTest {

  private static final String BASE_ATTRIBUTES_DIRECTORY_PATH = "src/main/resources/conf/attrs";

  @AfterEach
  void cleanup() {
    AttributeManager.destroy();
    LC.zimbra_attrs_directory.setDefault(LC.DEFAULT_ATTRS_DIRECTORY);
  }

  @Test
  void shouldLoadAllAttributesWhenUsingSingletonAndZimbraAttrsDirectoryIsSet()
      throws ServiceException {
    LC.zimbra_attrs_directory.setDefault(BASE_ATTRIBUTES_DIRECTORY_PATH);

    final AttributeManager attributeManager = AttributeManager.getInstance();

    assertLoadedAllAttributes(attributeManager);
  }

  @Test
  void shouldThrowExceptionWhenTheFileSystemPathDoesntExist() {
    LC.zimbra_attrs_directory.setDefault("src/main/resources/conf/non-existing-directory");

    final ServiceException serviceException = Assertions.assertThrows(ServiceException.class,
        AttributeManager::getInstance);

    Assertions.assertEquals(
        "system failure: attrs directory does not exists: src/main/resources/conf/non-existing-directory",
        serviceException.getMessage());
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
    assertEquals(1843, allAttrs.size());
  }

}