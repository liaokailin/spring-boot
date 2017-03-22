/*
 * Copyright 2012-2014 the original author or authors.
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

package org.springframework.boot.autoconfigure.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * {@link Condition} that checks for the presence or absence of
 * {@link WebApplicationContext}.
 *
 * @author Dave Syer
 * @see ConditionalOnWebApplication
 * @see ConditionalOnNotWebApplication
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
class OnWebApplicationCondition extends SpringBootCondition {

	private static final String WEB_CONTEXT_CLASS = "org.springframework.web.context."
			+ "support.GenericWebApplicationContext";

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context,
			AnnotatedTypeMetadata metadata) {
		boolean webApplicationRequired = metadata
				.isAnnotated(ConditionalOnWebApplication.class.getName());  //存在该注解表示必须要web环境
		ConditionOutcome webApplication = isWebApplication(context, metadata);

		if (webApplicationRequired && !webApplication.isMatch()) {
			return ConditionOutcome.noMatch(webApplication.getMessage());
		}

		if (!webApplicationRequired && webApplication.isMatch()) {
			return ConditionOutcome.noMatch(webApplication.getMessage());
		}

		return ConditionOutcome.match(webApplication.getMessage());
	}

	/**
	 * 判断是否为web应用
	 * @param context
	 * @param metadata
	 * @return
	 */
	private ConditionOutcome isWebApplication(ConditionContext context,
			AnnotatedTypeMetadata metadata) {

		if (!ClassUtils.isPresent(WEB_CONTEXT_CLASS, context.getClassLoader())) {  //判断指定类是否存在
			return ConditionOutcome.noMatch("web application classes not found");
		}

		if (context.getBeanFactory() != null) {
			String[] scopes = context.getBeanFactory().getRegisteredScopeNames();  //判断web特有的scope
			if (ObjectUtils.containsElement(scopes, "session")) {
				return ConditionOutcome.match("found web application 'session' scope");
			}
		}

		if (context.getEnvironment() instanceof StandardServletEnvironment) {  //看Environment
			return ConditionOutcome
					.match("found web application StandardServletEnvironment");
		}

		if (context.getResourceLoader() instanceof WebApplicationContext) {  //判断上下文
			return ConditionOutcome.match("found web application WebApplicationContext");
		}

		return ConditionOutcome.noMatch("not a web application");
	}

}
