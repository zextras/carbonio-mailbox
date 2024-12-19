package com.zimbra.cs.account;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class Console {

  final PrintStream stdOut;
  final PrintStream stdError;

  public Console(PrintStream stdOut, PrintStream stdError) {
    this.stdOut = stdOut;
    this.stdError = stdError;
  }

  public Console(OutputStream outputStream, OutputStream errorStream) {
    this.stdOut = new PrintStream(outputStream);
    this.stdError = new PrintStream(errorStream);
  }

  public void print(String data) {
    this.stdOut.print(data);
  }

  public void println(Object obj) {
    this.stdOut.println(obj);
  }

  public void println() {
    this.stdOut.println();
  }

  public void printStacktrace(Exception exception) {
    exception.printStackTrace(this.stdError);
  }

  public void printError(String text) {
    PrintStream ps = this.stdError;
    try {
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ps, StandardCharsets.UTF_8));
      writer.write(text + "\n");
      writer.flush();
    } catch (IOException e) {
      ps.println(text);
    }
  }

  public void printOutput(String text) {
    try {
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(this.stdOut, StandardCharsets.UTF_8));
      writer.write(text + "\n");
      writer.flush();
    } catch (IOException e) {
      this.stdOut.println(text);
    }
  }

}
