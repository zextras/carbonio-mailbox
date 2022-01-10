// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link SearchParams}.
 *
 * @author ysasaki
 */
public final class SearchParamsTest {

    @Test
    public void parseLocale() {
        Assert.assertEquals(new Locale("da"), SearchParams.parseLocale("da"));
        Assert.assertEquals(new Locale("da", "DK"), SearchParams.parseLocale("da_DK"));
        Assert.assertEquals(new Locale("en"), SearchParams.parseLocale("en"));
        Assert.assertEquals(new Locale("en", "US", "MAC"), SearchParams.parseLocale("en_US-MAC"));
    }
    
    
    public void testIsSortByReadFlag() {
        Assert.assertFalse(SearchParams.isSortByReadFlag(SortBy.DATE_ASC));
        Assert.assertFalse(SearchParams.isSortByReadFlag(SortBy.DATE_DESC));
        Assert.assertTrue(SearchParams.isSortByReadFlag(SortBy.READ_ASC));
        Assert.assertTrue(SearchParams.isSortByReadFlag(SortBy.READ_DESC));
    }

}
