/*
 * Copyright 2012-2015 the original author or authors.
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

package com.gwidgets.gwtprojectgenerator.metadata

/**
 * Defines a particular project type. Each type is associated to a concrete
 * action that should be invoked to generate the content of that type.
 *
 * @author Stephane Nicoll
 * @since 1.0
 */
class Type extends DefaultMetadataElement {

	String description

	@Deprecated
	String stsId

	String action

	final Map<String, String> tags = [:]

	void setAction(String action) {
		String actionToUse = action
		if (!actionToUse.startsWith("/")) {
			actionToUse =  "/" +  actionToUse
		}
		this.action = actionToUse
	}

}
