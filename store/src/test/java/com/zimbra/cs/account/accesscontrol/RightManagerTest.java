package com.zimbra.cs.account.accesscontrol;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RightManagerTest {

  @AfterEach
  void cleanup() {
    RightManager.destroy();
    LC.zimbra_rights_directory.setDefault("");
  }

  @Test
  void shouldLoadRightsUsingFilesystem() throws ServiceException {
    final AttributeManager attributeManager = AttributeManager.fromFileSystem("src/main/resources/conf/attrs");
    RightManager rightManager = RightManager.fromFileSystem("src/main/resources/conf/rights", attributeManager);

    assertRightsLoaded(rightManager);
  }

  @Test
  void shouldLoadRightsUsingResources() throws ServiceException {
    final AttributeManager attributeManager = AttributeManager.fromFileSystem("src/main/resources/conf/attrs");
    RightManager rightManager = RightManager.fromResources(attributeManager);

    assertRightsLoaded(rightManager);
  }

  @Test
  void shouldLoadRightsFromResourceWhenUsingSingleton() throws ServiceException {
    LC.zimbra_attrs_directory.setDefault("src/main/resources/conf/attrs");
    RightManager rightManager = RightManager.getInstance();

    assertRightsLoaded(rightManager);
  }

  @Test
  void shouldLoadRightsFromFileSystemWhenUsingSingleton() throws ServiceException {
    LC.zimbra_rights_directory.setDefault("src/main/resources/conf/rights");
    LC.zimbra_attrs_directory.setDefault("src/main/resources/conf/attrs");
    RightManager rightManager = RightManager.getInstance();

    assertRightsLoaded(rightManager);
  }

  @Test
  void shouldFailWhenRightDirectoryDoesNotExist() throws ServiceException {
    final AttributeManager attributeManager = AttributeManager.fromFileSystem("src/main/resources/conf/attrs");

    final ServiceException thrownException = Assertions.assertThrows(ServiceException.class,
        () -> RightManager.fromFileSystem("src/main/resources/non-existing", attributeManager));
    Assertions.assertEquals("system failure: rights directory does not exist: src/main/resources/non-existing", thrownException.getMessage());
  }

  private void assertRightsLoaded(RightManager rightManager) throws ServiceException {
    Assertions.assertEquals(423, rightManager.getAllAdminRights().size());
    Assertions.assertEquals(11, rightManager.getAllUserRights().size());

    final AdminRight domainAdminRights = rightManager.getAdminRight("domainAdminRights");
    Assertions.assertTrue(domainAdminRights.isComboRight());
  }
}