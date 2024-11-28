package com.zimbra.cs.account.provutil;

import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.provutil.Input.Result;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.zimbra.cs.account.provutil.CommandArgumentType.Rep;
import static com.zimbra.cs.account.provutil.CommandArgumentType.Seq;
import static com.zimbra.cs.account.provutil.CommandArgumentType.Simple;
import static com.zimbra.cs.account.provutil.CommandArgumentType.Union;
import static com.zimbra.cs.account.provutil.CommandArgumentType.of;
import static com.zimbra.cs.account.provutil.CommandArgumentType.optional;
import static com.zimbra.cs.account.provutil.CommandArgumentType.rep;
import static com.zimbra.cs.account.provutil.CommandArgumentType.rep1;
import static com.zimbra.cs.account.provutil.CommandArgumentType.seq;
import static com.zimbra.cs.account.provutil.CommandArgumentType.union;

class CommandArgumentParser {

  static final CommandArgumentType propertiesList = optional(
          rep(seq(of("attribute-name"), of("attribute-value")))
  );
  static final CommandArgumentType argumentsList = optional(rep(of("argument")));
  static final CommandArgumentType nameOrId = union(of("name"), of("id"));
  static final CommandArgumentType nameOrIdList = optional(rep(nameOrId));
  static final CommandArgumentType attrsList = optional(rep(of("attr")));
  static final CommandArgumentType serverList = union(of("all"), rep1(of("server-id")));
  static final CommandArgumentType accountList = optional(rep(of("account-id")));
  static final CommandArgumentType accountIdOrAccountName = union(of("account-id"), of("name@domain"));
  static final CommandArgumentType accountIdOrNameList = rep1(accountIdOrAccountName);
  static final CommandArgumentType typeOrIdList = optional(rep(union(of("id"), of("type")))
  );

