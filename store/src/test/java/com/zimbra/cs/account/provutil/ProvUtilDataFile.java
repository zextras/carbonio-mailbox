package com.zimbra.cs.account.provutil;

import com.zimbra.cs.account.ProvUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DataFileWriter {
  private final Path baseFolder;
  private final ArgGenerator argGenerator;
  private final int numberOfSamples;

  public DataFileWriter(Path baseFolder, ArgGenerator argGenerator, int numberOfSamples) {
    this.baseFolder = baseFolder;
    this.argGenerator = argGenerator;
    this.numberOfSamples = numberOfSamples;
  }

  public void writeFile(ProvUtil.Command cmd) throws IOException {
    var fileContent = getFileContent(cmd);
    Files.writeString( baseFolder.resolve(cmd.getName()), fileContent );
  }

  static <T> Stream<T> takeElements(Stream<T> items, int number) {
    var list = items.toList();
    int size = list.size();
    if (size < number) {
      return list.stream();
    } else {
      var arr = new ArrayList<T>();
      for (int i = 0; i < number; i++) {
        arr.add(list.get((i * 11 + 7) % size));
      }
      return arr.stream();
    }
  }

  private String getFileContent(ProvUtil.Command cmd) {
    var args = CommandArgumentParser.parse(cmd.getHelp());
    Stream<Stream<String>> argsValue = takeElements(argGenerator.generator(args), numberOfSamples);
    return argsValue
            .map(cmdArgs -> cmdArgs.collect(Collectors.joining(" ")))
            .collect(Collectors.joining("\n"));
  }

}
