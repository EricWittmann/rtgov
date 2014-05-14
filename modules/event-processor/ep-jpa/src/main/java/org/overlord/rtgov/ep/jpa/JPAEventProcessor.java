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
package org.overlord.rtgov.ep.jpa;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.Session;
import org.overlord.rtgov.common.jpa.JpaStore;
import org.overlord.rtgov.common.jpa.JpaStore.JpaWork;
import org.overlord.rtgov.ep.EventProcessor;

/**
 * This class represents the JPA implementation of the Event Processor.
 * 
 */
public class JPAEventProcessor extends EventProcessor {

    private static final String DEFAULT_HIBERNATE_CFG_XML = "hibernate.cfg.xml";

    private static final Logger LOG = Logger.getLogger(JPAEventProcessor.class.getName());

    private static final String JNDI_PROPERTY = "JPAEventProcessor.jndi.datasource";

    private JpaStore _jpaStore;

    @Deprecated
    private String _persistenceUnit;
    
    private String _configuration;

    /**
     * Constructor.
     */
    public JPAEventProcessor() {
    }

    /**
     * Constructor.
     * 
     * @param jpaStore Explicit JpaStore to use
     */
    public JPAEventProcessor(JpaStore jpaStore) {
        _jpaStore = jpaStore;
    }

    /**
     * {@inheritDoc}
     */
    public void init() throws Exception {
        super.init();
        
        String config=_configuration;
        
        if (config == null || config.trim().length() == 0) {
            config = DEFAULT_HIBERNATE_CFG_XML;
        }
        
        final URL configXml = Thread.currentThread().getContextClassLoader().getResource(config);
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Hibernate configure: "+configXml);
        }
        
        _jpaStore = new JpaStore(configXml, JNDI_PROPERTY);
    }
    
    /**
     * This method returns the JPA Store.
     * 
     * @return The JPA store
     */
    protected JpaStore getJpaStore() {
        return (_jpaStore);
    }
    
    /**
     * @return The optional configuration file name
     */
    public String getConfiguration() {
        return _configuration;
    }

    /**
     * @param config The optional configuration file name
     */
    public void setConfiguration(String config) {
        _configuration = config;
    }

    /**
     * @return The persistence unit name
     * 
     * @deprecated with no replacement
     */
    @Deprecated
    public String getEntityManager() {
        LOG.warning("JPAEventProcessor now uses native Hibernate ORM.  Include a hibernate.cfg.xml file in your "
                + "src/main/resources.  {@link #JPAEventProcessor()} will automatically find it.");
        return _persistenceUnit;
    }

    /**
     * @param persistenceUnit
     *            The persistence unit name
     * 
     * @deprecated JPAEventProcessor now uses native Hibernate ORM. Include a
     *             hibernate.cfg.xml file in your src/main/resources.
     *             {@link #JPAEventProcessor()} will automatically find it.
     */
    @Deprecated
    public void setEntityManager(String persistenceUnit) {
        LOG.warning("JPAEventProcessor now uses native Hibernate ORM.  Include a hibernate.cfg.xml file in your "
                + "src/main/resources.  {@link #JPAEventProcessor()} will automatically find it.");
        _persistenceUnit = persistenceUnit;
        _jpaStore = new JpaStore(persistenceUnit, JNDI_PROPERTY);
    }

    /**
     * {@inheritDoc}
     */
    public java.io.Serializable process(String source, final java.io.Serializable event, int retriesLeft)
            throws Exception {
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Process event '" + event + " from source '" + source + "' on JPA Event Processor");
        }

        _jpaStore.withJpa(new JpaWork<Void>() {
            public Void perform(Session s) {
                s.persist(event);
                return null;
            }
        });

        return null;
    }
}
