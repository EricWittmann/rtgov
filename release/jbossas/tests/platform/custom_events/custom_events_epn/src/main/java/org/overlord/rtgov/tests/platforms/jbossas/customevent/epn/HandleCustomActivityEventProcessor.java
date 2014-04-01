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
package org.overlord.rtgov.tests.platforms.jbossas.customevent.epn;

import java.io.Serializable;

import org.overlord.rtgov.activity.model.soa.RequestReceived;
import org.overlord.rtgov.tests.platforms.jbossas.customevent.data.CustomActivityEvent;

/**
 * This class provides a test implementation of the EventProcessor
 * interface, used to receive custom events.
 *
 */
public class HandleCustomActivityEventProcessor extends org.overlord.rtgov.ep.EventProcessor {

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable process(String source, Serializable event,
            int retriesLeft) throws Exception {
        Serializable ret=null;
        
        if (event instanceof CustomActivityEvent) {
            System.out.println(">>> CUSTOM ACTIVITY ASSOCIATED WITH: "
                    +((CustomActivityEvent)event).getActivityType());
            
            if (((CustomActivityEvent)event).getActivityType() instanceof RequestReceived) {
                RequestReceived rr=(RequestReceived)((CustomActivityEvent)event).getActivityType();
                
                if (rr.getProperties().containsKey("item")
                        && rr.getProperties().get("item").equals("JAM")) {
                    ret = event;
                }
            }
        } else {
            System.out.println(">>> NOT CUSTOM ACTIVITY EVENT: "+event);
        }
        
        return (ret);
    }

}
