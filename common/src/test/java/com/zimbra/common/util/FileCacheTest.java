// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

public class FileCacheTest {

    File tmpDir;

    @BeforeEach
    public void setUp() {
        tmpDir = Files.createTempDir();
    }

  /**
   * Tests getting an item from the cache.
   */
  @Test
  void get() throws IOException {
    FileCache<Integer> cache = FileCache.Builder.createWithIntegerKey(tmpDir, false).build();
    byte[] data = getRandomBytes(100);
    cache.put(1, new ByteArrayInputStream(data));
    FileCache.Item item = cache.get(1);
    assertArrayEquals(data, ByteUtil.getContent(item.file));

    File dataDir = new File(tmpDir, "data");
    assertEquals(dataDir, item.file.getParentFile());
  }

  /**
   * Tests getting an item that doesn't exist.
   */
  @Test
  void keyDoesNotExist() throws IOException {
    FileCache<Integer> cache = FileCache.Builder.createWithIntegerKey(tmpDir, false).build();
    assertNull(cache.get(1));
  }

  /**
   * Tests {@link FileCache#contains}.
   */
  @Test
  void contains() throws IOException {
    FileCache<Integer> cache = FileCache.Builder.createWithIntegerKey(tmpDir, false).build();
    byte[] data = getRandomBytes(100);
    cache.put(1, new ByteArrayInputStream(data));
    assertTrue(cache.contains(1));
    assertFalse(cache.contains(2));
  }

  /**
   * Verifies that files with the same digest are deduped.
   */
  @Test
  void dedupe() throws IOException {
    FileCache<Integer> cache = FileCache.Builder.createWithIntegerKey(tmpDir, false).build();
    byte[] data = getRandomBytes(100);
    cache.put(1, new ByteArrayInputStream(data));
    cache.put(2, new ByteArrayInputStream(data));
    assertEquals(1, cache.getNumFiles());
    assertEquals(2, cache.getNumKeys());
    assertEquals(100, cache.getNumBytes());
  }

  /**
   * Tests removing keys and files from the cache.
   */
  @Test
  void remove() throws IOException {
    FileCache<Integer> cache = FileCache.Builder.createWithIntegerKey(tmpDir, false).build();
    byte[] data = getRandomBytes(100);
    cache.put(1, new ByteArrayInputStream(data));
    cache.put(2, new ByteArrayInputStream(data));
    data = getRandomBytes(100);
    cache.put(3, new ByteArrayInputStream(data));

    FileCache.Item item1 = cache.get(1);
    FileCache.Item item2 = cache.get(2);
    FileCache.Item item3 = cache.get(3);
    assertEquals(item1.digest, item2.digest);
    assertNotEquals(item1.digest, item3.digest);

    assertFalse(cache.remove(1));
    assertFalse(cache.contains(1));
    assertEquals(2, cache.getNumFiles());
    assertEquals(200, cache.getNumBytes());

    assertTrue(item1.file.exists());
    assertTrue(item2.file.exists());
    assertTrue(item3.file.exists());

    assertTrue(cache.remove(2));
    assertFalse(cache.contains(2));
    assertEquals(1, cache.getNumFiles());
    assertEquals(100, cache.getNumBytes());

    assertFalse(item1.file.exists());
    assertFalse(item2.file.exists());
    assertTrue(item3.file.exists());
  }

  /**
   * Tests pruning the cache when the maximum number of files has been exceeded.
   */
  @Test
  void maxFiles() throws IOException {
    FileCache<Integer> cache = FileCache.Builder.createWithIntegerKey(tmpDir, false).maxFiles(2).build();
    byte[] data = getRandomBytes(100);
    cache.put(1, new ByteArrayInputStream(data));
    cache.put(2, new ByteArrayInputStream(data));
    data = getRandomBytes(100);
    cache.put(3, new ByteArrayInputStream(data));
    data = getRandomBytes(100);
    cache.put(4, new ByteArrayInputStream(data));

    assertEquals(2, cache.getNumFiles());
    assertEquals(200, cache.getNumBytes());
    assertFalse(cache.contains(1));
    assertFalse(cache.contains(2));
    assertTrue(cache.contains(3));
    assertTrue(cache.contains(4));
  }

