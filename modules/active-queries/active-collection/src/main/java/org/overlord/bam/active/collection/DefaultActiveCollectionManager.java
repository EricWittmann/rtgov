/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-12, Red Hat Middleware LLC, and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.overlord.bam.active.collection;

import static javax.ejb.ConcurrencyManagementType.BEAN;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.Singleton;

/**
 * This class provides the default implementation of the ActiveCollectionManager
 * interface.
 *
 */
@Singleton(name="ActiveCollectionManager")
@ConcurrencyManagement(BEAN)
public class DefaultActiveCollectionManager implements ActiveCollectionManager {

    private java.util.Map<String, ActiveCollection> _activeCollections=
                new java.util.HashMap<String, ActiveCollection>();
    private java.util.Map<String, java.lang.ref.SoftReference<ActiveCollection>> _derivedActiveCollections=
                new java.util.HashMap<String, java.lang.ref.SoftReference<ActiveCollection>>();
    
    /**
     * {@inheritDoc}
     */
    public void register(ActiveCollectionSource acs) throws Exception {
        
        // Check whether active collection for name has already been created
        synchronized (_activeCollections) {
            if (_activeCollections.containsKey(acs.getName())) {
                throw new IllegalArgumentException("Active collection already exists for '"
                        +acs.getName()+"'");
            }
            
            if (acs.getType() == ActiveCollectionType.List) {
                ActiveList list=new ActiveList(acs.getName());
                
                _activeCollections.put(acs.getName(), list);
                
                acs.setActiveCollection(list);
                
                // Initialize the active collection source
                acs.init();
                
            } else {
                throw new IllegalArgumentException("Active collection type not currently supported");
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void unregister(ActiveCollectionSource acs) throws Exception {
        
        synchronized (_activeCollections) {
            if (!_activeCollections.containsKey(acs.getName())) {
                throw new IllegalArgumentException("Active collection '"
                        +acs.getName()+"' is not registered");
            }
            
            // Close the active collection source
            acs.close();
          
            acs.setActiveCollection(null);
            
            _activeCollections.remove(acs.getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    public ActiveCollection getActiveCollection(String name) {
        ActiveCollection ret=_activeCollections.get(name);
        
        if (ret == null) {
        	java.lang.ref.SoftReference<ActiveCollection> ref=_derivedActiveCollections.get(name);
            
            if (ref != null) {
                ret = ref.get();
            }
        }
        
        return (ret);
    }

    /**
     * {@inheritDoc}
     */
    public ActiveCollection create(String name, ActiveCollection parent,
                    Predicate predicate) {
        ActiveCollection ret=null;
        
        synchronized (_derivedActiveCollections) {
            // Check if collection already exists
        	java.lang.ref.SoftReference<ActiveCollection> ref=_derivedActiveCollections.get(name);
            
            if (ref != null) {
                ret = ref.get();
                
                if (ret == null) {                    
                    _derivedActiveCollections.remove(name);
                }
            }
            
            if (ret == null) {
                ret = parent.derive(name, predicate);
                
                _derivedActiveCollections.put(name, new java.lang.ref.SoftReference<ActiveCollection>(ret));
            }
        }
        
        return (ret);
    }

    /**
     * {@inheritDoc}
     */
    public void remove(String name) {
        synchronized (_derivedActiveCollections) {
        	_derivedActiveCollections.remove(name);
        }
    }

}
