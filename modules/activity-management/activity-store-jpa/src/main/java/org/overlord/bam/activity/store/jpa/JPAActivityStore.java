/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-11, Red Hat Middleware LLC, and others contributors as indicated
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
package org.overlord.rtgov.activity.store.jpa;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.overlord.rtgov.activity.model.ActivityType;
import org.overlord.rtgov.activity.model.ActivityUnit;
import org.overlord.rtgov.activity.server.ActivityStore;
import org.overlord.rtgov.activity.server.QuerySpec;
import org.overlord.rtgov.activity.util.ActivityUtil;
import org.overlord.rtgov.common.util.RTGovPropertiesProvider;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.TransactionManager;

/**
 * This class provides the JPA implementation of the Activity Store.
 *
 */
@Singleton
public class JPAActivityStore implements ActivityStore {

    private static final String TRANSACTION_LOCAL = "transaction.local";
    private static final String TRANSACTION_MANAGER_PROPERTY = "transaction.manager";
    private static final String ENTITY_MANAGER_PROPERTY = "entity.manager";
    private static final String DEFAULT_TRANSACTION_MANAGER = "java:jboss/TransactionManager";

    private static final String EMF_NAME = "overlord-rtgov-activity";

    private static final Logger LOG=Logger.getLogger(JPAActivityStore.class.getName());
    
    private EntityManager _entityManager=null;
    private EntityManagerFactory _emf=null;
    private String _entityManagerName=null;
    
    private javax.transaction.TransactionManager _transactionManager=null;
    
    @Inject
    private RTGovPropertiesProvider _properties=null;
    
    /**
     * This is the default constructor for the JPA activity store.
     */
    public JPAActivityStore() {
    }
    
    /**
     * This method returns the entity manager name.
     * 
     * @return The entity manager name
     */
    public String getEntityManagerName() {
        if (_entityManagerName != null) {
            return (_entityManagerName);
        }
        if (_properties != null && _properties.getProperties().containsKey(ENTITY_MANAGER_PROPERTY)) {
            return ((String)_properties.getProperties().get(ENTITY_MANAGER_PROPERTY));
        }
        return (EMF_NAME);
    }
    
    /**
     * This method sets the entity manager name.
     * 
     * @param name The entity manager name
     */
    public void setEntityManagerName(String name) {
        _entityManagerName = name;
    }
    
    /**
     * This method initializes the activity store.
     * 
     */
    @PostConstruct
    protected synchronized void init() {
        
        if (_emf == null) {
            try {
                // BAM-120 Use separate thread as causes problem when hibernate creates
                // the schema within a transaction scope
                Executors.newSingleThreadExecutor().submit(new Runnable() {
                    public void run() {                    
                        java.util.Properties props=null;
                        
                        try {
                            if (_properties != null) {
                                props = _properties.getProperties();
                            }
                            
                            if (LOG.isLoggable(Level.FINE)) {
                                LOG.fine("Properties passed to entity manager factory creation: "+props);                
                            }
                            
                            _emf = Persistence.createEntityManagerFactory(getEntityManagerName(), props);
                    
                        } catch (Throwable e) {
                            LOG.log(Level.SEVERE, "Failed to create entity manager factory", e);
                        }
                    }
                }).get(5000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to initialize entity manager factory using executor", e);
            }
        }
            
        if (_entityManager == null) {
            try {
                _entityManager = _emf.createEntityManager();
                
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("BAM EntityManager '"+getEntityManagerName()+"' created");                
                }
            } catch (Throwable e) {
                LOG.log(Level.SEVERE, "Failed to create entity manager '"+getEntityManagerName()+"'", e);
            }
        }
            
        if (_transactionManager == null) {
            try {
                InitialContext ctx=new InitialContext();
                
                String txnMgr=DEFAULT_TRANSACTION_MANAGER;
                        
                if (_properties != null && _properties.getProperties().containsKey(TRANSACTION_MANAGER_PROPERTY)) {
                    txnMgr = _properties.getProperty(TRANSACTION_MANAGER_PROPERTY);
                }
                
                _transactionManager = (TransactionManager)ctx.lookup(txnMgr);
                
            } catch (Exception e) {
                LOG.log(Level.FINE, "Unable to obtain transaction manager", e);
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void store(List<ActivityUnit> activities) throws Exception {
        
        if (_entityManager == null) {
            init();
        }
        
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Store="+new String(ActivityUtil.serializeActivityUnitList(activities)));
        }
        
        boolean localtxn=false;
        
        if ((_transactionManager == null && !_entityManager.getTransaction().isActive())
                || (_properties != null && _properties.getProperties().containsKey(TRANSACTION_LOCAL)
                && _properties.getProperties().get(TRANSACTION_LOCAL).equals("true"))) {
            
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.finest("Beginning a local transaction");
            }
           
            _entityManager.getTransaction().begin();
            
            localtxn = true;
        }
        
        for (ActivityUnit au : activities) {
            _entityManager.persist(au);
        }
        
        if (localtxn) {
            _entityManager.flush();
            
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.finest("Committing a local transaction");
            }
            _entityManager.getTransaction().commit();
        }
    }

