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

/**
 * @author Joe Grandja
 */
public interface OAuth2ClientAttributeNames {

	String CLIENT_REGISTRATION_REPOSITORY = OAuth2ClientAttributeNames.class.getName() + ".CLIENT_REGISTRATION_REPOSITORY";

	String AUTHORIZED_CLIENT_SERVICE = OAuth2ClientAttributeNames.class.getName() + ".AUTHORIZED_CLIENT_SERVICE";

	String CLIENT_REGISTRATION_IDENTIFIER =  OAuth2ClientAttributeNames.class.getName() + ".CLIENT_REGISTRATION_IDENTIFIER";

	String RESOURCE_OWNER_PRINCIPAL  = OAuth2ClientAttributeNames.class.getName() + ".RESOURCE_OWNER_PRINCIPAL";

}