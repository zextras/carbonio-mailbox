// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.znative;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.Set;

/**
 * POSIX I/O operations for the mailbox store, replacing the JNI-based native library.
 *
 * <p>Uses Java FFM downcalls to {@code statx(2)} for {@link #fileInfo} (avoids the
 * {@code readAttributes} map allocation) and to {@code open(2)} + {@code dup2(2)} for
 * {@link #setStdoutStderrTo}. Uses {@code java.nio.file} for {@link #link} and {@link #chmod}.
 */
public final class IO {

  private IO() {}

  /** File metadata returned by {@link #fileInfo}: inode number, size, and hard link count. */
  public static class FileInfo {
    private final long inodeNum;
    private final long size;
    private final int linkCount;

    public FileInfo(long inodeNum, long size, int linkCount) {
      this.inodeNum = inodeNum;
      this.size = size;
      this.linkCount = linkCount;
    }

    public long getInodeNum() { return inodeNum; }
    public long getSize() { return size; }
    public int getLinkCount() { return linkCount; }
  }

  // POSIX permission constants (fixed on Linux)
  public static final int S_IRUSR = 0400;
  public static final int S_IWUSR = 0200;
  public static final int S_IXUSR = 0100;
  public static final int S_IRGRP = 0040;
  public static final int S_IWGRP = 0020;
  public static final int S_IXGRP = 0010;
  public static final int S_IROTH = 0004;
  public static final int S_IWOTH = 0002;
  public static final int S_IXOTH = 0001;
  public static final int S_ISUID = 04000;
  public static final int S_ISGID = 02000;
  public static final int S_ISVTX = 01000;

  // Precomputed immutable permission sets keyed by low 9 mode bits (0..0777).
  // Avoids per-call EnumSet.noneOf() allocation + 9 branch chain in chmod().
  @SuppressWarnings({"unchecked", "rawtypes"})
  private static final Set<PosixFilePermission>[] PERMS_CACHE = new Set[0x200];

  static {
    for (int m = 0; m < 0x200; m++) {
      EnumSet<PosixFilePermission> set = EnumSet.noneOf(PosixFilePermission.class);
      if ((m & S_IRUSR) != 0) set.add(PosixFilePermission.OWNER_READ);
      if ((m & S_IWUSR) != 0) set.add(PosixFilePermission.OWNER_WRITE);
      if ((m & S_IXUSR) != 0) set.add(PosixFilePermission.OWNER_EXECUTE);
      if ((m & S_IRGRP) != 0) set.add(PosixFilePermission.GROUP_READ);
      if ((m & S_IWGRP) != 0) set.add(PosixFilePermission.GROUP_WRITE);
      if ((m & S_IXGRP) != 0) set.add(PosixFilePermission.GROUP_EXECUTE);
      if ((m & S_IROTH) != 0) set.add(PosixFilePermission.OTHERS_READ);
      if ((m & S_IWOTH) != 0) set.add(PosixFilePermission.OTHERS_WRITE);
      if ((m & S_IXOTH) != 0) set.add(PosixFilePermission.OTHERS_EXECUTE);
      PERMS_CACHE[m] = set;  // raw EnumSet — JIT keeps fast iterator path
    }
  }

  // statx(2) constants. Offsets from Linux uapi/linux/stat.h (stable ABI).
  private static final int AT_FDCWD = -100;
  private static final int STATX_NLINK = 0x4;
  private static final int STATX_INO = 0x100;
  private static final int STATX_SIZE = 0x200;
  private static final int STATX_MASK = STATX_NLINK | STATX_INO | STATX_SIZE;
  private static final int STATX_STRUCT_SIZE = 256;
  private static final int OFF_STX_NLINK = 16;
  private static final int OFF_STX_INO = 32;
  private static final int OFF_STX_SIZE = 40;
  private static final int ENOENT = 2;

  // open(2) flags for setStdoutStderrTo — POSIX constants, stable Linux ABI.
  private static final int O_WRONLY = 1;
  private static final int O_CREAT = 64;
  private static final int O_APPEND = 1024;
  private static final int STDOUT_STDERR_MODE = 0644;

