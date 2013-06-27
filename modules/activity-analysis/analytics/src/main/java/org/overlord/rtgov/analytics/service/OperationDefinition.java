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
package org.overlord.rtgov.analytics.service;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;

/**
 * This class represents an operation within a service interface.
 *
 */
public class OperationDefinition implements java.io.Externalizable {

    private static final int VERSION = 1;

    private String _name=null;
    private java.util.List<OperationImplDefinition> _implementations=
            new java.util.ArrayList<OperationImplDefinition>();
    private java.util.List<OperationDefinition> _merged=
            new java.util.ArrayList<OperationDefinition>();

    /**
     * Default constructor.
     */
    public OperationDefinition() {
    }

    /**
     * This method creates a shallow copy.
     * 
     * @return The shallow copy
     */
    protected OperationDefinition shallowCopy() {
        OperationDefinition ret=new OperationDefinition();
        
        ret.setName(_name);
        
        return (ret);
    }

    /**
     * This method sets the operation name.
     * 
     * @param operation The operation name
     */
    public void setName(String operation) {
        _name = operation;
    }
    
    /**
     * This method gets the operation name.
     * 
     * @return The operation name
     */
    public String getName() {
        return (_name);
    }
    
    /**
     * This method sets the list of implementations associated
     * with the operation.
     * 
     * @param impls The operation implementations
     */
    public void setImplementations(java.util.List<OperationImplDefinition> impls) {
        _implementations = impls;
    }
    
    /**
     * This method returns the list of implementations associated
     * with the operation.
     * 
     * @return The operation implementations
     */
    public java.util.List<OperationImplDefinition> getImplementations() {
        return (_implementations);
    }
    
    /**
     * This method returns the specific operation information associated with the supplied
     * service type, if defined within the operation definition. If the supplied
     * service type is null, it can only be matched against an existing service type
     * op definition with a null service type (i.e. unknown service type).
     * 
     * @param serviceType The service type
     * @return The service type's operation definition, or null if not found
     */
    public OperationImplDefinition getServiceTypeOperation(String serviceType) {
        OperationImplDefinition ret=null;
        
        for (int i=0; i < _implementations.size(); i++) {
            if ((_implementations.get(i).getServiceType() == null
                    && serviceType == null)
                    || (_implementations.get(i).getServiceType() != null
                    && _implementations.get(i).getServiceType().equals(serviceType))) {
                ret = _implementations.get(i);
                break;
            }
        }
        
        return (ret);
    }
    
    /**
     * This method returns the aggregated invocation metric information
     * from the service type specific operation definitions.
     * 
     * @return The invocation metric
     */
    public InvocationMetric getMetrics() {
        java.util.List<InvocationMetric> metrics=
                new java.util.ArrayList<InvocationMetric>();
        
        for (OperationImplDefinition stod : getImplementations()) {
            metrics.add(stod.getMetrics());
        }

        return (new InvocationMetric(metrics));
    }
    
    /**
     * This method merges the supplied operation definition
     * with this.
     * 
     * @param opdef The operation definition to merge
     */
    public void merge(OperationDefinition opdef) {
        
        for (int i=0; i < opdef.getImplementations().size(); i++) {
            OperationImplDefinition stod=opdef.getImplementations().get(i);
            
            OperationImplDefinition cur=getServiceTypeOperation(stod.getServiceType());
             
            if (cur == null) {
                cur = stod.shallowCopy();
                getImplementations().add(cur);
            }

            cur.merge(stod);
       }
        
       _merged.add(opdef);
    }
    
    /**
     * This method returns the list of merged operation definitions.
     * 
     * @return The merged list
     */
    public java.util.List<OperationDefinition> getMerged() {
        return (Collections.unmodifiableList(_merged));
    }
    
    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return (_name.hashCode());
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        
        if (obj instanceof OperationDefinition
                  && ((OperationDefinition)obj).getName().equals(_name)) {
            return (true);
        }
        
        return (false);
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(VERSION);
        
        out.writeObject(_name);
        
        out.writeInt(_implementations.size());
        for (int i=0; i < _implementations.size(); i++) {
            out.writeObject(_implementations.get(i));
        }
        
        out.writeInt(_merged.size());
        for (int i=0; i < _merged.size(); i++) {
            out.writeObject(_merged.get(i));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        in.readInt(); // Consume version, as not required for now
        
        _name = (String)in.readObject();
        
        int len=in.readInt();
        for (int i=0; i < len; i++) {
            _implementations.add((OperationImplDefinition)in.readObject());
        }
        
        len = in.readInt();
        for (int i=0; i < len; i++) {
            _merged.add((OperationDefinition)in.readObject());
        }
    }
}
