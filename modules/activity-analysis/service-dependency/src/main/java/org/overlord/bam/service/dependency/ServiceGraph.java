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
package org.overlord.bam.service.dependency;

/**
 * This class represents the service graph.
 *
 */
public class ServiceGraph {

    private java.util.Set<ServiceNode> _nodes=new java.util.HashSet<ServiceNode>();
    private java.util.Set<InvocationLink> _links=new java.util.HashSet<InvocationLink>();
 
    /**
     * The default constructor.
     */
    public ServiceGraph() {
    }
    
    /**
     * This method returns the service nodes.
     * 
     * @return The service nodes
     */
    public java.util.Set<ServiceNode> getNodes() {
        return (_nodes);
    }
    
    /**
     * This method returns the service node associated with
     * the supplied service type.
     * 
     * @param serviceType The service type
     * @return The service node, or null if not found
     */
    public ServiceNode getNode(String serviceType) {
        ServiceNode ret=null;
        
        for (ServiceNode sn : _nodes) {
            if (sn.getService().getServiceType().equals(serviceType)) {
                ret = sn;
                break;
            }
        }
        
        return (ret);
    }
    
    /**
     * This method returns the service invocation links.
     * 
     * @return The service invocation links
     */
    public java.util.Set<InvocationLink> getLinks() {
        return (_links);
    }
}