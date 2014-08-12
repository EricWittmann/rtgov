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

import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.overlord.rtgov.common.util.VersionUtil;
import org.overlord.rtgov.epn.validation.EPNValidationListener;
import org.overlord.rtgov.epn.validation.EPNValidator;
import org.overlord.rtgov.internal.epn.jmx.EPNManagement;

/**
 * This class represents the abstract Event Process Network Manager
 * used as the base for any concrete implementation.
 *
 */
public abstract class AbstractEPNManager implements EPNManager {
    
    private static final Logger LOG=Logger.getLogger(AbstractEPNManager.class.getName());

    private java.util.Map<String, NetworkList> _networkMap=new java.util.HashMap<String, NetworkList>();
    private java.util.Map<String, java.util.List<Network>> _subjectMap=
                        new java.util.HashMap<String, java.util.List<Network>>();
    private java.util.Map<String, java.util.List<NotificationListener>> _notificationListeners=
                        new java.util.HashMap<String, java.util.List<NotificationListener>>();
    private java.util.List<NetworkListener> _networkListeners=
                        new java.util.ArrayList<NetworkListener>();
    private boolean _usePrePostEventListProcessing=false;
    
    private EPNManagement _epnManagement=null;
    
    /**
     * This is the default constructor.
     */
    public AbstractEPNManager() {
    }
    
    /**
     * Initialize the EPNManager.
     */
    public void init() {
        // Check if managed
        if (isManaged()) {
            _epnManagement = new EPNManagement(this);
            _epnManagement.init();
        }
    }
    
    /**
     * This method returns the Event Processor Network Container.
     * 
     * @return The container
     */
    protected abstract EPNContainer getContainer();
    
    /**
     * This method determines whether the EPNManager is managed. If managed,
     * then its MBean will be registered with the MBeanServer and available
     * to JMX compliant management systems.
     * 
     * @return Whether the EPNManager is managed
     */
    protected boolean isManaged() {
        return (false);
    }
    
    /**
     * {@inheritDoc}
     */
    public void register(Network network) throws Exception {
        
        LOG.info(MessageFormat.format(java.util.PropertyResourceBundle.getBundle(
                "epn-core.Messages").getString("EPN-CORE-13"),
                network.getName(), network.getVersion()));
        
        network.init(getContainer());
        
        // Validate network
        if (!EPNValidator.validate(network, getValidationListener())) {
            // Close the network
            network.close();
            
            throw new Exception(MessageFormat.format(java.util.PropertyResourceBundle.getBundle(
                    "epn-core.Messages").getString("EPN-CORE-12"),
                    network.getName(), network.getVersion()));
        }
        
        synchronized (_networkMap) {
            NetworkList nl=_networkMap.get(network.getName());
            
            if (nl == null) {
                nl = new NetworkList();
                _networkMap.put(network.getName(), nl);
            }
            
            Network oldnet=nl.getCurrent();
            
            // Add registered network to the list
            nl.add(network);
            
            // Check if current instance has changed to the
            // newly registered network
            if (nl.getCurrent() == network) {
                currentNetworkChanged(oldnet, network);
            }
        }
        
        synchronized (_networkListeners) {
            for (int i=0; i < _networkListeners.size(); i++) {
                _networkListeners.get(i).registered(network);
            }
        }
    }
    
    /**
     * This method returns the validation listener.
     * 
     * @return The validation listener
     */
    protected EPNValidationListener getValidationListener() {
        return (new EPNValidationListener() {

            public void error(Network epn, Object target, String issue) {
                LOG.severe(issue);
            }
            
        });
    }
    
