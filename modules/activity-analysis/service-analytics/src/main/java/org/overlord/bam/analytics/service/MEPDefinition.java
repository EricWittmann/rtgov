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
package org.overlord.bam.analytics.service;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * This class represents an abstract response within a service operation.
 *
 */
public abstract class MEPDefinition implements java.io.Externalizable {

    private static final int VERSION = 1;

    private java.util.List<InvocationDefinition> _invocations=
                new java.util.ArrayList<InvocationDefinition>();
    
    private InvocationMetric _metrics=new InvocationMetric();

    /**
     * This method sets the list of invocations associated
     * with the operation.
     * 
     * @param invocations The invocations
     */
    public void setInvocations(java.util.List<InvocationDefinition> invocations) {
        _invocations = invocations;
    }
    
    /**
     * This method returns the list of invocations associated
     * with the operation.
     * 
     * @return The invocations
     */
    public java.util.List<InvocationDefinition> getInvocations() {
        return (_invocations);
    }
    
    /**
     * This method returns the invocation metric information
     * from the fault response.
     * 
     * @return The invocation metric
     */
    public InvocationMetric getMetrics() {
        return (_metrics);
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(VERSION);
        
        out.writeObject(_metrics);
    }

    /**
     * {@inheritDoc}
     */
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        in.readInt(); // Consume version, as not required for now
        
        _metrics = (InvocationMetric)in.readObject();
    }
}
