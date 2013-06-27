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
package org.overlord.rtgov.activity.model.bpm;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.persistence.Entity;

/**
 * This activity type represents a process completed event.
 *
 */
@Entity
public class ProcessCompleted extends BPMActivityType implements java.io.Externalizable {

    private static final int VERSION = 1;

    /**
     * The status of the process upon completion.
     *
     */
    public enum Status {
        /** Process completed succcesfully. **/
        Success,
        /** Process failed. **/
        Fail
    }
    
    private Status _status=Status.Success;
    
    /**
     * The default constructor.
     */
    public ProcessCompleted() {
    }
    
    /**
     * The copy constructor.
     * 
     * @param ba The bpm activity to copy
     */
    public ProcessCompleted(ProcessCompleted ba) {
        super(ba);
        _status = ba._status;
    }
    
    /**
     * This method sets the status.
     * 
     * @param status The status
     */
    public void setStatus(Status status) {
        _status = status;
    }
    
    /**
     * This method gets the status.
     * 
     * @return The status
     */
    public Status getStatus() {
        return (_status);
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        
        out.writeInt(VERSION);
        
        out.writeObject(_status);
    }

    /**
     * {@inheritDoc}
     */
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        super.readExternal(in);
        
        in.readInt(); // Consume version, as not required for now
        
        _status = (Status)in.readObject();
    }
}
