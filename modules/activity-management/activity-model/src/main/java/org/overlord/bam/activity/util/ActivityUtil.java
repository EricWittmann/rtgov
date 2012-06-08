/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008-11, Red Hat Middleware LLC, and others contributors as indicated
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
package org.overlord.bam.activity.util;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;
import org.overlord.bam.activity.model.ActivityUnit;

/**
 * This class provides utility functions for the activity
 * model.
 *
 */
public final class ActivityUtil {
    
    private static final ObjectMapper MAPPER=new ObjectMapper();

    private static final TypeReference<java.util.List<ActivityUnit>> ACTIVITY_LIST=
                        new TypeReference<java.util.List<ActivityUnit>>() { };
    
    static {
        SerializationConfig config=MAPPER.getSerializationConfig()
                .withSerializationInclusion(JsonSerialize.Inclusion.NON_NULL)
                .withSerializationInclusion(JsonSerialize.Inclusion.NON_DEFAULT);
        
        MAPPER.setSerializationConfig(config);
    }
    
    /**
     * Private constructor.
     */
    private ActivityUtil() {
    }
    
    /**
     * This method serializes an Activity event into a JSON representation.
     * 
     * @param act The activity
     * @return The JSON serialized representation
     * @throws Exception Failed to serialize
     */
    public static byte[] serialize(ActivityUnit act) throws Exception {
        byte[] ret=null;
        
        java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
        
        MAPPER.writeValue(baos, act);
        
        ret = baos.toByteArray();
        
        baos.close();
        
        return (ret);
    }

    /**
     * This method serializes an object into a JSON string representation.
     * 
     * @param obj The object
     * @return The JSON serialized string representation
     * @throws Exception Failed to serialize
     */
    public static String objectToJSONString(Object obj) throws Exception {
        String ret=null;
        
        java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
        
        MAPPER.writeValue(baos, obj);
        
        ret = new String(baos.toByteArray());
        
        baos.close();
        
        return (ret);
    }

    /**
     * This method serializes an Activity event list into a JSON representation.
     * 
     * @param activities The activity list
     * @return The JSON serialized representation
     * @throws Exception Failed to serialize
     */
    public static byte[] serializeList(java.util.List<ActivityUnit> activities) throws Exception {
        byte[] ret=null;
        
        java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
        
        MAPPER.writeValue(baos, activities);
        
        ret = baos.toByteArray();
        
        baos.close();
        
        return (ret);
    }

    /**
     * This method deserializes an Activity event from a JSON representation.
     * 
     * @param act The JSON representation of the activity
     * @return The Activity event
     * @throws Exception Failed to deserialize
     */
    public static ActivityUnit deserialize(byte[] act) throws Exception {
        ActivityUnit ret=null;
        
        java.io.ByteArrayInputStream bais=new java.io.ByteArrayInputStream(act);
        
        ret = MAPPER.readValue(bais, ActivityUnit.class);
        
        bais.close();
        
        return (ret);
    }

    /**
     * This method deserializes an Activity event list from a JSON representation.
     * 
     * @param act The JSON representation of the activity
     * @return The Activity event list
     * @throws Exception Failed to deserialize
     */
    public static java.util.List<ActivityUnit> deserializeList(byte[] act) throws Exception {
        java.util.List<ActivityUnit> ret=null;
        
        java.io.ByteArrayInputStream bais=new java.io.ByteArrayInputStream(act);
        
        ret = MAPPER.readValue(bais, ACTIVITY_LIST);
        
        bais.close();
        
        return (ret);
    }
}
