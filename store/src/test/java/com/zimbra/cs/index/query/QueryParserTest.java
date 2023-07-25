// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.lucene.document.DateTools;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.zimbra.common.service.ServiceException;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.cs.account.MockProvisioning;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.index.ZimbraAnalyzer;
import com.zimbra.cs.index.query.parser.QueryParser;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;

/**
 * Unit test for {@link QueryParser}.
 *
 * @author ysasaki
 */
public final class QueryParserTest {
    private static QueryParser parser;

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();
        prov.createAccount("test@zimbra.com", "secret", new HashMap<String, Object>());

        Mailbox mbox = MailboxManager.getInstance().getMailboxByAccountId(MockProvisioning.DEFAULT_ACCOUNT_ID);
        parser = new QueryParser(mbox, ZimbraAnalyzer.getInstance());
    }

 @Test
 void defaultClause() throws Exception {
  String src = "zimbra";
  assertEquals("Q(l.content:zimbra)", Query.toString(parser.parse(src)));
 }

 @Test
 void modifier() throws Exception {
  String src = "+content:zimbra";
  assertEquals("+Q(l.content:zimbra)", Query.toString(parser.parse(src)));

  src = "-content:zimbra";
  assertEquals("-Q(l.content:zimbra)", Query.toString(parser.parse(src)));

  src = "not content:zimbra";
  assertEquals("-Q(l.content:zimbra)", Query.toString(parser.parse(src)));

  src = "from:(+@zimbra.com)";
  assertEquals("(+Q(from:@zimbra.com))", Query.toString(parser.parse(src)));

  src = "from:(-@zimbra.com)";
  assertEquals("(-Q(from:@zimbra.com))", Query.toString(parser.parse(src)));
 }

 @Test
 void sortBy() throws Exception {
  String src = "foo sort:dateAsc and bar";
  assertEquals("Q(l.content:foo) && Q(l.content:bar)", Query.toString(parser.parse(src)));
  assertEquals("dateAsc", parser.getSortBy());
 }

 @Test
 void text() throws Exception {
  String src = "x or y";
  assertEquals("Q(l.content:x) || Q(l.content:y)", Query.toString(parser.parse(src)));

  src = "(x or y)";
  assertEquals("(Q(l.content:x) || Q(l.content:y))", Query.toString(parser.parse(src)));

  src = "(x or y) and in:inbox";
  assertEquals("(Q(l.content:x) || Q(l.content:y)) && Q(IN:Inbox)", Query.toString(parser.parse(src)));

  src = "\"This is a \\\"phrase\\\" query\"";
  assertEquals("Q(l.content:phrase,query)", Query.toString(parser.parse(src)));
 }

 @Test
 void folder() throws ServiceException {
  String src = "in:inbox";
  assertEquals("Q(IN:Inbox)", Query.toString(parser.parse(src)));

  src = "in:(trash -junk)";
  assertEquals("(Q(IN:Trash) && -Q(IN:Junk))", Query.toString(parser.parse(src)));
 }

 @Test
 void date() throws Exception {
  String src = "date:-4d";
  assertEquals("Q(DATE:DATE," + getDate(-4) + "-" + getDate(-3) + ")", Query.toString(parser.parse(src)));

  src = "date:\"-4d\"";
  assertEquals("Q(DATE:DATE," + getDate(-4) + "-" + getDate(-3) + ")", Query.toString(parser.parse(src)));

  src = "(a or b) and before:1/1/2009 and -subject:\"quoted string\"";
  assertEquals("(Q(l.content:) || Q(l.content:b)) && Q(DATE:BEFORE,196912312359-200901010000) && " +
    "-Q(subject:quoted,string)", Query.toString(parser.parse(src)));

  src = "date:(01/01/2001 02/02/2002)";
  assertEquals("(Q(DATE:DATE,200101010000-200101020000) && Q(DATE:DATE,200202020000-200202030000))",
    Query.toString(parser.parse(src)));

  src = "date:-1d date:(01/01/2001 02/02/2002)";
  assertEquals("Q(DATE:DATE," + getDate(-1) + "-" + getDate(0) +
    ") && (Q(DATE:DATE,200101010000-200101020000) && Q(DATE:DATE,200202020000-200202030000))",
    Query.toString(parser.parse(src)));

  src = "date:(-1d or -2d)";
  assertEquals("(Q(DATE:DATE," + getDate(-1) + "-" + getDate(0) + ") || Q(DATE:DATE," +
    getDate(-2) + "-" + getDate(-1) + "))", Query.toString(parser.parse(src)));

  src = "date:\"+1d\"";
  assertEquals("Q(DATE:DATE," + getDate(1) + "-" + getDate(2) + ")", Query.toString(parser.parse(src)));

  src = "date:+2w";
  assertEquals("Q(DATE:DATE," + getWeek(2) + "-" + getWeek(3) + ")", Query.toString(parser.parse(src)));

  src = "not date:(1/1/2004 or 2/1/2004)";
  assertEquals("-(Q(DATE:DATE,200401010000-200401020000) || Q(DATE:DATE,200402010000-200402020000))",
    Query.toString(parser.parse(src)));
 }

    private String getDate(int day) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.add(Calendar.DATE, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        return DateTools.dateToString(cal.getTime(), DateTools.Resolution.MINUTE);
    }

    private String getWeek(int week) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.add(Calendar.WEEK_OF_YEAR, week);
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        return DateTools.dateToString(cal.getTime(), DateTools.Resolution.MINUTE);
    }

 /**
  * Validate that date queries parse to the proper ranges. The only caveat here is that a query like
  * {@code date:>foo} turns into the range {@code (foo+1, true, -1, false)} instead of the more obvious one
  * {@code (foo, false, -1, false)} -- this is a quirk of the query parsing code. Both are correct.
  */
 @Test
 void dateRange() throws Exception {
  final long JAN1 = 1167609600000L;
  final long JAN2 = 1167696000000L;

  String src = "date:01/01/2007";
  DateQuery dq = (DateQuery) parser.parse(src).get(0);
  assertEquals(JAN1, dq.getLowestTime());
  assertEquals(true, dq.isLowestInclusive());
  assertEquals(JAN2, dq.getHighestTime());
  assertEquals(false, dq.isHighestInclusive());

  src = "date:<01/01/2007";
  dq = (DateQuery) parser.parse(src).get(0);
  assertEquals(-1L, dq.getLowestTime());
  assertEquals(false, dq.isLowestInclusive());
  assertEquals(JAN1, dq.getHighestTime());
  assertEquals(false, dq.isHighestInclusive());

  src = "before:01/01/2007";
  dq = (DateQuery) parser.parse(src).get(0);
  assertEquals(-1L, dq.getLowestTime());
  assertEquals(false, dq.isLowestInclusive());
  assertEquals(JAN1, dq.getHighestTime());
  assertEquals(false, dq.isHighestInclusive());

  src = "date:<=01/01/2007";
  dq = (DateQuery) parser.parse(src).get(0);
  assertEquals(-1L, dq.getLowestTime());
  assertEquals(false, dq.isLowestInclusive());
  assertEquals(JAN2, dq.getHighestTime());
  assertEquals(false, dq.isHighestInclusive());

  src = "date:>01/01/2007";
  dq = (DateQuery) parser.parse(src).get(0);
  assertEquals(JAN2, dq.getLowestTime());
  assertEquals(true, dq.isLowestInclusive());
  assertEquals(-1L, dq.getHighestTime());
  assertEquals(false, dq.isHighestInclusive());

  src = "after:01/01/2007";
  dq = (DateQuery) parser.parse(src).get(0);
  assertEquals(JAN2, dq.getLowestTime());
  assertEquals(true, dq.isLowestInclusive());
  assertEquals(-1L, dq.getHighestTime());
  assertEquals(false, dq.isHighestInclusive());

  src = "date:>=01/01/2007";
  dq = (DateQuery) parser.parse(src).get(0);
  assertEquals(JAN1, dq.getLowestTime());
  assertEquals(true, dq.isLowestInclusive());
  assertEquals(-1L, dq.getHighestTime());
  assertEquals(false, dq.isHighestInclusive());

  src = "date:" + JAN1;
  dq = (DateQuery) parser.parse(src).get(0);
  assertEquals(JAN1, dq.getLowestTime());
  assertEquals(true, dq.isLowestInclusive());
  assertEquals(JAN1 + 1000L, dq.getHighestTime());
  assertEquals(false, dq.isHighestInclusive());

  src = "date:<" + JAN1;
  dq = (DateQuery) parser.parse(src).get(0);
  assertEquals(-1L, dq.getLowestTime());
  assertEquals(false, dq.isLowestInclusive());
  assertEquals(JAN1, dq.getHighestTime());
  assertEquals(false, dq.isHighestInclusive());

  src = "before:" + JAN1;
  dq = (DateQuery) parser.parse(src).get(0);
  assertEquals(-1L, dq.getLowestTime());
  assertEquals(false, dq.isLowestInclusive());
  assertEquals(JAN1, dq.getHighestTime());
  assertEquals(false, dq.isHighestInclusive());

  src = "date:<=" + JAN1;
  dq = (DateQuery) parser.parse(src).get(0);
  assertEquals(-1L, dq.getLowestTime());
  assertEquals(false, dq.isLowestInclusive());
  assertEquals(JAN1 + 1000, dq.getHighestTime());
  assertEquals(false, dq.isHighestInclusive());

  src = "date:>" + JAN1;
  dq = (DateQuery) parser.parse(src).get(0);
  assertEquals(JAN1 + 1000L, dq.getLowestTime());
  assertEquals(true, dq.isLowestInclusive());
  assertEquals(-1L, dq.getHighestTime());
  assertEquals(false, dq.isHighestInclusive());

  src = "after:" + JAN1;
  dq = (DateQuery) parser.parse(src).get(0);
  assertEquals(JAN1 + 1000L, dq.getLowestTime());
  assertEquals(true, dq.isLowestInclusive());
  assertEquals(-1L, dq.getHighestTime());
  assertEquals(false, dq.isHighestInclusive());

  src = "date:>=" + JAN1;
  dq = (DateQuery) parser.parse(src).get(0);
  assertEquals(JAN1, dq.getLowestTime());
  assertEquals(true, dq.isLowestInclusive());
  assertEquals(-1L, dq.getHighestTime());
  assertEquals(false, dq.isHighestInclusive());
 }

 @Test
 void braced() throws Exception {
  String src = "item:{1,2,3}";
  assertEquals("Q(ITEMID," + MockProvisioning.DEFAULT_ACCOUNT_ID + ":1," +
    MockProvisioning.DEFAULT_ACCOUNT_ID + ":2," + MockProvisioning.DEFAULT_ACCOUNT_ID + ":3)",
    Query.toString(parser.parse(src)));

  src = "item:({1,2,3} or {4,5,6})";
  assertEquals("(Q(ITEMID," + MockProvisioning.DEFAULT_ACCOUNT_ID + ":1," +
    MockProvisioning.DEFAULT_ACCOUNT_ID + ":2," + MockProvisioning.DEFAULT_ACCOUNT_ID + ":3) || Q(ITEMID," +
    MockProvisioning.DEFAULT_ACCOUNT_ID + ":4," + MockProvisioning.DEFAULT_ACCOUNT_ID + ":5," +
    MockProvisioning.DEFAULT_ACCOUNT_ID + ":6))", Query.toString(parser.parse(src)));
 }

 @Test
 void builtIn() throws Exception {
  String src = "is:unread is:remote";
  assertEquals("Q(TAG:\\Unread,UNREAD) && Q(UNDER:REMOTE)", Query.toString(parser.parse(src)));
 }

 @Test
 void address() throws Exception {
  String src = "from:foo@bar.com";
  assertEquals("Q(from:foo@bar.com)", Query.toString(parser.parse(src)));

  src = "from:\"foo bar\"";
  assertEquals("Q(from:foo,bar)", Query.toString(parser.parse(src)));

  src = "to:foo@bar.com";
  assertEquals("Q(to:foo@bar.com)", Query.toString(parser.parse(src)));

  src = "to:\"foo bar\"";
  assertEquals("Q(to:foo,bar)", Query.toString(parser.parse(src)));

  src = "cc:foo@bar.com";
  assertEquals("Q(cc:foo@bar.com)", Query.toString(parser.parse(src)));

  src = "cc:\"foo bar\"";
  assertEquals("Q(cc:foo,bar)", Query.toString(parser.parse(src)));
 }

 @Test
 void subject() throws Exception {
  String src = "subject:\"foo\"";
  assertEquals("Q(subject:foo)", Query.toString(parser.parse(src)));

  src = "subject:\"foo bar\" and content:\"baz gub\"";
  assertEquals("Q(subject:foo,bar) && Q(l.content:baz,gub)", Query.toString(parser.parse(src)));

  src = "subject:this_is_my_subject subject:\"this is_my_subject\"";
  assertEquals("Q(subject:my,subject) && Q(subject:my,subject)", Query.toString(parser.parse(src)));
 }

 @Test
 void has() throws Exception {
  String src = "has:attachment has:phone has:url";
  assertEquals("Q(attachment:any) && Q(has:phone) && Q(has:url)", Query.toString(parser.parse(src)));
 }

 @Test
 void filename() throws Exception {
  String src = "filename:foo filename:(\"foo\" \"foo bar\" gub)";
  assertEquals("Q(filename:foo) && (Q(filename:foo) && Q(filename:foo,bar) && Q(filename:gub))",
    Query.toString(parser.parse(src)));
 }

 @Test
 void typeAttachment() throws Exception {
  String src = "type:attachment";
  assertEquals("Q(type:attachment)", Query.toString(parser.parse(src)));
 }

 @Test
 void typeText() throws Exception {
  String src = "type:text";
  assertEquals("Q(type:text)", Query.toString(parser.parse(src)));
 }

 @Test
 void typeApplication() throws Exception {
  String src = "type:application";
  assertEquals("Q(type:application)", Query.toString(parser.parse(src)));
 }

 @Test
 void typeWord() throws Exception {
  String src = "type:word";
  String expected =
    "(Q(type:application/msword)" +
      " || Q(type:application/vnd.openxmlformats-officedocument.wordprocessingml.document)" +
      " || Q(type:application/vnd.openxmlformats-officedocument.wordprocessingml.template)" +
      " || Q(type:application/vnd.ms-word.document.macroenabled.12)" +
      " || Q(type:application/vnd.ms-word.template.macroenabled.12))";
  assertEquals(expected, Query.toString(parser.parse(src)));
  src = "type:msword";
  assertEquals(expected, Query.toString(parser.parse(src)));
 }

 @Test
 void typeExcel() throws Exception {
  String src = "type:xls";
  String expected =
    "(Q(type:application/vnd.ms-excel)" +
      " || Q(type:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet)" +
      " || Q(type:application/vnd.openxmlformats-officedocument.spreadsheetml.template)" +
      " || Q(type:application/vnd.ms-excel.sheet.macroenabled.12)" +
      " || Q(type:application/vnd.ms-excel.template.macroenabled.12)" +
      " || Q(type:application/vnd.ms-excel.addin.macroenabled.12)" +
      " || Q(type:application/vnd.ms-excel.sheet.binary.macroenabled.12))";
  assertEquals(expected, Query.toString(parser.parse(src)));
  src = "type:excel";
  assertEquals(expected, Query.toString(parser.parse(src)));
 }

 @Test
 void typePowerpoint() throws Exception {
  String src = "type:ppt";
  String expected =
    "(Q(type:application/vnd.ms-powerpoint)" +
      " || Q(type:application/vnd.openxmlformats-officedocument.presentationml.presentation)" +
      " || Q(type:application/vnd.openxmlformats-officedocument.presentationml.template)" +
      " || Q(type:application/vnd.openxmlformats-officedocument.presentationml.slideshow)" +
      " || Q(type:application/vnd.ms-powerpoint.addin.macroenabled.12)" +
      " || Q(type:application/vnd.ms-powerpoint.presentation.macroenabled.12)" +
      " || Q(type:application/vnd.ms-powerpoint.slideshow.macroenabled.12))";
  assertEquals(expected, Query.toString(parser.parse(src)));
  src = "type:powerpoint";
  assertEquals(expected, Query.toString(parser.parse(src)));
 }

 @Test
 void typePdf() throws Exception {
  String src = "type:pdf";
  assertEquals("Q(type:application/pdf)", Query.toString(parser.parse(src)));
 }

 @Test
 void typeTnef() throws Exception {
  String src = "type:ms-tnef";
  assertEquals("Q(type:application/ms-tnef)", Query.toString(parser.parse(src)));
 }

 @Test
 void typeImages() throws Exception {
  String src = "type:image type:jpeg type:gif type:bmp";
  assertEquals("Q(type:image) && Q(type:image/jpeg) && Q(type:image/gif) && Q(type:image/bmp)",
    Query.toString(parser.parse(src)));
 }

 @Test
 void typeNoneAndAny() throws Exception {
  String src = "type:none type:any";
  assertEquals("Q(type:none) && Q(type:any)", Query.toString(parser.parse(src)));
 }

 @Test
 void tag() throws Exception {
  String src = "is:(read unread)";
  assertEquals("(Q(TAG:\\Unread,READ) && Q(TAG:\\Unread,UNREAD))", Query.toString(parser.parse(src)));

  src = "is:(flagged unflagged)";
  assertEquals("(Q(TAG:\\Flagged,FLAGGED) && Q(TAG:\\Flagged,UNFLAGGED))",
    Query.toString(parser.parse(src)));

  src = "is:(\"sent\" received)";
  assertEquals("(Q(TAG:\\Sent,SENT) && Q(TAG:\\Sent,RECEIVED))", Query.toString(parser.parse(src)));

  src = "is:(replied unreplied)";
  assertEquals("(Q(TAG:\\Answered,REPLIED) && Q(TAG:\\Answered,UNREPLIED))",
    Query.toString(parser.parse(src)));

  src = "is:(forwarded unforwarded)";
  assertEquals("(Q(TAG:\\Forwarded,FORWARDED) && Q(TAG:\\Forwarded,UNFORWARDED))",
    Query.toString(parser.parse(src)));
 }

 @Test
 void priority() throws Exception {
  String src = "priority:high";
  assertEquals("Q(PRIORITY:HIGH)", Query.toString(parser.parse(src)));

  src = "priority:low";
  assertEquals("Q(PRIORITY:LOW)", Query.toString(parser.parse(src)));

  src = "priority:medium";
  try {
   parser.parse(src);
   fail();
  } catch (ServiceException e) {
   assertSame(MailServiceException.QUERY_PARSE_ERROR, e.getCode());
  }
 }

 @Test
 void size() throws Exception {
  String src = "size:(1 20 300 1k 10k 100kb 34mb)";
  assertEquals("(Q(SIZE:=1) && Q(SIZE:=20) && Q(SIZE:=300) && Q(SIZE:=1024) && Q(SIZE:=10240) && Q(SIZE:=102400) && Q(SIZE:=35651584))",
    Query.toString(parser.parse(src)));

  src = "size:(<1k >10k)";
  assertEquals("(Q(SIZE:<1024) && Q(SIZE:>10240))", Query.toString(parser.parse(src)));

  src = "larger:(1 20 300 100kb 34mb)";
  assertEquals("(Q(SIZE:>1) && Q(SIZE:>20) && Q(SIZE:>300) && Q(SIZE:>102400) && Q(SIZE:>35651584))",
    Query.toString(parser.parse(src)));

  src = "smaller:(1 20 300 100kb 34mb)";
  assertEquals("(Q(SIZE:<1) && Q(SIZE:<20) && Q(SIZE:<300) && Q(SIZE:<102400) && Q(SIZE:<35651584))",
    Query.toString(parser.parse(src)));
 }

 @Test
 void metadata() throws Exception {
  String src = "author:foo author:(\"foo\" \"foo bar\" gub)";
  assertEquals("Q(AUTHOR:foo) && (Q(AUTHOR:foo) && Q(AUTHOR:foo,bar) && Q(AUTHOR:gub))",
    Query.toString(parser.parse(src)));

  src = "title:foo title:(\"foo\" \"foo bar\" gub)";
  assertEquals("Q(TITLE:foo) && (Q(TITLE:foo) && Q(TITLE:foo,bar) && Q(TITLE:gub))",
    Query.toString(parser.parse(src)));

  src = "keywords:foo keywords:(\"foo\" \"foo bar\" gub)";
  assertEquals("Q(KEYWORDS:foo) && (Q(KEYWORDS:foo) && Q(KEYWORDS:foo,bar) && Q(KEYWORDS:gub))",
    Query.toString(parser.parse(src)));

  src = "company:foo company:(\"foo\" \"foo bar\" gub)";
  assertEquals("Q(COMPANY:foo) && (Q(COMPANY:foo) && Q(COMPANY:foo,bar) && Q(COMPANY:gub))",
    Query.toString(parser.parse(src)));
 }

 @Test
 void field() throws Exception {
  String src = "#company:\"zimbra:vmware\"";
  List<Query> result = parser.parse(src);
  assertEquals("Q(l.field:company:zimbra:vmware)", Query.toString(result));

  TextQuery query = (TextQuery) result.get(0);
  assertEquals("#company:\"zimbra:vmware\"",
    query.toQueryString(query.getField(), "company:zimbra:vmware"));
  assertEquals("#company:\"zimbra@vmware\"",
    query.toQueryString(query.getField(), "company:zimbra@vmware"));
  assertEquals("#company:\"zimbra\\\"vmware\"",
    query.toQueryString(query.getField(), "company:zimbra\"vmware"));
 }

 @Test
 void textLexicalState() throws Exception {
  String src = "from:and or from:or or not from:not";
  assertEquals("Q(from:and) || Q(from:or) || -Q(from:not)", Query.toString(parser.parse(src)));
 }

 @Test
 void contact() throws Exception {
  QueryParser parser = new QueryParser(null, ZimbraAnalyzer.getInstance());
  String src = "contact:\"Conf -\"";
  assertEquals("Q(CONTACT:conf,-)", Query.toString(parser.parse(src)));

  src = "contact:\"Conf - Prom\"";
  assertEquals("Q(CONTACT:conf,-,prom)", Query.toString(parser.parse(src)));

  src = "contact:\"Conf - Promontory E\"";
  assertEquals("Q(CONTACT:conf,-,promontory,e)", Query.toString(parser.parse(src)));

  src = "contact:\"Conf - Promontory E*****\"";
  assertEquals("Q(CONTACT:conf,-,promontory,e)", Query.toString(parser.parse(src)));

  src = "contact:\"Conf - Prom* E*\"";
  assertEquals("Q(CONTACT:conf,-,prom,e)", Query.toString(parser.parse(src)));
 }

 @Test
 void contactContent() throws Exception {
  QueryParser parser = new QueryParser(null, ZimbraAnalyzer.getInstance());
  parser.setTypes(EnumSet.of(MailItem.Type.CONTACT));

  String src = "zimbra";
  assertEquals("(Q(CONTACT:zimbra) || Q(l.content:zimbra))", Query.toString(parser.parse(src)));

  src = "in"; // stop word
  assertEquals("(Q(CONTACT:in) || Q(l.content:))", Query.toString(parser.parse(src)));
 }

 @Test
 void quoted() throws Exception {
  QueryParser parser = new QueryParser(null, ZimbraAnalyzer.getInstance());
  parser.setTypes(EnumSet.of(MailItem.Type.CONTACT));

  assertEquals("(Q(CONTACT:zimbra,quoted,test) || Q(l.content:zimbra,quoted,test))",
    Query.toString(parser.parse("\"Zimbra \\\"quoted\\\" test\"")));
 }

 @Test
 void quick() throws Exception {
  QueryParser parser = new QueryParser(null, ZimbraAnalyzer.getInstance());
  parser.setQuick(true);

  assertEquals("Q(l.content:all,hands,meeting[*])", Query.toString(parser.parse("all hands meeting")));
  assertEquals("Q(l.content:all,hands,meeting[*])", Query.toString(parser.parse("all hands meeting*")));
 }

}
