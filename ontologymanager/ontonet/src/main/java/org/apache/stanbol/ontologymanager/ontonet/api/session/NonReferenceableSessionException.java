/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.stanbol.ontologymanager.ontonet.api.session;

/**
 * Thrown whenever an attempt to access a KReS session that is bound for removal
 * is detected.
 * 
 * @author alessandro
 * 
 */
public class NonReferenceableSessionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1642512088759774124L;

	public NonReferenceableSessionException() {

	}

	public NonReferenceableSessionException(String message) {
		super(message);
	}

	public NonReferenceableSessionException(Throwable cause) {
		initCause(cause);
	}

}