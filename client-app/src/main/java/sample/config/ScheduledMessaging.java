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
package sample.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

/**
 * @author Joe Grandja
 */
@Configuration
@EnableScheduling
public class ScheduledMessaging {
	private final WebClient webClient;

	@Value("${oauth2.resource.messages-uri}")
	private String messagesUri;

	public ScheduledMessaging(@Qualifier("unauthenticatedWebClient") WebClient webClient) {
		this.webClient = webClient;
	}

	@Scheduled(fixedRate=30000)
	void generalMessages() {
		List generalMessages = this.webClient
				.get()
				.uri(this.messagesUri)
				.attributes(clientRegistrationId("general-messaging"))
				.retrieve()
				.bodyToMono(List.class)
				.block();

		System.out.println("***** General Messages *****");
		System.out.println(generalMessages);
	}
}