package com.phamdinhminhtri.example05.service;

import java.util.List;

import com.phamdinhminhtri.example05.payloads.OrderDTO;
import com.phamdinhminhtri.example05.payloads.OrderResponse;

public interface OrderService {
    OrderDTO placeOrder(String emailId,Long cartID,String paymentMethod);
    OrderDTO getOrder(String emailId,Long orderId);
    List<OrderDTO> getOrdersByUser(String emailId);
    
    OrderResponse getAllOrders(Integer pageNumber,Integer pageSize,String sortBy,String sortOrder);

    OrderDTO updateOrder(String emailId,Long orderId,String orderStatus);
}
