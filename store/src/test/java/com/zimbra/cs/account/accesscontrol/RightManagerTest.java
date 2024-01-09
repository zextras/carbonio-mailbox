package com.zimbra.cs.account.accesscontrol;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.service.ServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RightManagerTest {

  @Test
  void shouldLoadRights() throws ServiceException {
    RightManager rightManager = new RightManager("../store-conf/conf/rights/", false);
    Assertions.assertFalse(rightManager.getAllAdminRights().isEmpty());
  }

}