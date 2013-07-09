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
package org.overlord.rtgov.active.collection.epn;

import static org.junit.Assert.*;

import java.io.Serializable;
import java.util.List;

import org.junit.Test;
import org.overlord.rtgov.active.collection.ActiveCollectionType;
import org.overlord.rtgov.active.collection.ActiveList;
import org.overlord.rtgov.active.collection.epn.EPNActiveCollectionSource;
import org.overlord.rtgov.epn.AbstractEPNManager;
import org.overlord.rtgov.epn.EPNContainer;
import org.overlord.rtgov.epn.EPNManager;
import org.overlord.rtgov.epn.EventList;
import org.overlord.rtgov.epn.Network;
import org.overlord.rtgov.epn.NetworkListener;
import org.overlord.rtgov.epn.NotificationListener;

public class EPNActiveCollectionSourceTest {

    private static final String T_OBJ3 = "TObj3";
    private static final String T_OBJ2 = "TObj2";
    private static final String T_OBJ1 = "TObj1";
    private static final String TEST_ACTIVE_LIST = "TestActiveList";
    private static final String TEST_SUBJECT1 = "TestSubject1";
    private static final String TEST_SUBJECT2 = "TestSubject2";
    private static final String TEST_SUBJECT3 = "TestSubject3";

    @Test
    public void testSubjectAndTypeFiltering() {
        EPNActiveCollectionSource acs=new EPNActiveCollectionSource();
        
        acs.setActiveCollection(new ActiveList(TEST_ACTIVE_LIST));
        acs.setName(TEST_ACTIVE_LIST);
        
        acs.setSubject(TEST_SUBJECT1);
        
        acs.setType(ActiveCollectionType.List);
        
        TestEPNManager mgr=new TestEPNManager();
        acs.setEPNManager(mgr);
        
        try {
            acs.init(null);
        } catch(Exception e) {
            fail("Failed to initialize active collection source: "+e);
        }
        
        java.util.List<Serializable> resultList=new java.util.ArrayList<Serializable>();
        
        java.util.List<Serializable> eventList=new java.util.ArrayList<Serializable>();
        eventList.add(new TestObject(T_OBJ1, 1));
        eventList.add(new TestObject(T_OBJ2, 2));
        eventList.add(new TestObject(T_OBJ3, 3));
        
        resultList.addAll(eventList);
        
        EventList events=new EventList(eventList);
        
        mgr.publish(TEST_SUBJECT1, events);
        
        java.util.List<Serializable> eventList2=new java.util.ArrayList<Serializable>();
        eventList2.add(new TestObject("TObj21", 21));
        eventList2.add(new TestObject("TObj22", 22));
        eventList2.add(new TestObject("TObj23", 23));
        
        EventList events2=new EventList(eventList2);
        
        mgr.publish(TEST_SUBJECT2, events2);
        
        java.util.List<Serializable> eventList3=new java.util.ArrayList<Serializable>();
        eventList3.add(new TestObject("TObj31", 31));
        eventList3.add(new TestObject("TObj32", 32));
        eventList3.add(new TestObject("TObj33", 33));

        resultList.addAll(eventList3);

        EventList events3=new EventList(eventList3);
        
        mgr.publish(TEST_SUBJECT1, events3);
        
        java.util.List<Serializable> eventList4=new java.util.ArrayList<Serializable>();
        eventList4.add(new TestObject("TObj41", 41));
        eventList4.add(new TestObject("TObj42", 42));
        eventList4.add(new TestObject("TObj43", 43));
        
        EventList events4=new EventList(eventList4);
        
        mgr.publish(TEST_SUBJECT3, events4);
        
        // Review results
        ActiveList al=(ActiveList)acs.getActiveCollection();
        
        if (al.getSize() != 6) {
            fail("Should only be 6 events: "+al.getSize());
        }
        
        for (Object obj : al) {
            if (obj.equals(resultList.get(0))) {
                resultList.remove(0);
            } else {
                fail("Failed to match: "+obj+" with "+resultList.get(0));
            }
        }
        
    }

    public class TestEPNManager extends AbstractEPNManager implements EPNManager {
        
        private java.util.List<NotificationListener> _nodeListeners=new java.util.ArrayList<NotificationListener>();
        private java.util.List<NetworkListener> _networkListeners=new java.util.ArrayList<NetworkListener>();

        public void register(Network network) throws Exception {
        }

        public void unregister(String networkName, String version)
                throws Exception {
        }

        public void addNotificationListener(String network, NotificationListener l) {
            _nodeListeners.add(l);
        }

        public void removeNotificationListener(String network, NotificationListener l) {
            _nodeListeners.remove(l);
        }
        
        public void addNetworkListener(NetworkListener l) {
            _networkListeners.add(l);
        }

        public void removeNetworkListener(NetworkListener l) {
            _networkListeners.remove(l);
        }
        
        public void publish(String subject, EventList events) {
            for (NotificationListener l : _nodeListeners) {
                l.notify(subject, events);
            }
        }

        public void publish(String subject, List<? extends Serializable> events)
                throws Exception {
        }

        public void close() throws Exception {
        }

        protected EPNContainer getContainer() {
            return null;
        }
        
    }
}
