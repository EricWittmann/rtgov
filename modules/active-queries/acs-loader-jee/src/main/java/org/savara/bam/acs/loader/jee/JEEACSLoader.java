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
package org.savara.bam.acs.loader.jee;

import static javax.ejb.ConcurrencyManagementType.BEAN;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;

import org.savara.bam.active.collection.AbstractACSLoader;
import org.savara.bam.active.collection.ActiveCollectionManager;
import org.savara.bam.active.collection.ActiveCollectionSource;
import org.savara.bam.active.collection.util.ActiveCollectionUtil;

/**
 * This class provides the capability to load an Active Collection Source from a
 * defined file.
 *
 */
@ApplicationScoped
@Singleton
@Startup
@ConcurrencyManagement(BEAN)
public class JEEACSLoader extends AbstractACSLoader {
    
    private static final Logger LOG=Logger.getLogger(JEEACSLoader.class.getName());
    
    private static final String ACS_JSON = "acs.json";
    private static final String ACT_COLL_MANAGER = "java:global/overlord-bam/ActiveCollectionManager";

    private ActiveCollectionManager _acmManager=null;
    private ActiveCollectionSource _activeCollectionSource=null;
    
    /**
     * The constructor.
     */
    public JEEACSLoader() {
    }
    
    /**
     * This method initializes the ACS loader.
     */
    @PostConstruct
    public void init() {
        
        try {
            InitialContext ctx=new InitialContext();
            
            _acmManager = (ActiveCollectionManager)ctx.lookup(ACT_COLL_MANAGER);

            java.io.InputStream is=Thread.currentThread().getContextClassLoader().getResourceAsStream(ACS_JSON);
            
            if (is == null) {
                LOG.severe("Unable to locate '"+ACS_JSON+"'");
            } else {
                byte[] b=new byte[is.available()];
                is.read(b);
                is.close();
                
                _activeCollectionSource = ActiveCollectionUtil.deserialize(b);
                
                // Pre-initialize the source to avoid any contextual class
                // loading issues. Within JEE, the registration of the source
                // will be done in the context of the core war, while as the
                // source requires the classloading context associated
                // with the ActiveCollectionSource deployment.
                preInit(_activeCollectionSource);
                
                _acmManager.register(_activeCollectionSource);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load network", e);
        }
    }
    
    /**
     * This method closes the EPN loader.
     */
    @PreDestroy
    public void close() {
        
        if (_acmManager != null) {
            try {
                _acmManager.unregister(_activeCollectionSource);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to unregister active collection source", e);
            }
        }
    }       
}