  static Seq parse(String text) {
    if (text == null) return seq();
    var line = removeComments(text);
    if (line.isEmpty()) return seq();
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

  static <T> Optional<Result<List<T>>> repSep(String sep, Function<Input, Optional<Result<T>>> itemParser, Input input) {
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

  static Optional<Result<CommandArgumentType>> specialCases(Input input) {
    return onPatternReturn(input, "[attr value1 [attr2 value2...]]", propertiesList)
            .or(() -> onPatternReturn(input, "attr1 value1 [attr2 value2...]", propertiesList))
            .or(() -> onPatternReturn(input, "[attr1 value1 [attr2 value2...]]", propertiesList))
            .or(() -> onPatternReturn(input, "[attr1 value1 [attr value2...]]", propertiesList))
            .or(() -> onPatternReturn(input, "[name1|id1 [name2|id2...]]", nameOrIdList))
            .or(() -> onPatternReturn(input, "[attr1 [attr2...]]", attrsList))
            .or(() -> onPatternReturn(input, "[attr2 value2...]]", attrsList))
            .or(() -> onPatternReturn(input, "[arg1 [arg2...]]", argumentsList))
            .or(() -> onPatternReturn(input, "[-s/--server hostname]", optional(seq(union(of("-s"), of("--server")), of("hostname")))))
            .or(() -> onPatternReturn(input, "[arg1 [arg...]]", argumentsList))
            .or(() -> onPatternReturn(input, "all | mailbox-server [...]", serverList))
            .or(() -> onPatternReturn(input, "[account-id ...]", accountList))
            .or(() -> onPatternReturn(input, "{name@domain|id} [...]", accountIdOrNameList))
            .or(() -> onPatternReturn(input, "[{types|ids} {type or id} [,type or id...]]", typeOrIdList))
            .or(() -> onPatternReturn(input, "[attr op value...]", typeOrIdList))
            .or(() -> onPatternReturn(input, "[sortAscending 0|1*]", optional(seq(of("sortAscending"), union(of("0"), of("1"), of("true"), of("false"))))))
            .or( () -> onPatternReturn(input, "[[-v] [-ni] [{entry-type}]] | [-a {attribute-name}]", union(
                    seq(
                            optional(of("-v")),
                            optional(of("-ni")),
                            optional(of("entry-type"))
                    ),
                    optional(seq(of("-a"), of("attribute-name")))
            )));

  }

  private static Optional<Result<CommandArgumentType>> onPatternReturn(Input input, String pattern, CommandArgumentType arguments) {
    return input.expect(pattern).map(rest -> new Result<>(arguments, rest));
  }

  static Optional<Result<CommandArgumentType>> parseOptionalArgumentType(Input input) {
    return CommandArgumentParser.delimited("\\[",
            rest -> parseCommandArgumentsSequence(rest).map(v -> v.map(sq -> {
              if (sq.arguments().size() == 1) {
                return optional(sq.arguments().get(0));
              } else {
                return optional(sq);
              }
            })),
            "\\]",
            input);
  }

  private static Optional<Result<Seq>> parseCommandArgumentsSequence(Input rest) {
    return repSep("\s+", CommandArgumentParser::parseArgumentType, rest).map(v -> v.map(CommandArgumentType::seq));
  }

  private static Optional<Result<CommandArgumentType>> parseArgumentType(Input input) {
    return specialCases(input)
            .or(() -> parseRequiredArgumentType(input))
            .or(() -> parseOptionalArgumentType(input))
            .or(() -> parseUnion(input));
  }

  static Optional<Result<CommandArgumentType>> parseRequiredArgumentType(Input input) {
    return delimited("\\{", CommandArgumentParser::parseUnion, "\\}", input).map(v -> {
      var isSeqRes = v.rest().skipOpt("\\+");
      if (isSeqRes.isPresent()) {
        return new Result<>(rep1(v.value()), isSeqRes.get());
      } else {
        return v;
      }
    });
  }

  static CommandArgumentType unionSimplifiy(List<CommandArgumentType> members) {
    if (members.size() == 1) return members.get(0);
    return new Union(members);
  }

  private static Optional<Result<CommandArgumentType>> parseUnion(Input inp) {
    return repSep("\s*[|\\\\]\s*", CommandArgumentParser::parseIdentifier, inp).map(v -> v.map(CommandArgumentParser::unionSimplifiy));
  }

  static <T> Optional<Result<T>> delimited(String start, Function<Input, Optional<Result<T>>> parser, String end, Input input) {
    return input.skipOpt(start).map(parser::apply)
            .flatMap(v -> v.flatMap(res -> res.rest().skipOpt(end)
                    .map(rest -> new Result<>(res.value(), rest))));
  }

  static String removeComments(String helpText) {
    return helpText.replaceAll("\s*[\\\\(][^\\\\)]+[\\\\)]", "").trim();
  }


  static void showAll(String[] args) {
    for (var command : ProvUtil.Command.values()) {
      System.out.println(command.getHelp());
    }
  }

  public static void main1(String[] args) {
    System.out.println(parseRequiredArgumentType(new Input("{aaa|bbb}")));
  }

  static Set<String> findNames(CommandArgumentType arg) {
    if (arg instanceof Simple simple) {
      return Set.of(simple.value());
    } else if (arg instanceof Union union) {
      return union.members().stream().flatMap( cmdArg -> findNames(cmdArg).stream() ).collect(Collectors.toSet());
    } else if (arg instanceof Rep rep) {
      return findNames(rep.item());
    } else if (arg instanceof Seq seq) {
      return seq.arguments().stream().flatMap( cmdArg -> findNames(cmdArg).stream()).collect(Collectors.toSet());
    } else if (arg instanceof CommandArgumentType.Optional opt) {
      return findNames(opt.item());
    } else {
      throw new UnsupportedOperationException(String.format("Unsupported %s", arg));
    }
  }

  static <T> List<T> takeElements(List<T> list, int number) {
    int size = list.size();
    if (size < number) {
      return list;
    } else {
      var arr = new ArrayList<T>();
      for (int i = 0; i < number; i++) {
        arr.add(list.get((i * 11 + 7) % size));
      }
      return arr;
    }
  }

  private static String renderCommandArguments(List<List<String>> argsValue) {
    return argsValue.stream()
            .map(cmdArgs -> String.join(" ", cmdArgs))
            .collect(Collectors.joining("\n"));
  }

  private static List<List<String>> genCommandArguments(ProvUtil.Command cmd, ArgGenerator argGenerator, int numberOfSamples) {
    var args = CommandArgumentParser.parse(cmd.getHelp());
    return takeElements(argGenerator.generator(args).stream().map( cmdArgs -> {
      List<String> res = new ArrayList<String>();
      res.add(cmd.getName());
      res.addAll(cmdArgs);
      return res;
    }).toList(), numberOfSamples);
  }


  public static void main___(String[] args) {
    for (var command : ProvUtil.Command.values()) {
      String text = command.getHelp();
      System.out.println(command.getName() + " " + text);
    }
  }

  public static void main__old(String[] args) {
    for (var command : ProvUtil.Command.values()) {
      String text = command.getHelp();
      try {
        var argGen = new ArgGenerator(ProvUtilArgumentsValues.getValues());
//        System.out.println(command.getName() + " " + text);
//        System.out.println(parse(text));
        System.out.println(command.getName() + " " + text);
        var argSeq = parse(text);
        System.out.println(argSeq);
        System.out.println(argGen.generator(argSeq));
        System.out.println("-".repeat(80));
        System.out.println();
      } catch (Exception e) {
        System.out.println("*".repeat(80));
        System.out.println(String.format("""
          Error parsing : '%s'
          error         ; %s""", text, e.getMessage()));
        System.out.println("*".repeat(80));
      }
    }
  }

  public static void main(String[] args) {
    List<ProvUtil.Command> vs = Arrays.asList(ProvUtil.Command.values());
    vs.sort( (cmd1, cmd2) -> cmd1.getName().compareTo(cmd2.getName()) );

    for (var command : vs) {
      String text = command.getHelp();
      try {
        var argGen = new ArgGenerator(ProvUtilArgumentsValues.getValues());
//        System.out.println(command.getName() + " " + text);
//        System.out.println(parse(text));
//        System.out.println(command.getName() + " " + text);
//        var argSeq = parse(text);
//        System.out.println(argSeq);
//        System.out.println(argGen.generator(argSeq));
//        System.out.println("-".repeat(80));
//        System.out.println();
        List<List<String>> argsLists = genCommandArguments(command, argGen, 5);
        System.out.println( renderCommandArguments(argsLists) );
      } catch (Exception e) {
        System.out.println("*".repeat(80));
        System.out.println(String.format("""
          Error parsing : '%s'
          error         ; %s""", text, e.getMessage()));
        System.out.println("*".repeat(80));
      }
    }
  }

  public static void main_(String[] args) {
    var names = new HashSet<String>();
    for (var command : ProvUtil.Command.values()) {
      String text = command.getHelp();
      try {
        Seq arg = parse(text);
        names.addAll(findNames(arg));
      } catch (Exception e) {
        System.out.println("*".repeat(80));
        System.out.println(String.format("Error parsing '%s'", text));
        System.out.println("*".repeat(80));
      }
    }
    names.stream().sorted().forEach( nm -> {
      System.out.println( String.format( "res.put(\"%s\", () -> List.of(List.of(\"%s\")));",
              nm, nm
      ));
    });
  }

}
