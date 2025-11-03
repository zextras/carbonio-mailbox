package com.zimbra.cs.store.minio;

import com.zimbra.common.service.ServiceException;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.InsufficientDataException;
import io.minio.errors.InternalException;
import io.minio.errors.InvalidResponseException;
import io.minio.errors.ServerException;
import io.minio.errors.XmlParserException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class MinIOBlobClientAdapter {
		private final MinioClient minioClient;
		private final String bucketName;

	public MinIOBlobClientAdapter(MinioClient minioClient, String bucketName) {
		this.minioClient = minioClient;
		this.bucketName = bucketName;
	}

	public MinioBlob uploadOnMinIo(InputStream data, long actualSize, String key)
			throws IOException, ServiceException {
		try {
			minioClient.putObject(PutObjectArgs.builder()
					.bucket(bucketName)
					.stream(data, actualSize, 10 * 1024 * 1024)
					.object(key)
					.build()
			);
			return getFromMinIo(key);
		} catch (ErrorResponseException | ServerException | NoSuchAlgorithmException |
						 InvalidResponseException | InvalidKeyException | InternalException |
						 InsufficientDataException | XmlParserException e) {
			throw ServiceException.FAILURE(e.getMessage(), e);
		}
	}

	public MinioBlob getFromMinIo(String key)
			throws IOException, ServiceException {
		try {
			final GetObjectResponse minIOResponse = minioClient.getObject(GetObjectArgs.builder()
					.bucket(bucketName)
					.object(key)
					.build()
			);
			final byte[] bytes = minIOResponse.readAllBytes();
			return new MinioBlob(key, bytes, bytes.length);
		} catch (ErrorResponseException | ServerException | NoSuchAlgorithmException |
						 InvalidResponseException | InvalidKeyException | InternalException |
						 InsufficientDataException | XmlParserException e) {
			throw ServiceException.FAILURE(e.getMessage(), e);
		}
	}

	public void close() throws Exception {
		minioClient.close();
	}

	public boolean deleteFromMinIo(String key) throws ServiceException {
		try {
			minioClient.removeObject(
					RemoveObjectArgs.builder()
							.bucket(bucketName)
							.object(key)
							.build()
			);
			return true;
		} catch (IOException | ErrorResponseException | ServerException | NoSuchAlgorithmException |
						 InvalidResponseException | InvalidKeyException | InternalException |
						 InsufficientDataException | XmlParserException e) {
			throw ServiceException.FAILURE(e.getMessage(), e);
		}
	}
}
