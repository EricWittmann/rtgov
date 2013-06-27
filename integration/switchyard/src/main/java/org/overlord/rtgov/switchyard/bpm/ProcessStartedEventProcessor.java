/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-13, Red Hat Middleware LLC, and others contributors as indicated
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
package org.overlord.rtgov.switchyard.bpm;

import java.util.EventObject;

import org.drools.event.ProcessStartedEventImpl;
import org.kie.event.process.ProcessStartedEvent;
import org.overlord.rtgov.switchyard.AbstractEventProcessor;

/**
 * This class provides the BPM component implementation of the
 * event processor.
 *
 */
public class ProcessStartedEventProcessor extends AbstractEventProcessor {

    /**
     * This is the default constructor.
     */
    public ProcessStartedEventProcessor() {
        super(ProcessStartedEventImpl.class);
    }

    /**
     * {@inheritDoc}
     */
    public void handleEvent(EventObject event) {
        ProcessStartedEvent bpmEvent=(ProcessStartedEvent)event;
        
        org.overlord.rtgov.activity.model.bpm.ProcessStarted ps=
                new org.overlord.rtgov.activity.model.bpm.ProcessStarted();
        
        ps.setProcessType(bpmEvent.getProcessInstance().getProcessName());
        ps.setInstanceId(Long.toString(bpmEvent.getProcessInstance().getId()));
        
        recordActivity(event, ps);
    }

}