    /**
     * {@inheritDoc}
     */
    public void unregister(String networkName, String version) throws Exception {
        
        LOG.info(MessageFormat.format(java.util.PropertyResourceBundle.getBundle(
                "epn-core.Messages").getString("EPN-CORE-14"),
                networkName, version));
        
        Network network=null;

        synchronized (_networkMap) {
            NetworkList nl=_networkMap.get(networkName);
            
            if (nl != null) {
                network = (version == null ? nl.getCurrent() : nl.getVersion(version));
                
                if (network != null) {
                    Network oldcur=nl.getCurrent();
                    
                    nl.remove(network);
                    
                    Network newcur=nl.getCurrent();
                    
                    if (newcur != oldcur) {
                        currentNetworkChanged(oldcur, newcur);
                    }
                }
                
                if (nl.size() == 0) {
                    _networkMap.remove(networkName);
                }
            }
        }
        
        if (network != null) {
            synchronized (_networkListeners) {
                for (int i=0; i < _networkListeners.size(); i++) {
                    _networkListeners.get(i).unregistered(network);
                }
            }
            
            network.close();
        }
    }
    
    /**
     * This method is called to handle a change in the current version
     * of a network, due to a new (more recent) version being registered
     * or a current version being unregistered.
     * 
     * @param oldNet The old network version
     * @param newNet The new network version
     */
    protected void currentNetworkChanged(Network oldNet, Network newNet) {
        if (oldNet != null) {
            unregisterSubjects(oldNet);
        }
        
        if (newNet != null) {
            registerSubjects(newNet);
        }
    }
    
    /**
     * This method registers the supplied network against the
     * subjects it is interested in.
     * 
     * @param network The network
     */
    protected void registerSubjects(Network network) {
        for (String subject : network.subjects()) {
            java.util.List<Network> networks=_subjectMap.get(subject);
            
            if (networks == null) {
                networks = new java.util.concurrent.CopyOnWriteArrayList<Network>();
                _subjectMap.put(subject, networks);
            }
            
            networks.add(network);
        }
    }
    
