package com.zextras.mailbox.api.rest.resource.dto;

import java.util.List;

public record BatchRequest(List<String> ids, List<String> emails) {

}
