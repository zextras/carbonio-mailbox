package com.zimbra.cs.account;


import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.Sets;
import com.zimbra.common.service.ServiceException;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AttributeManagerTest {

  @Test
  void addAttributeShouldAddAttribute() {
    final AttributeManager attributeManager = new AttributeManager();
    Set<AttributeClass> requiredIn =
        Sets.newHashSet(
            AttributeClass.account,
            AttributeClass.alias,
            AttributeClass.distributionList,
            AttributeClass.domain,
            AttributeClass.server,
            AttributeClass.cos,
            AttributeClass.xmppComponent,
            AttributeClass.group,
            AttributeClass.groupDynamicUnit,
            AttributeClass.groupStaticUnit);
    Set<AttributeFlag> flags = Sets.newHashSet(AttributeFlag.accountInfo);
    AttributeInfo attributeInfo =
        new AttributeInfo(
            "zimbraId",
            1,
            null,
            0,
            null,
            AttributeType.TYPE_ID,
            null,
            "",
            true,
            null,
            null,
            AttributeCardinality.single,
            requiredIn,
            null,
            flags,
            null,
            null,
            null,
            null,
            null,
            "Zimbra Systems Unique ID",
            null,
            null,
            null);
    attributeManager.addAttribute(attributeInfo);
    final Map<String, AttributeInfo> allAttrs = attributeManager.getAttrs();

    assertEquals(1, allAttrs.size());

    for (String key : allAttrs.keySet()) {
      assertEquals("zimbraId", allAttrs.get(key).getName());
    }
  }

  @Test
  void shouldLoadAllAttributesFromAttrsFile() throws ServiceException {
    final AttributeManager attributeManager = new AttributeManager("../store/conf/attrs");
    final Map<String, AttributeInfo> allAttrs = attributeManager.getAttrs();

    assertEquals(1841, allAttrs.size());
  }
}