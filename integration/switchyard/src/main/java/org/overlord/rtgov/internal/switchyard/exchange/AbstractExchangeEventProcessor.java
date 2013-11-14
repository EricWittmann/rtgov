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
package org.overlord.rtgov.internal.switchyard.exchange;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.EventObject;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.overlord.rtgov.activity.model.soa.RPCActivityType;
import org.overlord.rtgov.activity.model.soa.RequestReceived;
import org.overlord.rtgov.activity.model.soa.RequestSent;
import org.overlord.rtgov.activity.model.soa.ResponseReceived;
import org.overlord.rtgov.activity.model.soa.ResponseSent;
import org.overlord.rtgov.internal.switchyard.AbstractEventProcessor;
import org.switchyard.Exchange;
import org.switchyard.ExchangePhase;
import org.switchyard.Message;
import org.switchyard.Property;
import org.switchyard.Scope;
import org.switchyard.Service;
import org.switchyard.ServiceReference;
import org.switchyard.extensions.wsdl.WSDLService;
import org.switchyard.metadata.ExchangeContract;
import org.switchyard.metadata.Registrant;
import org.switchyard.metadata.ServiceInterface;
import org.switchyard.extensions.java.JavaService;
import org.switchyard.security.context.SecurityContext;
import org.switchyard.security.context.SecurityContextManager;
import org.switchyard.security.credential.Credential;

/**
 * This class provides the abstract Exchange event based implementation of the
 * event processor.
 *
 */
public abstract class AbstractExchangeEventProcessor extends AbstractEventProcessor {
    
    private static final String RTGOV_REQUEST_SENT = "rtgov.request.sent";

    private static final String RTGOV_REQUEST_RECEIVED = "rtgov.request.received";

    private static final Logger LOG=Logger.getLogger(AbstractExchangeEventProcessor.class.getName());

    private boolean _completedEvent=false;
    
    /**
     * This is the constructor.
     * 
     * @param eventType The event type associated with the processor
     * @param completed Whether the event processor represents a completed event
     */
    public AbstractExchangeEventProcessor(Class<? extends EventObject> eventType, boolean completed) {
        super(eventType);    
        
        _completedEvent = completed;
    }
    
    /**
     * This method obtains the exchange from the supplied event.
     * 
     * @param event The event
     * @return The exchange
     */
    protected abstract Exchange getExchange(EventObject event);

