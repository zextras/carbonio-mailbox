package com.zimbra.cs.account;


import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AttributeManagerTest {

  @AfterEach
  void cleanup() {
    AttributeManager.destroy();
    LC.zimbra_attrs_directory.setDefault(LC.DEFAULT_ATTRS_DIRECTORY);
  }

  @Test
  void shouldLoadAllAttributesWhenUsingSingletonAndZimbraAttrsDirectoryIsSet()
      throws ServiceException {
    LC.zimbra_attrs_directory.setDefault("src/main/resources/conf/attrs");

    final AttributeManager attributeManager = AttributeManager.getInstance();

    assertLoadedAllAttributes(attributeManager);
  }

  @Test
  void shouldThrowExceptionWhenTheFileSystemPathDoesntExist() throws ServiceException {
    LC.zimbra_attrs_directory.setDefault("src/main/resources/conf/attrss");

    final ServiceException serviceException = Assertions.assertThrows(ServiceException.class,
        AttributeManager::getInstance);

    Assertions.assertEquals(
        "system failure: attrs directory does not exists: src/main/resources/conf/attrss",
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
        "src/main/resources/conf/attrs");

    assertLoadedAllAttributes(attributeManager);
  }

  private void assertLoadedAllAttributes(AttributeManager attributeManager) {
    final Map<String, AttributeInfo> allAttrs = attributeManager.getAttrs();
    assertEquals(1841, allAttrs.size());
  }

}