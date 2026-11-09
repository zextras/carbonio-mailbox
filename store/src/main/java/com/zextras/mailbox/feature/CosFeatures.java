package com.zextras.mailbox.feature;

import java.util.List;
import java.util.Map;

public record CosFeatures(
    String cosId, String name, String entryCsn, Map<String, List<String>> features) {}
