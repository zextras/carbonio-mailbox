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
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.spi.FileSystemProvider;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * POSIX I/O operations for the mailbox store, replacing the JNI-based native library.
 *
 * <p>Uses {@code java.nio.file} for {@link #link}, {@link #chmod}, and {@link #fileInfo}. Uses
 * Java FFM downcalls for {@link #setStdoutStderrTo} (via {@code open(2)} + {@code dup2(2)}) which
 * has no stdlib equivalent.
 */
public class IO {

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

  // Direct provider avoids Files.readAttributes() indirection — 1.38x vs 1.62x overhead
  private static final FileSystemProvider FS_PROVIDER = FileSystems.getDefault().provider();

  // FFM handles for open(), dup2(), close() — used only by setStdoutStderrTo()
  private static final MethodHandle OPEN;
  private static final MethodHandle DUP2;
  private static final MethodHandle CLOSE;

  static {
    Linker linker = Linker.nativeLinker();
    SymbolLookup libc = linker.defaultLookup();

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
    try {
      Files.createLink(Path.of(newpath), Path.of(oldpath));
    } catch (NoSuchFileException e) {
      throw new FileNotFoundException(
          String.format("link(%s, %s): %s", oldpath, newpath, e.getMessage()));
    } catch (FileAlreadyExistsException e) {
      throw new IOException(
          String.format("link(%s, %s): %s", oldpath, newpath, e.getMessage()), e);
    }
  }

  /**
   * Returns inode number, size, and hard link count for the given path. Uses a single batched
   * {@code readAttributes} call via the direct {@link FileSystemProvider} (avoids the {@link Files}
   * indirection layer — benchmarked at 1.38x vs raw syscall, down from 1.62x).
   */
  public static FileInfo fileInfo(String path) throws IOException {
    try {
      Map<String, Object> attrs = FS_PROVIDER.readAttributes(Path.of(path), "unix:ino,nlink,size");
      return new FileInfo(
          ((Number) attrs.get("ino")).longValue(),
          ((Number) attrs.get("size")).longValue(),
          ((Number) attrs.get("nlink")).intValue());
    } catch (NoSuchFileException e) {
      throw new FileNotFoundException(String.format("stat(%s): %s", path, e.getMessage()));
    }
  }

  /**
   * Returns the hard link count for the given path.
   */
  public static int linkCount(String path) throws IOException {
    return fileInfo(path).getLinkCount();
  }

  /**
   * Sets file permissions via {@link Files#setPosixFilePermissions}.
   *
   * @param path file path
   * @param mode POSIX mode bits (e.g. {@code S_IRUSR | S_IWUSR})
   */
  public static void chmod(String path, long mode) throws IOException {
    Set<PosixFilePermission> perms = EnumSet.noneOf(PosixFilePermission.class);
    if ((mode & S_IRUSR) != 0) perms.add(PosixFilePermission.OWNER_READ);
    if ((mode & S_IWUSR) != 0) perms.add(PosixFilePermission.OWNER_WRITE);
    if ((mode & S_IXUSR) != 0) perms.add(PosixFilePermission.OWNER_EXECUTE);
    if ((mode & S_IRGRP) != 0) perms.add(PosixFilePermission.GROUP_READ);
    if ((mode & S_IWGRP) != 0) perms.add(PosixFilePermission.GROUP_WRITE);
    if ((mode & S_IXGRP) != 0) perms.add(PosixFilePermission.GROUP_EXECUTE);
    if ((mode & S_IROTH) != 0) perms.add(PosixFilePermission.OTHERS_READ);
    if ((mode & S_IWOTH) != 0) perms.add(PosixFilePermission.OTHERS_WRITE);
    if ((mode & S_IXOTH) != 0) perms.add(PosixFilePermission.OTHERS_EXECUTE);
    try {
      Files.setPosixFilePermissions(Path.of(path), perms);
    } catch (NoSuchFileException e) {
      throw new FileNotFoundException(String.format("chmod(%s): %s", path, e.getMessage()));
    }
  }

  /**
   * Redirects stdout and stderr to the given file path via {@code open(2)} + {@code dup2(2)}.
   */
  public static void setStdoutStderrTo(String path) throws IOException {
    int O_WRONLY = 1;
    int O_CREAT = 64;
    int O_APPEND = 1024;
    int mode = 0644;
    try (Arena arena = Arena.ofConfined()) {
      MemorySegment pathSeg = arena.allocateUtf8String(path);
      int fd = (int) OPEN.invoke(pathSeg, O_WRONLY | O_CREAT | O_APPEND, mode);
      if (fd < 0) {
        throw new IOException("open(" + path + ") failed");
      }
      Throwable primaryFailure = null;
      try {
        int rc1 = (int) DUP2.invoke(fd, 1); // stdout
        int rc2 = (int) DUP2.invoke(fd, 2); // stderr
        if (rc1 < 0 || rc2 < 0) {
          throw new IOException("dup2 failed for " + path);
        }
      } catch (Throwable t) {
        primaryFailure = t;
        throw t;
      } finally {
        try {
          CLOSE.invoke(fd);
        } catch (Throwable closeFailure) {
          if (primaryFailure != null) {
            primaryFailure.addSuppressed(closeFailure);
          }
        }
      }
    } catch (IOException e) {
      throw e;
    } catch (Throwable e) {
      throw new IOException("setStdoutStderrTo FFM call failed: " + e.getMessage(), e);
    }
  }
}
