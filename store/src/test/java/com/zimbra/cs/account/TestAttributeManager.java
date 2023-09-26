// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.Version;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestAttributeManager {

  private AttributeManager am = null;

  public static final String ATTR_TWO_SINCE = "twoSinceAttr";
  public static final String ATTR_MULTI_SINCE = "multiSinceAttr";
  public static final String ATTR_FUTURE = "futureAttr";
  public static final String ATTR_ZIMBRAID = "zimbraId";

  @BeforeEach
  public void setup() throws ServiceException {
    am = new AttributeManager();

    Set<AttributeClass> requiredIn =
        Sets.newHashSet(
            AttributeClass.account,
            AttributeClass.alias,
            AttributeClass.distributionList,
            AttributeClass.domain,
            AttributeClass.server,
            AttributeClass.cos,
            AttributeClass.xmppComponent,
            AttributeClass.group,
            AttributeClass.groupDynamicUnit,
            AttributeClass.groupStaticUnit);
    Set<AttributeFlag> flags = Sets.newHashSet(AttributeFlag.accountInfo);
    AttributeInfo ai =
        new AttributeInfo(
            "zimbraId",
            1,
            null,
            0,
            null,
            AttributeType.TYPE_ID,
            null,
            "",
            true,
            null,
            null,
            AttributeCardinality.single,
            requiredIn,
            null,
            flags,
            null,
            null,
            null,
            null,
            null,
            "Zimbra Systems Unique ID",
            null,
            null,
            null);
    am.addAttribute(ai);

    requiredIn = null;
    Set<AttributeClass> optionalIn = null;
    flags = null;
    List<Version> since = Lists.newArrayList(new Version("8.0.8"), new Version("8.5.1"));
    ai =
        new AttributeInfo(
            ATTR_TWO_SINCE,
            99996,
            null,
            0,
            null,
            AttributeType.TYPE_STRING,
            null,
            "",
            false,
            null,
            null,
            AttributeCardinality.multi,
            requiredIn,
            optionalIn,
            flags,
            null,
            null,
            null,
            null,
            null,
            "test two since",
            null,
            since,
            null);
    am.addAttribute(ai);
    ;

    since =
        Lists.newArrayList(
            new Version("9.0.0"), new Version("8.0.8"), new Version("7.2.8"), new Version("8.5.2"));
    // out of order intentionally; attributeinfo class should handle that so we don't have bugs if
    // someone no-brains this
    ai =
        new AttributeInfo(
            ATTR_MULTI_SINCE,
            99997,
            null,
            0,
            null,
            AttributeType.TYPE_STRING,
            null,
            "",
            false,
            null,
            null,
            AttributeCardinality.multi,
            requiredIn,
            optionalIn,
            flags,
            null,
            null,
            null,
            null,
            null,
            "test multi since",
            null,
            since,
            null);
    am.addAttribute(ai);
    ;

    since = Lists.newArrayList(new Version(Version.FUTURE));
    ai =
        new AttributeInfo(
            ATTR_FUTURE,
            99998,
            null,
            0,
            null,
            AttributeType.TYPE_STRING,
            null,
            "",
            false,
            null,
            null,
            AttributeCardinality.single,
            requiredIn,
            optionalIn,
            flags,
            null,
            null,
            null,
            null,
            null,
            "test future",
            null,
            since,
            null);
    am.addAttribute(ai);
  }

  @AfterEach
  public void tearDown() {}

  @Test
  void testInVersion() throws Exception {

    assertTrue(am.inVersion(ATTR_ZIMBRAID, "0"));
    assertTrue(am.inVersion(ATTR_ZIMBRAID, "5.0.10"));

    assertTrue(am.inVersion(ATTR_TWO_SINCE, "8.0.8"));
    assertTrue(am.inVersion(ATTR_TWO_SINCE, "8.0.9"));
    assertTrue(am.inVersion(ATTR_TWO_SINCE, "8.5.1"));
    assertTrue(am.inVersion(ATTR_TWO_SINCE, "8.5.2"));
    assertTrue(am.inVersion(ATTR_TWO_SINCE, "9.0"));

    assertFalse(am.inVersion(ATTR_TWO_SINCE, "0"));
    assertFalse(am.inVersion(ATTR_TWO_SINCE, "0.0"));
    assertFalse(am.inVersion(ATTR_TWO_SINCE, "6"));
    assertFalse(am.inVersion(ATTR_TWO_SINCE, "7"));
    assertFalse(am.inVersion(ATTR_TWO_SINCE, "7.2"));
    assertFalse(am.inVersion(ATTR_TWO_SINCE, "7.2.7"));
    assertFalse(am.inVersion(ATTR_TWO_SINCE, "7.1.9"));
    assertFalse(am.inVersion(ATTR_TWO_SINCE, "8.5.0"));
    assertFalse(am.inVersion(ATTR_TWO_SINCE, "7.2.5"));
    assertFalse(am.inVersion(ATTR_TWO_SINCE, "7.2.7"));

    assertFuzzyMaintenaceReleaseCase(ATTR_TWO_SINCE, "8.4.1", true);
    assertFuzzyMaintenaceReleaseCase(ATTR_TWO_SINCE, "8.2.2", true);

    assertTrue(am.inVersion(ATTR_MULTI_SINCE, "7.2.8"));
    assertTrue(am.inVersion(ATTR_MULTI_SINCE, "7.2.9"));
    assertTrue(am.inVersion(ATTR_MULTI_SINCE, "8.0.8"));
    assertTrue(am.inVersion(ATTR_MULTI_SINCE, "8.0.9"));
    assertTrue(am.inVersion(ATTR_MULTI_SINCE, "8.5.2"));
    assertTrue(am.inVersion(ATTR_MULTI_SINCE, "9.0"));
    assertTrue(am.inVersion(ATTR_MULTI_SINCE, "9.0.0"));
    assertTrue(am.inVersion(ATTR_MULTI_SINCE, "9.0.1"));
    assertTrue(am.inVersion(ATTR_MULTI_SINCE, "9.1.1"));
    assertTrue(am.inVersion(ATTR_MULTI_SINCE, "10.0.0"));

    assertFalse(am.inVersion(ATTR_MULTI_SINCE, "0"));
    assertFalse(am.inVersion(ATTR_MULTI_SINCE, "0.0"));
    assertFalse(am.inVersion(ATTR_MULTI_SINCE, "6"));
    assertFalse(am.inVersion(ATTR_MULTI_SINCE, "7"));
    assertFalse(am.inVersion(ATTR_MULTI_SINCE, "7.2"));
    assertFalse(am.inVersion(ATTR_MULTI_SINCE, "7.1.9"));
    assertFalse(am.inVersion(ATTR_MULTI_SINCE, "8.5.0"));
    assertFalse(am.inVersion(ATTR_MULTI_SINCE, "8.5.1"));
    assertFalse(am.inVersion(ATTR_MULTI_SINCE, "7.2.5"));
    assertFalse(am.inVersion(ATTR_MULTI_SINCE, "7.2.7"));

    assertFuzzyMaintenaceReleaseCase(ATTR_MULTI_SINCE, "8.4.1", true);
  }

  @Test
  void testBeforeVersion() throws Exception {

    assertTrue(am.beforeVersion(ATTR_ZIMBRAID, "0"));
    assertTrue(am.beforeVersion(ATTR_ZIMBRAID, "5.0.10"));

    assertTrue(am.beforeVersion(ATTR_TWO_SINCE, "8.0.9"));
    assertTrue(am.beforeVersion(ATTR_TWO_SINCE, "8.5.2"));
    assertTrue(am.beforeVersion(ATTR_TWO_SINCE, "9.0"));

    assertFalse(am.beforeVersion(ATTR_TWO_SINCE, "8.0.8"));
    assertFalse(am.beforeVersion(ATTR_TWO_SINCE, "8.5.1"));

    assertFalse(am.beforeVersion(ATTR_TWO_SINCE, "0"));
    assertFalse(am.beforeVersion(ATTR_TWO_SINCE, "0.0"));
    assertFalse(am.beforeVersion(ATTR_TWO_SINCE, "6"));
    assertFalse(am.beforeVersion(ATTR_TWO_SINCE, "7"));
    assertFalse(am.beforeVersion(ATTR_TWO_SINCE, "7.2"));
    assertFalse(am.beforeVersion(ATTR_TWO_SINCE, "7.2.7"));
    assertFalse(am.beforeVersion(ATTR_TWO_SINCE, "7.1.9"));
    assertFalse(am.beforeVersion(ATTR_TWO_SINCE, "8.5.0"));
    assertFalse(am.beforeVersion(ATTR_TWO_SINCE, "7.2.5"));
    assertFalse(am.beforeVersion(ATTR_TWO_SINCE, "7.2.7"));

    assertFuzzyMaintenaceReleaseCase(ATTR_TWO_SINCE, "8.4.1", false);

    assertTrue(am.beforeVersion(ATTR_MULTI_SINCE, "7.2.9"));
    assertTrue(am.beforeVersion(ATTR_MULTI_SINCE, "8.0.9"));
    assertTrue(am.beforeVersion(ATTR_MULTI_SINCE, "9.0.1"));
    assertTrue(am.beforeVersion(ATTR_MULTI_SINCE, "9.1.1"));
    assertTrue(am.beforeVersion(ATTR_MULTI_SINCE, "10.0.0"));

    assertFalse(am.beforeVersion(ATTR_MULTI_SINCE, "7.2.8"));
    assertFalse(am.beforeVersion(ATTR_MULTI_SINCE, "8.0.8"));
    assertFalse(am.beforeVersion(ATTR_MULTI_SINCE, "8.5.2"));
    assertFalse(am.beforeVersion(ATTR_MULTI_SINCE, "9.0"));
    assertFalse(am.beforeVersion(ATTR_MULTI_SINCE, "9.0.0"));

    assertFalse(am.beforeVersion(ATTR_MULTI_SINCE, "0"));
    assertFalse(am.beforeVersion(ATTR_MULTI_SINCE, "0.0"));
    assertFalse(am.beforeVersion(ATTR_MULTI_SINCE, "6"));
    assertFalse(am.beforeVersion(ATTR_MULTI_SINCE, "7"));
    assertFalse(am.beforeVersion(ATTR_MULTI_SINCE, "7.2"));
    assertFalse(am.beforeVersion(ATTR_MULTI_SINCE, "7.1.9"));
    assertFalse(am.beforeVersion(ATTR_MULTI_SINCE, "8.5.0"));
    assertFalse(am.beforeVersion(ATTR_MULTI_SINCE, "8.5.1"));
    assertFalse(am.beforeVersion(ATTR_MULTI_SINCE, "7.2.5"));
    assertFalse(am.beforeVersion(ATTR_MULTI_SINCE, "7.2.7"));

    assertFuzzyMaintenaceReleaseCase(ATTR_MULTI_SINCE, "8.4.1", false);
  }

  private void assertFuzzyMaintenaceReleaseCase(String attrName, String version, boolean in)
      throws ServiceException {
    // this case is fuzzy, but we have to have it one way or the other
    // i.e. if attr is added in 8.0.8 and 8.5.1 does it appear in 8.4.1?
    // that depends if 8.4.1 is a predecessor of 8.5 or a successor of 8.0.8 chronologically and
    // branchwise
    // but we can make a rule here; and it should be rare
    // if attr was added in 8.0.8 and 8.5.1, then later we had a 8.4.1 we have to assume it is a
    // successor of 8.0.8
    // otherwise the attr needs to list 8.4.x specifically
    boolean check = in ? am.inVersion(attrName, version) : am.beforeVersion(attrName, version);
    assertTrue(check);
  }

  @Test
  void testIsFuture() {
    assertTrue(am.isFuture(ATTR_FUTURE));
    assertFalse(am.isFuture(ATTR_MULTI_SINCE));
    assertFalse(am.isFuture(ATTR_TWO_SINCE));
    assertFalse(am.isFuture(ATTR_ZIMBRAID));
  }

  @Test
  void testAddedIn() throws ServiceException {

    assertTrue(am.addedIn(ATTR_TWO_SINCE, "8.0.8"));
    assertTrue(am.addedIn(ATTR_TWO_SINCE, "8.5.1"));

    assertTrue(am.addedIn(ATTR_MULTI_SINCE, "9.0.0"));
    assertTrue(am.addedIn(ATTR_MULTI_SINCE, "8.0.8"));
    assertTrue(am.addedIn(ATTR_MULTI_SINCE, "7.2.8"));
    assertTrue(am.addedIn(ATTR_MULTI_SINCE, "8.5.2"));

    assertFalse(am.addedIn(ATTR_MULTI_SINCE, "8.5.3"));
    assertFalse(am.addedIn(ATTR_MULTI_SINCE, "8.5.1"));
    assertFalse(am.addedIn(ATTR_MULTI_SINCE, "9.0.1"));
    assertFalse(am.addedIn(ATTR_MULTI_SINCE, "8.0.7"));
    assertFalse(am.addedIn(ATTR_MULTI_SINCE, "8.0.9"));
    assertFalse(am.addedIn(ATTR_MULTI_SINCE, "7.2.7"));
    assertFalse(am.addedIn(ATTR_MULTI_SINCE, "7.2.9"));
    assertFalse(am.addedIn(ATTR_MULTI_SINCE, "8.5.7"));
    assertFalse(am.addedIn(ATTR_MULTI_SINCE, "8.5.3"));
    assertFalse(am.addedIn(ATTR_MULTI_SINCE, "8.5.1"));
    assertFalse(am.addedIn(ATTR_MULTI_SINCE, "8.0"));
    assertFalse(am.addedIn(ATTR_MULTI_SINCE, "8.1"));
    assertFalse(am.addedIn(ATTR_MULTI_SINCE, "7.1"));
    assertFalse(am.addedIn(ATTR_MULTI_SINCE, "7.2"));
    assertFalse(am.addedIn(ATTR_MULTI_SINCE, "10.0"));
  }
}
