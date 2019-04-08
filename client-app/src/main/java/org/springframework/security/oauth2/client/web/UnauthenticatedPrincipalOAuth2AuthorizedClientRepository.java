/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.security.oauth2.client.web;

import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.function.Supplier;

/**
 * @author Joe Grandja
 * @since 5.2
 */
public final class UnauthenticatedPrincipalOAuth2AuthorizedClientRepository implements OAuth2AuthorizedClientRepository {
	private final AuthenticationTrustResolver authenticationTrustResolver = new AuthenticationTrustResolverImpl();

	private final OAuth2AuthorizedClientService authorizedClientService;

	private Supplier<Authentication> unauthenticatedPrincipalSupplier =
			() -> new UnauthenticatedPrincipalAuthentication("unauthenticatedPrincipal");

	public UnauthenticatedPrincipalOAuth2AuthorizedClientRepository(OAuth2AuthorizedClientService authorizedClientService) {
		Assert.notNull(authorizedClientService, "authorizedClientService cannot be null");
		this.authorizedClientService = authorizedClientService;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId,
																		Authentication principal,
																		HttpServletRequest request) {
		Assert.hasText(clientRegistrationId, "clientRegistrationId cannot be empty");
		Assert.isTrue(isUnauthenticated(principal), "The user " + principal + " should not be authenticated");
		String unauthenticatedPrincipalName = principal != null ?
				principal.getName() : this.unauthenticatedPrincipalSupplier.get().getName();
		return (T) this.authorizedClientService.loadAuthorizedClient(clientRegistrationId, unauthenticatedPrincipalName);
	}

	@Override
	public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient,
										Authentication principal,
										HttpServletRequest request,
										HttpServletResponse response) {
		Assert.notNull(authorizedClient, "authorizedClient cannot be null");
		Assert.isTrue(isUnauthenticated(principal), "The user " + principal + " should not be authenticated");
		Authentication unauthenticatedPrincipal = principal != null ? principal : this.unauthenticatedPrincipalSupplier.get();
		this.authorizedClientService.saveAuthorizedClient(authorizedClient, unauthenticatedPrincipal);
	}

	@Override
	public void removeAuthorizedClient(String clientRegistrationId,
										Authentication principal,
										HttpServletRequest request,
										HttpServletResponse response) {
		Assert.hasText(clientRegistrationId, "clientRegistrationId cannot be empty");
		Assert.isTrue(isUnauthenticated(principal), "The user " + principal + " should not be authenticated");
		String unauthenticatedPrincipalName = principal != null ?
				principal.getName() : this.unauthenticatedPrincipalSupplier.get().getName();
		this.authorizedClientService.removeAuthorizedClient(clientRegistrationId, unauthenticatedPrincipalName);
	}

	public final void setUnauthenticatedPrincipalSupplier(Supplier<Authentication> unauthenticatedPrincipalSupplier) {
		Assert.notNull(unauthenticatedPrincipalSupplier, "unauthenticatedPrincipalSupplier cannot be null");
		this.unauthenticatedPrincipalSupplier = unauthenticatedPrincipalSupplier;
	}

	private boolean isUnauthenticated(Authentication authentication) {
		return authentication == null || this.authenticationTrustResolver.isAnonymous(authentication);
	}

	private static class UnauthenticatedPrincipalAuthentication implements Authentication {
		private final String name;

		private UnauthenticatedPrincipalAuthentication(String name) {
			this.name = name;
		}

		@Override
		public Collection<? extends GrantedAuthority> getAuthorities() {
			throw unsupported();
		}

		@Override
		public Object getCredentials() {
			throw unsupported();
		}

		@Override
		public Object getDetails() {
			throw unsupported();
		}

		@Override
		public Object getPrincipal() {
			throw unsupported();
		}

		@Override
		public boolean isAuthenticated() {
			return false;
		}

		@Override
		public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
			throw unsupported();
		}

		@Override
		public String getName() {
			return this.name;
		}

		private UnsupportedOperationException unsupported() {
			return new UnsupportedOperationException("Not Supported");
		}
	}
}