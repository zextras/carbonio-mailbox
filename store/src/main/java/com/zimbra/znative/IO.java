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
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.VarHandle;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.Set;

/**
 * POSIX I/O operations for the mailbox store, replacing the JNI-based native library.
 *
 * <p>Uses {@code java.nio.file} where possible ({@link #link}, {@link #chmod}) and Java FFM
 * downcalls for operations without stdlib equivalents ({@link #fileInfo} via {@code stat(2)},
 * {@link #setStdoutStderrTo} via {@code dup2(2)}).
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

  // FFM handles for stat() and dup2()
  private static final MethodHandle STAT;
  private static final MethodHandle OPEN;
  private static final MethodHandle DUP2;
  private static final MethodHandle CLOSE;

  // struct stat layout for x86_64 Linux (glibc)
  // Offsets: st_dev=0, st_ino=8, st_nlink=16, st_mode=24, ... st_size=48
  private static final long STAT_ST_INO_OFFSET = 8;
  private static final long STAT_ST_NLINK_OFFSET = 16;
  private static final long STAT_ST_SIZE_OFFSET = 48;
  private static final long STAT_STRUCT_SIZE = 144; // sizeof(struct stat) on x86_64 glibc

  static {
    Linker linker = Linker.nativeLinker();
    SymbolLookup libc = Linker.nativeLinker().defaultLookup();

    STAT = linker.downcallHandle(
        libc.find("stat").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

    OPEN = linker.downcallHandle(
        libc.find("open").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT),
        Linker.Option.firstVariadicArg(2));

    DUP2 = linker.downcallHandle(
        libc.find("dup2").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));

    CLOSE = linker.downcallHandle(
        libc.find("close").orElseThrow(),
        FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
  }

  /**
   * Creates a hard link from {@code oldpath} to {@code newpath}. Uses {@link Files#createLink}.
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
   * Returns inode number, size, and hard link count for the given path via {@code stat(2)}.
   */
  public static FileInfo fileInfo(String path) throws IOException {
    try (Arena arena = Arena.ofConfined()) {
      MemorySegment pathSeg = arena.allocateUtf8String(path);
      MemorySegment statBuf = arena.allocate(STAT_STRUCT_SIZE);
      int rc = (int) STAT.invoke(pathSeg, statBuf);
      if (rc != 0) {
        throw new IOException(String.format("stat(%s) failed", path));
      }
      long ino = statBuf.get(ValueLayout.JAVA_LONG, STAT_ST_INO_OFFSET);
      long nlink = statBuf.get(ValueLayout.JAVA_LONG, STAT_ST_NLINK_OFFSET);
      long size = statBuf.get(ValueLayout.JAVA_LONG, STAT_ST_SIZE_OFFSET);
      return new FileInfo(ino, size, (int) nlink);
    } catch (IOException e) {
      throw e;
    } catch (Throwable e) {
      throw new IOException("stat() FFM call failed: " + e.getMessage(), e);
    }
  }

  /**
   * Returns the hard link count for the given path.
   */
  public static int linkCount(String path) throws IOException {
    FileInfo info = fileInfo(path);
    return info != null ? info.getLinkCount() : -1;
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
    try (Arena arena = Arena.ofConfined()) {
      MemorySegment pathSeg = arena.allocateUtf8String(path);
      int fd = (int) OPEN.invoke(pathSeg, O_WRONLY | O_CREAT | O_APPEND);
      if (fd < 0) {
        throw new IOException("open(" + path + ") failed");
      }
      try {
        int rc1 = (int) DUP2.invoke(fd, 1); // stdout
        int rc2 = (int) DUP2.invoke(fd, 2); // stderr
        if (rc1 < 0 || rc2 < 0) {
          throw new IOException("dup2 failed for " + path);
        }
      } finally {
        CLOSE.invoke(fd);
      }
    } catch (IOException e) {
      throw e;
    } catch (Throwable e) {
      throw new IOException("setStdoutStderrTo FFM call failed: " + e.getMessage(), e);
    }
  }
}
