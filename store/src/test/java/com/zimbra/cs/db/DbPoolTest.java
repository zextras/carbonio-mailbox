package com.zimbra.cs.db;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("e2e")
class DbPoolTest {

	@Test
	void whenPoolShutdown_CannotAcquireConnection() throws Exception {
		DbPool dbPool = DbPool.newPool(TestDatabase.install());
		dbPool.shutdownInstance();
		Assertions.assertThrows(IllegalStateException.class, dbPool::getConnectionInstance);
	}
}
