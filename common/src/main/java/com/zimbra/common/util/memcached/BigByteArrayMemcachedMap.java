// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util.memcached;

import com.zimbra.common.service.ServiceException;
import java.util.Collection;

/**
 * A key/value lookup map backed by memcached, with support for values larger than 1MB. This class
 * does not implement java.util.Map because memcached doesn't allow iteration. To use this map, the
 * value object must support serialization to byte array. (rather than String)
 *
 * <p>Example:
 *
 * <p>class MyKey implements MemcachedKey { public String getKeyPrefix() { return "myApp:"; } public
 * String getKeyValue() { ... } }
 *
 * <p>class MyValue { ... }
 *
 * <p>class MySerializer implements ByteArraySerializer<MyValue> { public byte[] serialize(MyValue
 * value) { serialize to byte array } public MyValue deserialize(byte[] data) { deserialize from
 * byte array } }
 *
 * <p>ZimbraMemcachedClient mcdClient = new ZimbraMemcachedClient(...); MySerializer serializer =
 * new MySerializer(); BigByteArrayMemcachedMap<MyKey, MyValue> mcdMap = new
 * BigByteArrayMemcachedMap(mcdClient, serializer);
 *
 * <p>MyKey foo = new MyKey("foo"); MyValue bar = new MyValue("bar"); mcdMap.put(foo, bar); MyValue
 * bar2 = mcdMap.get(foo); mcdMap.remove(foo);
 *
 * @param <K> key implements the MemcachedKey interface
 * @param <V> value must have a ByteArraySerializer<V> implementation
 */
public class BigByteArrayMemcachedMap<K extends MemcachedKey, V> {

  private ZimbraMemcachedClient mClient;
  private ByteArraySerializer<V> mSerializer;
  private boolean mAckWrites;

  /**
   * Creates a map using a memcached client and serializer.
   *
   * @param client
   * @param serializer
   * @param ackWrites if false, put and remove operations return immediately, without waiting for an
   *     ack if true, put and remove operations block until ack or timeout
   */
  public BigByteArrayMemcachedMap(
      ZimbraMemcachedClient client, ByteArraySerializer<V> serializer, boolean ackWrites) {
    mClient = client;
    mSerializer = serializer;
    mAckWrites = ackWrites;
  }

  public BigByteArrayMemcachedMap(ZimbraMemcachedClient client, ByteArraySerializer<V> serializer) {
    this(client, serializer, true);
  }

  /**
   * Returns the value for a key. Null is returned if key is not found in memcached.
   *
   * @param key
   * @return
   * @throws ServiceException
   */
  public V get(K key) throws ServiceException {
    String prefix = key.getKeyPrefix();
    String kval = prefix != null ? prefix + key.getKeyValue() : key.getKeyValue();
    byte[] data = mClient.getBigByteArray(kval);
    V value = null;
    if (data != null) value = mSerializer.deserialize(data);
    return value;
  }

  /**
   * Sets the key/value pair in memcached.
   *
   * @param key
   * @param value
   * @throws ServiceException
   */
  public void put(K key, V value) throws ServiceException {
    String prefix = key.getKeyPrefix();
    String kval = prefix != null ? prefix + key.getKeyValue() : key.getKeyValue();
    byte[] data = mSerializer.serialize(value);
    mClient.putBigByteArray(kval, data, mAckWrites);
  }

  /**
   * Remove the key from memcached.
   *
   * @param key
   * @throws ServiceException
   */
  public void remove(K key) throws ServiceException {
    String prefix = key.getKeyPrefix();
    String kval = prefix != null ? prefix + key.getKeyValue() : key.getKeyValue();
    mClient.remove(kval, mAckWrites);
  }

  /**
   * Remove multiple keys from memcached. This operation is done serially in a loop.
   *
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
