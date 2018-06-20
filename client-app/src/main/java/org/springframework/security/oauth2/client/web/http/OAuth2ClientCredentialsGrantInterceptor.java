/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.security.oauth2.client.web.http;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Joe Grandja
 */
public class OAuth2ClientCredentialsGrantInterceptor
		implements ClientHttpRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body,
			ClientHttpRequestExecution execution) throws IOException {
		DefaultClientHttpRequestAttributes clientHttpRequestAttributes = (DefaultClientHttpRequestAttributes) request;

		String clientRegistrationId = (String) clientHttpRequestAttributes
				.getAttribute(OAuth2ClientAttributeNames.CLIENT_REGISTRATION_IDENTIFIER);
		Authentication resourceOwnerPrincipal = (Authentication) clientHttpRequestAttributes
				.getAttribute(OAuth2ClientAttributeNames.RESOURCE_OWNER_PRINCIPAL);
		OAuth2AuthorizedClientService authorizedClientService = (OAuth2AuthorizedClientService) clientHttpRequestAttributes
				.getAttribute(OAuth2ClientAttributeNames.AUTHORIZED_CLIENT_SERVICE);

		OAuth2AuthorizedClient authorizedClient = authorizedClientService
				.loadAuthorizedClient(clientRegistrationId,
						resourceOwnerPrincipal.getName());
		if (authorizedClient != null) {
			return execution.execute(request, body);
		}

		// Perform client_credentials grant flow
		ClientRegistrationRepository clientRegistrationRepository = (ClientRegistrationRepository) clientHttpRequestAttributes
				.getAttribute(OAuth2ClientAttributeNames.CLIENT_REGISTRATION_REPOSITORY);
		ClientRegistration clientRegistration = clientRegistrationRepository
				.findByRegistrationId(clientRegistrationId);

		MultiValueMap<String, String> clientCredentialsParams = new LinkedMultiValueMap<>();
		clientCredentialsParams.add("grant_type", "client_credentials");

		RequestEntity<MultiValueMap<String, String>> clientCredentialsRequest = RequestEntity
				.post(URI.create(clientRegistration.getProviderDetails().getTokenUri()))
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.body(clientCredentialsParams);

		RestTemplate restTemplate = new RestTemplateBuilder(rt -> {
		}).basicAuthorization(clientRegistration.getClientId(),
				clientRegistration.getClientSecret()).build();

		ResponseEntity<Map<String, String>> tokenResponse = restTemplate.exchange(
				clientCredentialsRequest,
				new ParameterizedTypeReference<Map<String, String>>() {
				});
		Map<String, String> tokenResponseAttributes = tokenResponse.getBody();

		String accessToken = tokenResponseAttributes.get("access_token");
		OAuth2AccessToken.TokenType accessTokenType = null;
		if (OAuth2AccessToken.TokenType.BEARER.getValue()
				.equalsIgnoreCase(tokenResponseAttributes.get("token_type"))) {
			accessTokenType = OAuth2AccessToken.TokenType.BEARER;
		}
		long expiresIn = 0;
		if (tokenResponseAttributes.containsKey("expires_in")) {
			expiresIn = Long.valueOf(tokenResponseAttributes.get("expires_in"));
		}
		Set<String> scopes = null;
		if (tokenResponseAttributes.containsKey("scope")) {
			scopes = Arrays.stream(tokenResponseAttributes.get("scope").split(" "))
					.collect(Collectors.toSet());
		}
		OAuth2AccessTokenResponse accessTokenResponse = OAuth2AccessTokenResponse
				.withToken(accessToken).tokenType(accessTokenType).expiresIn(expiresIn)
				.scopes(scopes).build();

		authorizedClient = new OAuth2AuthorizedClient(clientRegistration,
				resourceOwnerPrincipal.getName(), accessTokenResponse.getAccessToken());

		authorizedClientService.saveAuthorizedClient(authorizedClient,
				resourceOwnerPrincipal);

		return execution.execute(request, body);
	}

}