  /**
   * Verifies that the least recently accessed item is pruned.
   */
  @Test
  void accessOrder() throws IOException {
    FileCache<Integer> cache = FileCache.Builder.createWithIntegerKey(tmpDir, false).maxFiles(2).build();
    byte[] data = getRandomBytes(100);
    cache.put(1, new ByteArrayInputStream(data));
    data = getRandomBytes(100);
    cache.put(2, new ByteArrayInputStream(data));
    data = getRandomBytes(100);
    cache.get(1);
    cache.put(3, new ByteArrayInputStream(data));
    data = getRandomBytes(100);

    assertTrue(cache.contains(1));
    assertFalse(cache.contains(2));
    assertTrue(cache.contains(3));
  }

  @Test
  void maxBytes() throws IOException {
    FileCache<Integer> cache = FileCache.Builder.createWithIntegerKey(tmpDir, false).maxBytes(299).build();
    byte[] data = getRandomBytes(100);
    cache.put(1, new ByteArrayInputStream(data));
    data = getRandomBytes(100);
    cache.put(2, new ByteArrayInputStream(data));
    data = getRandomBytes(100);
    cache.put(3, new ByteArrayInputStream(data));

    assertEquals(2, cache.getNumFiles());
    assertEquals(200, cache.getNumBytes());
    assertFalse(cache.contains(1));
    assertTrue(cache.contains(2));
    assertTrue(cache.contains(3));
  }

    /**
     * Never prunes files that are smaller or equal to the given size.
     */
    private class KeepSmallFiles implements FileCache.RemoveCallback {
        private final long size;

        KeepSmallFiles(long size) {
            this.size = size;
        }

        @Override
        public boolean okToRemove(FileCache.Item item) {
            return item.file.length() > size;
        }
    }

  /**
   * Tests overriding the behavior of {@link FileCache#okToRemove}.
   */
  @Test
  void okToRemove() throws IOException {
    FileCache<Integer> cache = FileCache.Builder.createWithIntegerKey(tmpDir, false)
        .maxFiles(2)
        .removeCallback(new KeepSmallFiles(99)).build();
    byte[] data = getRandomBytes(99);
    cache.put(1, new ByteArrayInputStream(data));
    data = getRandomBytes(100);
    cache.put(2, new ByteArrayInputStream(data));
    data = getRandomBytes(100);
    cache.put(3, new ByteArrayInputStream(data));
    data = getRandomBytes(100);

    assertTrue(cache.contains(1));
    assertFalse(cache.contains(2));
    assertTrue(cache.contains(3));
  }

  /**
   * Verifies that files are not purged if their lifetime is lower than the minimum.
   */
  @Test
  void minLifetime() throws IOException, InterruptedException {
    FileCache<Integer> cache = FileCache.Builder.createWithIntegerKey(tmpDir, false).maxFiles(2).minLifetime(200).build();
    byte[] data = getRandomBytes(100);
    cache.put(1, new ByteArrayInputStream(data));
    data = getRandomBytes(100);
    cache.put(2, new ByteArrayInputStream(data));
    data = getRandomBytes(100);
    cache.get(1);
    cache.get(2);
    cache.put(3, new ByteArrayInputStream(data));

    assertTrue(cache.contains(1));
    assertTrue(cache.contains(2));
    assertTrue(cache.contains(3));

    Thread.sleep(250);

    data = getRandomBytes(100);
    cache.put(4, new ByteArrayInputStream(data));
    assertFalse(cache.contains(1));
    assertFalse(cache.contains(2));
    assertTrue(cache.contains(3));
    assertTrue(cache.contains(4));
  }

    /**
     * Verifies that cached files are cleaned up after the item is aged out.
     */
    private void cleanUpFiles(boolean persistent) throws IOException {
        FileCache<Integer> cache = FileCache.Builder.createWithIntegerKey(tmpDir, persistent).maxFiles(1).build();
        File dataDir = new File(tmpDir, "data");
        File propDir = new File(tmpDir, "properties");

        byte[] data = getRandomBytes(100);
        cache.put(1, new ByteArrayInputStream(data));
        FileCache.Item item1 = cache.get(1);
        File dataFile = new File(dataDir, item1.digest);
        File propFile = new File(propDir, item1.digest + ".properties");
        assertTrue(dataFile.isFile());
        assertEquals(persistent, propFile.isFile());

        data = getRandomBytes(100);
        cache.put(2, new ByteArrayInputStream(data));
        assertFalse(dataFile.exists());
        assertFalse(propFile.exists());
    }

