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
package org.overlord.rtgov.active.collection.infinispan;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.overlord.rtgov.active.collection.ActiveCollection;
import org.overlord.rtgov.active.collection.ActiveCollectionFactory;
import org.overlord.rtgov.active.collection.ActiveCollectionSource;
import org.overlord.rtgov.active.collection.ActiveCollectionType;
import org.overlord.rtgov.common.infinispan.InfinispanManager;


/**
 * This class provides the infinispan implementation of the ActiveCollectionFactory.
 *
 */
public class InfinispanActiveCollectionFactory extends ActiveCollectionFactory {
    
    private static final Logger LOG=Logger.getLogger(InfinispanActiveCollectionFactory.class.getName());

    private String _cache=null;
    private String _container=null;
    
    /**
     * This method sets the JNDI name for the container resource.
     * 
     * @param jndiName The JNDI name for the container resource
     */
    public void setContainer(String jndiName) {
        _container = jndiName;
    }
    
    /**
     * This method returns the JNDI name used to obtain
     * the container resource.
     * 
     * @return The JNDI name for the container resource
     */
    public String getContainer() {
        return (_container);
    }
    
    /**
     * This method returns the cache name.
     * 
     * @return The cache name
     */
    public String getCache() {
        return (_cache);
    }
    
    /**
     * This method sets the cache name.
     * 
     * @param cache The cache name
     */
    public void setCache(String cache) {
        _cache = cache;
    }
    
    /**
     * {@inheritDoc}
     */
    public ActiveCollection createActiveCollection(ActiveCollectionSource acs) {
        ActiveCollection ret=null;
        
        if (acs.getType() == ActiveCollectionType.Map) {
            
            // Obtain the infinspan cache
            org.infinispan.manager.CacheContainer cacheContainer=InfinispanManager.getCacheContainer(_container);
                
            if (cacheContainer != null) {
                java.util.Map<Object,Object> ac=cacheContainer.getCache(_cache);
                                
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Infinispan cache [container="+_container+" name="+_cache+"] = "+ac);
                }
 
                ret = new InfinispanActiveMap(acs, ac);
            }
            
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Infinispan ActiveMap = "+ret);
        }

        return (ret);
    }
    
}
