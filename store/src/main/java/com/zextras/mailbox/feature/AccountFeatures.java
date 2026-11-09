package com.zextras.mailbox.feature;

import java.util.List;
import java.util.Map;

public record AccountFeatures(
    String accountId, String entryCsn, Map<String, List<String>> features) {}
