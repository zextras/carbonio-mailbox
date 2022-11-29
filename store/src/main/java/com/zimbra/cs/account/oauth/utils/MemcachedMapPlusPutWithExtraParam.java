// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.account.oauth.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.memcached.MemcachedKey;
import com.zimbra.common.util.memcached.MemcachedSerializer;
import com.zimbra.common.util.memcached.ZimbraMemcachedClient;

/**
 * A key/value lookup map backed by memcached.  This class does not implement java.util.Map
 * because memcached doesn't allow iteration.  Use this class for values that can be serialized
 * as String, Integer or Long.  See also the BigByteArrayMemcachedMap that supports byte array
 * serialization and chunking of values larger than 1MB.
 * 
 * Example:
 * 
 *     class MyKey implements MemcachedKey {
 *         public String getKeyPrefix() { return "myApp:"; }
 *         public String getKeyValue() { ... }
 *     }
 * 
 *     class MyValue { ... }
 * 
 *     class MySerializer implements Serializer<MyValue> {
 *         public Object serialize(MyValue value) { return the serialized object, e.g. String or Integer }
 *         public MyValue deserialize(Object obj) { return new MyValue(obj); }
 *     }
 * 
 *     ZimbraMemcachedClient mcdClient = new ZimbraMemcachedClient(...);
 *     MySerializer serializer = new MySerializer();
 *     MemcachedMap<MyKey, MyValue> mcdMap = new MemcachedMap(mcdClient, serializer);
 * 
 *     MyKey foo = new MyKey("foo");
 *     MyValue bar = new MyValue("bar");
 *     mcdMap.put(foo, bar);
 *     MyValue bar2 = mcdMap.get(foo);
 *     mcdMap.remove(foo);
 * 
 *     MyKey k1 = new MyKey("k1");
 *     MyKey k2 = new MyKey("k2");
 *     List<MyKey> keys = new ArrayList<MyKey>();
 *     keys.add(k1);
 *     keys.add(k2);
 *     Map<MyKey, MyValue> values = mcdMap.getMulti(keys);
 *
 * @param <K> key implements the MemcachedKey interface
 * @param <V> value must have a Serializer<V> implementation
 */
public class MemcachedMapPlusPutWithExtraParam<K extends MemcachedKey, V> {

    private ZimbraMemcachedClient mClient = null;
    private MemcachedSerializer<V> mSerializer;
    private boolean mAckWrites;

    /**
     * Creates a map using a memcached client and serializer.
     * @param client
     * @param serializer
     * @param ackWrites if false, put and remove operations return immediately, without waiting for an ack
     *                  if true, put and remove operations block until ack or timeout
     */
    public MemcachedMapPlusPutWithExtraParam(ZimbraMemcachedClient client, MemcachedSerializer<V> serializer, boolean ackWrites) {
        mClient = client;
        mSerializer = serializer;
        mAckWrites = ackWrites;
    }

    public MemcachedMapPlusPutWithExtraParam(ZimbraMemcachedClient client, MemcachedSerializer<V> serializer) {
        this(client, serializer, true);
    }

    /**
     * Returns the value for a key.  Null is returned if key is not found in memcached.
     * @param key
     * @return
     * @throws ServiceException
     */
    public V get(K key) throws ServiceException {
        String prefix = key.getKeyPrefix();
        String kval = prefix != null ? prefix + key.getKeyValue() : key.getKeyValue();
        Object valobj = mClient.get(kval);
        V value = null;
        if (valobj != null)
            value = mSerializer.deserialize(valobj);
        return value;
    }

    /**
     * Returns values for given keys.  The returned java.util.Map is never null and has an
     * entry for every key.  Entry value will be null if key was not found in memcached.
     * This operation is batched and parallelized in the memcached client layer.  Use this
     * method rather than calling get() in a loop.
     * @param keys
     * @return
     * @throws ServiceException
     */
    public Map<K, V> getMulti(Collection<K> keys) throws ServiceException {
        Map<String, K> keyMap = new HashMap<String, K>(keys.size());
        for (K key : keys) {
            String prefix = key.getKeyPrefix();
            String kval = prefix != null ? prefix + key.getKeyValue() : key.getKeyValue();
            keyMap.put(kval, key);
        }
        Map<String, Object> valueMap = mClient.getMulti(keyMap.keySet());
        Map<K, V> result = new HashMap<K, V>(keys.size());
        // Put the values in a map keyed by the K objects.
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            K key = keyMap.get(entry.getKey());
            if (key != null) {
                Object valobj =  entry.getValue();
                V value = null;
                if (valobj != null)
                    value = mSerializer.deserialize(valobj);
                result.put(key, value);
            }
        }
        return result;
    }

    /**
     * Sets the key/value pair in memcached.
     * @param key
     * @param value
     * @throws ServiceException
     */
    public void put(K key, V value) throws ServiceException {
        String prefix = key.getKeyPrefix();
        String kval = prefix != null ? prefix + key.getKeyValue() : key.getKeyValue();
        Object valobj = mSerializer.serialize(value);
        mClient.put(kval, valobj, mAckWrites);
    }

    /**
     * Sets the key/value pair in memcached.
     * @param key
     * @param value
     * @param expirySec expiry in seconds
     * @param timeout in millis
     * @throws ServiceException
     */
    public void put(K key, V value, int expirySec, long timeout) throws ServiceException {
        String prefix = key.getKeyPrefix();
        String kval = prefix != null ? prefix + key.getKeyValue() : key.getKeyValue();
        Object valobj = mSerializer.serialize(value);
        mClient.put(kval, valobj, expirySec, timeout, mAckWrites);
    }

    /**
     * Sets multiple key/value pairs in memcached.  This operation is done serially in a loop.
     * @param map
     * @throws ServiceException
     */
    public void putMulti(Map<K, V> map) throws ServiceException {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Remove the key from memcached.
     * @param key
     * @throws ServiceException
     */
    public void remove(K key) throws ServiceException {
        String prefix = key.getKeyPrefix();
        String kval = prefix != null ? prefix + key.getKeyValue() : key.getKeyValue();
        mClient.remove(kval, mAckWrites);
    }

    /**
     * Remove multiple keys from memcached.  This operation is done serially in a loop.
     * @param keys
     * @throws ServiceException
     */
    public void removeMulti(Collection<K> keys) throws ServiceException {
        for (K key : keys) {
            String prefix = key.getKeyPrefix();
            String kval = prefix != null ? prefix + key.getKeyValue() : key.getKeyValue();
            mClient.remove(kval, mAckWrites);
        }
    }
}
