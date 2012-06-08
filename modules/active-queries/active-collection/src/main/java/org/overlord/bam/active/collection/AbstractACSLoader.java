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
package org.overlord.bam.active.collection;

/**
 * This class provides an abstract base class for ACS loaders.
 *
 */
public abstract class AbstractACSLoader {

    /**
     * This method pre-initializes the Active Collection Source
     * before it is registered with the manager. This is sometimes
     * required if the loader and manager are associated with different
     * contextual classloaders.
     * 
     * @param acs The active collection source to pre-initialize
     * @throws Exception Failed to pre-initialize source
     */
    protected void preInit(ActiveCollectionSource acs) throws Exception {
        acs.preInit();
    }
}
