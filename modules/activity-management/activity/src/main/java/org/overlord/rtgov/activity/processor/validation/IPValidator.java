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
package org.overlord.rtgov.activity.processor.validation;

import java.text.MessageFormat;

import org.overlord.rtgov.activity.model.Context.Type;
import org.overlord.rtgov.activity.processor.InformationProcessor;
import org.overlord.rtgov.activity.processor.TypeProcessor;
import org.overlord.rtgov.activity.processor.TypeProcessor.ContextEvaluator;
import org.overlord.rtgov.activity.processor.TypeProcessor.PropertyEvaluator;

/**
 * This class validates an Event Processor Network.
 * 
 */
public final class IPValidator {
    
    /**
     * The default constructor.
     */
    private IPValidator() {
    }

    /**
     * This method validates the supplied information processor and
     * returns results via a supplied listener.
     * 
     * @param ip The information processor to be validated
     * @param l The listener
     * @return Whether valid
     */
    public static boolean validate(InformationProcessor ip, IPValidationListener l) {
        boolean ret=true;
        
        if (!validateInformationProcessor(ip, l)) {
            ret = false;
        }
        
        if (!validateTypeProcessors(ip, l)) {
            ret = false;
        }
        
        return (ret);
    }
    
    /**
     * This method validates the information processor.
     * 
     * @param ip The information processor to be validated
     * @param l The listener
     * @return Whether valid
     */
    protected static boolean validateInformationProcessor(InformationProcessor ip, IPValidationListener l) {
        boolean ret=true;
        
        if (ip.getName() == null) {
            l.error(ip, ip, MessageFormat.format(java.util.PropertyResourceBundle.getBundle(
                    "activity.Messages").getString("ACTIVITY-14"),
                    "Information Processor", "name"));
            
            ret = false;
        }
        
        if (ip.getVersion() == null) {
            l.error(ip, ip, MessageFormat.format(java.util.PropertyResourceBundle.getBundle(
                    "activity.Messages").getString("ACTIVITY-14"),
                    "Information Processor", "version"));
            
            ret = false;
        }
        
        return (ret);
    }
    
    /**
     * This method validates the type processors.
     * 
     * @param ip The information processor to be validated
     * @param l The listener
     * @return Whether valid
     */
    protected static boolean validateTypeProcessors(InformationProcessor ip, IPValidationListener l) {
        boolean ret=true;
        
        for (TypeProcessor tp : ip.getTypeProcessors().values()) {
            
            if (!validateContextEvaluators(ip, tp, l)) {
                ret = false;
            }

            if (!validatePropertyEvaluators(ip, tp, l)) {
                ret = false;
            }
        }
        
        return (ret);
    }
    
    /**
     * This method validates the context evaluators.
     * 
     * @param ip The information processor
     * @param tp The type processor
     * @param l The validation listener
     * @return Whether valid
     */
    protected static boolean validateContextEvaluators(InformationProcessor ip, TypeProcessor tp,
                                        IPValidationListener l) {
        boolean ret=true;
        
        for (ContextEvaluator ce : tp.getContexts()) {
            
            if (ce.getEvaluator() == null) {
                l.error(ip, ce, java.util.PropertyResourceBundle.getBundle(
                        "activity.Messages").getString("ACTIVITY-15"));
                ret = false;
            }
            
            // If timeframe has been set, then context type must be Link,
            // however a Link context can be defined with no timeframe, indicating
            // it is the destination of the link (i.e. only the source needs
            // to indicate the timeframe in which the link is valid)
            if (ce.getTimeframe() != 0 && ce.getType() != Type.Link) {
                l.error(ip, ce, java.util.PropertyResourceBundle.getBundle(
                        "activity.Messages").getString("ACTIVITY-16"));
                ret = false;
            }
        }
        
        return (ret);
    }
    
    /**
     * This method validates the property evaluators.
     * 
     * @param ip The information processor
     * @param tp The type processor
     * @param l The validation listener
     * @return Whether valid
     */
    protected static boolean validatePropertyEvaluators(InformationProcessor ip, TypeProcessor tp,
                                        IPValidationListener l) {
        boolean ret=true;
        
        for (PropertyEvaluator pe : tp.getProperties()) {
            
            if (pe.getName() == null) {
                l.error(ip, ip, MessageFormat.format(java.util.PropertyResourceBundle.getBundle(
                        "activity.Messages").getString("ACTIVITY-14"),
                        "Property Evaluator", "name"));
                
                ret = false;
            }
            
            if (pe.getEvaluator() == null) {
                l.error(ip, pe, java.util.PropertyResourceBundle.getBundle(
                        "activity.Messages").getString("ACTIVITY-17"));
                ret = false;
            }
        }
        
        return (ret);
    }
}
