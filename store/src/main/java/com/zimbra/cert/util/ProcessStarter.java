package com.zimbra.cert.util;

public interface ProcessStarter {

  /**
   * Starts a process using given args.
   * Example: start("/bin/bash", "my_beautiful_script.sh", "myBeautifulValue")
   * @return the started process
   */
  Process start(String ...args);

}
