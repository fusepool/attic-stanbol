/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.whiteboard.fmeschbe.miltondav.impl.resources;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;

public class SlingResourceFactory implements ResourceFactory {
		 
	final public static String NAME = "org.apache.sling.whiteboard.fmeschbe.miltondav.impl.resources.SlingResourceFactory"; //SlingResourceFactory.class.getName();
	
		public Resource getResource(String host, String strPath) {
			Path path = Path.path(strPath);
			//STRIP PRECEEDING PATH
			//TODO make this depend on what the dav servlet is actually configured to
			path = path.getStripFirst();
			if (path.isRoot()) {
				return new RootResource();
			} else if (path.getFirst().equals(SlingResource.getFilename())) {
				return new SlingResource();
			}
	 
			return null;
		}
	 
	}