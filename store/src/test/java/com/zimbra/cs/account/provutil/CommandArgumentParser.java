package com.zimbra.cs.account.provutil;

import com.zimbra.cs.account.ProvUtil;
import com.zimbra.cs.account.provutil.Input.Result;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.zimbra.cs.account.provutil.CommandArgumentType.*;

public class CommandArgumentParser {

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
            .or(() -> onPatternReturn(input, "[sortAscending 0|1*]", optional(seq(of("sortAscending"), union(of("0"), of("1"))))))
            .or( () -> onPatternReturn(input, "[[-v] [-ni] [{entry-type}]] | [-a {attribute-name}]", union(
                    seq(
                            optional(of("-v")),
                            optional(of("-ni")),
                            optional(of("entry-type"))
                    ),
                    optional(seq(of("-a"), of("attribute-name")))
            )));

  }

  private static @NotNull Optional<Result<CommandArgumentType>> onPatternReturn(Input input, String pattern, CommandArgumentType arguments) {
    return input.expect(pattern).map(rest -> new Result<>(arguments, rest));
  }

  public static Optional<Result<CommandArgumentType>> parseOptionalArgumentType(Input input) {
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

  public static Optional<Result<CommandArgumentType>> parseRequiredArgumentType(Input input) {
    return delimited("\\{", CommandArgumentParser::parseUnion, "\\}", input).map(v -> {
      var isSeqRes = v.rest().skipOpt("\\+");
      if (isSeqRes.isPresent()) {
        return new Result<>(rep(v.value()), isSeqRes.get());
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

  static Map<String, Supplier<Stream<Stream<String>>>> bindings() {
    Map<String, Supplier<Stream<Stream<String>>>> res = new HashMap<String, Supplier<Stream<Stream<String>>>>();
    res.put("--server", () -> Stream.of(Stream.of("server-host.example.com")));
    res.put("-a", () -> Stream.of(Stream.of("account-name@example.com")));
    res.put("-c", () -> Stream.of(Stream.of("--category")));
    res.put("-d", () -> Stream.of(Stream.of("--debug")));
    res.put("-e", () -> Stream.of(Stream.of("--expand")));
    res.put("-f", () -> Stream.of(Stream.of("--file")));
    res.put("-g", () -> Stream.of(Stream.of("usr"), Stream.of("grp"), Stream.of("egp"), Stream.of("all"), Stream.of("dom"), Stream.of("edom"), Stream.of("gst"), Stream.of("key"), Stream.of("pub"), Stream.of("email")));
    res.put("-ni", () -> Stream.of(Stream.of("--non-inherited")));
    res.put("-s", () -> Stream.of(Stream.of("server-hostname.example.com")));
    res.put("-t", () -> Stream.of(Stream.of("account"), Stream.of("calresource"), Stream.of("cos"), Stream.of("dl"), Stream.of("group"), Stream.of("domain"), Stream.of("server"), Stream.of("xmppcomponent"), Stream.of("zimlet"), Stream.of("config"), Stream.of("global")));
    res.put("-v", () -> Stream.of(Stream.of("--verbose")));
    res.put("0", () -> Stream.of(Stream.of("0")));
    res.put("1", () -> Stream.of(Stream.of("1")));
    res.put("ADMIN", () -> Stream.of(Stream.of("ADMIN")));
    res.put("ALL", () -> Stream.of(Stream.of("ALL")));
    res.put("FALSE", () -> Stream.of(Stream.of("FALSE")));
    res.put("TRUE", () -> Stream.of(Stream.of("TRUE")));
    res.put("USER", () -> Stream.of(Stream.of("USER")));
    res.put("account", () -> Stream.of(Stream.of("account-d3175941-9d69-46de-83e7-0c191b7e0953")));
    res.put("account-id", () -> Stream.of(Stream.of("account-301c1dab-c07d-478c-b5db-eaffcc64b593")));
    res.put("acl", () -> Stream.of(Stream.of("mailing.list@example.com"))); // ??
    res.put("alias", () -> Stream.of(Stream.of("alias@example.com")));
    res.put("alias-domain-name", () -> Stream.of(Stream.of("example-alias.com")));
    res.put("alias@domain", () -> Stream.of(Stream.of("alias@example.com")));
    res.put("all", () -> Stream.of(Stream.of("all")));
    // *******
    res.put("argument", () -> Stream.of(Stream.of("argument")));
    // *******
    res.put("attr", () -> Stream.of(Stream.of("attribute")));
    // *******
    res.put("attribute-name", () -> Stream.of(Stream.of("zimbraId"), Stream.of("zimbraImapBindPort")));
    res.put("attribute-value", () -> Stream.of(Stream.of("\"attribute-value\"", "1")));
    res.put("auth-token", () -> Stream.of(Stream.of("0_2b6c930a7ca1a02daad5f27528d6c9986317204e_69643d33363a62333134613231652d666137392d346533352d613765352d6437666637303834333866363b6578703d31333a313733323535383437303239303b76763d323a31363b747970653d363a7a696d6272613b753d313a613b7469643d31303a313131353331313832383b")));
    // *******
    res.put("by", () -> Stream.of(Stream.of("by")));
    // *******
    res.put("calresource", () -> Stream.of(Stream.of("calendar-resource")));
    // *******
    res.put("cancel", () -> Stream.of(Stream.of("cancel")));
    // *******
    res.put("category", () -> Stream.of(Stream.of("category")));
    // *******
    res.put("classname", () -> Stream.of(Stream.of("classname")));
    // *******
    res.put("commands", () -> Stream.of(Stream.of("commands")));
    res.put("config", () -> Stream.of(Stream.of("global-config")));
    res.put("configName", () -> Stream.of(Stream.of("config-name")));
    res.put("cos", () -> Stream.of(Stream.of("cos")));
    res.put("cos-id", () -> Stream.of(Stream.of("cos-1829acc8-2fd3-45cf-aac5-f3b3078daaa8")));
    res.put("cos-name", () -> Stream.of(Stream.of("cos-name")));
    res.put("debug", () -> Stream.of(Stream.of("--debug")));
    res.put("dest-cos-name", () -> Stream.of(Stream.of("cos")));
    res.put("dl", () -> Stream.of(Stream.of("distribution-list")));
    res.put("domain", () -> Stream.of(Stream.of("example.com")));
    res.put("domain-id", () -> Stream.of(Stream.of("domain-36f01e27-88de-4495-bb4b-9b05443aa8f7")));
    res.put("domain-name", () -> Stream.of(Stream.of("example.com")));
    res.put("ds-id", () -> Stream.of(Stream.of("data-source-5bfd9bc4-d359-4a2c-8424-1101dffba0ee")));
    res.put("ds-name", () -> Stream.of(Stream.of("datasource")));
    // *************
    res.put("ds-type", () -> Stream.of(Stream.of("pop3"), Stream.of("imap"), Stream.of("gal")));
    // *************
    res.put("entry-type", () -> Stream.of(Stream.of("account"), Stream.of("cos"), Stream.of("domain"), Stream.of("globalConfig"), Stream.of("identity"), Stream.of("dataSource"), Stream.of("galDataSource"), Stream.of("distributionList"), Stream.of("group"), Stream.of("server"), Stream.of("mailRecipient"), Stream.of("timeZone"), Stream.of("zimletEntry")));
    res.put("error", () -> Stream.of(Stream.of("error")));
    // *************
    res.put("expandGetAttrs", () -> Stream.of(Stream.of("expandGetAttrs")));
    // *************
    res.put("expandSetAttrs", () -> Stream.of(Stream.of("expandSetAttrs")));
    // *************
    res.put("expires", () -> Stream.of(Stream.of("expires")));
    // *************
    res.put("extension-cache-type", () -> Stream.of(Stream.of("extension-cache-type")));
    res.put("false", () -> Stream.of(Stream.of("false")));
    res.put("folder-id", () -> Stream.of(Stream.of("folder-eb15a846-21ed-4f1f-bf21-2759f282c237")));
    res.put("foreignPrincipal", () -> Stream.of(Stream.of("foreign.pricipal@example.com")));
    res.put("galgroup", () -> Stream.of(Stream.of("galgroup")));
    res.put("globalgrant", () -> Stream.of(Stream.of("globalgrant")));
    res.put("grantee-id", () -> Stream.of(Stream.of("grantee-75aca60e-8616-4165-a3ba-a6b96d529c97")));
    res.put("grantee-name", () -> Stream.of(Stream.of("grantee.name@example.com")));
    res.put("grantee-type", () -> Stream.of(Stream.of("usr"), Stream.of("grp"), Stream.of("egp"), Stream.of("all"), Stream.of("dom"), Stream.of("edom"), Stream.of("gst"), Stream.of("key"), Stream.of("pub"), Stream.of("email")));
    res.put("group", () -> Stream.of(Stream.of("group")));
    res.put("groupName", () -> Stream.of(Stream.of("groupName")));
    res.put("habGrpId", () -> Stream.of(Stream.of("hab-group-ba8aab08-31d7-48c1-ad38-c2e436590782"))); //Hierarchical Address Book
    res.put("habParentGrpId", () -> Stream.of(Stream.of("parent-hab-2452819a-ca92-4d50-8294-a10564624b8e")));
    res.put("habRootGrpId", () -> Stream.of(Stream.of("hab-root-faa9ff15-7d84-45a4-92e9-eb2fee744013")));
    res.put("hostname", () -> Stream.of(Stream.of("host.example.com")));
    res.put("id", () -> Stream.of(Stream.of("id-8a64a712-cceb-4e03-b5ce-c131481bb455")));
    res.put("identity-name", () -> Stream.of(Stream.of("identity-name")));
    res.put("info", () -> Stream.of(Stream.of("info")));
    res.put("internalArchivingAccount", () -> Stream.of(Stream.of("internal.archiving.user@example.com")));
    res.put("internalUserAccount", () -> Stream.of(Stream.of("internal.user@example.com")));
    res.put("internalUserAccountX", () -> Stream.of(Stream.of("internalx.user@example.com")));
    res.put("java", () -> Stream.of(Stream.of("java")));
    res.put("ldap-query", () -> Stream.of(Stream.of("ldap-query")));
    res.put("license", () -> Stream.of(Stream.of("license")));
    res.put("limit", () -> Stream.of(Stream.of("limit")));
    res.put("list@domain", () -> Stream.of(Stream.of("list@example.com")));
    res.put("local-domain-name", () -> Stream.of(Stream.of("local-example.com")));
    res.put("locale", () -> Stream.of(Stream.of("locale")));
    res.put("logging-category", () -> Stream.of(Stream.of("zimbra.soap"), Stream.of("zimbra.lmtp")));
    res.put("member@domain", () -> Stream.of(Stream.of("member@example.com")));
    res.put("mime", () -> Stream.of(Stream.of("mime")));
    res.put("name", () -> Stream.of(Stream.of("name")));
    res.put("name@domain", () -> Stream.of(Stream.of("user@example.com")));
    res.put("namemask", () -> Stream.of(Stream.of("namemask")));
    res.put("newDomain", () -> Stream.of(Stream.of("new.example.com")));
    res.put("newName", () -> Stream.of(Stream.of("newName")));
    res.put("newName@domain", () -> Stream.of(Stream.of("newName@example.com")));
    res.put("number-of-accounts-to-create", () -> Stream.of(Stream.of("10")));
    res.put("offset", () -> Stream.of(Stream.of("0")));
    res.put("op", () -> Stream.of(Stream.of("op")));
    res.put("ouName", () -> Stream.of(Stream.of("ouName")));
    res.put("owner-id", () -> Stream.of(Stream.of("owner-8a64a712-cceb-4e03-yhu6-c131481bb455")));
    res.put("owner-name", () -> Stream.of(Stream.of("owner-name")));
    res.put("packages", () -> Stream.of(Stream.of("packages")));
    res.put("password", () -> Stream.of(Stream.of("password")));
    res.put("propertiesList", () -> Stream.of(Stream.of("propertiesList")));
    res.put("provider-name", () -> Stream.of(Stream.of("provider-name")));
    res.put("right", () -> Stream.of(Stream.of("right")));
    res.put("secret", () -> Stream.of(Stream.of("secret")));
    res.put("seniorityIndex", () -> Stream.of(Stream.of("seniorityIndex")));
    res.put("server", () -> Stream.of(Stream.of("server")));
    res.put("server-id", () -> Stream.of(Stream.of("server-22c6754a-ea39-4b65-a1c2-88447b30000f")));
    res.put("service", () -> Stream.of(Stream.of("service")));
    res.put("short-name", () -> Stream.of(Stream.of("short-name")));
    res.put("signature-id", () -> Stream.of(Stream.of("signature-id")));
    res.put("signature-name", () -> Stream.of(Stream.of("signature-name")));
    res.put("sortAscending", () -> Stream.of(Stream.of("sortAscending")));
    res.put("sortBy", () -> Stream.of(Stream.of("sortBy")));
    res.put("src-cos-name", () -> Stream.of(Stream.of("src-cos-name")));
    res.put("start", () -> Stream.of(Stream.of("start")));
    res.put("status", () -> Stream.of(Stream.of("status")));
    res.put("stop", () -> Stream.of(Stream.of("stop")));
    res.put("target-id", () -> Stream.of(Stream.of("target-22c3163a-ea39-4b65-a1c2-88447b30000f")));
    res.put("target-name", () -> Stream.of(Stream.of("target-name")));
    res.put("target-type", () -> Stream.of(Stream.of("account"), Stream.of("calresource"), Stream.of("cos"), Stream.of("dl"), Stream.of("group"), Stream.of("domain"), Stream.of("server"), Stream.of("xmppcomponent"), Stream.of("zimlet"), Stream.of("config"), Stream.of("global")));
    res.put("targetHabParentGrpId", () -> Stream.of(Stream.of("targetHabParentGrpId-56tr163a-ea39-4b65-a1c2-88447b30000f")));
    res.put("timestamp", () -> Stream.of(Stream.of("timestamp")));
    res.put("token", () -> Stream.of(Stream.of("token")));
    res.put("trace", () -> Stream.of(Stream.of("trace")));
    res.put("true", () -> Stream.of(Stream.of("true")));
    res.put("type", () -> Stream.of(Stream.of("type")));
    res.put("uistrings", () -> Stream.of(Stream.of("uistrings")));
    res.put("userAccount", () -> Stream.of(Stream.of("userAccount")));
    res.put("value", () -> Stream.of(Stream.of("value")));
    res.put("virtualHostname", () -> Stream.of(Stream.of("virtualHostname")));
    res.put("warn", () -> Stream.of(Stream.of("warn")));
    res.put("xmpp-component-name", () -> Stream.of(Stream.of("xmpp-component-name")));
    res.put("zimbraDataSourceEnabled", () -> Stream.of(Stream.of("zimbraDataSourceEnabled")));
    res.put("zimbraDataSourceFolderId", () -> Stream.of(Stream.of("zimbraDataSourceFolderId")));
    res.put("zimlet", () -> Stream.of(Stream.of("zimlet")));
    return res;
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

  public static void main(String[] args) {
    for (var command : ProvUtil.Command.values()) {
      String text = command.getHelp();
      try {
        var argGen = new ArgGenerator(bindings());
//        System.out.println(command.getName() + " " + text);
//        System.out.println(parse(text));
        System.out.println(command.getName() + " " + text);
        var argSeq = parse(text);
        System.out.println(argSeq);
        System.out.println(argGen.generator(argSeq).map(Stream::toList).toList());
        System.out.println(ArgGenerator.takeElements( argGen.generator(argSeq).map(Stream::toList), 4 ).toList());
        System.out.println("-".repeat(80));
        System.out.println();
      } catch (Exception e) {
        System.out.println("*".repeat(80));
        System.out.println(String.format("Error parsing '%s'", text));
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
      System.out.println( String.format( "res.put(\"%s\", () -> Stream.of(Stream.of(\"%s\")));",
              nm, nm
      ));
    });
  }

}
