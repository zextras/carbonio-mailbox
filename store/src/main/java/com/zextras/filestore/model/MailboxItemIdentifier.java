package com.zextras.filestore.model;

import com.zextras.common.ThrowingFunction;

public class MailboxItemIdentifier extends Identifier {

	private final String key;

	public MailboxItemIdentifier(String key) {
		super();
		this.key = key;
	}

	@Override
	public <T, E extends Exception> T fold(
			ThrowingFunction<MailboxItemIdentifier, T, E> throwingFunction,
			ThrowingFunction<FilesIdentifier, T, E> throwingFunction1,
			ThrowingFunction<ChatsIdentifier, T, E> throwingFunction2) throws E {
		return null;
	}

	@Override
	public IdentifierType type() {
		return IdentifierType.mail;
	}
}
