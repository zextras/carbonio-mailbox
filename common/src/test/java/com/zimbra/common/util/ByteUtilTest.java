// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ByteUtilTest {

    @ParameterizedTest
    @ValueSource(longs = {0, 8193, Integer.MAX_VALUE, 2_147_483_648L, 4_294_967_313L})
    void countsAllBytesAndClosesTheStream(long length) throws IOException {
        FiniteInputStream input = new FiniteInputStream(length);

        assertEquals(length, ByteUtil.getDataLength(input));
        assertTrue(input.closed);
    }

    private static class FiniteInputStream extends InputStream {
        private long remaining;
        private boolean closed;

        private FiniteInputStream(long length) {
            remaining = length;
        }

        @Override
        public int read() {
            if (remaining == 0) {
                return -1;
            }
            remaining--;
            return 0;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) {
            Objects.checkFromIndexSize(offset, length, buffer.length);
            if (length == 0) {
                return 0;
            }
            if (remaining == 0) {
                return -1;
            }
            int count = (int) Math.min(remaining, length);
            Arrays.fill(buffer, offset, offset + count, (byte) 0);
            remaining -= count;
            return count;
        }

        @Override
        public void close() {
            closed = true;
        }
    }
}
