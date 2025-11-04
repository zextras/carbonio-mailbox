package com.zimbra.cs.store.storages;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.store.BlobBuilder;
import com.zimbra.cs.store.storages.StoragesClientAdapter.StorageKey;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
import org.hsqldb.lib.StringInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StoragesIncomingBlobBuilderTest {

	private static String accountId = "test";

	@Test
	void shouldReturnBuilderInSetters() throws IOException {
		final StorageKey key = getStorageAccountIdKey();
		final StoragesIncomingBlobBuilder builder = new StoragesIncomingBlobBuilder(key);
		final BlobBuilder<StoragesBlob> builderInstance = builder.append("aaa".getBytes())
				.append(new ByteArrayInputStream("bbb".getBytes()))
				.append(new StringInputStream("ccc"))
				.disableDigest(true);
		Assertions.assertNotNull(builderInstance);
	}

	private static StorageKey getStorageAccountIdKey() {
		return new StorageKey(accountId, UUID.randomUUID().toString());
	}

	@Test
	void shouldBuildABlobWithGivenKeyAndData() throws IOException, ServiceException {
		final StoragesIncomingBlobBuilder builder = new StoragesIncomingBlobBuilder(
				getStorageAccountIdKey());
		final StoragesBlob blob = builder.append("aaa".getBytes()).finish();

		Assertions.assertEquals(accountId, blob.getKey().basePath());
		Assertions.assertEquals("aaa", new String(blob.getContent()));
	}

	@Test
	void shouldOverrideContentWhenAppending() throws IOException, ServiceException {
		final StoragesIncomingBlobBuilder builder = new StoragesIncomingBlobBuilder(
				getStorageAccountIdKey());
		final StoragesBlob blob = builder.append("aaa".getBytes())
				.append(new ByteArrayInputStream("bbb".getBytes()))
				.finish();

		Assertions.assertEquals("bbb", new String(blob.getContent()));
	}

}