  @Test
  void cleanUpFiles() throws IOException {
    cleanUpFiles(true); //make sure expiration works in both cases
    cleanUpFiles(false); //also confirm by proxy that non-persistent cache is not writing propFiles
  }

    private void storePersistentCache() throws IOException {
        FileCache<Integer> cache = FileCache.Builder.createWithIntegerKey(tmpDir, true).build();
        byte[] data = getRandomBytes(100);

        // Store 2 entries for the same content.
        Map<String, String> props = ImmutableMap.of("Bill", "Evans", "Wynton", "Kelly");
        cache.put(1, new ByteArrayInputStream(data), props);
        cache.put(2, new ByteArrayInputStream(data), props);

        FileCache.Item item = cache.get(1);
        assertEquals(2, item.properties.size());
        assertEquals("Evans", item.properties.get("Bill"));
        assertEquals("Kelly", item.properties.get("Wynton"));

        assertEquals(item, cache.get(2));
    }

  /**
   * Verifies reloading cache content.
   */
  @Test
  void reload() throws IOException {
    storePersistentCache();

    // Reload cache and compare content.
    FileCache<Integer> cache = FileCache.Builder.createWithIntegerKey(tmpDir, true).build();
    assertEquals(1, cache.getNumFiles());
    assertEquals(100, cache.getNumBytes());

    FileCache.Item item = cache.get(1);
    assertEquals(2, item.properties.size());
    assertEquals("Evans", item.properties.get("Bill"));
    assertEquals("Kelly", item.properties.get("Wynton"));

    assertEquals(item, cache.get(2));
  }

  @Test
  void reloadEmpty() throws IOException {
    storePersistentCache();

    // build cache with persistent - false. it must contain no entries
    FileCache<Integer> cache = FileCache.Builder.createWithIntegerKey(tmpDir, false).build();

    assertEquals(0, cache.getNumFiles());
    assertEquals(0, cache.getNumKeys());
    File dataDir = new File(tmpDir, "data");
    File propDir = new File(tmpDir, "properties");

    assertEquals(0, dataDir.list().length);
    assertEquals(0, propDir.list().length);
  }

  @Test
  void testKeyWithComma() throws IOException {
    FileCache<String> cache = FileCache.Builder.createWithStringKey(tmpDir, true).build();
    byte[] data = getRandomBytes(100);
    String key1 = "a,b";
    String key2 = "c,d";
    cache.put(key1, new ByteArrayInputStream(data));
    cache.put(key2, new ByteArrayInputStream(data));

    assertTrue(cache.contains(key1));
    assertTrue(cache.contains(key2));
    FileCache.Item item = cache.get(key1);
    assertEquals(item, cache.get(key2));
    assertArrayEquals(data, ByteUtil.getContent(item.file));
  }

  /**
   * Confirms that the cache properly replaces a value for an existing key (HS-7363).
   */
  @Test
  void replaceValueForKey() throws IOException {
    FileCache<Integer> cache = FileCache.Builder.createWithIntegerKey(tmpDir, false).build();

    // Put data into the cache 10 times and make sure that the number of files doesn't grow.
    byte[] data = null;
    for (int i = 1;i <= 10;i++) {
      data = getRandomBytes(100);
      cache.put(1, new ByteArrayInputStream(data));
      assertEquals(1, FileUtil.listFilesRecursively(tmpDir).size());
    }

    // Verify that we have the latest version of the data;
    FileCache.Item item = cache.get(1);
    assertArrayEquals(data, ByteUtil.getContent(item.file));
  }

  /**
   * Confirms that both the data file and the property file are deleted when an item
   * is removed.
   */
  @Test
  void removeProperties() throws IOException {
    FileCache<Integer> cache = FileCache.Builder.createWithIntegerKey(tmpDir, true).build();
    byte[] data = getRandomBytes(100);
    assertEquals(0, FileUtil.listFilesRecursively(tmpDir).size());
    cache.put(1, new ByteArrayInputStream(data));
    assertEquals(2, FileUtil.listFilesRecursively(tmpDir).size());
    cache.remove(1);
    assertEquals(0, FileUtil.listFilesRecursively(tmpDir).size());
  }

    private static byte[] getRandomBytes(int size) {
        byte[] data = new byte[size];
        Random r = new Random();
        r.nextBytes(data);
        return data;
    }

    @AfterEach
    public void tearDown() throws IOException {
        FileUtil.deleteDir(tmpDir);
    }
}
