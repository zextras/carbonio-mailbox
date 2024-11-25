package com.zimbra.cs.account.provutil;

import java.util.Arrays;
import java.util.List;

public sealed interface CommandArgumentType {

  record Simple(String value) implements CommandArgumentType {}
  record Seq(List<CommandArgumentType> arguments) implements CommandArgumentType {}
  record Union(List<CommandArgumentType> members) implements CommandArgumentType {}
  record Rep(CommandArgumentType item) implements CommandArgumentType {}
  record Optional(CommandArgumentType item) implements CommandArgumentType {}

  static Simple of(String value) {
    return new Simple(value);
  }

  static Seq seq(CommandArgumentType... arguments) {
    return seq(Arrays.asList(arguments));
  }

  static Seq seq(List<CommandArgumentType> arguments) {
    return new Seq(arguments);
  }

  static Union union(CommandArgumentType... members) {
    return union(Arrays.asList(members));
  }

  static Union union(List<CommandArgumentType> members) {
    return new Union(members);
  }

  static Rep rep(CommandArgumentType item) {
    return new Rep(item);
  }

  static CommandArgumentType rep1(CommandArgumentType item) {
    return seq(item, new Rep(item));
  }

  static Optional optional(CommandArgumentType item) {
    return new Optional(item);
  }

}
