// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

public class UnmodifiableBloomFilter<E> {

    private static final Double DEFAULT_TOLERANCE = 0.03;
    private static final Integer DEFAULT_BUFFER_SIZE = 4096;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private BloomFilter<E> cache;
    private boolean isInitialized = false;
    private double tolerance;
    private final String fileLocation;

    private UnmodifiableBloomFilter(String fileLocation, BloomFilter<E> cache, double tolerance) {
        this.fileLocation = fileLocation;
        this.cache = cache;
        this.tolerance = tolerance;
        this.isInitialized = StringUtil.isNullOrEmpty(fileLocation) ? true : false;
    }

    private UnmodifiableBloomFilter(BloomFilter<E> cache) {
        this.fileLocation = null;
        this.cache = cache;
        this.isInitialized = true;
    }

    public boolean mightContain(E entry) {
        ensureInitialized();
        return cache != null && cache.mightContain(entry);
    }

    /**
     * @return True if an attempt to load/set the inner cache was made
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * @return True if the instance is initialized but still null.
     */
    public boolean isDisabled() {
        return isInitialized && cache == null;
    }

    @SuppressWarnings("unchecked")
    private void ensureInitialized() {
        if (isInitialized) {
            return;
        }
        synchronized (fileLocation) {
            if (isInitialized) {
                return;
            }
            this.cache = (BloomFilter<E>) loadFilter(new File(fileLocation), tolerance);
            isInitialized = true;
        }
    }

    /**
     * Wraps a bloom filter so it may not be modified.
     * @param filter The filter to wrap
     * @return Unmodifiable bloom filter
     */
    public static <E> UnmodifiableBloomFilter<E> createFilter(BloomFilter<E> filter) {
        return new UnmodifiableBloomFilter<>(filter);
    }

    /**
     * @see UnmodifiableBloomFilter#createFilterFromFile(String, double)
     */
    public static UnmodifiableBloomFilter<String> createFilterFromFile(String fileLocation) {
        return createFilterFromFile(fileLocation, DEFAULT_TOLERANCE);
    }

    /**
     * Creates an instance that will immediately initialize from file.
     * @param fileLocation The file to load from
     * @param tolerance The expected false positive tolerance (0.xx-1)
     * @return A string instance that cannot be modified externally
     */
    public static UnmodifiableBloomFilter<String> createFilterFromFile(String fileLocation, double tolerance) {
        UnmodifiableBloomFilter<String> bloom = createLazyFilterFromFile(fileLocation, tolerance);
        bloom.ensureInitialized();
        return bloom;
    }

    /**
     * @see UnmodifiableBloomFilter#createLazyFilterFromFile(String, double)
     */
    public static UnmodifiableBloomFilter<String> createLazyFilterFromFile(String fileLocation) {
        return createLazyFilterFromFile(fileLocation, DEFAULT_TOLERANCE);
    }

    /**
     * Creates a bloom filter instance that will initialize from file on first use.
     * @param fileLocation The file to load from
     * @param tolerance The expected false positive tolerance (0.xx-1)
     * @return A lazy loading string instance that cannot be modified externally
     */
    public static UnmodifiableBloomFilter<String> createLazyFilterFromFile(String fileLocation, double tolerance) {
        // password filter file is unset, return disabled cache instance without warn
        if (StringUtil.isNullOrEmpty(fileLocation)) {
            return new UnmodifiableBloomFilter<>(null, null, tolerance);
        }
        return new UnmodifiableBloomFilter<>(fileLocation, null, tolerance);
    }

    private static BloomFilter<String> loadFilter(File file, Double tolerance) {
        try (
            BufferedReader reader = new BufferedReader(new FileReader(file), DEFAULT_BUFFER_SIZE)) {
            // determine entry count for accurate filter creation
            long entryCount = countEntries(new FileReader(file));
            ZimbraLog.cache.debug("Creating bloom filter for file with %d entries", entryCount);
            BloomFilter<String> pendingCache = BloomFilter
                .create(Funnels.stringFunnel(DEFAULT_CHARSET), entryCount, tolerance);
            // fill the filter as we read
            String st;
            while ((st = reader.readLine()) != null) {
                pendingCache.put(st);
            }
            return pendingCache;
        } catch (IOException e) {
            ZimbraLog.cache.warnQuietly("Unable to load bloom filter from file.", e);
            // return an instance with disabled cache since we failed during read/load
            return null;
        }
    }

    private static long countEntries(FileReader fileReader) throws IOException {
        try (LineNumberReader reader = new LineNumberReader(fileReader, DEFAULT_BUFFER_SIZE)) {
            reader.skip(Long.MAX_VALUE);
            return reader.getLineNumber() + 1L;
        }
    }
}
