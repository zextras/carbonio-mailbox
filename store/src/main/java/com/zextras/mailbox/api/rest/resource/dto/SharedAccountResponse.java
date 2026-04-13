package com.zextras.mailbox.api.rest.resource.dto;

import com.zimbra.cs.account.Account;

public record SharedAccountResponse(String id, String email, String domain, String cosId) {

	public static SharedAccountResponse from(Account account) {
		return new SharedAccountResponse(account.getId(), account.getName(),
				account.getDomainName(), account.getCOSId());
	}
}
