// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.filter;

import java.util.Arrays;

import com.zimbra.common.service.ServiceException;
import com.zimbra.common.util.DateParser;

/**
 * Container class for Sieve constants, enums, and operations.
 */
public final class Sieve {

    public static final DateParser DATE_PARSER = new DateParser("yyyyMMdd");

    public enum Condition {
        allof, anyof;

        public static Condition fromString(String value)
        throws ServiceException {
            if (value == null) {
                return null;
            }
            try {
                return Condition.valueOf(value);
            } catch (IllegalArgumentException e) {
                throw ServiceException.PARSE_ERROR(
                    "Invalid value: " + value + ", valid values: " + Arrays.asList(Condition.values()), e);
            }
        }
    }

    public enum Flag {
        read, flagged, priority;

        public static Flag fromString(String value) throws ServiceException {
            if (value == null) {
                return null;
            }
            try {
                return Flag.valueOf(value);
            } catch (IllegalArgumentException e) {
                throw ServiceException.PARSE_ERROR(
                        "Invalid value: " + value + ", valid values: " + Arrays.asList(Flag.values()), e);
            }
        }
    }

    public enum StringComparison {
        is, contains, matches;

        public static StringComparison fromString(String value) throws ServiceException {
            if (value == null) {
                return null;
            }
            try {
                return StringComparison.valueOf(value.toLowerCase());
            } catch (IllegalArgumentException e) {
                throw ServiceException.PARSE_ERROR(
                    "Invalid value: "+ value +", valid values: " + Arrays.asList(StringComparison.values()), e);
            }
        }
    }

    public enum AddressPart {
        all, localpart, domain;

        public static AddressPart fromString(String value)
        throws ServiceException {
            if (value == null) {
                return null;
            }
            try {
                return AddressPart.valueOf(value);
            } catch (IllegalArgumentException e) {
                throw ServiceException.PARSE_ERROR(
                    "Invalid value: "+ value +", valid values: " + Arrays.asList(AddressPart.values()), e);
            }
        }
    }

    public enum Comparator {
        ioctet("i;octet"),
        iasciicasemap("i;ascii-casemap"),
        iasciinumeric("i;ascii-numeric");

        private String value;

        Comparator(String value) {
            this.value = value;
        }

        public static Comparator fromString(String value)
        throws ServiceException {
            if (value == null)
                return null;
            for (Comparator comparator : Comparator.values()) {
                if (comparator.toString().equals(value))
                    return comparator;
            }
            throw ServiceException.PARSE_ERROR("Invalid Comparator value: " + value, null);
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public enum NumberComparison {
        over, under;

        public static NumberComparison fromString(String value) throws ServiceException {
            if (value == null) {
                return null;
            }
            try {
                return NumberComparison.valueOf(value.toLowerCase());
            } catch (IllegalArgumentException e) {
                throw ServiceException.PARSE_ERROR(
                    "Invalid value: "+ value +", valid values: " + Arrays.asList(NumberComparison.values()), e);
            }
        }
    }

    public enum DateComparison {
        before, after;

        public static DateComparison fromString(String value) throws ServiceException {
            if (value == null) {
                return null;
            }
            try {
                return DateComparison.valueOf(value.toLowerCase());
            } catch (IllegalArgumentException e) {
                throw ServiceException.PARSE_ERROR(
                    "Invalid value: "+ value +", valid values: " + Arrays.asList(StringComparison.values()), e);
            }
        }
    }

    public enum ValueComparison {
        gt, ge, lt, le, eq, ne;

        public static ValueComparison fromString(String value) throws ServiceException {
            if (value == null) {
                return null;
            }
            try {
                return ValueComparison.valueOf(value.toLowerCase());
            } catch (IllegalArgumentException e) {
                throw ServiceException.PARSE_ERROR(
                    "Invalid value: "+ value +", valid values: " + Arrays.asList(ValueComparison.values()), e);
            }
        }
    }

}
