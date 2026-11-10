package com.zextras.mailbox.feature;

import java.util.List;
import java.util.Map;

public record DomainAttributes(
    String domainName,
    String dn,
    String defaultCosId,
    String entryCsn,
    Map<String, List<String>> attributes) {}
