package com.zimbra.cs.account.provutil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ArgGenerator {

  private final Map<String, Supplier<Stream<Stream<String>>>> valuesGeneratorsMap;

//  final static int REP_COUNT = 3;
//  final static int UNION_COUNT = 4;

  public ArgGenerator(Map<String, Supplier<Stream<Stream<String>>>> valuesGeneratorsMap) {
    this.valuesGeneratorsMap = valuesGeneratorsMap;
  }

  @SafeVarargs static <A> Stream<A> concat(Stream<A>... streams) {
    return concat(Arrays.stream(streams));
  }

  static <A> Stream<A> concat(Stream<Stream<A>> streams) {
    return streams.flatMap(Function.identity());
  }

  <T> List<T> concat(List<T> a, T b) {
    return concat(a.stream(), Stream.of(b)).toList();
  }

  private static Stream<Integer> range(int min, int n) {
    return IntStream.range(min, n).mapToObj(Integer::valueOf);
  }

  Stream<Stream<String>> generator(CommandArgumentType arg) {
    if (arg instanceof CommandArgumentType.Simple simpleArg) {
      var result = valuesGeneratorsMap.get(simpleArg.value());
      if (result == null) throw new NullPointerException();
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

  Stream<Stream<String>> repGenerator(CommandArgumentType.Rep rep) {
    return concat(
        Stream.of(Stream.empty()),
        Stream.of(concat(generator(rep.item())))
    );
  }

  Stream<Stream<String>> optionalGenerator(CommandArgumentType.Optional opt) {
    return concat(Stream.of(Stream.empty()), generator(opt.item()));
  }

  Stream<Stream<String>> unionGenerator(CommandArgumentType.Union opt) {
    return concat(opt.members().stream().map(this::generator));
  }

  static <ACC, I> ACC foldRight(BiFunction<I, ACC, ACC> f, ACC acc, List<I> lst) {
    if (lst.isEmpty()) {
      return acc;
    } else {
      var last = lst.get(lst.size() - 1);
      return f.apply(last, foldRight(f, acc, lst.subList(0, lst.size() - 1)));
    }
  }

  Stream<Stream<String>> seqGenerator(CommandArgumentType.Seq seq) {
    return foldRight((CommandArgumentType arg, List<List<String>> acc) -> {
              Stream<Stream<String>> streamStream = acc.stream().flatMap(str ->
                      generator(arg).map(argStream ->
                              Stream.concat(str.stream(), argStream)));
              return streamStream.map(Stream::toList).toList();
            },
            List.of(List.of()),
            seq.arguments()
    ).stream().map(List::stream);
  }

  public static <T> Stream<T> takeElements(Stream<T> items, int number) {
    var list = items.toList();
    int size = list.size();
    if (size < number) {
      return list.stream();
    } else {
      var arr = new ArrayList<T>();
      int spacing = size / number;
      for (int i = 0; i < number; i++) {
        arr.add(list.get( (i * spacing + 2) % size ));
      }
      return arr.stream();
    }

  }
}
