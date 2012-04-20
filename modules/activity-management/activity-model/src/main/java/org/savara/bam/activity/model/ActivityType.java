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
package org.savara.bam.activity.model;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.savara.bam.activity.model.bpm.ProcessCompleted;
import org.savara.bam.activity.model.bpm.ProcessStarted;
import org.savara.bam.activity.model.mom.MessageReceived;
import org.savara.bam.activity.model.mom.MessageSent;
import org.savara.bam.activity.model.soa.RequestReceived;
import org.savara.bam.activity.model.soa.RequestSent;
import org.savara.bam.activity.model.soa.ResponseReceived;
import org.savara.bam.activity.model.soa.ResponseSent;

/**
 * This abstract class is the super type of all activity type classes.
 *
 */
@JsonTypeInfo(use=JsonTypeInfo.Id.NAME, include=JsonTypeInfo.As.PROPERTY, property="typeName")
@JsonSubTypes({@Type(value=MessageReceived.class),
    @Type(value=MessageSent.class),
    @Type(value=RequestReceived.class),
    @Type(value=RequestSent.class),
    @Type(value=ResponseReceived.class),
    @Type(value=ResponseSent.class),
    @Type(value=ProcessCompleted.class),
    @Type(value=ProcessStarted.class)})
public abstract class ActivityType {

}
