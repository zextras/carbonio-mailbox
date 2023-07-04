// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link SearchParams}.
 *
 * @author ysasaki
 */
public final class SearchParamsTest {

 @Test
 void parseLocale() {
  assertEquals(new Locale("da"), SearchParams.parseLocale("da"));
  assertEquals(new Locale("da", "DK"), SearchParams.parseLocale("da_DK"));
  assertEquals(new Locale("en"), SearchParams.parseLocale("en"));
  assertEquals(new Locale("en", "US", "MAC"), SearchParams.parseLocale("en_US-MAC"));
 }
    
    
    public void testIsSortByReadFlag() {
        assertFalse(SearchParams.isSortByReadFlag(SortBy.DATE_ASC));
        assertFalse(SearchParams.isSortByReadFlag(SortBy.DATE_DESC));
        assertTrue(SearchParams.isSortByReadFlag(SortBy.READ_ASC));
        assertTrue(SearchParams.isSortByReadFlag(SortBy.READ_DESC));
    }

}
