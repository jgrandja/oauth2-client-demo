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
package sample.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.http.OAuth2BearerTokenAuthorizationInterceptor;
import org.springframework.security.oauth2.client.web.http.OAuth2ClientAttributeNames;
import org.springframework.security.oauth2.client.web.http.OAuth2ClientCredentialsGrantInterceptor;
import org.springframework.security.oauth2.client.web.http.OAuth2ClientRestTemplateBuilder;

/**
 * @author Joe Grandja
 */
@Configuration
public class OAuth2ClientConfig {

	@Autowired
	private ClientRegistrationRepository clientRegistrationRepository;

	@Autowired
	private OAuth2AuthorizedClientService authorizedClientService;

	@Bean
	public OAuth2ClientRestTemplateBuilder oauth2ClientRestTemplateBuilder() {
		OAuth2ClientRestTemplateBuilder builder = new OAuth2ClientRestTemplateBuilder();

		builder.sharedAttribute(OAuth2ClientAttributeNames.CLIENT_REGISTRATION_REPOSITORY,
				this.clientRegistrationRepository);
		builder.sharedAttribute(OAuth2ClientAttributeNames.AUTHORIZED_CLIENT_SERVICE,
				this.authorizedClientService);
		builder.interceptors(new OAuth2ClientCredentialsGrantInterceptor(),
				new OAuth2BearerTokenAuthorizationInterceptor());

		return builder;
	}

}