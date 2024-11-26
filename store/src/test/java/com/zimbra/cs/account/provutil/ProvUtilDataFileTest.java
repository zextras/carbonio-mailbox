package com.zimbra.cs.account.provutil;

import com.zimbra.cs.account.ProvUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

class DataFileWriterTest {

  @TempDir
  File outputFolder;

  @Test
  void addAccountLogger() throws IOException {
    Map<String, Supplier<Stream<Stream<String>>>> values = new HashMap<>();
    values.put("-s", () -> Stream.of(Stream.of("-s")));
    values.put("--server", () -> Stream.of(Stream.of("--server")));
    values.put("hostname", () -> Stream.of(Stream.of("host.example.com")));
    values.put("name@domain", () -> Stream.of(Stream.of("user@example.com")));
    values.put("id", () -> Stream.of(Stream.of("752f70fd-d753-4fc6-aabb-4f338e79e77e")));
    values.put("logging-category", () -> Stream.of(Stream.of("zimbra.soap"), Stream.of("zimbra.lmtp")));
    values.put("trace", () -> Stream.of(Stream.of("trace")));
    values.put("debug", () -> Stream.of(Stream.of("debug")));
    values.put("info", () -> Stream.of(Stream.of("info")));
    values.put("warn", () -> Stream.of(Stream.of("warn")));
    values.put("error", () -> Stream.of(Stream.of("error")));
    ArgGenerator argGen = new ArgGenerator(values);
    var dfw = new DataFileWriter(outputFolder.toPath(), argGen, 4);

    dfw.writeFile(ProvUtil.Command.ADD_ACCOUNT_LOGGER);

    String content = Files.readString( outputFolder.toPath().resolve(ProvUtil.Command.ADD_ACCOUNT_LOGGER.getName()) );

    Assertions.assertEquals("""
            user@example.com zimbra.lmtp info
            752f70fd-d753-4fc6-aabb-4f338e79e77e zimbra.lmtp warn
            -s host.example.com user@example.com zimbra.lmtp error
            --server host.example.com user@example.com zimbra.soap trace""", content);
  }

}