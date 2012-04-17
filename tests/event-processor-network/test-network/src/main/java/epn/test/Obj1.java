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
package epn.test;

import java.io.Serializable;

public class Obj1 implements Serializable {

    private static final long serialVersionUID = -7692716785700403849L;

    private int _value=0;
    
    public Obj1(int val) {
        _value = val;
    }
    
    public int getValue() {
        return(_value);
    }
    
    public int hashCode() {
        return(_value);
    }
    
    public boolean equals(Object obj) {
        return (obj instanceof Obj1 &&
                ((Obj1)obj).getValue() == _value);
    }
}
