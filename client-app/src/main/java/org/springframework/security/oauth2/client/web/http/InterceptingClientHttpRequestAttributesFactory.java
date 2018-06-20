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

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.AbstractClientHttpRequestFactoryWrapper;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Joe Grandja
 */
class InterceptingClientHttpRequestAttributesFactory
		extends AbstractClientHttpRequestFactoryWrapper {

	private final List<? extends ClientHttpRequestInterceptor> interceptors;

	private Supplier<Map<String, Object>> requestAttributes;

	InterceptingClientHttpRequestAttributesFactory(
			ClientHttpRequestFactory requestFactory,
			List<? extends ClientHttpRequestInterceptor> interceptors) {
		super(requestFactory);
		this.interceptors = interceptors;
	}

	void setRequestAttributes(Supplier<Map<String, Object>> requestAttributes) {
		this.requestAttributes = requestAttributes;
	}

	@Override
	protected ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod,
			ClientHttpRequestFactory requestFactory) throws IOException {
		return new DefaultClientHttpRequestAttributes(
				requestFactory.createRequest(uri, httpMethod),
				this.requestAttributes.get(),
				new InterceptingClientHttpRequestExecution(this.interceptors));
	}

	private class InterceptingClientHttpRequestExecution
			implements ClientHttpRequestExecution {

		private final Iterator<? extends ClientHttpRequestInterceptor> interceptorsIterator;

		private InterceptingClientHttpRequestExecution(
				List<? extends ClientHttpRequestInterceptor> interceptors) {
			this.interceptorsIterator = interceptors.iterator();
		}

		@Override
		public ClientHttpResponse execute(HttpRequest request, byte[] body)
				throws IOException {
			if (this.interceptorsIterator.hasNext()) {
				ClientHttpRequestInterceptor nextInterceptor = this.interceptorsIterator
						.next();
				return nextInterceptor.intercept(request, body, this);
			}
			return ((DefaultClientHttpRequestAttributes) request).getWrappedRequest()
					.execute();
		}

	}

}