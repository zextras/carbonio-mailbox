package com.zimbra.cs.account.provutil;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class ArgGeneratorTest {

  private static final String UUID = "90b3dd71-8f88-4e81-ac3d-151ea81be13a";
  private static final Map<String, Supplier<Stream<Stream<String>>>> valuesGens = Map.of(
          "id", () -> Stream.of(Stream.of(UUID)),
          "num", () -> Stream.of(Stream.of("1"), Stream.of("2")),
          "type", () -> Stream.of(Stream.of("cos"), Stream.of("account"))
  );

  ArgGenerator generator = new ArgGenerator(valuesGens);

  @Test
  void testUnion() {
    var list = generator.unionGenerator(CommandArgumentType.union(
            CommandArgumentType.of("id"),
            CommandArgumentType.of("num")
    )).map(Stream::toList).toList();
    Assertions.assertEquals(List.of(List.of(UUID), List.of("1"), List.of("2")), list);
  }

  @Test
  void testOptional() {
    var list = generator.optionalGenerator(CommandArgumentType.optional(
            CommandArgumentType.of("num")
    )).map(Stream::toList).toList();
    Assertions.assertEquals(List.of(List.of(), List.of("1"), List.of("2")), list);
  }

  @Test
  void testRep() {
    var list = generator.repGenerator(CommandArgumentType.rep(
            CommandArgumentType.of("num")
    )).map(Stream::toList).toList();
    Assertions.assertEquals(List.of(List.of(), List.of("1", "2")), list);
  }


  @Test
  void testSeq() {
    var list = generator.seqGenerator(CommandArgumentType.seq(
            CommandArgumentType.of("num"),
            CommandArgumentType.of("type")
    )).map(Stream::toList).toList();
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
            ))).map(Stream::toList).toList();
    Assertions.assertEquals(List.of(
            List.of(),
            List.of("1", "cos", "1", "account", "2", "cos", "2", "account")
    ), list);
  }


}