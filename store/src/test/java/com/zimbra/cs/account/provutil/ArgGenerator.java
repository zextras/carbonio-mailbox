package com.zimbra.cs.account.provutil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ArgGenerator {

  private final Map<String, Supplier<List<List<String>>>> valuesGeneratorsMap;

  public ArgGenerator(Map<String, Supplier<List<List<String>>>> valuesGeneratorsMap) {
    this.valuesGeneratorsMap = valuesGeneratorsMap;
  }

  <T> List<T> concat(List<T> a, List<T>... bs) {
    var res = new ArrayList<T>(a);
    for (var b : bs) {
      res.addAll(b);
    }
    return res;
  }

  List<List<String>> generator(CommandArgumentType arg) {
    if (arg instanceof CommandArgumentType.Simple simpleArg) {
      var result = valuesGeneratorsMap.get(simpleArg.value());
      if (result == null) throw new NullPointerException(simpleArg.value());
      else return result.get();
    } else if (arg instanceof CommandArgumentType.Optional opt) {
      return optionalGenerator(opt);
    } else if (arg instanceof CommandArgumentType.Rep rep) {
      return repGenerator(rep);
    } else if (arg instanceof CommandArgumentType.Union union) {
      return unionGenerator(union);
    } else if (arg instanceof CommandArgumentType.Seq seq) {
      return seqGenerator(seq);
    } else {
      throw new UnsupportedOperationException(String.format("Argument '%s'", arg));
    }
  }

  static <T> List<T> ofSize(int size, List<List<T>> choices) {
    List<T> res = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      res.addAll(choices.get(i % choices.size()));
    }
    return res;
  }

  List<List<String>> repGenerator(CommandArgumentType.Rep rep) {
    List<List<String>> choices = generator(rep.item());
    return List.of(
            List.of(),
            ofSize(2, choices),
            ofSize(3, choices)
    );
  }

  List<List<String>> optionalGenerator(CommandArgumentType.Optional opt) {
    return concat(
            List.<List<String>>of(List.of()),
            generator(opt.item())
    );
  }

  List<List<String>> unionGenerator(CommandArgumentType.Union opt) {
    return concat(opt.members().stream().flatMap(v -> this.generator(v).stream()).toList());
  }

  static <ACC, I> ACC foldRight(BiFunction<I, ACC, ACC> f, ACC acc, List<I> lst) {
    if (lst.isEmpty()) {
      return acc;
    } else {
      var last = lst.get(lst.size() - 1);
      return f.apply(last, foldRight(f, acc, lst.subList(0, lst.size() - 1)));
    }
  }

  List<List<String>> seqGenerator(CommandArgumentType.Seq seq) {
    return foldRight((CommandArgumentType arg, List<List<String>> acc) ->
                    acc.stream().flatMap(str ->
                            generator(arg).stream().map(argStream ->
                                    concat(str, argStream))).toList(),
            List.of(List.of()),
            seq.arguments()
    );
  }
}
