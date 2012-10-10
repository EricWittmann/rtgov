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
package org.overlord.bam.activity.model;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.persistence.Embeddable;

/**
 * This class represents a reference to an ActivityType contained within an
 * ActivityUnit.
 *
 */
@Embeddable
public class ActivityTypeId implements java.io.Externalizable {

    private static final int VERSION = 1;

    private String _activityUnitId=null;
    private int _activityTypeIndex=0;

    /**
     * The default constructor.
     */
    public ActivityTypeId() {
    }
    
    /**
     * The copy constructor.
     * 
     * @param act The activity to copy.
     */
    public ActivityTypeId(ActivityTypeId act) {
        _activityUnitId = act._activityUnitId;
        _activityTypeIndex = act._activityTypeIndex;
    }
    
    /**
     * This constructor initializes the id and index
     * for the reference.
     * 
     * @param id The activity unit id
     * @param index The activity type index within the unit
     */
    public ActivityTypeId(String id, int index) {
        _activityUnitId = id;
        _activityTypeIndex = index;
    }
    
    /**
     * This method sets the activity unit id.
     * 
     * @param id The activity unit id
     */
    public void setActivityUnitId(String id) {
        _activityUnitId = id;
    }
    
    /**
     * This method gets the activity unit id.
     * 
     * @return The activity unit id
     */
    public String getActivityUnitId() {
        return (_activityUnitId);
    }
    
    /**
     * This method sets the index of the activity
     * type within the activity unit.
     * 
     * @param index The index
     */
    public void setActivityTypeIndex(int index) {
        _activityTypeIndex = index;
    }
    
    /**
     * This method sets the index of the activity
     * type within the activity unit.
     * 
     * @return The index
     */
    public int getActivityTypeIndex() {
        return (_activityTypeIndex);
    }
    
    /**
     * {@inheritDoc}
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(VERSION);
        
        out.writeObject(_activityUnitId);
        out.writeInt(_activityTypeIndex);
    }

    /**
     * {@inheritDoc}
     */
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        in.readInt(); // Consume version, as not required for now
        
        _activityUnitId = (String)in.readObject();
        _activityTypeIndex = in.readInt();
    }
    
}
