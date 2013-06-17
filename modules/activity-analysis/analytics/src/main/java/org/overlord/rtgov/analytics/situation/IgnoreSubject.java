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
package org.overlord.rtgov.analytics.situation;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * This class represents a situation subject that should be ignored.
 *
 */
@Entity
@Table(name="RTGOV_SITUATIONS_IGNORE_SUBJECTS")
public class IgnoreSubject implements java.io.Externalizable {

    private static final int VERSION = 1;
    
    private Long _id;
    
    private String _subject=null;
    private String _principal=null;
    private String _reason=null;
    private long _timestamp=0;

    /**
     * This method returns the id.
     * 
     * @return The id
     */
    @Id
    @GeneratedValue 
    protected Long getId() {
        return (_id);
    }
    
    /**
     * This method sets the id.
     * 
     * @param id The id
     */
    protected void setId(Long id) {
        _id = id;
    }
    
    /**
     * This method sets the subject.
     * 
     * @param subject The subject
     */
    public void setSubject(String subject) {
        _subject = subject;
    }
    
    /**
     * This method gets the subject.
     * 
     * @return The subject
     */
    public String getSubject() {
        return (_subject);
    }
    
    /**
     * This method sets the principal who recorded
     * the subject as being ignored.
     * 
     * @param principal The principal
     */
    public void setPrincipal(String principal) {
        _principal = principal;
    }
    
    /**
     * This method gets the principal.
     * 
     * @return The principal
     */
    public String getPrincipal() {
        return (_principal);
    }
    
    /**
     * This method sets the reason.
     * 
     * @param reason The reason
     */
    public void setReason(String reason) {
        _reason = reason;
    }
    
    /**
     * This method gets the reason.
     * 
     * @return The reason
     */
    public String getReason() {
        return (_reason);
    }
    
    /**
     * This method sets the timestamp.
     * 
     * @param timestamp The timestamp
     */
    public void setTimestamp(long timestamp) {
        _timestamp = timestamp;
    }
    
    /**
     * This method returns the timestamp.
     * 
     * @return The timestamp
     */
    public long getTimestamp() {
        return (_timestamp);
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return ("Ignore subject="+_subject+ " principal=" + _principal
                + " reason="+_reason+" when="+new java.util.Date(_timestamp));
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(VERSION);
        
        out.writeObject(_id);
        out.writeObject(_subject);
        out.writeObject(_principal);
        out.writeObject(_reason);
        out.writeLong(_timestamp);
    }

    /**
     * {@inheritDoc}
     */
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        in.readInt(); // Consume version, as not required for now
        
        _id = (Long)in.readObject();
        _subject = (String)in.readObject();
        _principal = (String)in.readObject();
        _reason = (String)in.readObject();
        _timestamp = in.readLong();
    }
}
