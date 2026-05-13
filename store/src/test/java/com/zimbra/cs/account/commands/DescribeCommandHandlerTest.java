package com.zimbra.cs.account.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.zimbra.cs.account.AttributeInfo;
import com.zimbra.cs.account.AttributeType;
import com.zimbra.cs.account.AttributeVersion;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import org.junit.jupiter.api.Test;

class DescribeCommandHandlerTest {

  @Test
  void countsDistinctSinceVersionsAndOrdersOldestFirst() throws Exception {
    List<AttributeInfo> attrs = new ArrayList<>();
    attrs.add(attrIntroducedIn("26.6.0"));
    attrs.add(attrIntroducedIn("26.6.0"));
    attrs.add(attrIntroducedIn("5.0.0"));
    attrs.add(attrIntroducedIn("26.7.0"));
    attrs.add(attrIntroducedIn("5.0.0"));
    attrs.add(attrIntroducedIn("5.0.0"));

    SortedMap<AttributeVersion, Integer> counts = DescribeCommandHandler.countSinceVersions(attrs);

    assertEquals(3, counts.size());
    List<String> orderedVersions = new ArrayList<>();
    counts.keySet().forEach(v -> orderedVersions.add(v.toString()));
    assertEquals(List.of("5.0.0", "26.6.0", "26.7.0"), orderedVersions);

    assertEquals(3, counts.get(new AttributeVersion("5.0.0")));
    assertEquals(2, counts.get(new AttributeVersion("26.6.0")));
    assertEquals(1, counts.get(new AttributeVersion("26.7.0")));
  }

  @Test
  void countsAttributeWithMultipleSinceValuesUnderEachVersion() throws Exception {
    List<AttributeInfo> attrs = List.of(
            attrIntroducedIn(new ArrayList<>(List.of(
                    new AttributeVersion("5.0.0"), new AttributeVersion("8.0.0")))));

    SortedMap<AttributeVersion, Integer> counts = DescribeCommandHandler.countSinceVersions(attrs);

    assertEquals(2, counts.size());
    assertEquals(1, counts.get(new AttributeVersion("5.0.0")));
    assertEquals(1, counts.get(new AttributeVersion("8.0.0")));
  }

  @Test
  void skipsAttributesWithoutSinceMetadata() {
    List<AttributeInfo> attrs = List.of(attrIntroducedIn((List<AttributeVersion>) null));

    SortedMap<AttributeVersion, Integer> counts = DescribeCommandHandler.countSinceVersions(attrs);

    assertTrue(counts.isEmpty());
  }

  private static AttributeInfo attrIntroducedIn(String version) throws Exception {
    return attrIntroducedIn(List.of(new AttributeVersion(version)));
  }

  private static AttributeInfo attrIntroducedIn(List<AttributeVersion> since) {
    return new AttributeInfo(
            "carbonioFakeAttr",
            999,
            null,
            0,
            null,
            AttributeType.TYPE_STRING,
            null,
            null,
            false,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "fake",
            null,
            since,
            null);
  }
}
