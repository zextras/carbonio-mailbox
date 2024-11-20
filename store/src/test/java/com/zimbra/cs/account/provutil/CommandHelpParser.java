package com.zimbra.cs.account.provutil;

import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.provutil.Input.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class CommandHelpParser {

  static final CommandArgumentType propertiesList = CommandArgumentType.optional(CommandArgumentType.of("propertiesList"));
  static final CommandArgumentType argumentsList = CommandArgumentType.rep(CommandArgumentType.of("argument"));
  static final CommandArgumentType nameOrId = CommandArgumentType.union(CommandArgumentType.of("name"), CommandArgumentType.of("id"));
  static final CommandArgumentType nameOrIdList = CommandArgumentType.rep(nameOrId);
  static final CommandArgumentType attrsList = CommandArgumentType.rep(CommandArgumentType.of("attr"));
  static final CommandArgumentType serverList = CommandArgumentType.union(
          CommandArgumentType.of("all"),
          CommandArgumentType.rep1(CommandArgumentType.of("server-id")));
  static final CommandArgumentType accountList = CommandArgumentType.rep(CommandArgumentType.of("account-id"));
  static final CommandArgumentType accountIdOrAccountName = CommandArgumentType.union(CommandArgumentType.of("account-id"), CommandArgumentType.of("name@domain"));
  static final CommandArgumentType accountIdOrNameList = CommandArgumentType.rep1(accountIdOrAccountName);
  static final CommandArgumentType typeOrIdList = CommandArgumentType.rep(
          CommandArgumentType.union(CommandArgumentType.of("id"), CommandArgumentType.of("type"))
  );

  static CommandArgumentType.Seq parse(String text) {
    if (text == null) return CommandArgumentType.seq();
    var line = removeComments(text);
    if (line.isEmpty()) return CommandArgumentType.seq();
    var resOpt = parseCommandArgumentsSequence(new Input(line));
    if (resOpt.isEmpty()) throw new ParseException(String.format("Error parsing %s ", line));
    var res = resOpt.get();
    if (res.rest().hasNext()) throw new ParseException(String.format("Unparsed content '%s'", res.rest()));
    else return res.value();
  }

  private static Optional<Result<CommandArgumentType>> parseIdentifier(Input rest) {
    return rest
            .consume("[\\w\\-_@]+")
            .map(v -> v.map(CommandArgumentType::of));
  }

  public static <T> Optional<Result<List<T>>> repSep(String sep, Function<Input, Optional<Result<T>>> itemParser, Input input) {
    var inp = input;
    List<T> result = new ArrayList<>();
    while (inp.hasNext()) {
      var res1 = itemParser.apply(inp);
      if (res1.isEmpty()) {
        if (result.isEmpty()) return Optional.of(new Result<>(result, inp));
        return Optional.empty();
      } else {
        Result<T> nxt = res1.get();
        result.add(nxt.value());
        Input rest = nxt.rest();
        if (rest.hasNext()) {
          var sepParseResult = rest.skipOpt(sep);
          if (sepParseResult.isEmpty()) {
            return Optional.of(new Result<>(result, rest));
          } else {
            inp = sepParseResult.get();
          }
        } else {
          return Optional.of(new Result<>(result, rest));
        }
      }
    }
    return Optional.empty();
  }

  public static Optional<Result<CommandArgumentType>> specialCases(Input input) {
    return input.expect("[attr value1 [attr2 value2...]]").map(rest -> new Result<>(propertiesList, rest))
            .or(() -> input.expect("attr1 value1 [attr2 value2...]").map(rest -> new Result<>(propertiesList, rest)))
            .or(() -> input.expect("[attr1 value1 [attr2 value2...]]").map(rest -> new Result<>(propertiesList, rest)))
            .or(() -> input.expect("[attr1 value1 [attr value2...]]").map(rest -> new Result<>(propertiesList, rest)))
            .or(() -> input.expect("[name1|id1 [name2|id2...]").map(rest -> new Result<>(nameOrIdList, rest)))
            .or(() -> input.expect("[attr1 [attr2...]]").map(rest -> new Result<>(attrsList, rest)))
            .or(() -> input.expect("[attr2 value2...]]").map(rest -> new Result<>(attrsList, rest)))
            .or(() -> input.expect("[arg1 [arg2...]]").map(rest -> new Result<>(argumentsList, rest)))
            .or(() -> input.expect("[-s/--server hostname]").map(rest ->
                    new Result<>(
                            CommandArgumentType.seq(
                              CommandArgumentType.union(CommandArgumentType.of("-s"), CommandArgumentType.of("--server")),
                              CommandArgumentType.of("hostname")
                            ),
                            rest)) )
            .or(() -> input.expect("[arg1 [arg...]]").map(rest -> new Result<>(argumentsList, rest)))
            .or(() -> input.expect("all | mailbox-server [...]").map(rest -> new Result<>(serverList, rest)))
            .or(() -> input.expect("[account-id ...]").map(rest -> new Result<>(accountList, rest)))
            .or(() -> input.expect("{name@domain|id} [...]").map(rest -> new Result<>(accountIdOrNameList, rest)))
            .or(() -> input.expect("[{types|ids} {type or id} [,type or id...]]").map(rest -> new Result<>(typeOrIdList, rest)))
            .or(() -> input.expect("[sortAscending 0|1*]").map(rest -> new Result<>(
                    CommandArgumentType.optional(CommandArgumentType.seq(
                            CommandArgumentType.of("sortAscending"),
                            CommandArgumentType.union(
                                    CommandArgumentType.of("0"),
                                    CommandArgumentType.of("1")
                            )
                    )),
                    rest)));

  }

  public static Optional<Result<CommandArgumentType>> parseOptionalArgumentType(Input input) {
    return CommandHelpParser.delimited("\\[",
            rest -> parseCommandArgumentsSequence(rest).map(v -> v.map(sq -> {
              if (sq.arguments().size() == 1) {
                return CommandArgumentType.optional(sq.arguments().get(0));
              } else {
                return CommandArgumentType.optional(sq);
              }
            })),
            "\\]",
            input);
  }

  private static Optional<Result<CommandArgumentType.Seq>> parseCommandArgumentsSequence(Input rest) {
    return repSep("\s+", CommandHelpParser::parseArgumentType, rest).map(v -> v.map(CommandArgumentType::seq));
  }

  private static Optional<Result<CommandArgumentType>> parseArgumentType(Input input) {
    return specialCases(input)
            .or(() -> parseRequiredArgumentType(input))
            .or(() -> parseOptionalArgumentType(input))
            .or(() -> parseUnion(input));
  }

  public static Optional<Result<CommandArgumentType>> parseRequiredArgumentType(Input input) {
    return delimited("\\{", CommandHelpParser::parseUnion, "\\}", input).map(v -> {
      var isSeqRes = v.rest().skipOpt("\\+");
      if (isSeqRes.isPresent()) {
        return new Result<>(CommandArgumentType.rep(v.value()), isSeqRes.get());
      } else {
        return v;
      }
    });
  }

  private static Optional<Result<CommandArgumentType>> parseUnion(Input inp) {
    return repSep("\s*[|\\\\]\s*", CommandHelpParser::parseIdentifier, inp).map(v -> v.map(CommandArgumentType::union));
  }

  public static <T> Optional<Result<T>> delimited(String start, Function<Input, Optional<Result<T>>> parser, String end, Input input) {
    return input.skipOpt(start).map(parser::apply)
            .flatMap(v -> v.flatMap(res -> res.rest().skipOpt(end)
                    .map(rest -> new Result<>(res.value(), rest))));
  }

  static String removeComments(String helpText) {
    return helpText.replaceAll("\s*[\\\\(][^\\\\)]+[\\\\)]", "").trim();
  }


  public static void showAll(String[] args) {
    for (var command : ProvUtil.Command.values()) {
      System.out.println(command.getHelp());
    }
  }

  public static void main1(String[] args) {
    System.out.println(parseRequiredArgumentType(new Input("{aaa|bbb}")));
  }

  public static void main(String[] args) {
    for (var command : ProvUtil.Command.values()) {
      String text = command.getHelp();
      try {
        System.out.println(command.getName() + " " + text);
        System.out.println(parse(text));
        System.out.println();
        parse(text);
      } catch (Exception e) {
        System.out.println("*".repeat(80));
        System.out.println(String.format("Error parsing '%s'", text));
        System.out.println("*".repeat(80));
      }
    }
  }


}
