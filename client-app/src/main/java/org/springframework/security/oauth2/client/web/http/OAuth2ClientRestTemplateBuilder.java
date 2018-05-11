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

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Joe Grandja
 */
public class OAuth2ClientRestTemplateBuilder {
	private final ClientHttpRequestFactory defaultRequestFactory = new SimpleClientHttpRequestFactory();
	private ClientHttpRequestFactory requestFactory;
	private Set<HttpMessageConverter<?>> messageConverters;
	private List<? extends ClientHttpRequestInterceptor> interceptors;
	private ResponseErrorHandler errorHandler;
	private Map<String, Object> requestAttributes = new LinkedHashMap<>();
	private Map<String, Object> sharedAttributes = new LinkedHashMap<>();


	public OAuth2ClientRestTemplateBuilder requestFactory(ClientHttpRequestFactory requestFactory) {
		Assert.notNull(requestFactory, "requestFactory cannot be null");
		this.requestFactory = requestFactory;
		return this;
	}

	public OAuth2ClientRestTemplateBuilder messageConverters(HttpMessageConverter<?>... messageConverters) {
		Assert.notEmpty(messageConverters, "messageConverters cannot be empty");
		this.messageConverters = Arrays.stream(messageConverters).collect(Collectors.toSet());
		return this;
	}

	public OAuth2ClientRestTemplateBuilder interceptors(ClientHttpRequestInterceptor... interceptors) {
		Assert.notEmpty(interceptors, "interceptors cannot be empty");
		this.interceptors = Arrays.asList(interceptors);
		return this;
	}

	public OAuth2ClientRestTemplateBuilder errorHandler(ResponseErrorHandler errorHandler) {
		Assert.notNull(errorHandler, "errorHandler cannot be null");
		this.errorHandler = errorHandler;
		return this;
	}

	public OAuth2ClientRestTemplateBuilder requestAttribute(String name, Object value) {
		Assert.hasText(name, "name cannot be empty");
		Assert.notNull(value, "value cannot be null");
		this.requestAttributes.put(name, value);
		return this;
	}

	public OAuth2ClientRestTemplateBuilder sharedAttribute(String name, Object value) {
		Assert.hasText(name, "name cannot be empty");
		Assert.notNull(value, "value cannot be null");
		this.sharedAttributes.put(name, value);
		return this;
	}

	public RestTemplate build() {
		ClientHttpRequestFactory requestFactory =
				this.requestFactory != null ? this.requestFactory : this.defaultRequestFactory;
		List<? extends ClientHttpRequestInterceptor> interceptors =
				this.interceptors != null ? this.interceptors : Collections.emptyList();
		InterceptingClientHttpRequestAttributesFactory requestAttributesFactory =
				new InterceptingClientHttpRequestAttributesFactory(requestFactory, interceptors);
		requestAttributesFactory.setRequestAttributes(this.getRequestAttributes());
		this.requestAttributes.clear();		// Reset the per-request attributes

		RestTemplate restTemplate = new RestTemplate() {
			@Override
			public ClientHttpRequestFactory getRequestFactory() {
				return requestAttributesFactory;
			}
		};
		restTemplate.setRequestFactory(requestAttributesFactory);
		restTemplate.setInterceptors(new ArrayList<>(interceptors));

		if (!CollectionUtils.isEmpty(this.messageConverters)) {
			restTemplate.setMessageConverters(new ArrayList<>(this.messageConverters));
		}
		if (this.errorHandler != null) {
			restTemplate.setErrorHandler(this.errorHandler);
		}

		return restTemplate;
	}

	private Supplier<Map<String, Object>> getRequestAttributes() {
		Map<String, Object> requestAttributes = new LinkedHashMap<>();
		requestAttributes.putAll(this.requestAttributes);
		requestAttributes.putAll(this.sharedAttributes);
		return () -> requestAttributes;
	}
}