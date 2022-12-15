// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package qa.unittest;

import static org.junit.Assert.assertEquals;

import com.google.common.base.Joiner;
import com.zimbra.cs.db.DbPool;
import com.zimbra.cs.db.DbPool.DbConnection;
import com.zimbra.cs.db.StatTrackingPreparedStatement;
import com.zimbra.cs.stats.ActivityTracker;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Testing that {@link StatTrackingPreparedStatement} can properly track the number of SELECT,
 * INSERT, DELETE, UPDATE, and other queries.
 *
 * @author iraykin
 */
public class TestSqlStats {
  // the file sql_test.csv should't actually get created since we're never dumping the data
  ActivityTracker tracker = new ActivityTracker("sql_test.csv");
  ActivityTracker trackerPrometheus = new ActivityTracker("sql_test.prom");
  DbConnection conn;

  @Before
  public void startup() throws Exception {
    DbPool.startup();
    conn = DbPool.getConnection();
  }

  @Test
  public void test() throws Exception {
    // create the table (tracks "other")
    StatTrackingPreparedStatement create =
        (StatTrackingPreparedStatement)
            conn.prepareStatement("CREATE TABLE stats_test(col1 INTEGER)");
    create.setTracker(tracker, trackerPrometheus);
    create.execute();

    // track INSERT
    StatTrackingPreparedStatement insert =
        (StatTrackingPreparedStatement)
            conn.prepareStatement("INSERT INTO stats_test(col1) VALUES(?)");
    insert.setInt(1, 1);
    insert.setTracker(tracker, trackerPrometheus);
    insert.execute();

    // track SELECT
    StatTrackingPreparedStatement select =
        (StatTrackingPreparedStatement) conn.prepareStatement("SELECT * FROM stats_test");
    select.setTracker(tracker, trackerPrometheus);
    select.execute();

    // track UPDATE
    StatTrackingPreparedStatement update =
        (StatTrackingPreparedStatement) conn.prepareStatement("UPDATE stats_test SET col1 = ?");
    update.setInt(1, 10);
    update.setTracker(tracker, trackerPrometheus);
    update.execute();

    // track DELETE
    StatTrackingPreparedStatement delete =
        (StatTrackingPreparedStatement) conn.prepareStatement("DELETE FROM stats_test");
    delete.setTracker(tracker, trackerPrometheus);
    delete.execute();

    // delete the table (tracks "other" again)
    StatTrackingPreparedStatement drop =
        (StatTrackingPreparedStatement) conn.prepareStatement("DROP TABLE stats_test");
    drop.setTracker(tracker, trackerPrometheus);
    drop.execute();

    // get the statistics
    ArrayList<String> datalines = (ArrayList<String>) tracker.getDataLines();

    // drop the last column, since average run time can vary. this leaves only name and count.
    ArrayList<String> results = new ArrayList<String>();
    for (String line : datalines) {
      results.add(Joiner.on(",").join(Arrays.asList(line.split(",")).subList(0, 2)));
    }
    // sort alphabetically, since the lines are not returned in any particular order
    Collections.sort(results);

    // the expected lines, also in alphabetical order
    List<String> expected =
        Arrays.asList("DELETE,1", "INSERT,1", "SELECT,1", "UPDATE,1", "other,2");
    assertEquals(expected, results);
  }

  @After
  public void shutdown() throws Exception {
    DbPool.shutdown();
  }
}
