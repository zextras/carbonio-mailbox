package com.zimbra.doc.soap;

import com.zimbra.soap.admin.message.UnregisterMailboxMoveOutRequest;

import java.util.HashSet;
import java.util.Set;

public final class DocExcludedServices {

    private static final Set<String> SERVICE_SET;
    public static final String REQUEST_SUFFIX = "Request";

    static {
        SERVICE_SET = new HashSet<>(63);
        SERVICE_SET.add(removeRequest(UnregisterMailboxMoveOutRequest.class));
    }

    private DocExcludedServices() {}

    public static boolean isExclude(String serviceName) {
        return SERVICE_SET.contains(serviceName);
    }

    public static boolean isExcludeRequest(String serviceName) {
        return SERVICE_SET.contains(removeRequest(serviceName));
    }

    public static String removeRequest(String serviceName) {

        int requestLength = REQUEST_SUFFIX.length();
        int serviceLength = serviceName.length();
        return serviceLength >= requestLength && serviceName.endsWith(REQUEST_SUFFIX)
                ? serviceName.substring(0, serviceLength - requestLength)
                : serviceName;
    }

    public static String removeRequest(Class<?> clazz) {
        return removeRequest(clazz.getSimpleName());
    }

}
