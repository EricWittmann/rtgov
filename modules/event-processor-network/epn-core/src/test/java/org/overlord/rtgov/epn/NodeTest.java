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
package org.overlord.rtgov.epn;

import static org.junit.Assert.*;

import java.io.Serializable;

import org.junit.Test;
import org.overlord.rtgov.epn.EventList;
import org.overlord.rtgov.epn.Node;
import org.overlord.rtgov.ep.Predicate;
import org.overlord.rtgov.epn.testdata.TestChannel;
import org.overlord.rtgov.epn.testdata.TestEPNContainer;
import org.overlord.rtgov.epn.testdata.TestEvent1;
import org.overlord.rtgov.epn.testdata.TestEvent2;
import org.overlord.rtgov.epn.testdata.TestEventProcessorA;

public class NodeTest {

    @Test
    public void testPredicateAllowsEvent() {
        Node node=new Node();
        
        node.setPredicate(new Predicate() {
            public boolean evaluate(Object event) {
                return true;
            }
        });
        
        TestEventProcessorA ep=new TestEventProcessorA();
        
        node.setEventProcessor(ep);
        
        java.util.List<Serializable> eventsList=new java.util.ArrayList<Serializable>();
        EventList events=new EventList(eventsList);
        
        TestEvent1 te1=new TestEvent1(1);
        TestEvent2 te2=new TestEvent2(2);
        
        eventsList.add(te1);
        eventsList.add(te2);
        
        try {
            EventList retry=node.process(null, null, events, 1);
            
            if (retry != null) {
                fail("Should be no retries");
            }
            
            if (ep.getEvents().size() != 2) {
                fail("Should be two events processed: "+ep.getEvents().size());
            }
            
        } catch(Exception e) {
            fail("Failed to process events");
        }
    }

    @Test
    public void testPredicateDisallowsEvent() {
        Node node=new Node();
        
        node.setPredicate(new Predicate() {
            public boolean evaluate(Object event) {
                return false;
            }
        });
        
        TestEventProcessorA ep=new TestEventProcessorA();
        
        node.setEventProcessor(ep);
        
        java.util.List<Serializable> eventsList=new java.util.ArrayList<Serializable>();
        EventList events=new EventList(eventsList);
        
        TestEvent1 te1=new TestEvent1(1);
        TestEvent2 te2=new TestEvent2(2);
        
        eventsList.add(te1);
        eventsList.add(te2);
        
        try {
            EventList retry=node.process(null, null, events, 1);
            
            if (retry != null) {
                fail("Should be no retries");
            }
            
            if (ep.getEvents().size() != 0) {
                fail("Should be 0 events processed: "+ep.getEvents().size());
            }
            
        } catch(Exception e) {
            fail("Failed to process events");
        }
    }

    @Test
    public void testEventRetry() {
        Node node=new Node();
        
        node.setPredicate(new Predicate() {
            public boolean evaluate(Object event) {
                return true;
            }
        });
        
        TestEventProcessorA ep=new TestEventProcessorA();
        
        node.setEventProcessor(ep);
        
        java.util.List<Serializable> eventsList=new java.util.ArrayList<Serializable>();
        EventList events=new EventList(eventsList);
        
        TestEvent1 te1=new TestEvent1(1);
        TestEvent2 te2=new TestEvent2(2);
        
        eventsList.add(te1);
        eventsList.add(te2);
        
        ep.retry(te1);
        
        try {
            EventList retry=node.process(null, null, events, 1);
            
            if (retry == null) {
                fail("Should be retries");
            }
            
            if (retry.size() != 1 && !retry.contains(te1)) {
                fail("Expecting te1 to be in retry list");
            }
            
            if (ep.getEvents().size() != 1 && !ep.getEvents().contains(te2)) {
                fail("Should be one event processed: "+ep.getEvents().size());
            }
            
        } catch(Exception e) {
            fail("Failed to process events");
        }
    }

    @Test
    public void testEventForward() {
        Node node=new Node();
        
        node.setPredicate(new Predicate() {
            public boolean evaluate(Object event) {
                return true;
            }
        });
        
        TestEventProcessorA ep=new TestEventProcessorA();
        
        node.setEventProcessor(ep);
        
        node.getSourceNodes().add("TestNode");
        
        java.util.List<Serializable> eventsList=new java.util.ArrayList<Serializable>();
        EventList events=new EventList(eventsList);
        
        TestEvent1 te1=new TestEvent1(1);
        TestEvent2 te2=new TestEvent2(2);
        
        eventsList.add(te1);
        eventsList.add(te2);

        TestChannel channel=new TestChannel();
        
        TestEPNContainer container=new TestEPNContainer(channel);
        
        ep.setForwardEvents(true);
        
        try {
            node.getChannels().add(channel);
            
            node.init();
            
            EventList retry=node.process(container, null, events, 1);
            
            if (retry != null) {
                fail("Should be no retries");
            }
            
            if (ep.getEvents().size() != 2) {
                fail("Should be two events processed: "+ep.getEvents().size());
            }
            
            if (channel.getEvents().size() != 2) {
                fail("Should be two forwarded events: "+channel.getEvents().size());
            }
            
        } catch(Exception e) {
            fail("Failed to process events");
        }
    }

    @Test
    public void testEventForwardAndRetry() {
        Node node=new Node();
        
        node.setPredicate(new Predicate() {
            public boolean evaluate(Object event) {
                return true;
            }
        });
        
        TestEventProcessorA ep=new TestEventProcessorA();
        
        node.setEventProcessor(ep);
        
        node.getSourceNodes().add("TestNode");
        
        java.util.List<Serializable> eventsList=new java.util.ArrayList<Serializable>();
        EventList events=new EventList(eventsList);
        
        TestEvent1 te1=new TestEvent1(1);
        TestEvent2 te2=new TestEvent2(2);
        
        eventsList.add(te1);
        eventsList.add(te2);

        TestChannel channel=new TestChannel();
        
        TestEPNContainer container=new TestEPNContainer(channel);
        
        ep.setForwardEvents(true);
        ep.retry(te2);
        
        try {
            node.getChannels().add(channel);
            
            node.init();
            
            EventList retry=node.process(container, null, events, 1);
            
            if (retry == null) {
                fail("Should be retries");
            }
            
            if (retry.size() != 1 && !retry.contains(te2)) {
                fail("Expecting te2 in retry list");
            }
            
            if (ep.getEvents().size() != 1) {
                fail("Should be one event processed: "+ep.getEvents().size());
            }
            
            if (channel.getEvents().size() != 1) {
                fail("Should be one forwarded event: "+channel.getEvents().size());
            }
            
        } catch(Exception e) {
            fail("Failed to process events");
        }
    }
    
}
