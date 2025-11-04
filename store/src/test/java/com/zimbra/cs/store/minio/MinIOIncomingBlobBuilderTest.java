package com.zimbra.cs.store.minio;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.store.BlobBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.hsqldb.lib.StringInputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MinIOIncomingBlobBuilderTest {

	@Test
	void shouldReturnBuilderInSetters() throws IOException {
		final StoragesIncomingBlobBuilder builder = new StoragesIncomingBlobBuilder("test");
		final BlobBuilder<StoragesBlob> builderInstance = builder.append("aaa".getBytes())
				.append(new ByteArrayInputStream("bbb".getBytes()))
				.append(new StringInputStream("ccc"))
				.disableDigest(true);
		Assertions.assertNotNull(builderInstance);
	}

	@Test
	void shouldBuildABlobWithGivenKeyAndData() throws IOException, ServiceException {
		final StoragesIncomingBlobBuilder builder = new StoragesIncomingBlobBuilder("test");
		final StoragesBlob blob = builder.append("aaa".getBytes()).finish();

		Assertions.assertEquals("test",blob.getKey());
		Assertions.assertEquals("aaa", new String(blob.getContent()));
	}

	@Test
	void shouldOverrideContentWhenAppending() throws IOException, ServiceException {
		final StoragesIncomingBlobBuilder builder = new StoragesIncomingBlobBuilder("test");
		final StoragesBlob blob = builder.append("aaa".getBytes())
				.append(new ByteArrayInputStream("bbb".getBytes()))
				.finish();

		Assertions.assertEquals("bbb", new String(blob.getContent()));
	}

}