  private static final MethodHandle STATX;
  private static final StructLayout CAPTURE_LAYOUT;
  private static final VarHandle ERRNO_HANDLE;

  // FFM handles for open(), dup2(), close() — used only by setStdoutStderrTo()
  private static final MethodHandle OPEN;
  private static final MethodHandle DUP2;
  private static final MethodHandle CLOSE;

  /**
   * Per-thread scratch segments for {@link #fileInfo} — avoids per-call Arena and allocation
   * overhead. Uses {@link Arena#ofAuto} so the native memory is released when the thread dies
   * and the Scratch becomes unreachable.
   */
  private static final class Scratch {
    static final int INITIAL_PATH_CAPACITY = 4096;
    final MemorySegment statxBuf;
    final MemorySegment capture;
    MemorySegment pathBuf;
    int pathBufSize;

    Scratch() {
      Arena arena = Arena.ofAuto();
      this.statxBuf = arena.allocate(STATX_STRUCT_SIZE);
      this.capture = arena.allocate(CAPTURE_LAYOUT);
      this.pathBuf = arena.allocate(INITIAL_PATH_CAPACITY);
      this.pathBufSize = INITIAL_PATH_CAPACITY;
    }

    MemorySegment writePath(String s) {
      byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
      int needed = bytes.length + 1;
      if (needed > pathBufSize) {
        int grow = Math.max(needed, pathBufSize * 2);
        this.pathBuf = Arena.ofAuto().allocate(grow);
        this.pathBufSize = grow;
      }
      MemorySegment.copy(bytes, 0, pathBuf, ValueLayout.JAVA_BYTE, 0, bytes.length);
      pathBuf.set(ValueLayout.JAVA_BYTE, bytes.length, (byte) 0);
      return pathBuf;
    }
  }

  // Scratch native memory is owned by Arena.ofAuto(); it is released when the thread
  // terminates and the Scratch becomes unreachable. Calling remove() would defeat the
  // per-thread amortization this field exists to provide.
  @SuppressWarnings("java:S5164")
  private static final ThreadLocal<Scratch> SCRATCH = ThreadLocal.withInitial(Scratch::new);

  static {
    Linker linker = Linker.nativeLinker();
    SymbolLookup libc = linker.defaultLookup();

    CAPTURE_LAYOUT = Linker.Option.captureStateLayout();
    ERRNO_HANDLE = CAPTURE_LAYOUT.varHandle(MemoryLayout.PathElement.groupElement("errno"));

    STATX = linker.downcallHandle(
        libc.find("statx").orElseThrow(),
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT,
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS,
            ValueLayout.JAVA_INT,
            ValueLayout.JAVA_INT,
            ValueLayout.ADDRESS),
        Linker.Option.captureCallState("errno"));

