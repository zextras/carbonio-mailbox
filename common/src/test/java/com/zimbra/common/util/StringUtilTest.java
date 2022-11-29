// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.junit.Assert;
import org.junit.Test;


public class StringUtilTest {

    @Test
    public void testFillTemplate() {
        String template = "The quick ${COLOR} ${ANIMAL}\njumped over the ${ADJECTIVE} dogs.\n";
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("COLOR", "brown");
        vars.put("ANIMAL", "fox");
        vars.put("ADJECTIVE", "lazy");
        String result = StringUtil.fillTemplate(template, vars);
        String expected = "The quick brown fox\njumped over the lazy dogs.\n";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testFillTemplateWithNewlineValue() {
        String template = "New message received at ${RECIPIENT_ADDRESS}." +
            "${NEWLINE}Sender: ${SENDER_ADDRESS}${NEWLINE}Subject: ${SUBJECT}";

        Map<String, String> vars = new HashMap<String, String>();
        vars.put("SENDER_ADDRESS", "sender@example.zimbra.com");
        vars.put("RECIPIENT_ADDRESS", "recipient@example.zimbra.com");
        vars.put("RECIPIENT_DOMAIN", "example.zimbra.com");
        vars.put("NOTIFICATION_ADDRESS", "notify@example.zimbra.com");
        vars.put("SUBJECT", "Cool stuff");
        vars.put("NEWLINE", "\n");

        String expected = "New message received at recipient@example.zimbra.com." +
        "\nSender: sender@example.zimbra.com\nSubject: Cool stuff";
        String actual = StringUtil.fillTemplate(template, vars);
        Assert.assertEquals("expected: '" + expected + "', actual: '" + actual + "'",
                expected, actual);
    }

    @Test
    public void testFillTemplateWithBraces() {
        String template = "Beginning ${VAR} { end }";
        Map<String, String> vars = new HashMap<String, String>();
        vars.put("VAR", "middle");
        String result = StringUtil.fillTemplate(template, vars);
        String expected = "Beginning middle { end }";
        Assert.assertEquals(expected, result);
    }

    @Test
    public void testJoin() {
        List<String> list = new ArrayList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        Assert.assertEquals("a,b,c", StringUtil.join(",", list));
        String[] array = new String[list.size()];
        list.toArray(array);
        Assert.assertEquals("a,b,c", StringUtil.join(",", array));

        // Make sure things still work if the first element is empty (bug 29513)
        list.set(0, "");
        Assert.assertEquals(",b,c", StringUtil.join(",", list));
        list.toArray(array);
        Assert.assertEquals(",b,c", StringUtil.join(",", array));
    }

    @Test
    public void testStripControlCharacters() {
        Assert.assertEquals("null string", StringUtil.stripControlCharacters(null), null);
        Assert.assertEquals("empty string", StringUtil.stripControlCharacters(""), "");
        Assert.assertEquals("no stripping", StringUtil.stripControlCharacters("ccc"), "ccc");
        Assert.assertEquals("one NUL", StringUtil.stripControlCharacters("\u0000"), "");
        Assert.assertEquals("just strippable chars", StringUtil.stripControlCharacters("\u0000\u0002"), "");
        Assert.assertEquals("char between strippable chars", StringUtil.stripControlCharacters("\u0000v\u0002"), "v");
        Assert.assertEquals("char, strip, char, strip", StringUtil.stripControlCharacters("c\u0000v\u0002"), "cv");
        Assert.assertEquals("strip, char, strip, char", StringUtil.stripControlCharacters("\u0000v\u0002x"), "vx");
        Assert.assertEquals("misordered surrogates at start", StringUtil.stripControlCharacters("\uDC00\uDBFFv\u0002x"), "vx");
        Assert.assertEquals("misordered surrogates at end", StringUtil.stripControlCharacters("v\u0002x\uDC00\uDBFF"), "vx");
        Assert.assertEquals("surrogates and char, strip, char", StringUtil.stripControlCharacters("\uDBFF\uDC00v\u0002x"), "\uDBFF\uDC00vx");
        Assert.assertEquals("surrogates and BOM", StringUtil.stripControlCharacters("\uDBFF\uDC00\uFFFFvx"), "\uDBFF\uDC00vx");
    }

    @Test
    public void sanitizeFilename() {
        Assert.assertEquals("abc .pdf", StringUtil.sanitizeFilename("abc\t.pdf"));
    }

    @Test
    public void testReplaceSurrogates() {
        Assert.assertEquals("null string", StringUtil.removeSurrogates(null), null);
        Assert.assertEquals("empty string", StringUtil.removeSurrogates(""), "");
        Assert.assertEquals("no surrogates", StringUtil.removeSurrogates("asda"), "asda");
        Assert.assertEquals("leading surrogate", StringUtil.removeSurrogates("\uDBFF\uDC00\uFFFFvx"), "?\uFFFFvx");
        Assert.assertEquals("trailing surrogate", StringUtil.removeSurrogates("\uFFFFvx\uDBFF\uDC00"), "\uFFFFvx?");
        Assert.assertEquals("consecutive surrogates", StringUtil.removeSurrogates("\uFFFFvx\uDBFF\uDC00\uDBFF\uDC00"), "\uFFFFvx??");
    }

    /**
     * Tests {@link StringUtil#newMatcher(String, String)} and the regex pattern cache.
     */
    @Test
    public void testPatternCache() {
        String s = "abcdefghijklmnopqrstuvwxyz";
        Matcher m = StringUtil.newMatcher("(g.*j)", s);
        Assert.assertTrue(m.find());
        Assert.assertEquals("ghij", m.group(1));
    }

    @Test
    public void testReplaceAll() {
        Assert.assertEquals("abc456def456ghi", StringUtil.replaceAll("abc123def12223ghi", "1\\d+3", "456"));
    }

    @Test
    public void testLfToCrlf() {
        Assert.assertEquals("abc\r\ndef\r\nhij\r\n", StringUtil.lfToCrlf("abc\r\ndef\nhij\n"));
    }

    @Test
    public void testEqual() {
        Assert.assertEquals(true, StringUtil.equal(null, null));
        Assert.assertEquals(false, StringUtil.equal(null, ""));
        Assert.assertEquals(false, StringUtil.equal("", null));
        Assert.assertEquals(false, StringUtil.equal(null, "  "));
        Assert.assertEquals(false, StringUtil.equal("  ", null));
        Assert.assertEquals(false, StringUtil.equal("abc", " abc "));
        Assert.assertEquals(false, StringUtil.equal(null, "abc"));
        Assert.assertEquals(false, StringUtil.equal("a bc", "abc"));
    }
}
