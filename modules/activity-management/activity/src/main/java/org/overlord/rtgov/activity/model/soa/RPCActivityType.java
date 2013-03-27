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
package org.overlord.rtgov.activity.model.soa;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.overlord.rtgov.activity.model.Context;
import org.overlord.rtgov.activity.model.common.MessageExchange;

/**
 * This activity type represents a RPC activity.
 *
 */
@Entity
public abstract class RPCActivityType extends MessageExchange implements java.io.Externalizable {

    private static final int VERSION = 1;

    private String _serviceType=null;
    private String _interface=null;
    private String _operation=null;
    private String _fault=null;

    /**
     * The default constructor.
     */
    public RPCActivityType() {
    }
    
    /**
     * The copy constructor.
     * 
     * @param rpc The RPC activity to copy
     */
    public RPCActivityType(RPCActivityType rpc) {
        super(rpc);
        _serviceType = rpc._serviceType;
        _interface = rpc._interface;
        _operation = rpc._operation;
        _fault = rpc._fault;
    }
    
    /**
     * This method determines whether the RPC activity is
     * associated with the service provider.
     * 
     * @return Whether associated with the service provider
     */
    @Transient
    public abstract boolean isServiceProvider();
    
    /**
     * This method determines whether the RPC activity is
     * a request.
     * 
     * @return Whether a request
     */
    @Transient
    public abstract boolean isRequest();
    
    /**
     * This method sets the optional service type.
     * 
     * @param serviceType The optional service type
     */
    public void setServiceType(String serviceType) {
        _serviceType = serviceType;
    }
    
    /**
     * This method gets the optional service type.
     * 
     * @return The optional service type
     */
    public String getServiceType() {
        return (_serviceType);
    }
    
    /**
     * This method sets the interface.
     * 
     * @param intf The interface
     */
    public void setInterface(String intf) {
    	_interface = intf;
    }
    
    /**
     * This method gets the interface.
     * 
     * @return The interface
     */
    public String getInterface() {
        return (_interface);
    }
    
    /**
     * This method sets the operation.
     * 
     * @param operation The operation
     */
    public void setOperation(String operation) {
        _operation = operation;
    }
    
    /**
     * This method gets the operation.
     * 
     * @return The operation
     */
    public String getOperation() {
        return (_operation);
    }
    
    /**
     * This method sets the fault.
     * 
     * @param fault The fault
     */
    public void setFault(String fault) {
        _fault = fault;
    }
    
    /**
     * This method gets the fault.
     * 
     * @return The fault
     */
    public String getFault() {
        return (_fault);
    }
    
    /**
     * This method gets the message id. When used
     * for correlation against a response, it should
     * only be used to correlate against the
     * sending or receiving action performed in the
     * same service - not in the endpoint being
     * communicated with, as the message id may not
     * be carried with the message content, as is
     * therefore only relevant in the local service
     * context.
     * 
     * @return The message id
     */
    @Transient
    @JsonIgnore
    public String getMessageId() {
        for (Context context : getContext()) {
            if (context.getType() == Context.Type.Message) {
                return (context.getValue());
            }
        }
        
        return (null);
    }
    
    /**
     * This method sets the message id associated with the
     * activity. The information is actually stored as a
     * context entry for the Message type.
     * 
     * @param id The id
     */
    public void setMessageId(String id) {
        Context current=null;
        
        for (Context context : getContext()) {
            if (context.getType() == Context.Type.Message) {
                current = context;
                break;
            }
        }
        
        if (current == null) {
            current = new Context();
            current.setType(Context.Type.Message);
            getContext().add(current);
        }
        
        current.setValue(id);
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return (getClass().getSimpleName()+":"
                +" serviceType="+_serviceType
                +" interface="+_interface
                +" operation="+_operation
                +" fault="+_fault
                +" messageType="+getMessageType()
                +" content="+getContent());
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        
        out.writeInt(VERSION);
        
        out.writeObject(_serviceType);
        out.writeObject(_interface);
        out.writeObject(_operation);
        out.writeObject(_fault);
    }

    /**
     * {@inheritDoc}
     */
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        super.readExternal(in);
        
        in.readInt(); // Consume version, as not required for now
        
        _serviceType = (String)in.readObject();
        _interface = (String)in.readObject();
        _operation = (String)in.readObject();
        _fault = (String)in.readObject();
    }
}