    OPEN = linker.downcallHandle(
        libc.find("open").orElseThrow(),
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT),
        Linker.Option.firstVariadicArg(2));

    DUP2 = linker.downcallHandle(
        libc.find("dup2").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));

    CLOSE = linker.downcallHandle(
        libc.find("close").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
  }

  /**
   * Creates a hard link from {@code oldpath} to {@code newpath}.
   */
  public static void link(String oldpath, String newpath) throws IOException {
    link(Path.of(oldpath), Path.of(newpath));
  }

  /**
   * Creates a hard link from {@code oldpath} to {@code newpath}.
   */
  public static void link(Path oldpath, Path newpath) throws IOException {
    try {
      Files.createLink(newpath, oldpath);
    } catch (NoSuchFileException e) {
      throw new FileNotFoundException(
          String.format("link(%s, %s): %s", oldpath, newpath, e.getMessage()));
    } catch (FileAlreadyExistsException e) {
      throw new IOException(
          String.format("link(%s, %s): %s", oldpath, newpath, e.getMessage()), e);
    }
  }

  /**
   * Returns inode number, size, and hard link count for the given path via {@code statx(2)}.
   */
  public static FileInfo fileInfo(String path) throws IOException {
    Scratch s = SCRATCH.get();
    MemorySegment pathSeg = s.writePath(path);
    int rc;
    try {
      rc = (int) STATX.invoke(s.capture, AT_FDCWD, pathSeg, 0, STATX_MASK, s.statxBuf);
    } catch (Throwable t) {
      throw new IOException("statx(" + path + ") failed: " + t.getMessage(), t);
    }
    if (rc != 0) {
      int errno = (int) ERRNO_HANDLE.get(s.capture);
      if (errno == ENOENT) {
        throw new FileNotFoundException("stat(" + path + "): No such file or directory");
      }
      throw new IOException("statx(" + path + ") failed: errno=" + errno);
    }
    return new FileInfo(
        s.statxBuf.get(ValueLayout.JAVA_LONG, OFF_STX_INO),
        s.statxBuf.get(ValueLayout.JAVA_LONG, OFF_STX_SIZE),
        s.statxBuf.get(ValueLayout.JAVA_INT, OFF_STX_NLINK));
  }

  /**
   * Returns inode number, size, and hard link count for the given path via {@code statx(2)}.
   */
  public static FileInfo fileInfo(Path path) throws IOException {
    return fileInfo(path.toString());
  }

  /**
   * Returns the hard link count for the given path.
   */
  public static int linkCount(String path) throws IOException {
    return fileInfo(path).getLinkCount();
  }

  /**
   * Returns the hard link count for the given path.
   */
  public static int linkCount(Path path) throws IOException {
    return fileInfo(path).getLinkCount();
  }

  /**
   * Sets file permissions via {@link Files#setPosixFilePermissions}.
   *
   * @param path file path
   * @param mode POSIX mode bits (e.g. {@code S_IRUSR | S_IWUSR})
   */
  public static void chmod(String path, long mode) throws IOException {
    try {
      Files.setPosixFilePermissions(Path.of(path), PERMS_CACHE[(int) (mode & 0x1FF)]);
    } catch (NoSuchFileException e) {
      throw new FileNotFoundException(String.format("chmod(%s): %s", path, e.getMessage()));
    }
  }

  /**
   * Sets file permissions via {@link Files#setPosixFilePermissions}.
   */
  public static void chmod(Path path, long mode) throws IOException {
    try {
      Files.setPosixFilePermissions(path, PERMS_CACHE[(int) (mode & 0x1FF)]);
    } catch (NoSuchFileException e) {
      throw new FileNotFoundException(String.format("chmod(%s): %s", path, e.getMessage()));
    }
  }

  /**
   * Redirects stdout and stderr to the given file path via {@code open(2)} + {@code dup2(2)}.
   */
  public static void setStdoutStderrTo(String path) throws IOException {
    int fd = openForRedirect(path);
    IOException dupErr = dupToStdoutStderr(fd, path);
    IOException closeErr = closeFd(fd, path);
    if (dupErr != null) {
      if (closeErr != null) {
        dupErr.addSuppressed(closeErr);
      }
      throw dupErr;
    }
    if (closeErr != null) {
      throw closeErr;
    }
  }

  private static int openForRedirect(String path) throws IOException {
    try (Arena arena = Arena.ofConfined()) {
      MemorySegment pathSeg = arena.allocateUtf8String(path);
      int fd = (int) OPEN.invoke(pathSeg, O_WRONLY | O_CREAT | O_APPEND, STDOUT_STDERR_MODE);
      if (fd < 0) {
        throw new IOException("open(" + path + ") failed");
      }
      return fd;
    } catch (IOException e) {
      throw e;
    } catch (Throwable t) {
      throw new IOException("open(" + path + ") FFM call failed: " + t.getMessage(), t);
    }
  }

  private static IOException dupToStdoutStderr(int fd, String path) {
    try {
      int rc1 = (int) DUP2.invoke(fd, 1); // stdout
      int rc2 = (int) DUP2.invoke(fd, 2); // stderr
      if (rc1 < 0 || rc2 < 0) {
        return new IOException("dup2 failed for " + path);
      }
      return null;
    } catch (Throwable t) {
      return new IOException("setStdoutStderrTo FFM call failed: " + t.getMessage(), t);
    }
  }

  private static IOException closeFd(int fd, String path) {
    try {
      CLOSE.invoke(fd);
      return null;
    } catch (Throwable t) {
      return new IOException("close(" + fd + ") failed for " + path, t);
    }
  }
}
