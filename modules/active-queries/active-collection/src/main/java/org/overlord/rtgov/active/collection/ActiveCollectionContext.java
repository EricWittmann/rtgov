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
package org.overlord.rtgov.active.collection;


/**
 * This interface represents the context used by the active collections,
 * e.g. to help predicates evaluate objects being applied to the active collections.
 *
 */
public interface ActiveCollectionContext {

    /**
     * This method returns the named active list.
     * 
     * @param name The name of the active list
     * @return The active list, or null if not found
     */
    public ActiveList getList(String name);
    
    /**
     * This method returns the named active map.
     * 
     * @param name The name of the active map
     * @return The active map, or null if not found
     */
    public ActiveMap getMap(String name);
    
}
