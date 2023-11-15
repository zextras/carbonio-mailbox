package com.zimbra.cs.account.callback;


import com.zimbra.common.service.ServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CheckAutoProvTimestampFormatTest {

  @Test
  @DisplayName("Should accept valid timestamp formats")
  void should_return_true_when_isAutoProvTimestampValid_supplied_correct_timestamp_format() {
    final boolean autoProvTimestampValid = new CheckAutoProvTimestampFormat().isAutoProvTimestampValid(
        "yyyyMMddHHmmss.SSS'Z'");
    Assertions.assertTrue(autoProvTimestampValid);
  }

  @Test
  @DisplayName("Should not accept invalid timestamp formats")
  void should_return_false_when_isAutoProvTimestampValid_supplied_malformed_timestamp_format() {
    final boolean autoProvTimestampValid = new CheckAutoProvTimestampFormat().isAutoProvTimestampValid(
        "ac123yMMddHHmmss.SSS'Z'");
    Assertions.assertFalse(autoProvTimestampValid);
  }

  @Test
  @DisplayName("Should accept empty timestamp format strings(this is to allow set empty value to "
      + "this attribute)")
  void should_return_true_when_isAutoProvTimestampValid_supplied_empty_timestamp_format() {
    final boolean autoProvTimestampValid = new CheckAutoProvTimestampFormat().isAutoProvTimestampValid(
        "");
    Assertions.assertTrue(autoProvTimestampValid);
  }

  @Test
  @DisplayName("Should throw NPE when timestamp format is null")
  void should_throw_npe_when_isAutoProvTimestampValid_supplied_null_as_timestamp_format() {
    CheckAutoProvTimestampFormat checkAutoProvTimestampFormat = new CheckAutoProvTimestampFormat();
    Assertions.assertThrows(NullPointerException.class,
        () -> checkAutoProvTimestampFormat.isAutoProvTimestampValid(
            null));
  }

  @Test
  void should_throw_npe_when_preModify_supplied_null_values() {
    CheckAutoProvTimestampFormat checkAutoProvTimestampFormat = new CheckAutoProvTimestampFormat();
    Assertions.assertThrows(NullPointerException.class,
        () -> checkAutoProvTimestampFormat.preModify(null, null, null, null, null));
  }

  @Test
  @DisplayName("Validate exception is thrown when malformed format is passed")
  void should_throw_exception_when_preModify_supplied_malformed_timestamp_format() {
    CheckAutoProvTimestampFormat checkAutoProvTimestampFormat = new CheckAutoProvTimestampFormat();
    final ServiceException serviceException = Assertions.assertThrows(ServiceException.class,
        () -> checkAutoProvTimestampFormat.preModify(null, null, "ac123yMMddHHmmss.SSS'Z'", null,
            null));

    Assertions.assertEquals(
        "system failure: Supplied timestamp format value is not valid, and cannot be set!",
        serviceException.getMessage());
  }
}