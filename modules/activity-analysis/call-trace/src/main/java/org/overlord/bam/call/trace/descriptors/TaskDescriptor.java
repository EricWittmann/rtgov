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
package org.overlord.bam.call.trace.descriptors;

import org.overlord.bam.activity.model.ActivityType;

/**
 * This class provides a descriptor for activities that may be
 * presented within the call trace.
 *
 */
public interface TaskDescriptor {

    /**
     * This method determines if the descriptor
     * supports the specified activity type.
     * 
     * @param at The activity type
     * @return Whether the descriptor supports the type
     */
    public boolean isSupported(ActivityType at);
    
    /**
     * This method returns the description for
     * the supplied activity type.
     * 
     * @param at The activity type
     * @return The description
     */
    public String getDescription(ActivityType at);
    
}
