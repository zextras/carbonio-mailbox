package com.zimbra.cs.account.provutil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;

class CommandHelpParserTest {

  @Test
  void testId() {
    var v = CommandHelpParser.parse("aaa");
    Assertions.assertEquals(1, v.arguments().size());
    Assertions.assertEquals(CommandArgumentType.of("aaa"), v.arguments().get(0));
  }

  @Test
  void testRequired() {
    var v = CommandHelpParser.parse("{aaa}");
    Assertions.assertEquals(1, v.arguments().size());
    Assertions.assertEquals(CommandArgumentType.of("aaa"), v.arguments().get(0));
  }

  @Test
  void testRequiredAlt() {
    var v = CommandHelpParser.parse("{aaa|bbb}");
    Assertions.assertEquals(1, v.arguments().size());
    CommandArgumentType  arg = v.arguments().get(0);
    assertThat(arg, instanceOf(CommandArgumentType.Union.class));
    CommandArgumentType.Union union = (CommandArgumentType.Union) arg;
    Assertions.assertEquals( CommandArgumentType.of("aaa"),  union.members().get(0));
    Assertions.assertEquals( CommandArgumentType.of("bbb"),  union.members().get(1));
  }

  @Test
  void testRequiredAltRep() {
    var v = CommandHelpParser.parse("{aaa|bbb}+");
    Assertions.assertEquals(1, v.arguments().size());
    CommandArgumentType  arg = v.arguments().get(0);
    assertThat(arg, instanceOf(CommandArgumentType.Rep.class));
    CommandArgumentType.Rep rep = (CommandArgumentType.Rep) arg;
    assertThat(rep.item(), instanceOf(CommandArgumentType.Union.class));
    CommandArgumentType.Union union = (CommandArgumentType.Union) rep.item();
    Assertions.assertEquals( CommandArgumentType.of("aaa"),  union.members().get(0));
    Assertions.assertEquals( CommandArgumentType.of("bbb"),  union.members().get(1));
  }

  @Test
  void testRequiredSeq() {
    var v = CommandHelpParser.parse("{aaa} {bbb}");
    Assertions.assertEquals(2, v.arguments().size());
    CommandArgumentType  arg0 = v.arguments().get(0);
    CommandArgumentType  arg1 = v.arguments().get(1);
    Assertions.assertEquals( CommandArgumentType.of("aaa"),  arg0);
    Assertions.assertEquals( CommandArgumentType.of("bbb"),  arg1);
  }

  @Test
  void testRequiredRepSeq() {
    var v = CommandHelpParser.parse("{aaa} {bbb}+");
    Assertions.assertEquals(2, v.arguments().size());
    CommandArgumentType  arg0 = v.arguments().get(0);
    CommandArgumentType  arg1 = v.arguments().get(1);
    Assertions.assertEquals( CommandArgumentType.of("aaa"),  arg0);
    Assertions.assertEquals( CommandArgumentType.rep(CommandArgumentType.of("bbb")),  arg1);
  }

  @Test
  void test1() {
//    System.out.println(new Input("[attr value1 [attr2 value2...]]").expect("[attr value1 [attr2 value2...]]"));
//    System.out.println(new Input("[attr value1 [attr2 value2...]]").expect("[attr1 value1 [attr2 value2...]]"));
    var v = CommandHelpParser.parse("[-v] {ldap-query} [limit {limit}] [offset {offset}] [sortBy {attr}] [sortAscending 0|1*] [domain {domain}]");
    System.out.println(v);
  }

}