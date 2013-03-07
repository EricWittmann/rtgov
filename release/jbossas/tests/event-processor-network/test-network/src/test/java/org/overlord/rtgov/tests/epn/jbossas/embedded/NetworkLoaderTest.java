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
package org.overlord.rtgov.tests.epn.jbossas.embedded;

import static org.junit.Assert.*;

import org.junit.Test;
import org.overlord.rtgov.epn.EPNManager;
import org.overlord.rtgov.epn.Network;
import org.overlord.rtgov.epn.Node;
import org.overlord.rtgov.tests.epn.Child;
import org.overlord.rtgov.tests.epn.NetworkLoader;
import org.overlord.rtgov.tests.epn.Obj1;
import org.overlord.rtgov.tests.epn.Obj2;


public class NetworkLoaderTest {

    @Test
    public void testLoadNetwork() {
        NetworkLoader tl=new NetworkLoader();
        
        Network net=tl.loadNetwork();
        
        if (net == null) {
            fail("Failed to load network");
        }
    }
    
    protected EPNManager getEPNManager() {
        EPNManager ret=null;
        
        try {
            Class<?> cls=
                    NetworkLoaderTest.class.getClassLoader().loadClass("org.overlord.rtgov.epn.embedded.EmbeddedEPNManager");
            ret = (EPNManager)cls.newInstance();
            
        } catch(Exception e) {
            fail("Failed to get EPNManager: "+e);
        }
        return(ret);
    }

    @Test
    public void testTransformation() {
        NetworkLoader tl=new NetworkLoader();
        
        Network net=tl.loadNetwork();
        
        java.util.List<java.io.Serializable> events=new java.util.Vector<java.io.Serializable>();
        
        Obj1 o1=new Obj1(5);
        events.add(o1);
        
        EPNManager mgr=getEPNManager();
        
        try {
            mgr.register(net);
        } catch(Exception e) {
            fail("Failed to register network: "+e);
        }
        
        try {
            mgr.publish(NetworkLoader.TEST_SUBJECT, events);
            
            Thread.sleep(1000);
            
        } catch(Exception e) {
            fail("Failed to process events: "+e);
        }
        
        // Check that event was transformed into Obj2 at ChildA (due to predicate)
        Node childAnode=net.getNode(NetworkLoader.CHILD_A);
        
        if (childAnode == null) {
            fail("Failed to get child A");
        }
        
        Child childA=(Child)childAnode.getEventProcessor();
        
        if (childA.events().size() != 1) {
            fail("Child A does not have 1 event: "+childA.events().size());
        }
        
        if (!(childA.events().get(0) instanceof Obj2)) {
            fail("Child A event is not correct type");
        }
        
        if (((Obj2)childA.events().get(0)).getValue() != 5) {
            fail("Child A event has wrong value: "+((Obj2)childA.events().get(0)).getValue());
        }
    }

    @Test
    public void testPredicates() {
        NetworkLoader tl=new NetworkLoader();
        
        Network net=tl.loadNetwork();
        
        java.util.List<java.io.Serializable> events=new java.util.Vector<java.io.Serializable>();
        
        Obj1 o1=new Obj1(15);
        Obj1 o2=new Obj1(5);
        Obj1 o3=new Obj1(12);
        events.add(o1);
        events.add(o2);
        events.add(o3);
        
        EPNManager mgr=getEPNManager();
        
        try {
            mgr.register(net);
        } catch(Exception e) {
            fail("Failed to register network: "+e);
        }
        
        try {
            mgr.publish(NetworkLoader.TEST_SUBJECT, events);
            
            Thread.sleep(1000);
            
        } catch(Exception e) {
            fail("Failed to process events: "+e);
        }
        
        Node childAnode=net.getNode(NetworkLoader.CHILD_A);
        Node childBnode=net.getNode(NetworkLoader.CHILD_B);
        
        if (childAnode == null) {
            fail("Failed to get child A");
        }
        
        if (childBnode == null) {
            fail("Failed to get child B");
        }
        
        Child childA=(Child)childAnode.getEventProcessor();
        Child childB=(Child)childBnode.getEventProcessor();
        
        if (childA.events().size() != 1) {
            fail("Child A does not have 1 event: "+childA.events().size());
        }
        
        if (childB.events().size() != 2) {
            fail("Child B does not have 2 events: "+childB.events().size());
        }
        
        if (childA.retries().size() != 0) {
            fail("Child A should have no retries: "+childA.retries().size());
        }
        
        if (childB.retries().size() != 0) {
            fail("Child B should have no retries: "+childB.retries().size());
        }
    }

    @Test
    public void testRetries() {
        NetworkLoader tl=new NetworkLoader();
        
        Network net=tl.loadNetwork();
        
        java.util.List<java.io.Serializable> events=new java.util.Vector<java.io.Serializable>();
        
        Obj1 o1=new Obj1(15);
        Obj1 o2=new Obj1(5);
        Obj1 o3=new Obj1(12);
        events.add(o1);
        events.add(o2);
        events.add(o3);
        
        EPNManager mgr=getEPNManager();
        
        try {
            mgr.register(net);
        } catch(Exception e) {
            fail("Failed to register network: "+e);
        }
        
        Node childAnode=net.getNode(NetworkLoader.CHILD_A);
        Node childBnode=net.getNode(NetworkLoader.CHILD_B);
        
        if (childAnode == null) {
            fail("Failed to get child A");
        }
        
        if (childBnode == null) {
            fail("Failed to get child B");
        }
        
        Child childA=(Child)childAnode.getEventProcessor();
        Child childB=(Child)childBnode.getEventProcessor();
        
        Obj2 rej3=new Obj2(o3.getValue());
        childB.reject(rej3);
        
        try {
            mgr.publish(NetworkLoader.TEST_SUBJECT, events);
            
            Thread.sleep(1000);
            
        } catch(Exception e) {
            fail("Failed to process events: "+e);
        }
        
        if (childA.events().size() != 1) {
            fail("Child A does not have 1 event: "+childA.events().size());
        }
        
        if (childB.events().size() != 2) {
            fail("Child B does not have 2 events: "+childB.events().size());
        }
        
        if (childA.retries().size() != 0) {
            fail("Child A should have no retries: "+childA.retries().size());
        }
        
        if (childB.retries().size() != 1) {
            fail("Child B should have 1 retries: "+childB.retries().size());
        }
        
        if (!childB.retries().contains(rej3)) {
            fail("Child B retry event is wrong");
        }
    }
}
