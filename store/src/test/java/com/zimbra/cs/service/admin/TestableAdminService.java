package com.zimbra.cs.service.admin;

import com.zimbra.cs.account.Server;
import com.zimbra.cs.rmgmt.RemoteManager;
import java.util.function.Function;
import org.mockito.Mockito;

public class TestableAdminService extends AdminService {

	@Override
	protected Function<Server, RemoteManager> getRemoteManagerProvider() {
		return (Server server) -> Mockito.mock(RemoteManager.class);
	}

}