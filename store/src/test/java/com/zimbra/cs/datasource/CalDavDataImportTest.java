// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.datasource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.DataSource;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CalDavDataImportTest {

  @Test
  void shouldNotPushLocalChangesWhenImportOnlyEnabled() throws ServiceException {
    DataSource ds = Mockito.mock(DataSource.class);
    Mockito.when(ds.getAccount()).thenReturn(null);
    Mockito.when(ds.isImportOnly()).thenReturn(true);

    CalDavDataImport importer = new CalDavDataImport(ds, true);

    assertFalse(importer.shouldPushLocalChanges(1));
    assertFalse(importer.shouldPushLocalChanges(123));
  }

  @Test
  void shouldPushLocalChangesWhenImportOnlyDisabledAndNotInitialSync() throws ServiceException {
    DataSource ds = Mockito.mock(DataSource.class);
    Mockito.when(ds.getAccount()).thenReturn(null);
    Mockito.when(ds.isImportOnly()).thenReturn(false);

    CalDavDataImport importer = new CalDavDataImport(ds, true);

    assertTrue(importer.shouldPushLocalChanges(1));
  }

  @Test
  void shouldNotPushLocalChangesOnInitialSyncEvenWhenImportOnlyDisabled() throws ServiceException {
    DataSource ds = Mockito.mock(DataSource.class);
    Mockito.when(ds.getAccount()).thenReturn(null);
    Mockito.when(ds.isImportOnly()).thenReturn(false);

    CalDavDataImport importer = new CalDavDataImport(ds, true);

    assertFalse(importer.shouldPushLocalChanges(0));
  }
}

