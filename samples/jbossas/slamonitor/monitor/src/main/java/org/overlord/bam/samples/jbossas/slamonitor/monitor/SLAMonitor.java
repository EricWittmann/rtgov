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
package org.overlord.rtgov.samples.jbossas.slamonitor.monitor;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.overlord.rtgov.active.collection.ActiveCollectionManager;
import org.overlord.rtgov.active.collection.ActiveList;
import org.overlord.rtgov.active.collection.predicate.MVEL;
import org.overlord.rtgov.active.collection.predicate.Predicate;
import org.overlord.rtgov.analytics.Situation;
import org.overlord.rtgov.analytics.service.ResponseTime;

/**
 * This is the custom event monitor that receives node notifications
 * from the EPN, and makes the events available via a REST API.
 *
 */
@Path("/monitor")
@ApplicationScoped
public class SLAMonitor {

    private static final String SERVICE_RESPONSE_TIMES = "ServiceResponseTimes";
    private static final String SITUATIONS = "Situations";

    private static final Logger LOG=Logger.getLogger(SLAMonitor.class.getName());
    
    private static final String ACM_MANAGER = "java:global/overlord-rtgov/ActiveCollectionManager";

    private ActiveCollectionManager _acmManager=null;
    private ActiveList _serviceResponseTime=null;
    private ActiveList _situations=null;
    
    /**
     * This is the default constructor.
     */
    public SLAMonitor() {
        
        try {
            InitialContext ctx=new InitialContext();
            
            _acmManager = (ActiveCollectionManager)ctx.lookup(ACM_MANAGER);

            _serviceResponseTime = (ActiveList)
                    _acmManager.getActiveCollection(SERVICE_RESPONSE_TIMES);
        
            _situations = (ActiveList)
                    _acmManager.getActiveCollection(SITUATIONS);
        
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to initialize active collection manager", e);
        }

    }
    
    /**
     * This method returns the list of response times.
     * 
     * @param serviceType The optional service type
     * @param operation The optional operation
     * @param fault The optional fault
     * @return The response times
     */
    @GET
    @Path("/responseTimes")
    @Produces("application/json")
    public java.util.List<ResponseTime> getResponseTimes(
    					@QueryParam("serviceType") String serviceType,
    					@QueryParam("operation") String operation,
    					@QueryParam("fault") String fault) {
        java.util.List<ResponseTime> ret=new java.util.ArrayList<ResponseTime>();

        ActiveList list=getResponseTimeList(serviceType, operation, fault);
        
        for (Object obj : list) {
            if (obj instanceof ResponseTime) {
                ret.add((ResponseTime)obj);
            }
        }
        
        return (ret);
    }

    /**
     * This method returns the active list for the response times
     * associated with the supplied query parameters.
     * 
     * @param serviceType The optional service type
     * @param operation The optional operation
     * @param fault The optional fault
     * @return The active list of response times
     */
    protected ActiveList getResponseTimeList(String serviceType, String operation, String fault) {
    	ActiveList ret=_serviceResponseTime;
    	
    	if (LOG.isLoggable(Level.FINE)) {
    	    LOG.fine("Get Response Time List: serviceType="+serviceType+" operation="
    	            +operation+" fault="+fault);
    	}
    	
    	if (serviceType != null || operation != null || fault != null) {
        	String alname="RespTime:"+serviceType+":"+operation+":"+fault;

        	ret = (ActiveList)_acmManager.getActiveCollection(alname);
        	
        	if (ret == null) {
        	    String expr=expressionBuilder(null, "serviceType", serviceType);
                expr = expressionBuilder(expr, "operation", operation);
                expr = expressionBuilder(expr, "fault", fault);
        	    
        		Predicate predicate=new MVEL(expr);        		
        		
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Create derived collection for: serviceType="+serviceType+" operation="
                            +operation+" fault="+fault);
                }
                
        		ret = (ActiveList)_acmManager.create(alname, _serviceResponseTime, predicate);
        	}
    	}
    	
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Returning: serviceType="+serviceType+" operation="
                    +operation+" fault="+fault+" ret="+ret);
        }
        
    	return (ret);
    }
    
    /**
     * This method builds an expression based on the supplied property and value.
     * 
     * @param expr The initial expression
     * @param prop The property being built
     * @param value The optional property value
     * @return The new expression, taking into account the supplied property information if relevant
     */
    protected static String expressionBuilder(String expr, String prop, String value) {
        if (value != null) {
            String subexpr=prop+" == \""+value+"\"";
            if (expr == null) {
                expr = subexpr;
            } else {
                expr += " && "+subexpr;
            }
        }
        
        return (expr);
    }
    
    /**
     * This method returns the list of situations.
     * 
     * @return The situations
     */
    @GET
    @Path("/situations")
    @Produces("application/json")
    public java.util.List<Situation> getSituations() {
        java.util.List<Situation> ret=new java.util.ArrayList<Situation>();

        for (Object obj : _situations) {
            if (obj instanceof Situation) {
                ret.add((Situation)obj);
            }
        }
        
        return (ret);
    }
    
}
