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
package org.overlord.rtgov.internal.ep;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.overlord.rtgov.common.service.Service;
import org.overlord.rtgov.ep.EPContext;
import org.overlord.rtgov.ep.ResultHandler;

/**
 * This class provides services to the EventProcessor
 * implementations that process the events.
 *
 * NOTE: Retained the internal version for compatibility with FSW 6.0. This class
 * can be deprecated once this is no longer an issue.
 */
public class DefaultEPContext implements EPContext {

    private static final Logger LOG=Logger.getLogger(DefaultEPContext.class.getName());

    private static final ThreadLocal<Object> RESULT=new ThreadLocal<Object>();
    
    private java.util.Map<String,Service> _services=null;
    private java.util.Map<String,Object> _parameters=null;
    private ResultHandler _handler=null;
    
    /**
     * The default constructor.
     */
    public DefaultEPContext() {
    }
    
    /**
     * This constructor initializes the service map.
     * 
     * @param services The map of services available
     */
    public DefaultEPContext(java.util.Map<String,Service> services) {
        _services = services;
    }
    
    /**
     * This constructor initializes the service map.
     * 
     * @param services The map of services available
     * @param parameters The map of parameters avaiable
     */
    public DefaultEPContext(java.util.Map<String,Service> services, java.util.Map<String,Object> parameters) {
        _services = services;
        _parameters = parameters;
    }
    
    /**
     * This method sets the result handler.
     * 
     * @param handler The asynchronous result handler
     * 
     * NOTE: This mechanism is experimental, so may change in the future.
     */
    public void setResultHandler(ResultHandler handler) {
        _handler = handler;
    }
    
    /**
     * This method sets the service map.
     * 
     * @param services The services
     */
    public void setServices(java.util.Map<String,Service> services) {
        _services = services;
    }
    
    /**
     * {@inheritDoc}
     */
    public void logInfo(String info) {
        LOG.info(info);
    }

    /**
     * {@inheritDoc}
     */
    public void logError(String error) {
        LOG.severe(error);
    }

    /**
     * {@inheritDoc}
     */
    public void logDebug(String debug) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine(debug);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void handle(Object result) {
        
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest(">>> "+this+": Handle result="+result);
        }
        
        if (_handler != null) {
            
            if (result instanceof java.io.Serializable) {
                _handler.handle((java.io.Serializable)result);
            } else {
                LOG.severe("Result from event processor is not serializable: "+result);
            }
        } else {
            RESULT.set(result);
        }
    }

    /**
     * This method retrieves the result forwarded by the rule.
     * 
     * @return The result, or null if not defined
     */
    public Object getResult() {
        Object ret=RESULT.get();
        
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.finest(">>> "+this+": Get result="+ret);
        }
        
        return (ret);
    }

    /**
     * {@inheritDoc}
     */
    public Service getService(String name) {
        Service ret=null;
        
        if (_services != null) {
            ret = _services.get(name);
        }

        return (ret);
    }

    /**
     * {@inheritDoc}
     */
    public Object getParameter(String name) {
        Object ret=null;
        
        if (_parameters != null) {
            ret = _parameters.get(name);
        }

        return (ret);
    }
}
