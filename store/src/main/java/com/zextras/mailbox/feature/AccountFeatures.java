package com.zextras.mailbox.feature;

import java.util.List;
import java.util.Map;

public record AccountFeatures(
    String accountId,
    String domainName,
    String cosId,
    String entryCsn,
    Map<String, List<String>> features) {}
