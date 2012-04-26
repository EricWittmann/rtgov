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
package org.savara.tests.switchyard.beanservice;

import javax.inject.Inject;

import org.savara.bam.collector.ActivityCollector;
import org.switchyard.Exchange;
import org.switchyard.ExchangeHandler;
import org.switchyard.HandlerException;
import org.switchyard.handlers.MessageTrace;

public class ExchangeInterceptor extends MessageTrace implements ExchangeHandler {
    
    @Inject
    private ActivityCollector _activityCollector=null;

    public void handleMessage(Exchange exchange) throws HandlerException {
        System.out.println("HANDLE MESSAGE="+exchange+" COLLECTOR="+_activityCollector);
        super.handleMessage(exchange);
    }

    public void handleFault(Exchange exchange) {
        System.out.println("HANDLE FAULT="+exchange+" COLLECTOR="+_activityCollector);
        super.handleFault(exchange);
    }

}
