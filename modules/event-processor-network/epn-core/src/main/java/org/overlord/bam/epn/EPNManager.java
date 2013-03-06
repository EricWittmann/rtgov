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
package org.overlord.rtgov.epn;

/**
 * This interfaces represents the Event Process Network
 * Manager.
 *
 */
public interface EPNManager {
    
    /**
     * The URI for the EPNManager registered in JNDI.
     */
    public static final String URI="java:global/overlord-rtgov/EPNManager";

    /**
     * This method registers a network.
     * 
     * @param network The network
     * @throws Exception Failed to register the network
     */
    public void register(Network network) throws Exception;
    
    /**
     * This method unregisters a network, associated with
     * the supplied name and version.
     * 
     * @param networkName The network name
     * @param version The version, or null if current
     * @throws Exception Failed to unregister the network
     */
    public void unregister(String networkName, String version) throws Exception;
    
    /**
     * This method registers a network listener.
     * 
     * @param l The listener
     */
    public void addNetworkListener(NetworkListener l);
    
    /**
     * This method unregisters a network listener.
     * 
     * @param l The listener
     */
    public void removeNetworkListener(NetworkListener l);
    
    /**
     * This method registers a node listener for the specified network.
     * 
     * @param network The network to listen to
     * @param l The listener
     */
    public void addNotificationListener(String network, NotificationListener l);
    
    /**
     * This method unregisters a node listener for the specified network.
     * 
     * @param network The network was listening to
     * @param l The listener
     */
    public void removeNotificationListener(String network, NotificationListener l);
    
    /**
     * This method publishes the supplied events to be processed
     * by any network subscribed to the nominated subject.
     * 
     * @param subject The subject upon which to publish the events
     * @param events The list of events to be processed
     * @throws Exception Failed to publish the events
     */
    public void publish(String subject,
                 java.util.List<? extends java.io.Serializable> events) throws Exception;
    
    /**
     * This method closes the manager.
     * 
     * @throws Exception Failed to close manager
     */
    public void close() throws Exception;
    
}
