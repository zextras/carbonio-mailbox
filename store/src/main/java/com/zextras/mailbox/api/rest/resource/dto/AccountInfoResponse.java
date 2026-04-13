package com.zextras.mailbox.api.rest.resource.dto;

import com.zimbra.common.account.ZAttrProvisioning.AccountStatus;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.AuthToken;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public record AccountInfoResponse(String id, String name, String displayName, String cosId,
																	String domainId, String domain, AccountStatus status,
																	boolean isGlobalAdmin,
																	boolean isExternal, String locale, Map<String, Boolean> features,
																	Map<String, String> capabilities, Long sessionLifetimeMs) {

	public static AccountInfoResponse from(Account account) {
		return AccountInfoResponse.withLifetime(account, null);
	}

	public static AccountInfoResponse from(AuthToken authToken) throws ServiceException {
		long sessionLifetimeMs = authToken.getExpires() - System.currentTimeMillis();
		var account = authToken.getAccount();
		return AccountInfoResponse.withLifetime(account, sessionLifetimeMs);
	}


	private static AccountInfoResponse withLifetime(Account account, Long sessionLifetimeMs) {
		var features = account.getAttrs().entrySet().stream()
				.filter(entry -> entry.getKey().startsWith("carbonioFeature"))
				.collect(Collectors.toMap(Entry::getKey,
						entry -> Boolean.parseBoolean(entry.getValue().toString())));
		var capabilities = account.getAttrs().entrySet().stream()
				.filter(entry -> {
					String key = entry.getKey();
					return key.startsWith("carbonioWsc") || key.startsWith("carbonioFiles")
							|| key.startsWith("carbonioTasks") || key.startsWith("carbonioDocs")
							|| key.startsWith("carbonioPreview");
				})
				.collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().toString()));
		boolean isExternal;
		try {
			isExternal = account.isAccountExternal();
		} catch (ServiceException e) {
			// If we cannot determine whether the account is external, default to true.
			// An internal account failing this check implies a non-standard transport
			// configuration, which is more consistent with an external account.
			isExternal = true;
		}
		return new AccountInfoResponse(account.getId(), account.getName(), account.getDisplayName(),
				account.getCOSId(), account.getDomainId(), account.getPublicServiceUrl(),
				account.getAccountStatus(), account.isIsAdminAccount(), isExternal,
				account.getLocaleAsString(), features, capabilities, sessionLifetimeMs);
	}
}
