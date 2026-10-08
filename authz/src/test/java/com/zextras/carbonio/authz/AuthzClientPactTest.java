/*
 * SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.carbonio.authz;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonArrayMinLike;
import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "carbonio-authz")
class AuthzClientPactTest {

	private static final String CONSUMER = "carbonio-authz-client";
	private static final String JSON = "application/json";
	private static final String TOKEN = "example-token";
	private static final String ACCOUNT_ID = "example-account-id";

	@Pact(consumer = CONSUMER)
	V4Pact adminHasRight(PactDslWithProvider builder) {
		return builder.given("a global admin and an account")
				.uponReceiving("a configureQuota check by a global admin")
				.method("POST").path("/authz/rights/check").headers(Map.of("Content-Type", JSON))
				.body(checkBody("adminToken", "configureQuota"))
				.willRespondWith().status(200)
				.body(newJsonBody(body -> body.booleanValue("allowed", true)).build())
				.toPact(V4Pact.class);
	}

	@Pact(consumer = CONSUMER)
	V4Pact userLacksRight(PactDslWithProvider builder) {
		return builder.given("a user and another account")
				.uponReceiving("a configureQuota check by a plain user")
				.method("POST").path("/authz/rights/check").headers(Map.of("Content-Type", JSON))
				.body(checkBody("userToken", "configureQuota"))
				.willRespondWith().status(200)
				.body(newJsonBody(body -> body.booleanValue("allowed", false)).build())
				.toPact(V4Pact.class);
	}

	@Pact(consumer = CONSUMER)
	V4Pact invalidToken(PactDslWithProvider builder) {
		return builder.given("an account")
				.uponReceiving("a check with an invalid token")
				.method("POST").path("/authz/rights/check").headers(Map.of("Content-Type", JSON))
				.body(newJsonBody(body -> {
					body.object("subject", subject -> subject.stringValue("token", "invalid-token"));
					body.stringValue("right", "configureQuota");
					body.object("target", target -> {
						target.stringValue("type", "account");
						target.valueFromProviderState("id", "${accountId}", ACCOUNT_ID);
					});
				}).build())
				.willRespondWith().status(401)
				.body(newJsonBody(body -> body.stringType("error", "Invalid auth token")).build())
				.toPact(V4Pact.class);
	}

	@Pact(consumer = CONSUMER)
	V4Pact unknownRight(PactDslWithProvider builder) {
		return builder.given("a global admin and an account")
				.uponReceiving("a check of an unknown right")
				.method("POST").path("/authz/rights/check").headers(Map.of("Content-Type", JSON))
				.body(checkBody("adminToken", "notARight"))
				.willRespondWith().status(400)
				.body(newJsonBody(body -> body.stringType("error", "Unknown right: notARight")).build())
				.toPact(V4Pact.class);
	}

	@Pact(consumer = CONSUMER)
	V4Pact listCosRights(PactDslWithProvider builder) {
		return builder
				.uponReceiving("a listing of the rights checkable on a cos")
				.method("GET").path("/authz/rights").query("targetType=cos")
				.willRespondWith().status(200)
				.body(newJsonArrayMinLike(1, rights -> rights.object(right -> {
					right.stringType("name", "configureQuota");
					right.stringType("type", "setAttrs");
					right.booleanType("userRight", false);
					right.stringType("description", "configure quota");
				})).build())
				.toPact(V4Pact.class);
	}

	@Pact(consumer = CONSUMER)
	V4Pact listWithoutTargetType(PactDslWithProvider builder) {
		return builder
				.uponReceiving("a listing without target type")
				.method("GET").path("/authz/rights").query("targetType=")
				.willRespondWith().status(400)
				.body(newJsonBody(body -> body.stringType("error", "Missing required query parameter: targetType")).build())
				.toPact(V4Pact.class);
	}

	@Test
	@PactTestFor(pactMethod = "adminHasRight")
	void adminHasRight(MockServer mockServer) throws Exception {
		assertTrue(client(mockServer).checkRight(RightSubject.token(TOKEN), AuthzRight.CONFIGURE_QUOTA, account()));
	}

	@Test
	@PactTestFor(pactMethod = "userLacksRight")
	void userLacksRight(MockServer mockServer) throws Exception {
		assertFalse(client(mockServer).checkRight(RightSubject.token(TOKEN), AuthzRight.CONFIGURE_QUOTA, account()));
	}

	@Test
	@PactTestFor(pactMethod = "invalidToken")
	void invalidTokenIsUnauthorized(MockServer mockServer) {
		assertThrows(UnauthorizedException.class, () -> client(mockServer)
				.checkRight(RightSubject.token("invalid-token"), AuthzRight.CONFIGURE_QUOTA, account()));
	}

	@Test
	@PactTestFor(pactMethod = "unknownRight")
	void unknownRightIsBadRequest(MockServer mockServer) {
		assertThrows(BadRequestException.class, () -> client(mockServer)
				.checkRight(RightSubject.token(TOKEN), "notARight", account()));
	}

	@Test
	@PactTestFor(pactMethod = "listCosRights")
	void listsRights(MockServer mockServer) throws Exception {
		final List<RightInfo> rights = client(mockServer).listRights(AuthzTargetType.COS);

		assertTrue(rights.stream().anyMatch(right -> right.name().equals("configureQuota")));
	}

	@Test
	@PactTestFor(pactMethod = "listWithoutTargetType")
	void listingWithoutTargetTypeIsBadRequest(MockServer mockServer) {
		assertThrows(BadRequestException.class, () -> client(mockServer).listRights(""));
	}

	private static DslPart checkBody(String tokenKey, String right) {
		return newJsonBody(body -> {
			body.object("subject", subject -> subject.valueFromProviderState("token", "${" + tokenKey + "}", TOKEN));
			body.stringValue("right", right);
			body.object("target", target -> {
				target.stringValue("type", "account");
				target.valueFromProviderState("id", "${accountId}", ACCOUNT_ID);
			});
		}).build();
	}

	private static RightTarget account() {
		return RightTarget.of(AuthzTargetType.ACCOUNT, ACCOUNT_ID);
	}

	private static AuthzClient client(MockServer mockServer) {
		return AuthzClient.builder(mockServer.getUrl() + "/authz").build();
	}
}
