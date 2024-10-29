package com.zextras.mailbox.consul;

import io.vavr.control.Try;

public interface ConsulTokenProvider {

	Try<String> getToken();

}
