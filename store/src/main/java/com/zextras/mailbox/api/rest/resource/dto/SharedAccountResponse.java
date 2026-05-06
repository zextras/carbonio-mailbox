package com.zextras.mailbox.api.rest.resource.dto;

import com.zimbra.cs.account.Account;

public record SharedAccountResponse(String id, String email, String domainId, String cosId) {

	public static SharedAccountResponse from(Account account) {
		return new SharedAccountResponse(account.getId(), account.getName(),
				account.getDomainId(), account.getCOSId());
	}
}
