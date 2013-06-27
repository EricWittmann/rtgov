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
package org.switchyard.quickstarts.demos.orders;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.overlord.rtgov.active.collection.ActiveMap;
import org.overlord.rtgov.jee.CollectionManager;
import org.overlord.rtgov.jee.DefaultCollectionManager;
import org.switchyard.Exchange;
import org.switchyard.ExchangeHandler;
import org.switchyard.ExchangePhase;
import org.switchyard.HandlerException;
import org.switchyard.Message;
import org.switchyard.Property;

public class PolicyEnforcer implements ExchangeHandler {
    
    private static final String PRINCIPALS = "Principals";

    private static final Logger LOG=Logger.getLogger(PolicyEnforcer.class.getName());
    
    private CollectionManager _collectionManager=new DefaultCollectionManager();
    
    private ActiveMap _principals=null;
    
    private boolean _initialized=false;
    
    private static final ObjectMapper MAPPER=new ObjectMapper();

    static {
        SerializationConfig config=MAPPER.getSerializationConfig()
                .withSerializationInclusion(JsonSerialize.Inclusion.NON_NULL)
                .withSerializationInclusion(JsonSerialize.Inclusion.NON_DEFAULT);
        
        MAPPER.setSerializationConfig(config);
    }

    protected void init() {
                
        if (_collectionManager != null) {
            _principals = _collectionManager.getMap(PRINCIPALS);
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("*********** Policy Enforcer Initialized with acm="
                        +_collectionManager+" ac="+_principals);
        }
        
        _initialized = true;
    }

    public void handleMessage(Exchange exchange) throws HandlerException {
        if (!_initialized) {
            init();
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("********* Exchange="+exchange);
        }
        
        if (_principals != null) {            
            Property p = exchange.getContext().getProperty("org.switchyard.contentType",
                    org.switchyard.Scope.MESSAGE);
            
            if (p != null && exchange.getPhase() == ExchangePhase.IN
                    && p.getValue().toString().equals(
                            "{urn:switchyard-quickstart-demo:orders:1.0}submitOrder")) {

                String customer=getCustomer(exchange);
                       
                if (customer != null) {
                    if (_principals.containsKey(customer)) {
                        
                        @SuppressWarnings("unchecked")
                        java.util.Map<String,java.io.Serializable> props=
                                (java.util.Map<String,java.io.Serializable>)
                                        _principals.get(customer);
                        
                        // Check if customer is suspended
                        if (props.containsKey("suspended")
                                && props.get("suspended").equals(Boolean.TRUE)) {                            
                            throw new HandlerException("Customer '"+customer
                                    +"' has been suspended");
                        }
                    }
                    
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("*********** Policy Enforcer: customer '"
                                +customer+"' has not been suspended");
                        LOG.fine("*********** Principal: "+_principals.get(customer));
                    }
                }
            }
        }
    }

    public void handleFault(Exchange exchange) {
        if (!_initialized) {
            init();
        }
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("********* Fault="+exchange);
        }
    }

    /**
     * This method returns the customer associated with the
     * exchange.
     * 
     * @param exchange The exchange
     * @return The customer
     */
    protected String getCustomer(Exchange exchange) {
        String customer=null;

        String content=getMessageContent(exchange);

        int start=content.indexOf("<customer>");
        
        if (start != -1) {
            int end=content.indexOf("</", start);
            
            if (end != -1) {
                customer = content.substring(start+10, end);
            }
        }
        
        return (customer);
    }
    
    /**
     * This method returns a string representation of the
     * message content, or null if no available.
     * 
     * @param exchange The exchange
     * @return The string representation, or null if not possible
     */
    protected String getMessageContent(Exchange exchange) {
        String ret=null;
        
        // try to convert the payload to a string
        Message msg = exchange.getMessage();

        try {    
            ret = msg.getContent(String.class);
            
            // check to see if we have to put content back into the message 
            // after the conversion to string
            if (java.io.InputStream.class.isAssignableFrom(msg.getContent().getClass())) {
                msg.setContent(new java.io.ByteArrayInputStream(ret.getBytes()));
            } else if (java.io.Reader.class.isAssignableFrom(msg.getContent().getClass())) {
                msg.setContent(new java.io.StringReader(ret));
            }

        } catch (Exception ex) {
            try {
                // If contents cannot be represented as a string, then try a
                // JSON serialized form
                java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
                
                MAPPER.writeValue(baos, msg.getContent());
                
                ret = new String(baos.toByteArray());
                
                baos.close();
            } catch (Exception ex2) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.finest("Failed to convert message content for '"+exchange
                            +"' to string: ex="+ex+" ex2="+ex2);
                }
            }
        }

        return(ret);
    }
}
