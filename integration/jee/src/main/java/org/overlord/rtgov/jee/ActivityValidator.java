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
package org.overlord.rtgov.jee;

import org.overlord.rtgov.activity.model.ActivityType;

/**
 * This interface represents the capability for validating
 * activity information from a JEE application.
 *
 */
public interface ActivityValidator {

    /**
     * This method validates the activity event.
     * 
     * @param actType The activity type
     * @throws Exception Failed to validate activity
     */
    public void validate(ActivityType actType) throws Exception;

}
