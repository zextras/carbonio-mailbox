package com.zimbra.cs.store.storages;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.store.BlobBuilder;
import com.zimbra.cs.store.storages.StoragesClientAdapter.StorageKey;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.hsqldb.lib.StringInputStream;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StoragesIncomingBlobBuilderTest {

	@Test
	void shouldReturnBuilderInSetters() throws IOException {
		final StorageKey key = getStorageKey();
		final StoragesIncomingBlobBuilder builder = new StoragesIncomingBlobBuilder(key);
		final BlobBuilder<StoragesBlob> builderInstance = builder.append("aaa".getBytes())
				.append(new ByteArrayInputStream("bbb".getBytes()))
				.append(new StringInputStream("ccc"))
				.disableDigest(true);
		Assertions.assertNotNull(builderInstance);
	}

	private static StorageKey getStorageKey() {
		return new StorageKey("test", 0, 0, "");
	}

	@Test
	void shouldBuildABlobWithGivenKeyAndData() throws IOException, ServiceException {
		final StoragesIncomingBlobBuilder builder = new StoragesIncomingBlobBuilder(getStorageKey());
		final StoragesBlob blob = builder.append("aaa".getBytes()).finish();

		Assertions.assertEquals("test",blob.getKey());
		Assertions.assertEquals("aaa", new String(blob.getContent()));
	}

	@Test
	void shouldOverrideContentWhenAppending() throws IOException, ServiceException {
		final StoragesIncomingBlobBuilder builder = new StoragesIncomingBlobBuilder(getStorageKey());
		final StoragesBlob blob = builder.append("aaa".getBytes())
				.append(new ByteArrayInputStream("bbb".getBytes()))
				.finish();

		Assertions.assertEquals("bbb", new String(blob.getContent()));
	}

}