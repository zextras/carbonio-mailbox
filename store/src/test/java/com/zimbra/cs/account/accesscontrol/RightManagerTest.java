package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RightManagerTest {

  @Test
  void shouldLoadRights() throws ServiceException {
    LC.zimbra_rights_directory.setDefault("../store-conf/conf/rights");
    LC.zimbra_attrs_directory.setDefault("../store/conf/attrs");
    RightManager rightManager = RightManager.getInstance();
    Assertions.assertFalse(rightManager.getAllAdminRights().isEmpty());
    Assertions.assertFalse(rightManager.getAllUserRights().isEmpty());
  }

}