package com.zimbra.cs.account.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.AttributeInfo;
import com.zimbra.cs.account.AttributeManagerException;
import com.zimbra.cs.account.AttributeType;
import com.zimbra.cs.account.AttributeVersion;
import java.util.List;
import org.junit.jupiter.api.Test;

class DescribeArgsTest {

  @Test
  void parsesSinceFlagAsExactVersion() throws Exception {
    DescribeArgs args = parse("desc", "-since", "26.6");

    assertNotNull(args.mSince);
    assertEquals("26.6", args.mSince.toString());
    assertNull(args.mSinceAfter);
    assertTrue(args.hasSinceFilter());
  }

  @Test
  void parsesSinceAfterFlag() throws Exception {
    DescribeArgs args = parse("desc", "-since-after", "26.5.0");

    assertNull(args.mSince);
    assertNotNull(args.mSinceAfter);
    assertEquals("26.5.0", args.mSinceAfter.toString());
    assertTrue(args.hasSinceFilter());
  }

  @Test
  void rejectsBothSinceAndSinceAfter() {
    ServiceException e = assertThrows(ServiceException.class,
            () -> parse("desc", "-since", "26.6", "-since-after", "26.5"));
    assertTrue(e.getMessage().contains("cannot specify both -since and -since-after"));
  }

  @Test
  void rejectsSinceWithSpecificAttribute() {
    ServiceException e = assertThrows(ServiceException.class,
            () -> parse("desc", "-a", "zimbraId", "-since", "26.6"));
    assertTrue(e.getMessage().contains("cannot specify -since when -a is specified"));
  }

  @Test
  void rejectsSinceWithoutVersion() {
    ServiceException e = assertThrows(ServiceException.class,
            () -> parse("desc", "-since"));
    assertTrue(e.getMessage().contains("-since requires a version"));
  }

  @Test
  void rejectsInvalidVersion() {
    ServiceException e = assertThrows(ServiceException.class,
            () -> parse("desc", "-since", "26.6.banana"));
    assertTrue(e.getMessage().contains("invalid version"));
  }

  @Test
  void rejectsBareMajorVersion() {
    ServiceException e = assertThrows(ServiceException.class,
            () -> parse("desc", "-since", "26"));
    assertTrue(e.getMessage().contains("must include at least major and minor"));
  }

  @Test
  void noFilterFlagsLeavesFilterDisabled() throws Exception {
    DescribeArgs args = parse("desc");
    assertFalse(args.hasSinceFilter());
  }

  @Test
  void parsesListSinceVersionsFlag() throws Exception {
    DescribeArgs args = parse("desc", "-list-since-versions");
    assertTrue(args.mListSinceVersions);
  }

  @Test
  void rejectsListSinceVersionsCombinedWithSince() {
    ServiceException e = assertThrows(ServiceException.class,
            () -> parse("desc", "-list-since-versions", "-since", "26.6"));
    assertTrue(e.getMessage().contains("-list-since-versions cannot be combined"));
  }

  @Test
  void rejectsListSinceVersionsCombinedWithEntryType() {
    ServiceException e = assertThrows(ServiceException.class,
            () -> parse("desc", "-list-since-versions", "cos"));
    assertTrue(e.getMessage().contains("-list-since-versions cannot be combined"));
  }

  @Test
  void sinceFilterMatchesAttributeIntroducedInSameMinorRelease() throws Exception {
    DescribeArgs args = parse("desc", "-since", "26.6");

    assertTrue(args.matchesSinceFilter(attrIntroducedIn("26.6.0")));
    assertTrue(args.matchesSinceFilter(attrIntroducedIn("26.6.5")));
    assertFalse(args.matchesSinceFilter(attrIntroducedIn("26.5.0")));
    assertFalse(args.matchesSinceFilter(attrIntroducedIn("27.0.0")));
  }

  @Test
  void sinceFilterMatchesExactPatchWhenFullySpecified() throws Exception {
    DescribeArgs args = parse("desc", "-since", "26.6.1");

    assertTrue(args.matchesSinceFilter(attrIntroducedIn("26.6.1")));
    assertFalse(args.matchesSinceFilter(attrIntroducedIn("26.6.0")));
    assertFalse(args.matchesSinceFilter(attrIntroducedIn("26.6.2")));
    assertFalse(args.matchesSinceFilter(attrIntroducedIn("26.5.1")));
  }

  @Test
  void sinceAfterFilterMatchesLaterMajorMinorReleases() throws Exception {
    DescribeArgs args = parse("desc", "-since-after", "26.5");

    assertTrue(args.matchesSinceFilter(attrIntroducedIn("26.6.0")));
    assertTrue(args.matchesSinceFilter(attrIntroducedIn("27.0.0")));
    assertFalse(args.matchesSinceFilter(attrIntroducedIn("26.5.9")));
    assertFalse(args.matchesSinceFilter(attrIntroducedIn("26.4.0")));
  }

  @Test
  void sinceAfterFilterMatchesStrictlyLaterWhenFullySpecified() throws Exception {
    DescribeArgs args = parse("desc", "-since-after", "26.6.0");

    assertTrue(args.matchesSinceFilter(attrIntroducedIn("26.6.1")));
    assertTrue(args.matchesSinceFilter(attrIntroducedIn("26.7.0")));
    assertFalse(args.matchesSinceFilter(attrIntroducedIn("26.6.0")));
    assertFalse(args.matchesSinceFilter(attrIntroducedIn("26.5.9")));
  }

  @Test
  void filterRejectsAttributesWithoutSinceMetadata() throws Exception {
    DescribeArgs args = parse("desc", "-since-after", "5.0");
    assertFalse(args.matchesSinceFilter(attrIntroducedIn((List<AttributeVersion>) null)));
  }

  private static DescribeArgs parse(String... args) throws ServiceException {
    return DescribeArgs.parseDescribeArgs(args);
  }

  private static AttributeInfo attrIntroducedIn(String version) throws AttributeManagerException {
    return attrIntroducedIn(List.of(new AttributeVersion(version)));
  }

  private static AttributeInfo attrIntroducedIn(List<AttributeVersion> since) {
    return new AttributeInfo(
            "carbonioFakeAttr",   // attrName
            999,                   // id
            null,                  // parentId
            0,                     // groupId
            null,                  // callbackClassName
            AttributeType.TYPE_STRING,
            null,                  // order
            null,                  // value
            false,                 // immutable
            null,                  // min
            null,                  // max
            null,                  // cardinality
            null,                  // requiredIn
            null,                  // optionalIn
            null,                  // flags
            null,                  // globalConfigValues
            null,                  // defaultCOSValues
            null,                  // defaultExternalCOSValues
            null,                  // globalConfigValuesUpgrade
            null,                  // defaultCOSValuesUpgrade
            "fake",                // description
            null,                  // requiresRestart
            since,                 // since
            null);                 // deprecatedSince
  }
}
