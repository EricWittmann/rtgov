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
package org.overlord.bam.performance.jee.app;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.overlord.bam.activity.model.soa.RequestReceived;
import org.overlord.bam.activity.model.soa.RequestSent;
import org.overlord.bam.activity.model.soa.ResponseReceived;
import org.overlord.bam.activity.model.soa.ResponseSent;
import org.overlord.bam.collector.ActivityCollector;

/**
 * REST app for creating activity events related to a virtual
 * business transaction.
 *
 */
@Path("/app")
@ApplicationScoped
public class JEEApp {

    private static final Logger LOG=Logger.getLogger(JEEApp.class.getName());
    
    private static final String ACTIVITY_COLLECTOR = "java:global/overlord-bam/ActivityCollector";

    private ActivityCollector _activityCollector=null;
    
    private long _firstTxn=0;
    private long _lastTxn=0;

    /**
     * This is the default constructor.
     */
    public JEEApp() {
        
        try {
            InitialContext ctx=new InitialContext();
            
            _activityCollector = (ActivityCollector)ctx.lookup(ACTIVITY_COLLECTOR);
            
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to initialize activity collector", e);
        }

    }
    
    /**
     * This method creates a new business transaction.
     * 
     * @param id The id
     * @return The response
     */
    @GET
    @Path("/create")
    @Produces("application/json")
    public Response create(@QueryParam("id") int id) {
    	    	
        if (_firstTxn == 0) {
        	_firstTxn = System.currentTimeMillis();
        }
    	
    	_activityCollector.startScope();
    	
        RequestReceived recvd=new RequestReceived();
        
        recvd.setServiceType("Order");                
        recvd.setOperation("buy");
        recvd.setContent(""+id);
        recvd.setMessageType("order");
        recvd.setMessageId("a"+id);
        
        _activityCollector.record(recvd);
        
        try {
        	synchronized (this) {
        		wait(50);
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        }

        RequestSent sent=new RequestSent();
        
        sent.setServiceType("CreditCheck");   
        sent.setOperation("check");
        sent.setContent(""+id);
        sent.setMessageType("creditCheck");
        sent.setMessageId("b"+id);
        
        _activityCollector.record(sent);
        
        try {
        	synchronized (this) {
        		wait(5);
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        }

        RequestReceived recvd2=new RequestReceived();
        
        recvd2.setServiceType("CreditCheck");   
        recvd2.setOperation("check");
        recvd2.setContent(""+id);
        recvd2.setMessageType("creditCheck");
        recvd2.setMessageId("b"+id);
        
        _activityCollector.record(recvd2);

        try {
        	synchronized (this) {
        		wait(50);
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        }

        ResponseSent sent2=new ResponseSent();
        
        sent2.setServiceType("CreditCheck");                
        sent2.setOperation("check");
        sent2.setContent(""+id);
        sent2.setMessageType("creditCheckResp");
        sent2.setMessageId("c"+id);
        sent2.setReplyToId("b"+id);
        
        _activityCollector.record(sent2);
        
        try {
        	synchronized (this) {
        		wait(5);
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        }

        ResponseReceived recvd3=new ResponseReceived();
        
        recvd3.setServiceType("CreditCheck");                
        recvd3.setOperation("check");
        recvd3.setContent(""+id);
        recvd3.setMessageType("creditCheckResp");
        recvd3.setMessageId("c"+id);
        recvd3.setReplyToId("b"+id);
        
        _activityCollector.record(recvd3);
        
        try {
        	synchronized (this) {
        		wait(50);
        	}
        } catch (Exception e) {
        	e.printStackTrace();
        }

        ResponseSent sent3=new ResponseSent();
        
        sent3.setServiceType("Order");                
        sent3.setOperation("buy");
        sent3.setContent(""+id);
        sent3.setMessageType("orderResp");
        sent3.setMessageId("d"+id);
        sent3.setReplyToId("a"+id);
        
        _activityCollector.record(sent3);
        
        _activityCollector.endScope();
        
        _lastTxn = System.currentTimeMillis();
        
    	return Response.status(200).entity("create txn id: " + id).build();
    }

    /**
     * This method returns the duration.
     * 
     * @return The duration
     */
    @GET
    @Path("/duration")
    @Produces("application/json")
    public long getDuration() {
    	if (LOG.isLoggable(Level.FINE)) {
    		LOG.fine("Duration="+(_lastTxn - _firstTxn));
    	}
    	return (_lastTxn - _firstTxn);
    }

}
