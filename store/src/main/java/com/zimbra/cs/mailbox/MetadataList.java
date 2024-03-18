// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mailbox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.BEncoding;
import com.zimbra.common.util.BEncoding.BEncodingException;

/**
 * @since Jul 8, 2005
 * @author dkarp
 */
public class MetadataList {
    List<Object> list;

    public MetadataList() {
        list = new ArrayList<>();
    }

    public MetadataList(List<?> list) {
        this.list = new ArrayList<>(list);
    }

    public MetadataList(String encoded) throws ServiceException {
        try {
            list = BEncoding.decode(encoded);
        } catch (BEncodingException e) {
            throw ServiceException.FAILURE("error decoding list metadata: " + encoded, e);
        }
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int getVersion() {
        return Metadata.LEGACY_METADATA_VERSION;
    }

    public MetadataList copy(MetadataList source) {
        list.addAll(source.list);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> asList() {
        List<T> result = new ArrayList<>();
        for (Object obj : list) {
            if (obj == null) {
            } else if (obj instanceof Map) {
                result.add((T) new Metadata((Map<String, ?>) obj));
            } else if (obj instanceof List) {
                result.add((T) new MetadataList((List<?>) obj));
            } else {
                result.add((T) obj);
            }
        }
        return result;
    }

    public MetadataList add(Object value) {
        if (value != null) {
            list.add(value);
        }
        return this;
    }

    public MetadataList add(int value) {
        list.add(value);
        return this;
    }

    public MetadataList add(long value) {
        list.add(value);
        return this;
    }

    public MetadataList add(double value) {
        list.add(value);
        return this;
    }

    public MetadataList add(boolean value) {
        list.add(value);
        return this;
    }

    public MetadataList add(Metadata value)  {
        if (value != null) {
            list.add(value.map);
        }
        return this;
    }

    public MetadataList add(MetadataList value) {
        if (value != null) {
            list.add(value.list);
        }
        return this;
    }

    public void remove(int index) {
        if (index < list.size()) {
            list.remove(index);
        }
    }
    public void remove(Object value) {
        list.remove(value);
    }

    public String get(int index) throws ServiceException {
        Object obj = list.get(index);
        return checkNull(index, obj).toString();
    }

    public int getInt(int index) throws ServiceException {
        return parseInt(index, get(index));
    }

    public long getLong(int index) throws ServiceException {
        return parseLong(index, get(index));
    }

    public double getDouble(int index) throws ServiceException {
        return parseDouble(index, get(index));
    }

    public boolean getBool(int index) throws ServiceException {
        return parseBool(index, get(index));
    }

    public MetadataList getList(int index) throws ServiceException {
        Object value = list.get(index);
        if (value instanceof List)  {
            return new MetadataList((List<?>) value);
        }
        throw ServiceException.INVALID_REQUEST("invalid/null value for index: " + index, null);
    }

    public Metadata getMap(int index) throws ServiceException {
        Object value = list.get(index);
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, ?> cast = (Map<String, ?>) value;
            return new Metadata(cast);
        }
        throw ServiceException.INVALID_REQUEST("invalid/null value for attribute: " + index, null);
    }

    private static Object checkNull(int index, Object value) throws ServiceException {
        if (value == null)
            throw ServiceException.INVALID_REQUEST("null element in list: " + index, null);
        return value;
    }

    public static int parseInt(int index, String value)
            throws ServiceException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw ServiceException.INVALID_REQUEST("invalid value for index: "
                    + index, e);
        }
    }

    public static long parseLong(int index, String value) throws ServiceException {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw ServiceException.INVALID_REQUEST("invalid value for index: " + index, e);
        }
    }

    public static double parseDouble(int index, String value) throws ServiceException {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw ServiceException.INVALID_REQUEST("invalid value for index: " + index, e);
        }
    }

    public static boolean parseBool(int index, String value) throws ServiceException {
        if (value.equals("1") || value.equalsIgnoreCase("true")) {
            return true;
        } else if (value.equals("0") || value.equalsIgnoreCase("false")) {
            return false;
        }
        throw ServiceException.INVALID_REQUEST("invalid boolean value for index: " + index, null);
    }

    @Override
    public String toString() {
        return BEncoding.encode(list);
    }
}
