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

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Joe Grandja
 */
class DefaultClientHttpRequestAttributes implements ClientHttpRequest, ClientHttpRequestAttributes {
	private final ClientHttpRequest wrappedRequest;
	private final Map<String, Object> requestAttributes;
	private final ClientHttpRequestExecution requestExecution;

	DefaultClientHttpRequestAttributes(ClientHttpRequest request,
										Map<String, Object> requestAttributes,
										ClientHttpRequestExecution requestExecution) {
		this.wrappedRequest = request;
		this.requestAttributes = new HashMap<>(requestAttributes);
		this.requestExecution = requestExecution;
	}

	@Override
	public ClientHttpResponse execute() throws IOException {
		return this.requestExecution.execute(this, new byte[1024]);
	}

	@Override
	public OutputStream getBody() throws IOException {
		return this.wrappedRequest.getBody();
	}

	@Override
	public HttpMethod getMethod() {
		return this.wrappedRequest.getMethod();
	}

	@Override
	public String getMethodValue() {
		return this.wrappedRequest.getMethodValue();
	}

	@Override
	public URI getURI() {
		return this.wrappedRequest.getURI();
	}

	@Override
	public HttpHeaders getHeaders() {
		return this.wrappedRequest.getHeaders();
	}

	@Override
	public Object getAttribute(String name) {
		return this.requestAttributes.get(name);
	}

	@Override
	public void setAttribute(String name, Object value) {
	}

	@Override
	public void removeAttribute(String name) {
	}

	@Override
	public String[] getAttributeNames() {
		return this.requestAttributes.keySet().toArray(new String[0]);
	}

	ClientHttpRequest getWrappedRequest() {
		return this.wrappedRequest;
	}
}