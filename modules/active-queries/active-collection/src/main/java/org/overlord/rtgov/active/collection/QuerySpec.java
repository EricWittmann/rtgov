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
package org.overlord.rtgov.active.collection;

import org.overlord.rtgov.active.collection.predicate.Predicate;

/**
 * This class defines the query specification that can be used to define
 * the active collection that is required. If a name and no predicate/parent
 * is specified, then a lookup on the name will be returned. If the query
 * spec defines a name, predicate and parent collection, then initially
 * a check will be made for the collection name. If it does not exist, then
 * a derived collection of that name will be created based on the parent
 * collection name and predicate.
 *
 */
public class QuerySpec {

    private String _collection=null;
    private Predicate _predicate=null;
    private String _parent=null;
    private int _maxItems=0;
    private Truncate _truncate=Truncate.Start;
    private Style _style=Style.Normal;
    private java.util.Map<String,Object> _properties=new java.util.HashMap<String, Object>();
    
    /**
     * The default constructor.
     */
    public QuerySpec() {
    }
    
    /**
     * This method returns the name of the collection.
     * 
     * @return The collection name
     */
    public String getCollection() {
        return (_collection);
    }
    
    /**
     * This method sets the name of the collection.
     * 
     * @param name The collection name
     */
    public void setCollection(String name) {
        _collection = name;
    }
    
    /**
     * This method returns the optional predicate. If specified,
     * along with the parent collection name, it can
     * be used to derive a sub-collection.
     * 
     * @return The predicate
     */
    public Predicate getPredicate() {
        return (_predicate);
    }
    
    /**
     * This method sets the optional predicate. If specified,
     * along with the parent collection name, it can
     * be used to derive a sub-collection.
     * 
     * @param pred The predicate
     */
    public void setPredicate(Predicate pred) {
        _predicate = pred;
    }

    /**
     * This method returns the name of the parent collection.
     * 
     * @return The parent collection name
     */
    public String getParent() {
        return (_parent);
    }
    
    /**
     * This method sets the name of the parent collection.
     * 
     * @param name The parent collection name
     */
    public void setParent(String name) {
        _parent = name;
    }
    
    /**
     * This method returns the maximum number of items
     * that should be included in the query result,
     * or 0 if unrestricted.
     * 
     * @return The maximum number of items, or 0 if unrestricted
     */
    public int getMaxItems() {
        return (_maxItems);
    }
    
    /**
     * This method sets the maximum number of items
     * that should be included in the query result,
     * or 0 if unrestricted.
     * 
     * @param maxItems The maximum number of items, or 0 if unrestricted
     */
    public void setMaxItems(int maxItems) {
        _maxItems = maxItems;
    }
    
    /**
     * This method returns which part of the collection
     * should be truncated if the collection contains
     * more items than can be returned in the query result.
     * 
     * @return The truncation location
     */
    public Truncate getTruncate() {
        return (_truncate);
    }
    
    /**
     * This method sets which part of the collection
     * should be truncated if the collection contains
     * more items than can be returned in the query result.
     * 
     * @param truncate The truncation location
     */
    public void setTruncate(Truncate truncate) {
        _truncate = truncate;
    }
    
    /**
     * This method returns the style that should be used
     * for returning the result.
     * 
     * @return The style
     */
    public Style getStyle() {
        return (_style);
    }
    
    /**
     * This method sets the style that should be used
     * for returning the result.
     * 
     * @param style The style
     */
    public void setStyle(Style style) {
        _style = style;
    }
    
    /**
     * This method returns the additional properties.
     * 
     * @return The properties
     */
    public java.util.Map<String,Object> getProperties() {
        return (_properties);
    }
    
    /**
     * This method sets the additional properties.
     * 
     * @param props The properties
     */
    public void setProperties(java.util.Map<String,Object> props) {
        _properties = props;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return ("Query[collection="+_collection+" parent="
                    +_parent+" predicate="+_predicate
                    +" maxItems="+_maxItems+" truncate="
                    +_truncate+" style="+_style+"]");
    }
    
    /**
     * This enumerated type defines where truncation should
     * occur if the collection size is greater than the
     * maximum number of items specified.
     */
    public enum Truncate {
        
        /**
         * This value indicates that the last portion of the
         * collection should be returned.
         */
        Start,
        
        /**
         * This value indicates that the first portion of the
         * collection should be returned.
         */
        End
    }
    
    /**
     * This enumerated type defines the style used for returning
     * the results.
     */
    public enum Style {
        
        /**
         * This value indicates that the results should be returned
         * as normal.
         */
        Normal,
        
        /**
         * This value indicates that the results should be returned
         * in reverse order.
         */
        Reversed
    }
}
