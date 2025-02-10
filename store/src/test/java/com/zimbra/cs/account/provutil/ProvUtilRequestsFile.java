package com.zimbra.cs.account.provutil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ProvUtilRequestsFile {

  private final Path baseFolder;
  public ProvUtilRequestsFile(Path baseFolder) {
    this.baseFolder = baseFolder;
  }

  static String getFileName(String cmd) {
    String fileName = cmd.replaceAll("\"\\s@\\.", "_");
    if (fileName.length() >= 255) {
      return fileName.substring(0, 255);
    } else {
      return fileName;
    }
  }

  public Path getFilePath(List<String> args) {
    var cmd = args.get(0);
    return baseFolder.resolve(cmd).resolve(getFileName(String.join("_", args)));
  }

  public Path getActualFilePath(List<String> args) {
    var cmd = args.get(0);
    return baseFolder.resolve(cmd).resolve(String.format("%s.actual", getFileName(String.join("_", args))));
  }

  void write(List<String> command, List<String> requests) throws IOException {
    var filePath = getFilePath(command);
    File file = filePath.toFile();
    createParentFolder(file);
    Files.writeString(filePath, createFileContent(command, requests));
  }

  private static void createParentFolder(File file) throws IOException {
    if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
      throw new IOException(String.format("Could not create folder %s", file.getParentFile().getAbsolutePath()));
    }
  }

  public enum DiffResult {
    ok, notOk
  }

  public DiffResult diffOrStore(List<String> arguments, List<String> requests) throws IOException {
    var filePath = getFilePath(arguments);
    if (!filePath.toFile().exists()) {
      write(arguments, requests);
      return DiffResult.ok;
    } else {
      String actualContent = createFileContent(arguments, requests);
      String expectedContent = Files.readString(filePath);
      boolean actualMatchExpected = actualContent.equals(expectedContent);
      if (!actualMatchExpected) {
        var actualFilePath = getActualFilePath(arguments);
        createParentFolder(actualFilePath.toFile());
        Files.writeString(actualFilePath, actualContent);
      }
      return actualMatchExpected ? DiffResult.ok : DiffResult.notOk;
    }
  }

  String createFileContent(List<String> arguments, List<String> requests) {
    var lines = new ArrayList<String>();
    lines.add(String.join(" ", arguments));
    lines.addAll(requests);
    return String.join("\n", lines);
  }

}
