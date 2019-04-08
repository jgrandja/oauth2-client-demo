/*
 * Copyright 2002-2019 the original author or authors.
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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

/**
 * @author Joe Grandja
 */
@Controller
@RequestMapping("/messages")
public class MessagesController {
	private final WebClient webClient;

	@Value("${oauth2.resource.messages-uri}")
	private String messagesUri;

	public MessagesController(WebClient webClient) {
		this.webClient = webClient;
	}

	@GetMapping
	public String getMessages(Model model, @RegisteredOAuth2AuthorizedClient("messaging") OAuth2AuthorizedClient authorizedClient) {
		List messages = this.webClient
				.get()
				.uri(this.messagesUri)
				.attributes(oauth2AuthorizedClient(authorizedClient))
				.retrieve()
				.bodyToMono(List.class)
				.block();
		model.addAttribute("messages", messages);
		model.addAttribute("generalMessages", getGeneralMessages());

		return "message-list";
	}

	private List<String> getGeneralMessages() {
		List generalMessages = this.webClient
				.get()
				.uri(this.messagesUri)
				.attributes(clientRegistrationId("general-messaging"))
				.retrieve()
				.bodyToMono(List.class)
				.block();

		return generalMessages;
	}
}