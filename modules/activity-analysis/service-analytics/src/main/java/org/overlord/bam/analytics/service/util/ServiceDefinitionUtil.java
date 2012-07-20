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
package org.overlord.bam.analytics.service.util;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.overlord.bam.activity.model.ActivityType;
import org.overlord.bam.activity.model.ActivityUnit;
import org.overlord.bam.activity.model.soa.RequestReceived;
import org.overlord.bam.activity.model.soa.RequestSent;
import org.overlord.bam.activity.model.soa.ResponseReceived;
import org.overlord.bam.activity.model.soa.ResponseSent;
import org.overlord.bam.analytics.service.InvocationDefinition;
import org.overlord.bam.analytics.service.InvocationMetric;
import org.overlord.bam.analytics.service.MEPDefinition;
import org.overlord.bam.analytics.service.OperationDefinition;
import org.overlord.bam.analytics.service.RequestFaultDefinition;
import org.overlord.bam.analytics.service.RequestResponseDefinition;
import org.overlord.bam.analytics.service.ServiceDefinition;

/**
 * This class provides utility functions related to the service
 * definition.
 *
 */
public final class ServiceDefinitionUtil {

    private static final ObjectMapper MAPPER=new ObjectMapper();

    static {
        SerializationConfig config=MAPPER.getSerializationConfig()
                .withSerializationInclusion(JsonSerialize.Inclusion.NON_NULL)
                .withSerializationInclusion(JsonSerialize.Inclusion.NON_DEFAULT);
        
        DeserializationConfig config2=MAPPER.getDeserializationConfig()
                .without(Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        
        MAPPER.setSerializationConfig(config);
        MAPPER.setDeserializationConfig(config2);
    }
    
    /**
     * The default constructor.
     */
    private ServiceDefinitionUtil() {
    }

    /**
     * This method serializes a Service Definition into a JSON representation.
     * 
     * @param sdef The service definition
     * @return The JSON serialized representation
     * @throws Exception Failed to serialize
     */
    public static byte[] serializeServiceDefinition(ServiceDefinition sdef) throws Exception {
        byte[] ret=null;
        
        java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
        
        MAPPER.writeValue(baos, sdef);
        
        ret = baos.toByteArray();
        
        baos.close();
        
        return (ret);
    }

    /**
     * This method deserializes a Service Definition from a JSON representation.
     * 
     * @param sdef The JSON representation of the service definition
     * @return The service definition
     * @throws Exception Failed to deserialize
     */
    public static ServiceDefinition deserializeServiceDefinition(byte[] sdef) throws Exception {
        ServiceDefinition ret=null;
        
        java.io.ByteArrayInputStream bais=new java.io.ByteArrayInputStream(sdef);
        
        ret = MAPPER.readValue(bais, ServiceDefinition.class);
        
        bais.close();
        
        return (ret);
    }
    
    /**
     * This method processes the supplied activity unit to derive
     * service definition information.
     * 
     * @param actUnit The activity unit
     * @return The service definitions
     */
    public static java.util.Collection<ServiceDefinition> derive(ActivityUnit actUnit) {
        java.util.Map<String,ServiceDefinition> ret=
                        new java.util.HashMap<String,ServiceDefinition>();
        
        checkForServiceInvoked(ret, actUnit, 0, actUnit.getActivityTypes().size());
        
        return (ret.values());
    }

    /**
     * This method checks for the events associated with the service
     * being invoked.
     * 
     * @param sdefs The service definitions
     * @param actUnit The activity unit
     * @param from The 'from' index
     * @param to The 'to' index
     */
    protected static void checkForServiceInvoked(java.util.Map<String,ServiceDefinition> sdefs,
                        ActivityUnit actUnit, int from, int to) {
        
        // Scan the activity types for a received request
        for (int i=from; i < to; i++) {
            ActivityType at1=actUnit.getActivityTypes().get(i);
            
            if (at1 instanceof RequestReceived) {
                RequestReceived rqr=(RequestReceived)at1;
                
                // Locate the matching response sent activity
                for (int j=i+1; j < to; j++) {
                    ActivityType at2=actUnit.getActivityTypes().get(j);
                    
                    if (at2 instanceof ResponseSent
                            && ((ResponseSent)at2).getReplyToId().equals(
                                    rqr.getMessageId())) {
                        ResponseSent rps=(ResponseSent)at2;
                        
                        // Process the activities related to this
                        // matched interaction
                        MEPDefinition resp=processServiceInvoked(sdefs, rqr, rps);
                        
                        // Check if any invocations are performed in the
                        // scope of this req/resp
                        checkForExternalInvocations(sdefs, actUnit, resp, i+1, j);
                        
                        // Advance 'i' so only checks after the sent
                        // response
                        i = j;
                        
                        // Escape from this loop, as the response has
                        // been found
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Check if there are any external service invocations during the scope of
     * the suppled MEP definition.
     * 
     * @param sdefs The service definitions
     * @param actUnit The activity unit
     * @param mep The MEP definition
     * @param from The 'from' index
     * @param to The 'to' index
     */
    protected static void checkForExternalInvocations(java.util.Map<String,ServiceDefinition> sdefs,
            ActivityUnit actUnit, MEPDefinition mep, int from, int to) {
        
        // Scan the activity types for a received request
        for (int i=from; i < to; i++) {
            ActivityType at1=actUnit.getActivityTypes().get(i);
            
            if (at1 instanceof RequestSent) {
                RequestSent rqs=(RequestSent)at1;
                
                // Locate the matching response received activity
                for (int j=i+1; j < to; j++) {
                    ActivityType at2=actUnit.getActivityTypes().get(j);
                    
                    if (at2 instanceof ResponseReceived
                            && ((ResponseReceived)at2).getReplyToId().equals(
                                    rqs.getMessageId())) {
                        ResponseReceived rpr=(ResponseReceived)at2;
                        
                        // Process the activities related to this
                        // matched interaction
                        processExternalInvocation(sdefs, mep, rqs, rpr);
                        
                        // Check if any invocations are performed in the
                        // scope of this req/resp
                        checkForServiceInvoked(sdefs, actUnit, i+1, j);
                        
                        // Advance 'i' so only checks after the received
                        // response
                        i = j;
                        
                        // Escape from this loop, as the response has
                        // been found
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * This method processes the service invocation.
     * 
     * @param sdefs The service definitions
     * @param rqr The request received event
     * @param rps The response sent event
     * @return The response definition associated with the req/resp
     */
    protected static MEPDefinition processServiceInvoked(java.util.Map<String,ServiceDefinition> sdefs,
                    RequestReceived rqr, ResponseSent rps) {
        MEPDefinition ret=null;
        
        // Get service definition associated with the service type
        ServiceDefinition sd=sdefs.get(rqr.getServiceType());
        
        // If not found, then create
        if (sd == null) {
            sd = new ServiceDefinition();
            sd.setServiceType(rqr.getServiceType());
            sdefs.put(rqr.getServiceType(), sd);
        }
        
        OperationDefinition op=sd.getOperation(rqr.getOperation());
        
        if (op == null) {
            op = new OperationDefinition();
            op.setOperation(rqr.getOperation());
            sd.getOperations().add(op);
        }
        
        // Check if normal or fault response
        InvocationMetric metrics=null;
        
        if (rps.getFault() == null || rps.getFault().trim().length() == 0) {
            RequestResponseDefinition nrd=op.getRequestResponse();
            
            if (nrd == null) {
                nrd = new RequestResponseDefinition();
                op.setRequestResponse(nrd);
            }
            
            metrics = nrd.getMetrics();
            
            ret = nrd;
        } else {
            RequestFaultDefinition frd=op.getRequestFault(rps.getFault());
            
            if (frd == null) {
                frd = new RequestFaultDefinition();
                frd.setFault(rps.getFault());
                
                op.getRequestFaults().add(frd);
            }
            
            metrics = frd.getMetrics();
            
            ret = frd;
        }
        
        long duration=rps.getTimestamp()-rqr.getTimestamp();
        
        metrics.setAverage(((metrics.getAverage() * metrics.getCount())+duration)
                            / (metrics.getCount()+1));
        
        if (metrics.getMin() == 0 || duration < metrics.getMin()) {
            metrics.setMin(duration);
        }

        if (duration > metrics.getMax()) {
            metrics.setMax(duration);
        }

        metrics.setCount(metrics.getCount()+1);
        
        return (ret);
    }
    
    /**
     * This method processes the external invocation and associates the
     * details with the supplied MEP definition.
     * 
     * @param sdefs The service definitions
     * @param call The MEP definition
     * @param rqs The request
     * @param rpr The response
     */
    protected static void processExternalInvocation(java.util.Map<String,ServiceDefinition> sdefs,
                MEPDefinition call, RequestSent rqs, ResponseReceived rpr) {
        
        InvocationDefinition idef=call.getInvocation(rqs.getServiceType(),
                        rqs.getOperation(), rpr.getFault());
        
        if (idef == null) {
            idef = new InvocationDefinition();
            idef.setServiceType(rqs.getServiceType());
            idef.setOperation(rqs.getOperation());
            idef.setFault(rpr.getFault());
            
            call.getInvocations().add(idef);
        }
        
        InvocationMetric metrics=idef.getMetrics();

        long duration=rpr.getTimestamp()-rqs.getTimestamp();
        
        metrics.setAverage(((metrics.getAverage() * metrics.getCount())+duration)
                            / (metrics.getCount()+1));
        
        if (metrics.getMin() == 0 || duration < metrics.getMin()) {
            metrics.setMin(duration);
        }

        if (duration > metrics.getMax()) {
            metrics.setMax(duration);
        }

        metrics.setCount(metrics.getCount()+1);        
    }
}
