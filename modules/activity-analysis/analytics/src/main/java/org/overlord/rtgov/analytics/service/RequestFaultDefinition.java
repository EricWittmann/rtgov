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
package org.overlord.rtgov.analytics.service;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * This class represents a request/fault MEP associated
 * with a service operation.
 *
 */
public class RequestFaultDefinition extends MEPDefinition implements java.io.Externalizable {

    private static final int VERSION = 1;

    private String _fault=null;

    /**
     * Default constructor.
     */
    public RequestFaultDefinition() {
    }

    /**
     * Copy constructor.
     * 
     * @param rfd The source to copy
     */
    public RequestFaultDefinition(RequestFaultDefinition rfd) {
        super(rfd);
        
        _fault = rfd.getFault();
    }

    /**
     * This method sets the optional fault.
     * 
     * @param fault The fault
     */
    public void setFault(String fault) {
        _fault = fault;
    }
    
    /**
     * This method gets the optional fault.
     * 
     * @return The optional fault
     */
    public String getFault() {
        return (_fault);
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return (_fault.hashCode());
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        
        if (obj instanceof RequestFaultDefinition
                  && ((RequestFaultDefinition)obj).getFault().equals(_fault)) {
            return (true);
        }
        
        return (false);
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        
        out.writeInt(VERSION);
        
        out.writeObject(_fault);
    }

    /**
     * {@inheritDoc}
     */
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        super.readExternal(in);
        
        in.readInt(); // Consume version, as not required for now
        
        _fault = (String)in.readObject();
    }
}
