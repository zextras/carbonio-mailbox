package com.zimbra.cs.service.admin;

import static com.zimbra.cs.service.admin.GetQuotaUsage.SORT_TOTAL_USED;
import static org.junit.Assert.assertEquals;

import com.zimbra.cs.service.admin.GetQuotaUsage.AccountQuota;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class GetQuotaUsageTest {

  @Test
  public void shouldReturnUniqueAccountQuotaUsagesWhenCalledGetUniqueAccountQuotaList() {
    AccountQuota a1 = new AccountQuota();
    a1.setId("sdfj-sdfhk-jdfk-sdf");
    a1.setName("a1");
    a1.setSortQuotaLimit(250000L);
    a1.setQuotaUsed(2500L);

    // should be removed from expectedAccountQuotas list as the ID is duplicate
    AccountQuota a2 = new AccountQuota();
    a2.setId("sdfj-sdfhk-jdfk-sdf");
    a2.setName("a2");
    a2.setSortQuotaLimit(250000L);
    a2.setQuotaUsed(2500L);

    AccountQuota a3 = new AccountQuota();
    a3.setId("sdfj-sdfhk-jdfk-sdfr");
    a3.setName("a3");
    a3.setSortQuotaLimit(250000L);
    a3.setQuotaUsed(2500L);

    List<AccountQuota> accountQuotas = Arrays.asList(a1, a2, a3);

    List<AccountQuota> expectedAccountQuotas = Arrays.asList(a1, a3);

    final List<AccountQuota> uniqueAccountQuotas =
        GetQuotaUsage.getUniqueAccountQuotaList(accountQuotas);

    assertEquals(uniqueAccountQuotas, expectedAccountQuotas);
  }

  /**
   * Unit test for {@link GetQuotaUsage#getSortedAccountsQuotaList(List, String, boolean)} with
   * {@link GetQuotaUsage#SORT_TOTAL_USED} and sortAscending parameters
   */
  @Test
  public void shouldReturnSortedAccountQuotaUsagesWhenCalledGetSortedAccountQuotaList() {
    AccountQuota a1 = new AccountQuota();
    a1.setId("sdfj-sdfhk-jdfk-sdf");
    a1.setName("a1");
    a1.setSortQuotaLimit(250000L);
    a1.setQuotaUsed(3500L);

    // should be removed from expectedAccountQuotas list as the ID is duplicate
    AccountQuota a2 = new AccountQuota();
    a2.setId("sdfj-sdfhk-jdfk-sdf");
    a2.setName("a2");
    a2.setSortQuotaLimit(250000L);
    a2.setQuotaUsed(1500L);

    AccountQuota a3 = new AccountQuota();
    a3.setId("sdfj-sdfhk-jdfk-sdfr");
    a3.setName("a3");
    a3.setSortQuotaLimit(250000L);
    a3.setQuotaUsed(2500L);

    List<AccountQuota> accountQuotas = Arrays.asList(a1, a2, a3);

    // when sortAscending is true
    List<AccountQuota> expectedSortAscendingAccountQuotas = Arrays.asList(a2, a3, a1);
    final List<AccountQuota> sortedAscendingAccountQuotas =
        GetQuotaUsage.getSortedAccountsQuotaList(accountQuotas, SORT_TOTAL_USED, true);
    assertEquals(sortedAscendingAccountQuotas, expectedSortAscendingAccountQuotas);

    // when sortAscending is false
    List<AccountQuota> expectedSortDescendingAccountQuotas = Arrays.asList(a1, a3, a2);
    final List<AccountQuota> sortedDescendingAccountsQuotaList =
        GetQuotaUsage.getSortedAccountsQuotaList(accountQuotas, SORT_TOTAL_USED, false);
    assertEquals(sortedDescendingAccountsQuotaList, expectedSortDescendingAccountQuotas);
  }
}
