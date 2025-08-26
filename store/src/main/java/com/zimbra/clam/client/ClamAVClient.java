// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.clam.client;

import com.zimbra.clam.client.exceptions.ClamAVSizeLimitException;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * The ClamAVClient class represents a client for communicating with a ClamAV server. It provides
 * functionality to connect to the server, scan files or data streams, and retrieve scan results.
 * <p>
 *
 * @author Keshav Bhatt
 * @since 23.7.0
 */
public class ClamAVClient {

  private final String mHostName;
  private final int mPort;
  private final int mTimeout;
  private final int mChunkSize;

  /**
   * @param hostname The hostname of the server running clamav-daemon
   * @param port     The port that clamav-daemon listing to
   * @param timeout  Timeout value zero means infinite timeout
   */
  public ClamAVClient(String hostname, int port,
      int timeout, int chunkSize) {

    if (timeout < 0) {
      throw new IllegalArgumentException("Unexpected negative timeout value supplied as argument");
    }
    this.mHostName = hostname;
    this.mPort = port;
    this.mTimeout = timeout;
    this.mChunkSize = chunkSize;
  }

  /**
   * Interpret the result from a  ClamAV scan, and determine if the result means the data is clean
   *
   * @param reply The reply from the server after scanning
   * @return true if no virus was found according to the clamd reply message
   *
   * <p>
   * @author Keshav Bhatt
   * @since 23.7.0
   */
  public static boolean replyOk(byte[] reply) {
    final String output = new String(reply, StandardCharsets.UTF_8);
    return (output.contains("OK") && !output.contains("FOUND"));
  }

  /**
   * Reads all available bytes from the provided InputStream and returns them as a byte array.
   * <p>
   *
   * @param is the InputStream to read from
   * @return a byte array containing all the read bytes
   * @throws IOException if an I/O error occurs during the reading process
   *                     <p>
   * @author Keshav Bhatt
   * @since 23.7.0
   */
  private static byte[] readAll(InputStream is) throws IOException {
    final ByteArrayOutputStream tmp = new ByteArrayOutputStream();

    byte[] buf = new byte[2000];
    int read;
    do {
      read = is.read(buf);
      if (read > 0) {
        tmp.write(buf, 0, read);
      }
    } while ((read > 0) && (is.available() > 0));
    return tmp.toByteArray();
  }

  public int getChunkSize() {
    return mChunkSize;
  }

  public String getHostName() {
    return mHostName;
  }

  public int getPort() {
    return mPort;
  }

  public int getTimeout() {
    return mTimeout;
  }

  /**
   * Streams the given data to the ClamAV server in chunks. This method is preferred when you do not
   * want to keep the entire data in memory, such as when scanning a file on disk.
   * <p>
   * This method opens a socket connection to the ClamAV server, sends the data in chunks, and reads
   * the server's reply. The input stream is NOT closed by this method.
   *
   * <p>
   *
   * @param inputStream the data stream to be scanned. It should not be closed by this method.
   * @return the server's reply as a byte array
   * @throws IOException if an I/O error occurs during the scan process
   *
   *                     <p>
   * @author Keshav Bhatt
   * @since 23.7.0
   */
  public byte[] scan(InputStream inputStream) throws IOException {
    try (Socket s = new Socket(mHostName, mPort); OutputStream outs = new BufferedOutputStream(
        s.getOutputStream())) {
      s.setSoTimeout(mTimeout);

      // handshake
      outs.write(("zINSTREAM\0").getBytes(StandardCharsets.UTF_8));
      outs.flush();
      byte[] chunk = new byte[mChunkSize];

      try (InputStream clamIs = s.getInputStream()) {
        // send data
        int read = inputStream.read(chunk);
        while (read >= 0) {
          // The format of the chunk is: '<length><data>' where <length> is the size of the
          // following data in bytes expressed as a 4 byte unsigned
          // integer in network byte order and <data> is the actual chunk. Streaming is terminated
          // by sending a zero-length chunk.
          byte[] chunkSize = ByteBuffer.allocate(4).putInt(read).array();

          outs.write(chunkSize);
          outs.write(chunk, 0, read);
          if (clamIs.available() > 0) {
            // reply from server before scan command has been terminated
            byte[] reply = assertSizeLimit(readAll(clamIs));
            throw new IOException(
                "Scan aborted. Reply from server: " + new String(reply, StandardCharsets.UTF_8));
          }
          read = inputStream.read(chunk);
        }

        // terminate scan
        outs.write(new byte[]{0, 0, 0, 0});
        outs.flush();
        // read reply
        return assertSizeLimit(readAll(clamIs));
      }
    }
  }

  /**
   * Scans the given bytes for viruses by passing the bytes to ClamAV.
   * <p>
   *
   * @param in the data to scan
   * @return the server reply
   * @throws IOException if an I/O error occurs during the scan process
   *
   *                     <p>
   * @author Keshav Bhatt
   * @since 23.7.0
   */
  public byte[] scan(byte[] in) throws IOException {
    final ByteArrayInputStream bis = new ByteArrayInputStream(in);
    return scan(bis);
  }

  /**
   * Asserts if the reply from the ClamAV server indicates that the size limit has been exceeded.
   * <p>
   *
   * @param reply the reply from the ClamAV server as a byte array
   * @return the original reply if the size limit is not exceeded
   * @throws ClamAVSizeLimitException if the reply indicates that the ClamAV daemon's size limit has
   *                                  been exceeded
   *                                  <p>
   * @author Keshav Bhatt
   * @since 23.7.0
   */
  private byte[] assertSizeLimit(byte[] reply) {
    final String r = new String(reply, StandardCharsets.UTF_8);
    if (r.startsWith("INSTREAM size limit exceeded.")) {
      throw new ClamAVSizeLimitException(
          "ClamAV daemon size limit exceeded. Full reply from server: " + r);
    }
    return reply;
  }
}
