/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved. 
 * See the copyright.txt in the distribution for a 
 * full listing of individual contributors.
 *
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

package org.switchyard.quickstarts.demos.orders;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.switchyard.component.bean.Reference;
import org.switchyard.component.bean.Service;

@Service(OrderService.class)
@ApplicationScoped
public class OrderServiceBean implements OrderService {
    
    @Inject @Reference
    private InventoryService _inventory;
    
    @Inject @Reference
    private LogisticsService _logistics;
    
    @Override
    public OrderAck submitOrder(Order order) {
        // Create an order ack
        OrderAck orderAck = new OrderAck().setOrderId(order.getOrderId());
        // Check the inventory
        try {
            if (order.getItemId().equals("JAM")) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            Item orderItem = _inventory.lookupItem(order.getItemId());
            // Check quantity on hand and generate the ack
            if (orderItem.getQuantity() >= order.getQuantity()) {
                
                // Arrange delivery
                _logistics.deliver(order);
                
                orderAck.setAccepted(true).setStatus("Order Accepted");
                orderAck.setTotal(orderItem.getUnitPrice()*order.getQuantity());
            } else {
                orderAck.setAccepted(false).setStatus("Insufficient Quantity");
            }
            
            orderAck.setCustomer(order.getCustomer());
            
        } catch (ItemNotFoundException infEx) {
            orderAck.setAccepted(false).setStatus("Item Not Available");
        }
        return orderAck;
    }

    public Receipt makePayment(Payment payment) {
        Receipt ret=new Receipt();
        
        ret.setCustomer(payment.getCustomer());
        ret.setAmount(payment.getAmount());
        
        return (ret);
    }
}
