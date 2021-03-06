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

package org.overlord.rtgov.quickstarts.demos.orders;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.switchyard.Context;
import org.switchyard.component.bean.Reference;
import org.switchyard.component.bean.Service;

/**
 * This class provides the implementation of the order service.
 *
 */
@Service(OrderService.class)
@ApplicationScoped
public class OrderServiceBean implements OrderService {
    
    @Inject @Reference
    private InventoryService _inventory;
    
    @Inject @Reference
    private LogisticsService _logistics;
    
    @Inject
    private Context _context;

    /**
     * {@inheritDoc}
     */
    @Override
    public OrderAck submitOrder(Order order) {
        
        Object prop=_context.getPropertyValue("{http://www.projectoverlord.io/example/}ExampleHeaderValue");
        if (prop != null) {
            System.out.println("ORDER EXAMPLE HEADER: "+prop);
        }
        
        // Create an order ack
        OrderAck orderAck = new OrderAck().setOrderId(order.getOrderId())
                        .setCustomer(order.getCustomer());

        // Check the inventory
        try {
            Item orderItem = _inventory.lookupItem(order.getItemId());
            
            // Check quantity on hand and generate the ack
            if (orderItem.getQuantity() >= order.getQuantity()) {
                
                // Arrange delivery
                _logistics.deliver(order);
                
                orderAck.setAccepted(true).setStatus("Order Accepted");
                orderAck.setTotal(orderItem.getUnitPrice() * order.getQuantity());
            } else {
                orderAck.setAccepted(false).setStatus("Insufficient Quantity");
            }
            
        } catch (ItemNotFoundException infEx) {
            orderAck.setAccepted(false).setStatus("Item Not Available");
        }
        return orderAck;
    }

    /**
     * {@inheritDoc}
     */
    public Receipt makePayment(Payment payment) {
        Receipt ret=new Receipt();
        
        ret.setCustomer(payment.getCustomer());
        ret.setAmount(payment.getAmount());
        
        return (ret);
    }
}
