package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RightManagerTest {

  @Test
  void shouldLoadRightsUsingFilesystem() throws ServiceException {

    final AttributeManager attributeManager = new AttributeManager("../store/conf/attrs");
    RightManager rightManager = RightManager.fromFileSystem("src/main/resources/conf/rights", false, attributeManager);

    assertRightsLoaded(rightManager);
  }

  @Test
  void shouldLoadRightsUsingResources() throws ServiceException {
    final AttributeManager attributeManager = new AttributeManager("../store/conf/attrs");
    RightManager rightManager = RightManager.fromResources(attributeManager);

    assertRightsLoaded(rightManager);
  }

  @Test
  void shouldLoadRightsUsingSingleton() throws ServiceException {
    LC.zimbra_attrs_directory.setDefault("../store/conf/attrs");
    LC.zimbra_rights_directory.setDefault("src/main/resources/conf/rights");
    RightManager rightManager = RightManager.getInstance();

    assertRightsLoaded(rightManager);
  }

  private void assertRightsLoaded(RightManager rightManager) throws ServiceException {
    Assertions.assertFalse(rightManager.getAllAdminRights().isEmpty());
    Assertions.assertFalse(rightManager.getAllUserRights().isEmpty());
    Assertions.assertEquals(423, rightManager.getAllAdminRights().size());
    Assertions.assertEquals(11, rightManager.getAllUserRights().size());

    final AdminRight domainAdminRights = rightManager.getAdminRight("domainAdminRights");
    Assertions.assertTrue(domainAdminRights.isComboRight());
  }


}