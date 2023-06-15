// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.util.concurrent.ConcurrentMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.googlecode.concurrentlinkedhashmap.EvictionListener;

public class ConcurrentLinkedHashMapTest {
    private static Integer shouldEvictKey = null;
    private static final ConcurrentMap<Integer, Object> gal_searcher_cache = 
        new ConcurrentLinkedHashMap.Builder<Integer, Object>()
        .maximumWeightedCapacity(5)
        .listener(new EvictionListener<Integer, Object>() {
            @Override 
            public void onEviction(Integer key, Object value) {
                System.out.println("Evicted key=" + key + ", value=" + value);
                if (shouldEvictKey != null) {
                    assertEquals(key, shouldEvictKey);
                }
              }
            })
        .build();
    
    @BeforeEach
    public void setUp() throws Exception {
        shouldEvictKey = null;
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        gal_searcher_cache.clear();   
    }

 @Test
 void put() {
  assertEquals(gal_searcher_cache.size(), 0);
  gal_searcher_cache.put(1, new Integer(1));
  gal_searcher_cache.put(2, new Integer(2));
  gal_searcher_cache.put(3, new Integer(3));
  gal_searcher_cache.put(4, new Integer(4));
  gal_searcher_cache.put(5, new Integer(5));
  assertEquals(gal_searcher_cache.size(), 5);
 }

 @Test
 void replace() {
  assertEquals(gal_searcher_cache.size(), 0);
  assertNull(gal_searcher_cache.put(1, new Integer(1)));
  assertEquals(gal_searcher_cache.put(1, new Integer(2)), new Integer(1));
  assertEquals(gal_searcher_cache.put(1, new Integer(3)), new Integer(2));
  assertEquals(gal_searcher_cache.size(), 1);
 }

 @Test
 void evict() {
  assertEquals(gal_searcher_cache.size(), 0);
  gal_searcher_cache.put(1, new Integer(1));
  gal_searcher_cache.put(2, new Integer(2));
  gal_searcher_cache.put(3, new Integer(3));
  gal_searcher_cache.put(4, new Integer(4));
  gal_searcher_cache.put(5, new Integer(5));

  //at this point the oldest entry should get evicted!!
  shouldEvictKey = new Integer(1);

  gal_searcher_cache.put(6, new Integer(6));
  assertEquals(gal_searcher_cache.size(), 5);
 }

 @Test
 void remove() {
  assertEquals(gal_searcher_cache.size(), 0);
  gal_searcher_cache.put(1, new Integer(1));
  assertEquals(gal_searcher_cache.get(1), new Integer(1));
  gal_searcher_cache.remove(1);
  assertNull(gal_searcher_cache.get(1));
  assertEquals(gal_searcher_cache.size(), 0);
 }

 @Test
 void clear() {
  assertEquals(gal_searcher_cache.size(), 0);
  gal_searcher_cache.put(1, new Integer(1));
  gal_searcher_cache.put(2, new Integer(2));
  gal_searcher_cache.put(3, new Integer(3));
  gal_searcher_cache.clear();
  assertEquals(gal_searcher_cache.size(), 0);
 }
    
    
    
}
