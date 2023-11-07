package com.zextras.mailbox.support;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public abstract class Scenario {
  protected final String serviceName;

  public Scenario(String serviceName) {
    this.serviceName = serviceName;
  }

  protected String getContent(String scenario) {
    return getXmlFile(fullPathFor(scenario, getType()));
  }

  private String fullPathFor(String scenario, String type) {
    return String.format("soap/%s/%s/%s.xml", serviceName, scenario, type);
  }

  private String getXmlFile(String path) {
    try (InputStream resource = getClass().getClassLoader().getResourceAsStream(path)) {
      if (Objects.isNull(resource))
        throw new FileNotFoundException("Missing test resource: " + path);

      return new String(resource.readAllBytes(), StandardCharsets.UTF_8)
          // This replacement is necessary to remove the indentation and new lines
          .replaceAll(">\\s+<", "><")
          // This replacement is necessary to remove the end of file new line
          .replaceAll("\n", "");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract String getType();
}
