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
package org.overlord.rtgov.activity.processor;

import org.overlord.rtgov.activity.model.ActivityType;

/**
 * This interface manages a set of InformationProcessor
 * implementations.
 *
 */
public interface InformationProcessorManager {
    
    /**
     * This method registers the information processor.
     * 
     * @param ip The information processor
     * @throws Exception Failed to register
     */
    public void register(InformationProcessor ip) throws Exception;
    
    /**
     * This method returns the information processor associated
     * with the supplied name.
     * 
     * @param name The name
     * @return The information processor, or null if not found
     */
    public InformationProcessor getInformationProcessor(String name);
    
    /**
     * This method processes supplied information to
     * extract relevant details, and then return an
     * appropriate representation of that information
     * for public distribution.
     * 
     * @param processor The optional information processor to use
     * @param type The information type
     * @param info The information to be processed
     * @param headers The optional header information
     * @param actType The activity type to be annotated with
     *              details extracted from the information
     * @return The public representation of the information
     */
    public String process(String processor, String type,
                    Object info, java.util.Map<String, Object> headers, ActivityType actType);
    
    /**
     * This method registers the information processor.
     * 
     * @param ip The information processor
     * @throws Exception Failed to unregister
     */
    public void unregister(InformationProcessor ip) throws Exception;
    
}
