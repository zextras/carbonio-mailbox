package com.zextras.mailbox.feature;

import java.util.List;
import java.util.Map;

public record CosAttributes(
    String cosId, String dn, String name, String entryCsn, Map<String, List<String>> attributes) {}
