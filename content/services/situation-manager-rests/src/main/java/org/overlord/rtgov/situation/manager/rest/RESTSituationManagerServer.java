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
package org.overlord.rtgov.situation.manager.rest;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.overlord.rtgov.analytics.situation.IgnoreSubject;
import org.overlord.rtgov.analytics.util.SituationUtil;
import org.overlord.rtgov.situation.manager.SituationManager;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.naming.InitialContext;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

/**
 * This class represents the RESTful interface to the situation manager server.
 *
 */
@Path("/situation/manager")
@ApplicationScoped
public class RESTSituationManagerServer {

    private static final Logger LOG=Logger.getLogger(RESTSituationManagerServer.class.getName());
    
    private SituationManager _situationManager=null;
    
    /**
     * This is the default constructor.
     */
    @SuppressWarnings("unchecked")
    public RESTSituationManagerServer() {
        
        try {
            // Need to obtain situation manager directly, as inject does not
            // work for REST service, and RESTeasy/CDI integration did not
            // appear to work in AS7. Directly accessing the bean manager
            // should be portable.
            BeanManager bm=InitialContext.doLookup("java:comp/BeanManager");
            
            java.util.Set<Bean<?>> beans=bm.getBeans(SituationManager.class);
            
            for (Bean<?> b : beans) {                
                CreationalContext<Object> cc=new CreationalContext<Object>() {
                    public void push(Object arg0) {
                    }
                    public void release() {
                    }                   
                };
                
                _situationManager = (SituationManager)((Bean<Object>)b).create(cc);
                
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Situation manager="+_situationManager+" for bean="+b);
                }
                
                if (_situationManager != null) {
                    break;
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, java.util.PropertyResourceBundle.getBundle(
                    "situation-manager-rests.Messages").getString("SITUATION-MANAGER-RESTS-1"), e);
        }
    }
    
    /**
     * This method sets the situation manager.
     * 
     * @param sm The situation manager
     */
    public void setSituationManager(SituationManager sm) {
        LOG.info("Set Situation Manager="+sm);
        _situationManager = sm;
    }
    
    /**
     * This method ignores a situation subject.
     * 
     * @param details The ignore subject details
     * @param context The security context
     * @return A response indicating success or failure
     * @throws Exception Failed to ignore the subject
     */
    @POST
    @Path("/ignore")
    public Response ignore(String details, @Context SecurityContext context) throws Exception {
 
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Ignore situation: "+details);        
        }
        
        IgnoreSubject ignore=SituationUtil.deserializeIgnoreSubject(details.getBytes());       
        
        // Override the principal and timestamp
        if (context.getUserPrincipal() != null) {
            ignore.setPrincipal(context.getUserPrincipal().getName());
        }
        ignore.setTimestamp(System.currentTimeMillis());
        
        try {
            _situationManager.ignore(ignore);
            
            return Response.status(Status.OK).entity("Subject ignored").build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to ignore subject: "+e).build();
        }
    }
    
    /**
     * This method observes a situation subject, which means
     * stop ignoring it.
     * 
     * @param subject The subject
     * @param context The security context
     * @return A response indicating success or failure
     * @throws Exception Failed to observe the subject
     */
    @POST
    @Path("/observe")
    public Response observe(String subject, @Context SecurityContext context) throws Exception {
 
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Observe subject: "+subject);        
        }
        
        // Obtain the principal
        String principal=null;
        
        if (context.getUserPrincipal() != null) {
            principal = context.getUserPrincipal().getName();
        }
        
        try {
            _situationManager.observe(subject, principal);
            
            return Response.status(Status.OK).entity("Subject observed").build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to observe subject: "+e).build();
        }
    }
}
