// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.zimbra.common.localconfig.LC;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.zimbra.cs.stats.ZimbraPerf;

/**
 * Unit test for {@link LuceneDirectory}.
 *
 * @author ysasaki
 */
public final class LuceneDirectoryTest {
    private static File tmpDir;

    @BeforeAll
    public static void init() throws Exception {
        tmpDir = new File("build/test/" + LuceneDirectoryTest.class.getSimpleName());
        if (!tmpDir.isDirectory()) {
            tmpDir.mkdirs();
        }
        // make sure index perf counters are enabled.
        LC.zimbra_index_disable_perf_counters.setDefault(false);
    }

 @Test
 void read() throws IOException {
  FileOutputStream out = new FileOutputStream(new File(tmpDir, "read"));
  out.write(new byte[]{0, 1, 2, 3, 4});
  out.close();

  long count = ZimbraPerf.COUNTER_IDX_BYTES_READ.getCount();
  long total = ZimbraPerf.COUNTER_IDX_BYTES_READ.getTotal();
  LuceneDirectory dir = LuceneDirectory.open(tmpDir);
  IndexInput in = dir.openInput("read");
  in.readBytes(new byte[5], 0, 5);
  in.close();
  assertEquals(1, ZimbraPerf.COUNTER_IDX_BYTES_READ.getCount() - count);
  assertEquals(5, ZimbraPerf.COUNTER_IDX_BYTES_READ.getTotal() - total);
 }

 @Test
 void write() throws IOException {
  long count = ZimbraPerf.COUNTER_IDX_BYTES_WRITTEN.getCount();
  long total = ZimbraPerf.COUNTER_IDX_BYTES_WRITTEN.getTotal();
  LuceneDirectory dir = LuceneDirectory.open(new File("/tmp"));
  IndexOutput out = dir.createOutput("write");
  out.writeBytes(new byte[]{0, 1, 2}, 3);
  out.close();

  assertEquals(1, ZimbraPerf.COUNTER_IDX_BYTES_WRITTEN.getCount() - count);
  assertEquals(3, ZimbraPerf.COUNTER_IDX_BYTES_WRITTEN.getTotal() - total);
 }

}
