package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RightManagerTest {

  @Test
  void shouldLoadRights() throws ServiceException {

    final AttributeManager attributeManager = new AttributeManager("../store/conf/attrs");
    attributeManager.computeClassToAllAttrsMap();
    RightManager rightManager = new RightManager("../store-conf/conf/rights", false, attributeManager);

    Assertions.assertFalse(rightManager.getAllAdminRights().isEmpty());
    Assertions.assertFalse(rightManager.getAllUserRights().isEmpty());
  }

  @Test
  void shouldLoadRightsUsingSingleton() throws ServiceException {
    LC.zimbra_attrs_directory.setDefault("../store/conf/attrs");
    LC.zimbra_rights_directory.setDefault("../store-conf/conf/rights");
    RightManager rightManager = RightManager.getInstance();
    Assertions.assertFalse(rightManager.getAllAdminRights().isEmpty());
    Assertions.assertFalse(rightManager.getAllUserRights().isEmpty());
  }

}