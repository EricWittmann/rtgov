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
package org.overlord.rtgov.internal.switchyard;

import static javax.ejb.ConcurrencyManagementType.BEAN;

import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.overlord.rtgov.activity.collector.ActivityCollector;

/**
 * This class is responsible for registering the configured set of
 * event processor implementations against the switchyard Event Manager.
 *
 */
@ApplicationScoped
@Singleton
@Startup
@ConcurrencyManagement(BEAN)
public class EventProcessorManager {
    
    private static final String SWITCHAYRD_MANAGEMENT_LOCAL = "org.switchyard.admin:type=Management.Local";

    private static final Logger LOG=Logger.getLogger(EventProcessorManager.class.getName());

    @Inject
    private ActivityCollector _activityCollector=null;

    private java.util.List<EventProcessor> _eventProcessors=new java.util.ArrayList<EventProcessor>();
    
    private static final String[] EVENT_PROCESSOR_CLASS_NAMES={
        "org.overlord.rtgov.internal.switchyard.bpel.NewProcessInstanceEventProcessor",
        "org.overlord.rtgov.internal.switchyard.bpel.ProcessCompletionEventProcessor",
        "org.overlord.rtgov.internal.switchyard.bpel.ProcessTerminationEventProcessor",
        "org.overlord.rtgov.internal.switchyard.bpel.VariableModificationEventProcessor",
        "org.overlord.rtgov.internal.switchyard.bpm.ProcessCompletedEventProcessor",
        "org.overlord.rtgov.internal.switchyard.bpm.ProcessStartedEventProcessor",
        "org.overlord.rtgov.internal.switchyard.bpm.ProcessVariableChangedEventProcessor",
        "org.overlord.rtgov.internal.switchyard.camel.ExchangeCompletedEventProcessor",
        "org.overlord.rtgov.internal.switchyard.camel.ExchangeCreatedEventProcessor"
    };
    
    /**
     * Initialize the event processors.
     */
    @PostConstruct
    public void init() {
        
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("SwitchYard EventProcessorManager Initialized with collector="+_activityCollector);
        }

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
        ObjectName objname=null;
        
        try {
            // Check if switchyard present - if not then will fail and not try to load beans
            String observerClassName=org.switchyard.event.EventObserver.class.getName();
            
            objname = new ObjectName(SWITCHAYRD_MANAGEMENT_LOCAL);
                       
            for (String clsName : EVENT_PROCESSOR_CLASS_NAMES) {    
                Class<?> cls=EventProcessorManager.class.getClassLoader().loadClass(clsName);
                
                EventProcessor ep=(EventProcessor)cls.newInstance();
                
                _eventProcessors.add(ep);
                                
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("SwitchYard EventProcessorManager register event processor="+ep);
                }

                // Initialize with the activity collector
                ep.init(_activityCollector);
                
                java.util.List<Class<?>> eventTypes=new java.util.ArrayList<Class<?>>();
                eventTypes.add(ep.getEventType());               
                
                Object[] params={ep, eventTypes};
                
                String[] types={observerClassName,
                        java.util.List.class.getName()};
                
                mbs.invoke(objname, "addObserver", params, types);
            }

        } catch (Throwable e) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Failed to register SwitchYard event processor", e);
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("SwitchYard EventProcessorManager Initialization Completed");
        }
    }
    
    /**
     * Close the event processors.
     */
    @PreDestroy
    public void close() {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
        ObjectName objname=null;
        
        try {
            objname = new ObjectName(SWITCHAYRD_MANAGEMENT_LOCAL);
            
            for (EventProcessor ep : _eventProcessors) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("SwitchYard EventProcessorManager unregister event processor="+ep);
                }

                Object[] params={ep};
                
                String[] types={org.switchyard.event.EventObserver.class.getName()};
                
                mbs.invoke(objname, "removeObserver", params, types);
           }
            
        } catch (Exception e) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "Failed to unregister SwitchYard event observer via MBean", e);
            }
        }

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("SwitchYard EventProcessorManager Close Completed");
        }
    }
}