    /**
     * This method unregisters the supplied network from the
     * subjects it is interested in.
     * 
     * @param network The network
     */
    protected void unregisterSubjects(Network network) {
        for (String subject : network.subjects()) {
            java.util.List<Network> networks=_subjectMap.get(subject);
            
            if (networks != null) {
                networks.remove(network);
                if (networks.size() == 0) {
                    _subjectMap.remove(subject);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addNotificationListener(String subject, NotificationListener l) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Register notification listener="+l+" on subject="+subject);
        }
        
        synchronized (_notificationListeners) {
            java.util.List<NotificationListener> listeners=_notificationListeners.get(subject);
            
            if (listeners == null) {
                listeners = new java.util.ArrayList<NotificationListener>();
                _notificationListeners.put(subject, listeners);
            }
            
            listeners.add(l);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeNotificationListener(String subject, NotificationListener l) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Unregister notification listener="+l+" on subject="+subject);
        }
        
        synchronized (_notificationListeners) {
            java.util.List<NotificationListener> listeners=_notificationListeners.get(subject);
            
            if (listeners != null) {                
                listeners.remove(l);
                
                if (listeners.size() == 0) {
                    _notificationListeners.remove(subject);
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void addNetworkListener(NetworkListener l) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Register network listener="+l);
        }
        
        synchronized (_networkListeners) {
            _networkListeners.add(l);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void removeNetworkListener(NetworkListener l) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Unregister network listener="+l);
        }
        
        synchronized (_networkListeners) {
            _networkListeners.remove(l);
        }
    }
    
    /**
     * This method sets the last accessed timestamp on the supplied
     * network.
     * 
     * @param network The network
     * @param timestamp The timestamp
     */
    protected void setLastAccessed(Network network, long timestamp) {
        network.lastAccessed(timestamp);
    }
    
    /**
     * This method returns the network associated with the
     * supplied name.
     * 
     * @param name The network name
     * @param version The version, or null for current version
     * @return The network, or null if not found
     */
    protected Network getNetwork(String name, String version) {
        Network ret=null;
        NetworkList nl=_networkMap.get(name);
        
        if (nl != null) {
            ret = (version == null ? nl.getCurrent() : nl.getVersion(version));
        }
        
        return (ret);
    }
    
    /**
     * This method returns the list of networks that subscribe to
     * the supplied subject.
     * 
     * @param subject The subject
     * @return The list of networks, or null of no networks subscribe to the subject
     */
    protected java.util.List<Network> getNetworksForSubject(String subject) {
        return (_subjectMap.get(subject));
    }

    /**
     * This method returns the node associated with the
     * supplied network and node name.
     * 
     * @param networkName The network name
     * @param version The version, or null for current
     * @param nodeName The node name
     * @return The node, or null if not found
     * @throws Exception Failed to find the specified node
     */
    protected Node getNode(String networkName, String version, String nodeName) throws Exception {
        Network net=getNetwork(networkName, version);
        
        if (net == null) {
            throw new Exception("No network '"+networkName+"' version["+version+"] was found");
        }
        
        Node node=net.getNode(nodeName);
        
        if (node == null) {
            throw new Exception("No node '"+nodeName+"' was found in network '"+networkName
                    +"' version["+version+"");
        }
        
        return (node);
    }

    /**
     * This method sets whether to use pre/post event list processing.
     * 
     * @param b Whether to use pre/post event list processing
     */
    protected void setUsePrePostEventListProcessing(boolean b) {
        _usePrePostEventListProcessing = b;
    }
    
    /**
     * This method deserializes the events in the context of the supplied
     * network. This method is only relevant for EPN manager implementations
     * that load EPN networks in their own classloader context.
     * 
     * @param events The events
     * @param network The network
     */
    protected void preProcessEvents(EventList events, Network network) {
        preProcessEvents(events, network.contextClassLoader());
    }
    
    /**
     * This method deserializes the events in the context of the supplied
     * classloader. This method is only relevant for EPN manager implementations
     * that load EPN networks in their own classloader context.
     * 
     * @param events The events
     * @param classloader The classloader
     */
    protected void preProcessEvents(EventList events, ClassLoader classloader) {
        if (classloader != null) {
            events.resolve(classloader);
        }
    }
    
    /**
     * This method resets the events, to enable them to be used in the
     * context of another classloader. This method is only relevant for EPN
     * manager implementations that load EPN networks in their own 
     * classloader context.
     * 
     * @param events The events
     */
    protected void postProcessEvents(EventList events) {
        events.reset();
    }
    
    /**
     * This method dispatches a set of events directly to the supplied
     * node.
     * 
     * @param network The network
     * @param node The node
     * @param source The source node/subject
     * @param events The list of events to be processed
     * @param retriesLeft The number of retries left
     * @return The events to retry, or null if no retries necessary
     * @throws Exception Failed to dispatch the events for processing
     */
    protected EventList process(Network network, Node node, String source, EventList events,
                            int retriesLeft) throws Exception {
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Process events on network="+network.getName()+" node="+node.getName()
                    +" source="+source+" retriesLeft="+retriesLeft+" events="+events);
        }

        EventList ret=node.process(source, events, retriesLeft);
        
        if (ret == null || ret.size() < events.size()) { 
            EventList notifyList=null;
            
            for (int i=0; i < node.getNotifications().size(); i++) {
                Notification no=node.getNotifications().get(i);
                
                if (no.getType() == NotificationType.Processed) {
                    
                    if (notifyList == null) {                        
                        if (ret != null) {
                            java.util.List<java.io.Serializable> processed = new java.util.ArrayList<java.io.Serializable>();
                            
                            for (int j=0; j < events.size(); j++) {
                                java.io.Serializable event=events.get(j);
                                if (!ret.contains(event)) {
                                    processed.add(event);
                                }
                            }
                            
                            if (processed.size() > 0) {
                                notifyList = new EventList(processed);
                            }
                        } else {
                            notifyList = events;
                        }
                    }
                    
                    if (notifyList != null) {
                        notifyListeners(no.getSubject(), notifyList);
                    }
                }
            }
        }

        if (ret != null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Processed events on network="+network.getName()
                    +" version="+network.getVersion()+" node="+node.getName()
                    +" source="+source+" retrying="+ret);
            }
        } else if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Processed events on network="+network.getName()
                    +" version="+network.getVersion()+" node="+node.getName()
                    +" source="+source+": no retries");
        }

        return (ret);
    }

    /**
     * This method sends a notification to any registered listeners that
     * a situation has occurred associated with the specified subject.
     * 
     * @param subject The subject
     * @param events The list of events
     * @throws Exception Failed to notify
     */
    protected void notifyListeners(String subject, EventList events) throws Exception {
        dispatchNotificationToListeners(subject, events);
    }
    
    /**
     * This method dispatches the notifications to the registered listeners.
     * 
     * @param subject The subject
     * @param events The list of events
     */
    protected void dispatchNotificationToListeners(String subject, EventList events) {
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest("Notify processed events on  subject="+subject
                    +" events="+events);
        }
        
        java.util.List<NotificationListener> listeners=_notificationListeners.get(subject);

        if (listeners != null) {
            for (int i=0; i < listeners.size(); i++) {
                NotificationListener nl=listeners.get(i);
                
                if (_usePrePostEventListProcessing && !(nl instanceof ContextualNotificationListener)) {
                    try {
                        preProcessEvents(events, nl.getClass().getClassLoader());
                    } catch (Throwable t) {
                        LOG.log(Level.SEVERE, MessageFormat.format(java.util.PropertyResourceBundle.getBundle(
                                "epn-core.Messages").getString("EPN-CORE-1"),
                                nl.getClass().getName()), t);
                
                        // Don't attempt to send events to the node listener
                        continue;
                    }
                }
                
                nl.notify(subject, events);
                
                if (_usePrePostEventListProcessing) {
                    postProcessEvents(events);
                }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void close() throws Exception {
        if (_epnManagement != null) {
            _epnManagement.close();
            _epnManagement = null;
        }
    }

    /**
     * This class represents a list of Network instances
     * for the same name, but different versions.
     *
     */
    public class NetworkList {
        
        private java.util.List<Network> _networks=new java.util.ArrayList<Network>();
        
        /**
         * The constructor.
         */
        public NetworkList() {
        }
        
        /**
         * This method adds a new instance of the Network to the list.
         * 
         * @param network The network
         */
        public void add(Network network) {
            synchronized (_networks) {
                boolean inserted=false;
                for (int i=0; i < _networks.size(); i++) {
                    if (VersionUtil.isNewerVersion(_networks.get(i).getVersion(),
                                    network.getVersion())) {
                        _networks.add(i, network);
                        inserted = true;
                        break;
                    }
                }
                if (!inserted) {
                    _networks.add(network);
                }
            }
        }
        
        /**
         * This method removes an instance of the Network from the list.
         * 
         * @param network The network
         */
        public void remove(Network network) {
            synchronized (_networks) {
                _networks.remove(network);
            }
        }
        
        /**
         * This method returns the most recent instance of the network.
         * 
         * @return The current instance of the network
         */
        public Network getCurrent() {
            Network ret=null;
            
            synchronized (_networks) {
                if (_networks.size() > 0) {
                    ret = _networks.get(0);
                }
            }

            return (ret);
        }
        
        /**
         * This method returns the network instance associated with
         * the supplied version.
         * 
         * @param version The version
         * @return The network instance, or null if not found
         */
        public Network getVersion(String version) {
            Network ret=null;
            
            synchronized (_networks) {
                for (Network network : _networks) {
                    if (network.getVersion().equals(version)) {
                        ret = network;
                        break;
                    }
                }
            }

            return (ret);
        }
        
        /**
         * This method returns the list of networks.
         * 
         * @return The list of networks
         */
        public java.util.List<Network> getNetworks() {
            return (_networks);
        }
        
        /**
         * This method returns the number of networks in the list.
         * 
         * @return The number of networks
         */
        public int size() {
            synchronized (_networks) {
                return (_networks.size());
            }
        }
    }
}
