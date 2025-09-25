// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link SizeQuery}.
 *
 * @author ysasaki
 */
public final class SizeQueryTest {

	@Test
	void parseSize() throws Exception {
		SizeQuery query = new SizeQuery(SizeQuery.Type.EQ, "1KB");
		assertEquals("Q(SIZE:=1024)", query.toString());

		query = new SizeQuery(SizeQuery.Type.EQ, ">1KB");
		assertEquals("Q(SIZE:>1024)", query.toString());

		query = new SizeQuery(SizeQuery.Type.EQ, "<1KB");
		assertEquals("Q(SIZE:<1024)", query.toString());

		query = new SizeQuery(SizeQuery.Type.EQ, ">=1KB");
		assertEquals("Q(SIZE:>1023)", query.toString());

		query = new SizeQuery(SizeQuery.Type.EQ, "<=1KB");
		assertEquals("Q(SIZE:<1025)", query.toString());

		query = new SizeQuery(SizeQuery.Type.EQ, "1 KB");
		assertEquals("Q(SIZE:=1024)", query.toString());

		Assertions.assertThrows(ParseException.class, () -> new SizeQuery(SizeQuery.Type.EQ, "x KB"));

	}

}
