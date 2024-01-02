package com.zimbra.doc.soap;

import com.zimbra.soap.account.message.GetSMIMEPublicCertsRequest;
import com.zimbra.soap.admin.message.AbortHsmRequest;
import com.zimbra.soap.admin.message.AbortXMbxSearchRequest;
import com.zimbra.soap.admin.message.BackupAccountQueryRequest;
import com.zimbra.soap.admin.message.BackupQueryRequest;
import com.zimbra.soap.admin.message.BackupRequest;
import com.zimbra.soap.admin.message.CreateArchiveRequest;
import com.zimbra.soap.admin.message.CreateXMbxSearchRequest;
import com.zimbra.soap.admin.message.DeleteXMbxSearchRequest;
import com.zimbra.soap.admin.message.DisableArchiveRequest;
import com.zimbra.soap.admin.message.EnableArchiveRequest;
import com.zimbra.soap.admin.message.ExportMailboxRequest;
import com.zimbra.soap.admin.message.FailoverClusterServiceRequest;
import com.zimbra.soap.admin.message.GetApplianceHSMFSRequest;
import com.zimbra.soap.admin.message.GetClusterStatusRequest;
import com.zimbra.soap.admin.message.GetHsmStatusRequest;
import com.zimbra.soap.admin.message.GetMailboxVersionRequest;
import com.zimbra.soap.admin.message.GetMailboxVolumesRequest;
import com.zimbra.soap.admin.message.GetSMIMEConfigRequest;
import com.zimbra.soap.admin.message.GetXMbxSearchesListRequest;
import com.zimbra.soap.admin.message.HsmRequest;
import com.zimbra.soap.admin.message.InstallCertRequest;
import com.zimbra.soap.admin.message.ModifySMIMEConfigRequest;
import com.zimbra.soap.admin.message.MoveBlobsRequest;
import com.zimbra.soap.admin.message.MoveMailboxRequest;
import com.zimbra.soap.admin.message.PurgeMovedMailboxRequest;
import com.zimbra.soap.admin.message.QueryMailboxMoveRequest;
import com.zimbra.soap.admin.message.RegisterMailboxMoveOutRequest;
import com.zimbra.soap.admin.message.ReloadAccountRequest;
import com.zimbra.soap.admin.message.RestoreRequest;
import com.zimbra.soap.admin.message.RolloverRedoLogRequest;
import com.zimbra.soap.admin.message.ScheduleBackupsRequest;
import com.zimbra.soap.admin.message.SearchMultiMailboxRequest;
import com.zimbra.soap.admin.message.UnloadMailboxRequest;
import com.zimbra.soap.admin.message.UnregisterMailboxMoveOutRequest;

import java.util.HashSet;
import java.util.Set;

public final class DocExcludedServices {

    private static final Set<String> SERVICE_SET;
    public static final String REQUEST_SUFFIX = "Request";

    static {
        SERVICE_SET = new HashSet<>(63);
        SERVICE_SET.add(removeRequest(GetSMIMEPublicCertsRequest.class));
        SERVICE_SET.add(removeRequest(AbortHsmRequest.class));
        SERVICE_SET.add(removeRequest(AbortXMbxSearchRequest.class));
        SERVICE_SET.add(removeRequest(BackupRequest.class));
        SERVICE_SET.add(removeRequest(BackupAccountQueryRequest.class));
        SERVICE_SET.add(removeRequest(BackupQueryRequest.class));
        SERVICE_SET.add(removeRequest(CreateArchiveRequest.class));
        SERVICE_SET.add(removeRequest(CreateXMbxSearchRequest.class));
        SERVICE_SET.add(removeRequest(DeleteXMbxSearchRequest.class));
        SERVICE_SET.add(removeRequest(DisableArchiveRequest.class));
        SERVICE_SET.add(removeRequest(EnableArchiveRequest.class));
        SERVICE_SET.add(removeRequest(ExportMailboxRequest.class));
        SERVICE_SET.add(removeRequest(FailoverClusterServiceRequest.class));
        SERVICE_SET.add(removeRequest(GetApplianceHSMFSRequest.class));
        SERVICE_SET.add(removeRequest(GetClusterStatusRequest.class));
        SERVICE_SET.add(removeRequest(GetHsmStatusRequest.class));
        SERVICE_SET.add(removeRequest(GetMailboxVersionRequest.class));
        SERVICE_SET.add(removeRequest(GetMailboxVolumesRequest.class));
        SERVICE_SET.add(removeRequest(GetSMIMEConfigRequest.class));
        SERVICE_SET.add(removeRequest(GetXMbxSearchesListRequest.class));
        SERVICE_SET.add(removeRequest(HsmRequest.class));
        SERVICE_SET.add(removeRequest(InstallCertRequest.class));
        SERVICE_SET.add(removeRequest(ModifySMIMEConfigRequest.class));
        SERVICE_SET.add(removeRequest(MoveBlobsRequest.class));
        SERVICE_SET.add(removeRequest(MoveMailboxRequest.class));
        SERVICE_SET.add(removeRequest(PurgeMovedMailboxRequest.class));
        SERVICE_SET.add(removeRequest(QueryMailboxMoveRequest.class));
        SERVICE_SET.add(removeRequest(RegisterMailboxMoveOutRequest.class));
        SERVICE_SET.add(removeRequest(ReloadAccountRequest.class));
        SERVICE_SET.add(removeRequest(RestoreRequest.class));
        SERVICE_SET.add(removeRequest(RolloverRedoLogRequest.class));
        SERVICE_SET.add(removeRequest(ScheduleBackupsRequest.class));
        SERVICE_SET.add(removeRequest(SearchMultiMailboxRequest.class));
        SERVICE_SET.add(removeRequest(UnloadMailboxRequest.class));
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
