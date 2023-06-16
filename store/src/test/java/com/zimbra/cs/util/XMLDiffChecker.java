// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.util;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;

public class XMLDiffChecker {

    public static void assertXMLEquals(String expected, String actual) {
        Diff myDiff = DiffBuilder.compare(Input.fromString(expected))
            .withTest(Input.fromString(actual)).ignoreWhitespace().build();
        assertFalse(myDiff.hasDifferences(), myDiff.toString());
    }
}
