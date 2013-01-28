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
package org.overlord.bam.active.collection.infinispan;

import static org.junit.Assert.*;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.junit.Test;
import org.overlord.bam.active.collection.ActiveChangeListener;
import org.overlord.bam.active.collection.ActiveCollectionSource;
import org.overlord.bam.active.collection.infinispan.InfinispanActiveMapTest.TestChange.TestChangeType;

public class InfinispanActiveMapTest {

    @Test
    public void testNotifications() {
        
        CacheContainer cc=org.overlord.bam.common.util.InfinispanUtil.getCacheContainer(null);
        
        if (cc == null) {
            fail("Failed to get default cache container");
        }
        
        Cache<Object,Object> cache=cc.getCache("Test");
        
        ActiveCollectionSource acs=new ActiveCollectionSource();
        acs.setName("Test");
        
        InfinispanActiveMap iam=new InfinispanActiveMap(acs, cache);
        
        TestListener l=new TestListener();
        
        iam.addActiveChangeListener(l);
        
        cache.put("t1", "v1");
        cache.put("t2", "v2");
        cache.put("t1", "v1.1");        
        cache.remove("t2");
        
        if (l._changes.size() != 4) {
            fail("Expecting 4 notifications: "+l._changes.size());
        }
        
        if (l._changes.get(0)._type != TestChangeType.Insert) {
            fail("0 should be insert");
        }
        
        if (!l._changes.get(0)._key.equals("t1")) {
            fail("0 key incorrect");
        }
        
        if (!l._changes.get(0)._value.equals("v1")) {
            fail("0 value incorrect");
        }
        
        if (l._changes.get(1)._type != TestChangeType.Insert) {
            fail("1 should be insert");
        }
        
        if (!l._changes.get(1)._key.equals("t2")) {
            fail("1 key incorrect");
        }
        
        if (!l._changes.get(1)._value.equals("v2")) {
            fail("1 value incorrect");
        }
        
        if (l._changes.get(2)._type != TestChangeType.Update) {
            fail("2 should be Update");
        }
        
        if (!l._changes.get(2)._key.equals("t1")) {
            fail("2 key incorrect");
        }
        
        if (!l._changes.get(2)._value.equals("v1.1")) {
            fail("2 value incorrect");
        }
        
        if (l._changes.get(3)._type != TestChangeType.Remove) {
            fail("3 should be Remove");
        }

        if (!l._changes.get(3)._key.equals("t2")) {
            fail("3 key incorrect");
        }
        
        if (!l._changes.get(3)._value.equals("v2")) {
            fail("3 value incorrect");
        }
        
    }
    
    public static class TestChange {
        public enum TestChangeType {
            Insert,
            Update,
            Remove
        }
        
        public TestChangeType _type=null;
        public Object _key=null;
        public Object _value=null;
    }

    public static class TestListener implements ActiveChangeListener {
        
        private java.util.List<TestChange> _changes=new java.util.Vector<TestChange>();
        
        public void inserted(Object key, Object value) {
            TestChange tc=new TestChange();
            tc._type = TestChangeType.Insert;
            tc._key = key;
            tc._value = value;
            _changes.add(tc);
        }

        public void updated(Object key, Object value) {
            TestChange tc=new TestChange();
            tc._type = TestChangeType.Update;
            tc._key = key;
            tc._value = value;
            _changes.add(tc);
        }

        public void removed(Object key, Object value) {
            TestChange tc=new TestChange();
            tc._type = TestChangeType.Remove;
            tc._key = key;
            tc._value = value;
            _changes.add(tc);
        }
        
    }
}
