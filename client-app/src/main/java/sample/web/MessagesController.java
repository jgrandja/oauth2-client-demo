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
package sample.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.http.OAuth2ClientAttributeNames;
import org.springframework.security.oauth2.client.web.http.OAuth2ClientRestTemplateBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

/**
 * @author Joe Grandja
 */
@Controller
@RequestMapping("/messages")
public class MessagesController {

	@Value("${oauth2.resource.messages-uri}")
	private String messagesUri;

	@Autowired
	private OAuth2ClientRestTemplateBuilder oauth2ClientRestTemplateBuilder;

	@GetMapping
	public String getMessages(@RegisteredOAuth2AuthorizedClient("messaging") OAuth2AuthorizedClient authorizedClient,
								Authentication authentication, Model model) {

		List messages = WebClient.builder()
				.filter(oauth2Credentials(authorizedClient))
				.build()
				.get()
				.uri(this.messagesUri)
				.retrieve()
				.bodyToMono(List.class)
				.block();
		model.addAttribute("messages", messages);

		List<String> generalMessages = this.getGeneralMessages(authentication);
		model.addAttribute("generalMessages", generalMessages);

		return "message-list";
	}

	private List<String> getGeneralMessages(Authentication authentication) {
		String clientRegistrationId = "general-messaging";

		RestTemplate restTemplate = this.oauth2ClientRestTemplateBuilder
				.requestAttribute(OAuth2ClientAttributeNames.CLIENT_REGISTRATION_IDENTIFIER, clientRegistrationId)
				.requestAttribute(OAuth2ClientAttributeNames.RESOURCE_OWNER_PRINCIPAL, authentication)
				.build();

		RequestEntity<Void> request = RequestEntity.get(URI.create(this.messagesUri)).build();

		ParameterizedTypeReference<List<String>> responseType = new ParameterizedTypeReference<List<String>>() {};

		ResponseEntity<List<String>> response = restTemplate.exchange(request, responseType);

		return response.getBody();
	}

	private ExchangeFilterFunction oauth2Credentials(OAuth2AuthorizedClient authorizedClient) {
		return ExchangeFilterFunction.ofRequestProcessor(
				clientRequest -> {
					ClientRequest authorizedRequest = ClientRequest.from(clientRequest)
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + authorizedClient.getAccessToken().getTokenValue())
							.build();
					return Mono.just(authorizedRequest);
				});
	}
}