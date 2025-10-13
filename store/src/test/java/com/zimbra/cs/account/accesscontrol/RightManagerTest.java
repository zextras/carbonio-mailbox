package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeManager;
import com.zimbra.cs.account.AttributeManagerException;
import javax.management.AttributeNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RightManagerTest {

  @AfterEach
  void cleanup() {
    RightManager.destroy();
  }

  @Test
  void shouldLoadRightsUsingResources() throws Exception {
    final AttributeManager attributeManager = AttributeManager.fromResource();
    RightManager rightManager = RightManager.fromResources(attributeManager);

    assertRightsLoaded(rightManager);
  }

  @Test
  void shouldLoadRightsFromResourceWhenUsingSingleton() throws ServiceException {
    RightManager rightManager = RightManager.getInstance();

    assertRightsLoaded(rightManager);
  }

  private void assertRightsLoaded(RightManager rightManager) throws ServiceException {
    Assertions.assertEquals(419, rightManager.getAllAdminRights().size());
    Assertions.assertEquals(11, rightManager.getAllUserRights().size());

    final AdminRight domainAdminRights = rightManager.getAdminRight("domainAdminRights");
    Assertions.assertTrue(domainAdminRights.isComboRight());
  }
}