package com.zimbra.cs.imap;

import com.zimbra.common.localconfig.LC;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

class ImapSearchTest {

    public static final String SEARCH = "1:123,456:*";

    @AfterEach
    void tearDown() {
        LC.ignore_imap_uid_range_search.setDefault(true);
    }

    @Test
    void test_when_search_range_true_and_other_checks_true_then_not_append() throws Exception {
        LC.ignore_imap_uid_range_search.setDefault(true);
        ImapSearch.SequenceSearch sequenceSearch = Mockito.mock(ImapSearch.SequenceSearch.class);
        Field mSubSequence = ImapSearch.SequenceSearch.class.getDeclaredField("mSubSequence");
        mSubSequence.setAccessible(true);
        mSubSequence.set(sequenceSearch, SEARCH);
        ImapSearch.AndOperation andOperation = new ImapSearch.AndOperation(sequenceSearch);
        ImapFolder i4folder = Mockito.mock();
        Mockito.when(i4folder.getSubsequence(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(Mockito.mock());
        andOperation.toZimbraSearch(i4folder);
        Mockito.verify(sequenceSearch, Mockito.times(0)).toZimbraSearch(i4folder);
    }

    @Test
    void test_when_search_range_false_and_other_checks_true_then_append() throws Exception {
        LC.ignore_imap_uid_range_search.setDefault(false);
        ImapSearch.SequenceSearch sequenceSearch = Mockito.mock(ImapSearch.SequenceSearch.class);
        Field mSubSequence = ImapSearch.SequenceSearch.class.getDeclaredField("mSubSequence");
        mSubSequence.setAccessible(true);
        mSubSequence.set(sequenceSearch, SEARCH);
        ImapSearch.AndOperation andOperation = new ImapSearch.AndOperation(sequenceSearch);
        ImapFolder i4folder = Mockito.mock();
        Mockito.when(i4folder.getSubsequence(Mockito.anyString(), Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyBoolean())).thenReturn(Mockito.mock());
        andOperation.toZimbraSearch(i4folder);
        Mockito.verify(sequenceSearch, Mockito.times(1)).toZimbraSearch(i4folder);
    }
}