    /**
     * {@inheritDoc}
     */
    public void handleEvent(EventObject event) {
        try {
            Exchange exch=getExchange(event);
            
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.finest("********* Exchange="+exch);
            }
            
            org.switchyard.Message mesg=exch.getMessage();
            
            ExchangePhase phase=exch.getPhase();
    
            if (phase == null) {
                LOG.severe("Could not obtain phase from exchange: "+exch);
                return;
            }
    
            if (mesg == null) {
                LOG.severe("Could not obtain message for phase ("+phase+") and exchange: "+exch);
                return;
            }
            
            org.switchyard.Context context=exch.getContext();
            
            Service provider=exch.getProvider();
            ServiceReference consumer=exch.getConsumer();
            
            // TODO: If message is transformed, then should the contentType
            // be updated to reflect the transformed type?
            
            String messageId=null;
            Property mip=context.getProperty(Exchange.MESSAGE_ID, org.switchyard.Scope.MESSAGE);
            if (mip != null) {
                messageId = (String)mip.getValue();
            }
            
            String contentType=null;
            Property ctp=context.getProperty(Exchange.CONTENT_TYPE, org.switchyard.Scope.MESSAGE);
            if (ctp != null) {
                contentType = ((QName)ctp.getValue()).toString();
                
                // RTGOV-250 - remove java: prefix from Java types, to make the type consistent with
                // events reported outside switchyard
                if (contentType != null && contentType.startsWith("java:")) {
                    contentType = contentType.substring(5);
                }
            }
            
            if (phase == ExchangePhase.IN) {
                handleInExchange(exch, provider, consumer, messageId, contentType, mesg);
                
            } else if (phase == ExchangePhase.OUT) {            
                String relatesTo=null;
                Property rtp=context.getProperty(Exchange.RELATES_TO, org.switchyard.Scope.MESSAGE);
                if (rtp != null) {
                    relatesTo = (String)rtp.getValue();
                }
                
                handleOutExchange(exch, provider, consumer, messageId, relatesTo, contentType, mesg);
            }
        } catch (Throwable t) {
            LOG.log(Level.SEVERE, java.util.PropertyResourceBundle.getBundle(
                    "rtgov-switchyard.Messages").getString("RTGOV-SWITCHYARD-1"), t);
        }
    }
    
    /**
     * This method handles the 'in' exchange.
     * 
     * @param exch The exchange
     * @param provider The provider
     * @param consumer The consumer
     * @param messageId The message id
     * @param contentType The content type
     * @param mesg The message
     */
    protected void handleInExchange(Exchange exch,
            Service provider, ServiceReference consumer, String messageId,
            String contentType, org.switchyard.Message mesg) {
        Registrant consumerReg=consumer.getServiceMetadata().getRegistrant();
        
        if (_completedEvent) {
            // The return side of a one way exchange, so record a response sent/received
            handleOutExchange(exch, provider, consumer, messageId+"onewayreturn", messageId, null, null);
            
            // Nothing to do, as appears to be a one-way exchange
            return;
        }

        Registrant providerReg=(provider == null ? null : provider.getServiceMetadata().getRegistrant());

        String intf=getInterface(consumer, provider, consumerReg);
        
        SecurityContextManager scm=new SecurityContextManager(exch.getConsumer().getDomain());
        
        SecurityContext securityContext=scm.getContext(exch);

        ExchangeContract contract=exch.getContract();
        
        // Extract service type and operation from the consumer
        // (service reference), as provider is not always available
        QName serviceType=consumer.getName();
        String opName=contract.getConsumerOperation().getName();
        
        if (consumerReg.isBinding()) {
            getActivityCollector().startScope();
        } else {
            // Only record the request being sent, if the
            // source is a component, not a binding
        
            RequestSent sent=new RequestSent();
            
            // Only report service type if provider is not a binding
            if (providerReg == null
                    || !providerReg.isBinding()) {
                sent.setServiceType(serviceType.toString()); 
            }
            
            sent.setInterface(intf);                
            sent.setOperation(opName);
            sent.setMessageId(messageId);
            
            record(mesg, contentType, sent, securityContext, exch); 
            
            if (intf == null) {
                // Save activity event in exchange
                exch.getContext().setProperty(RTGOV_REQUEST_SENT, new TransientWrapper(sent), Scope.EXCHANGE);
            }
        }
        
        if (providerReg == null
                || !providerReg.isBinding()) {
            RequestReceived recvd=new RequestReceived();
            
            recvd.setServiceType(serviceType.toString());                
            recvd.setInterface(intf);                
            recvd.setOperation(opName);
            recvd.setMessageId(messageId);
            
            record(mesg, contentType, recvd, securityContext, exch); 
            
            // Save activity event in exchange
            // RTGOV-262 Need to store this event, event if interface set,
            // in case needs to establish relationship from exception response
            exch.getContext().setProperty(RTGOV_REQUEST_RECEIVED, new TransientWrapper(recvd), Scope.EXCHANGE);
        }
    }
    
    /**
     * This method handles the 'in' exchange.
     * 
     * @param exch The exchange
     * @param provider The provider
     * @param consumer The consumer
     * @param messageId The message id
     * @param relatesTo The relates-to id
     * @param contentType The content type
     * @param mesg The message
     */
    protected void handleOutExchange(Exchange exch,
            Service provider, ServiceReference consumer, String messageId, String relatesTo,
            String contentType, org.switchyard.Message mesg) {

        Registrant consumerReg=consumer.getServiceMetadata().getRegistrant();
        Registrant providerReg=(provider == null ? null : provider.getServiceMetadata().getRegistrant());

        // Check if interface on associated request needs to be set
        String intf=getInterface(consumer, provider, consumerReg);
        
        SecurityContextManager scm=new SecurityContextManager(exch.getConsumer().getDomain());
        
        SecurityContext securityContext=scm.getContext(exch);

        ExchangeContract contract=exch.getContract();
        
        // Extract service type and operation from the consumer
        // (service reference), as provider is not always available
        QName serviceType=consumer.getName();
        String opName=contract.getConsumerOperation().getName();
        
        // Attempt to retrieve any stored request activity event
        Property rrtw=exch.getContext().getProperty(RTGOV_REQUEST_RECEIVED);
        Property rstw=exch.getContext().getProperty(RTGOV_REQUEST_SENT);
        
        RequestReceived rr=(rrtw == null ? null : (RequestReceived)((TransientWrapper)rrtw.getValue()).getContent());
        RequestSent rs=(rstw == null ? null : (RequestSent)((TransientWrapper)rstw.getValue()).getContent());
 
        if (intf != null) {
            if (rr != null) {
                rr.setInterface(intf);
            }
            if (rs != null) {
                rs.setInterface(intf);
            }
        }
        
        // Record the response
        if (providerReg == null
                || !providerReg.isBinding()) {
            ResponseSent sent=new ResponseSent();
                            
            // Only report service type if provider is not a binding
            if (providerReg == null
                    || !providerReg.isBinding()) {
                sent.setServiceType(serviceType.toString()); 
            }

            sent.setInterface(intf);                
            sent.setOperation(opName);
            sent.setMessageId(messageId);
            
            // RTGOV-262 Check if replyTo id not set, due to exception - if so, then
            // use request received id if available
            if (relatesTo == null && rr != null) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.finest("Exception seems to have occurred, " +
                    		"so establishing relationship to original request: "+rr.getMessageId());
                }
                relatesTo = rr.getMessageId();
            }
            
            sent.setReplyToId(relatesTo);
            
            record(mesg, contentType, sent, securityContext, exch); 
        }
        
        if (consumerReg.isBinding()) {
            getActivityCollector().endScope();
        } else {
            // Only record the response being received, if the
            // target is a component, not a binding
            ResponseReceived recvd=new ResponseReceived();
            
            recvd.setServiceType(serviceType.toString());                
            recvd.setInterface(intf);                
            recvd.setOperation(opName);
            recvd.setMessageId(messageId);
            recvd.setReplyToId(relatesTo);
            
            record(mesg, contentType, recvd, securityContext, exch); 
        }
    }
    
    /**
     * This method extracts the interface from the exchange details.
     * 
     * @param consumer The exchange consumer
     * @param provider The exchange provider
     * @param consumerReg The consumer registrant
     * @return The interface
     */
    protected String getInterface(ServiceReference consumer, Service provider, Registrant consumerReg) {
        String ret=null;
        ServiceInterface intf=null;
        
        if (consumerReg.isBinding()) {
            intf = consumer.getInterface();
        } else if (provider != null) {
            intf = provider.getInterface();
        }
        
        if (intf != null) {
            if (JavaService.TYPE.equals(intf.getType())) {
                ret = ((JavaService)intf).getJavaInterface().getName();
            } else if (WSDLService.TYPE.equals(intf.getType())) {
                ret = ((WSDLService)intf).getPortType().toString();
            }
        }
        
        return (ret);
    }
    
    /**
     * This method records the supplied information as an activity
     * event.
     * 
     * @param exchange The exchange
     * @param contentType The message content type
     * @param at The activity type
     * @param sc The optional security context
     * @param exch The original exchange event
     */
    protected void record(Message msg, String contentType,
                RPCActivityType at, SecurityContext sc, Exchange exch) {
        if (at != null) {
            at.setMessageType(contentType);
            
            if (msg != null) {
                Object content=msg.getContent();
                
                if (contentType != null) {
                    at.setContent(getActivityCollector().processInformation(null,
                              contentType, content, new PropertyAccessor(msg.getContext()), at));
                    
                } else if (content != null) {
                    // Assume this is an exception response
                    at.setContent(content.toString());
                }
            }
            
            // Check if principal has been defined
            if (sc != null && sc.getCredentials().size() > 0) {
                for (Credential cred : sc.getCredentials()) {
                    if (cred instanceof org.switchyard.security.credential.NameCredential) {
                        at.setPrincipal(((org.switchyard.security.credential.NameCredential)cred).getName());
                        break;
                    } else if (cred instanceof org.switchyard.security.credential.PrincipalCredential) {
                        at.setPrincipal(((org.switchyard.security.credential.PrincipalCredential)cred)
                                            .getPrincipal().getName());
                        break;
                    }
                }
            }
            
            recordActivity(exch, at);
        }
    }

    /**
     * This class provides a wrapper for exchange properties that must not be
     * serialized, however will remain accessible to the requester who
     * stored the property.
     *
     * NOTE: If migrating to use the switchyard exchange start/completed events,
     * then investigating using the transient property mechanism in switchyard.
     */
    public static class TransientWrapper implements java.io.Externalizable {
        
        private transient Object _content=null;
        
        /**
         * This constructor initializes the content.
         * 
         * @param content The content
         */
        public TransientWrapper(Object content) {
            _content = content;
        }
        
        /**
         * This method returns the content.
         * 
         * @return The content
         */
        public Object getContent() {
            return (_content);
        }

        /**
         * {@inheritDoc}
         */
        public void writeExternal(ObjectOutput out) throws IOException {
            // Don't implement as we don't want to serialize the contents
        }

        /**
         * {@inheritDoc}
         */
        public void readExternal(ObjectInput in) throws IOException,
                ClassNotFoundException {
            // Don't implement as we don't want to deserialize the contents
        }
        
    }
}
