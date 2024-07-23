package com.zimbra.cs.service.servlet.preview;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.zimbra.common.service.ServiceException;
import org.junit.jupiter.api.Test;

 class ItemIdFactoryTest {

  private final ItemIdFactory itemIdFactory = new ItemIdFactory();

  @Test
   void should_throw_exception_when_itemId_is_null() {
    assertThrows(ServiceException.class, () -> itemIdFactory.create(null, "defaultAccountId"));
  }

  @Test
   void should_throw_exception_when_itemId_is_empty() {
    assertThrows(ServiceException.class, () -> itemIdFactory.create("", "defaultAccountId"));
  }

  @Test
   void should_throw_exception_when_itemId_is_malformed_with_delimiter_at_start() {
    assertThrows(ServiceException.class, () -> itemIdFactory.create(":itemId", "defaultAccountId"));
  }

  @Test
   void should_throw_exception_when_itemId_is_malformed_with_delimiter_at_end() {
    assertThrows(ServiceException.class, () -> itemIdFactory.create("itemId:", "defaultAccountId"));
  }

  @Test
   void should_throw_exception_when_itemId_has_invalid_number_format() {
    assertThrows(ServiceException.class, () -> itemIdFactory.create("invalidNumber", "defaultAccountId"));
  }
}
