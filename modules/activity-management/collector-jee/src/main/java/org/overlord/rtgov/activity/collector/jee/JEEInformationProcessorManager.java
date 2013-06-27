/*
 * 2012-3 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.rtgov.activity.collector.jee;

import static javax.ejb.ConcurrencyManagementType.BEAN;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.Singleton;
import org.overlord.rtgov.activity.processor.AbstractInformationProcessorManager;
import org.overlord.rtgov.activity.processor.InformationProcessorManager;

/**
 * This class provides a JEE implementation of the information
 * processor manager interface.
 *
 */
@Singleton(name="InformationProcessorManager")
@ConcurrencyManagement(BEAN)
public class JEEInformationProcessorManager extends AbstractInformationProcessorManager
                        implements InformationProcessorManager {

}
