/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-13, Red Hat Middleware LLC, and others contributors as indicated
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
package org.overlord.rtgov.reports;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.overlord.rtgov.common.util.VersionUtil;
import org.overlord.rtgov.reports.model.Report;

/**
 * This class represents the default implementation of the ReportManager
 * interface.
 *
 */
public class AbstractReportManager implements ReportManager {
    
    private static final Logger LOG=Logger.getLogger(AbstractReportManager.class.getName());

    private java.util.Map<String,ReportDefinition> _reportDefinitionIndex=
            new java.util.HashMap<String,ReportDefinition>();
    private java.util.List<ReportDefinition> _reportDefinitions=
                new java.util.ArrayList<ReportDefinition>();
    
    @Inject
    private ReportContext _context=null;

    /**
     * This method initializes the report manager.
     */
    @PostConstruct
    protected void init() {
    }
    
    /**
     * This method sets the report context.
     * 
     * @param context The context
     */
    public void setContext(ReportContext context) {
        _context = context;
    }
    
    /**
     * This method returns the report context.
     * 
     * @return The report context
     */
    public ReportContext getContext() {
        return (_context);
    }
    
    /**
     * {@inheritDoc}
     */
    public void register(ReportDefinition rd) throws Exception {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Register: report definitoin name="
                        +rd.getName()+" version="
                        +rd.getVersion()+" definition="+rd);
        }
        
        // Initialize the report definition
        rd.init();
        
        synchronized (_reportDefinitionIndex) {
            boolean f_add=false;
            
            // Check if report definition for same name already exists
            ReportDefinition existing=_reportDefinitionIndex.get(rd.getName());
            
            if (existing != null) {
                
                // Check whether newer version
                if (VersionUtil.isNewerVersion(existing.getVersion(),
                        rd.getVersion())) {
                    
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Replace existing report definition version="
                                    +existing.getVersion());
                    }
                    
                    // Unregister old version
                    unregister(existing);
                    
                    // Add new version
                    f_add = true;                  
                } else {
                                      
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("Newer version '"+existing.getVersion()
                                +"' already registered");
                    }
                }
            } else {
                f_add = true;
            }
            
            if (f_add) {
                _reportDefinitionIndex.put(rd.getName(), rd);
                _reportDefinitions.add(rd);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void unregister(ReportDefinition rd) throws Exception {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Unregister: report manager name="
                        +rd.getName()+" version="
                        +rd.getVersion()+" definition="+rd);
        }
        
        synchronized (_reportDefinitionIndex) {
            
            if (_reportDefinitions.contains(rd)) {
                ReportDefinition removed=
                        _reportDefinitionIndex.remove(rd.getName());
                _reportDefinitions.remove(removed);
                
                removed.close();
                
            } else if (_reportDefinitionIndex.containsKey(rd.getName())) {
                
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Another version of report definition name="
                            +rd.getName()+" is currently registered: existing version ="
                            +_reportDefinitionIndex.get(rd.getName()).getVersion());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<ReportDefinition> getReportDefinitions() {
        return (java.util.Collections.unmodifiableList(_reportDefinitions));
    }

    /**
     * {@inheritDoc}
     */
    public Report generate(String reportName, Map<String, Object> params)
            throws Exception {
        
        ReportDefinition rd=_reportDefinitionIndex.get(reportName);
        
        if (rd != null && rd.getGenerator() != null) {
            return (rd.getGenerator().generate(_context, params));
        }
        
        return null;
    }

}
