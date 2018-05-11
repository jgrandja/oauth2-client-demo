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

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

import java.io.IOException;

/**
 * @author Joe Grandja
 */
public class OAuth2BearerTokenAuthorizationInterceptor implements ClientHttpRequestInterceptor {

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		DefaultClientHttpRequestAttributes clientHttpRequestAttributes = (DefaultClientHttpRequestAttributes) request;

		String clientRegistrationId = (String) clientHttpRequestAttributes
				.getAttribute(OAuth2ClientAttributeNames.CLIENT_REGISTRATION_IDENTIFIER);
		Authentication resourceOwnerPrincipal = (Authentication) clientHttpRequestAttributes
				.getAttribute(OAuth2ClientAttributeNames.RESOURCE_OWNER_PRINCIPAL);
		OAuth2AuthorizedClientService authorizedClientService = (OAuth2AuthorizedClientService) clientHttpRequestAttributes
				.getAttribute(OAuth2ClientAttributeNames.AUTHORIZED_CLIENT_SERVICE);

		OAuth2AuthorizedClient authorizedClient = authorizedClientService
				.loadAuthorizedClient(clientRegistrationId, resourceOwnerPrincipal.getName());
		request.getHeaders().add("Authorization", "Bearer " + authorizedClient.getAccessToken().getTokenValue());

		return execution.execute(request, body);
	}
}