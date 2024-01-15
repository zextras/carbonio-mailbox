package com.zimbra.cs.account;


import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AttributeManagerTest {

  @Test
  void shouldLoadAllAttributesFromAttrsFile() throws ServiceException {
    final AttributeManager attributeManager = AttributeManager.fromFileSystem("src/main/resources/conf/attrs");
    final Map<String, AttributeInfo> allAttrs = attributeManager.getAttrs();

    assertLoadedAllAttributes(allAttrs);
  }

  @Test
  void shouldLoadAllAttributesWhenUsingSingleton() throws ServiceException {
    LC.zimbra_attrs_directory.setDefault("src/main/resources/conf/attrs");

    final AttributeManager attributeManager = AttributeManager.getInstance();
    final Map<String, AttributeInfo> allAttrs = attributeManager.getAttrs();

    assertLoadedAllAttributes(allAttrs);
  }

  private void assertLoadedAllAttributes(Map<String, AttributeInfo> allAttrs) {
    assertEquals(1841, allAttrs.size());
  }
}