package com.zimbra.cs.account.provutil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

class ArgGeneratorTest {

  private static final String UUID = "90b3dd71-8f88-4e81-ac3d-151ea81be13a";
  private static final Map<String, Supplier<List<List<String>>>> valuesGens = Map.of(
          "id", () -> List.of(List.of(UUID)),
          "num", () -> List.of(List.of("1"), List.of("2")),
          "type", () -> List.of(List.of("cos"), List.of("account"))
  );

  ArgGenerator generator = new ArgGenerator(valuesGens);

  @Test
  void testUnion() {
    var list = generator.unionGenerator(CommandArgumentType.union(
            CommandArgumentType.of("id"),
            CommandArgumentType.of("num")
    ));
    Assertions.assertEquals(List.of(List.of(UUID), List.of("1"), List.of("2")), list);
  }

  @Test
  void testOptional() {
    var list = generator.optionalGenerator(CommandArgumentType.optional(
            CommandArgumentType.of("num")
    ));
    Assertions.assertEquals(List.of(List.of(), List.of("1"), List.of("2")), list);
  }

  @Test
  void testRep() {
    var list = generator.repGenerator(CommandArgumentType.rep(
            CommandArgumentType.of("num")
    ));
    Assertions.assertEquals(List.of(List.of(), List.of("1", "2"), List.of("1", "2", "1")), list);
  }


  @Test
  void testSeq() {
    var list = generator.seqGenerator(CommandArgumentType.seq(
            CommandArgumentType.of("num"),
            CommandArgumentType.of("type")
    ));
    Assertions.assertEquals(List.of(
            List.of("1", "cos"),
            List.of("1", "account"),
            List.of("2", "cos"),
            List.of("2", "account")
    ), list);
  }

  @Test
  void testRepOfSeq() {
    var list =  generator.generator(CommandArgumentType.rep(
            CommandArgumentType.seq(
                    CommandArgumentType.of("num"),
                    CommandArgumentType.of("type")
            )));
    Assertions.assertEquals(List.of(
            List.of(),
            List.of("1", "cos", "1", "account"),
            List.of("1", "cos", "1", "account", "2", "cos")
    ), list);
  }


}