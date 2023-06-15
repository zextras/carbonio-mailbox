// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;


public class UnmodifiableBloomFilterTest {

     public String testName;
    
  protected static UnmodifiableBloomFilter<String> bloomFilter =
      UnmodifiableBloomFilter.createFilterFromFile("src/test/resources/common-passwords.txt");

  @BeforeEach
  public void setUp(TestInfo testInfo) {
    Optional<Method> testMethod = testInfo.getTestMethod();
    if (testMethod.isPresent()) {
      this.testName = testMethod.get().getName();
    }
    assertTrue(bloomFilter.isInitialized());
    assertFalse(bloomFilter.isDisabled());
  }

  @Test
  void testMightContain() {
    assertTrue(bloomFilter.isInitialized());
    assertTrue(bloomFilter.mightContain("test123"));
    assertTrue(bloomFilter.mightContain("hunter2"));
  }

  @Test
  void testMightContainFalse() {
    assertTrue(bloomFilter.isInitialized());
    assertFalse(bloomFilter.mightContain("not-in-the-test-file"));
  }

  @Test
  void testCreateFilterFromMissingFile() {
    UnmodifiableBloomFilter<String> missingFileFilter =
        UnmodifiableBloomFilter.createFilterFromFile("src/test/resources/fake-file-not-found");
    // expect to immediately initialize
    assertTrue(missingFileFilter.isInitialized());
    assertTrue(missingFileFilter.isDisabled());
    assertFalse(missingFileFilter.mightContain("test123"));
  }

  @Test
  void testCreateFilterFromEmptySpecifiedFile() {
    UnmodifiableBloomFilter<String> noFileFilter = UnmodifiableBloomFilter
        .createFilterFromFile("");
    // expect to immediately consider empty file as initialized
    assertTrue(noFileFilter.isInitialized());
    assertTrue(noFileFilter.isDisabled());
    assertFalse(noFileFilter.mightContain("test123"));
  }

  @Test
  void testCreateFilterFromNullSpecifiedFile() {
    UnmodifiableBloomFilter<String> noFileFilter = UnmodifiableBloomFilter
        .createFilterFromFile(null);
    // expect to immediately consider null file as initialized
    assertTrue(noFileFilter.isInitialized());
    assertTrue(noFileFilter.isDisabled());
    assertFalse(noFileFilter.mightContain("test123"));
  }

  @Test
  void testMightContainLazyLoad() {
    UnmodifiableBloomFilter<String> lazyFilter =
        UnmodifiableBloomFilter.createLazyFilterFromFile(
            "src/test/resources/common-passwords.txt");
    // expect to initialize on demand
    assertFalse(lazyFilter.isInitialized());
    assertFalse(lazyFilter.isDisabled());
    assertTrue(lazyFilter.mightContain("test123"));
    assertTrue(lazyFilter.mightContain("hunter2"));
    assertTrue(lazyFilter.isInitialized());
    assertFalse(lazyFilter.isDisabled());
  }

  @Test
  void testCreateLazyFilterFromMissingFile() {
    UnmodifiableBloomFilter<String> missingFileFilter =
        UnmodifiableBloomFilter.createLazyFilterFromFile("src/test/resources/fake-file-not-found");
    // expect to initialize on demand
    assertFalse(missingFileFilter.isInitialized());
    assertFalse(missingFileFilter.mightContain("test123"));
    assertTrue(missingFileFilter.isInitialized());
    // file not found results in disabled instance
    assertTrue(missingFileFilter.isDisabled());
  }

  @Test
  void testCreateLazyFilterFromEmptySpecifiedFile() {
    UnmodifiableBloomFilter<String> noFileFilter = UnmodifiableBloomFilter
        .createLazyFilterFromFile("");
    // expect to immediately consider empty file as initialized
    assertTrue(noFileFilter.isInitialized());
    assertTrue(noFileFilter.isDisabled());
    assertFalse(noFileFilter.mightContain("test123"));
  }

  @Test
  void testCreateLazyFilterFromNullSpecifiedFile() {
    UnmodifiableBloomFilter<String> noFileFilter = UnmodifiableBloomFilter
        .createLazyFilterFromFile(null);
    // expect to immediately consider null file as initialized
    assertTrue(noFileFilter.isInitialized());
    assertTrue(noFileFilter.isDisabled());
    assertFalse(noFileFilter.mightContain("test123"));
  }

}