    /**
     * {@inheritDoc}
     */
    public ActivityUnit getActivityUnit(String id) throws Exception {  
        
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Get Activity Unit="+id);
        }

        if (_entityManager == null) {
            init();
        }

        ActivityUnit ret=(ActivityUnit)
                _entityManager.createQuery("SELECT au FROM ActivityUnit au "
                        +"WHERE au.id = '"+id+"'")
                .getSingleResult();
        
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("ActivityUnit id="+id+" Result="
                    +new String(ActivityUtil.serializeActivityUnit(ret)));
        }

        return (ret);
    }

    /**
     * {@inheritDoc}
     */
    public List<ActivityType> query(QuerySpec query) throws Exception {  
        
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Query="+query);
        }

        if (_entityManager == null) {
            init();
        }

        return (query(query.getExpression()));
    }

    /**
     * {@inheritDoc}
     */
    public List<ActivityType> getActivityTypes(String context) throws Exception {
        
        if (_entityManager == null) {
            init();
        }

        @SuppressWarnings("unchecked")
        List<ActivityType> ret=(List<ActivityType>)
                _entityManager.createQuery("SELECT at from ActivityType at "
                        +"JOIN at.context ctx "
                        +"WHERE ctx.value = '"+context+"'")
                .getResultList();
        
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("ActivityTypes context '"+context+"' Result="
                        +new String(ActivityUtil.serializeActivityTypeList(ret)));
        }

        return (ret);
    }
    
    /**
     * This method performs the query associated with the supplied
     * query expression, returning the results as a list of activity
     * types.
     * 
     * @param query The query expression
     * @return The list of activity types
     * @throws Exception Failed to perform query
     */
    public List<ActivityType> query(String query) throws Exception {
        
        if (_entityManager == null) {
            init();
        }

        @SuppressWarnings("unchecked")
        List<ActivityType> ret=(List<ActivityType>)
                _entityManager.createQuery(query)
                .getResultList();
        
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Query="+query+" Result="
                    +new String(ActivityUtil.serializeActivityTypeList(ret)));
        }

        return (ret);
    }
    
    /**
     * This method removes the supplied activity unit.
     * 
     * @param au The activity unit
     * @throws Exception Failed to remove activity unit
     */
    public void remove(ActivityUnit au) throws Exception {
        if (_entityManager == null) {
            init();
        }

        _entityManager.remove(au);
    }
    
    /**
     * This method closes the entity manager.
     */
    @PreDestroy
    public void close() {
        if (_entityManager != null) {
            _entityManager.close();
        }
        if (_emf != null) {
            _emf.close();
        }
    }
}
