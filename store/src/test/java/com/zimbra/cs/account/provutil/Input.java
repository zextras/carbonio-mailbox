package com.zimbra.cs.account.provutil;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Input {

  private final String text;
  private final int index;

  record Result<T>(T value, Input rest) {
    public <B> Result<B> map(Function<T,B> f) {
      return new Result<>(f.apply(value), rest);
    }
  }

  public Input(String text) {
    this(text, 0);
  }
  public Input(String text, int index) {
    this.text = text;
    this.index = index;
  }

  public int getIndex() {
    return index;
  }

  boolean hasNext() {
    return index < text.length();
  }

  Optional<Result<Character>> next() {
    if (hasNext()) {
      return Optional.of(new Result(text.substring(index, index +1), new Input(text, index+1)));
    } else {
      return Optional.empty();
    }
  }

  public Input skip(String rg) {
    return skipOpt(rg).orElse(this);
  }

  public Optional<Input> skipOpt(String rg) {
    return consume(rg).map(Result::rest);
  }

  public Result<Optional<String>> consumeOpt(String rg) {
    return consume(rg)
            .map( res -> new Result<>(Optional.of(res.value), res.rest()))
            .orElseGet( () -> new Result<>(Optional.empty(), this));
  }

  public Optional<Result<String>> consume(String rg) {
    Pattern pattern = Pattern.compile(rg);
    Matcher matcher = pattern.matcher(getValue());
    if (matcher.lookingAt()) {
      String value = matcher.group();
      return Optional.of(new Result(value, new Input(text, index + value.length())));
    } else {
      return Optional.empty();
    }
  }

  public Optional<Input> expect(String literalValue) {
    var v = getValue();
    if (v.startsWith(literalValue)) {
      return Optional.of(new Input(text, index + literalValue.length()));
    } else {
      return Optional.empty();
    }
  }

  public String getValue() {
    return text.substring(index);
  }

  @Override public String toString() {
    return String.format("""
            %s
            (%s) -> %s         
            """, text.substring(0, index), index, text.substring(index));
  }
}
