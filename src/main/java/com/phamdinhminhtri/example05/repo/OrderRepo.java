package com.phamdinhminhtri.example05.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.phamdinhminhtri.example05.entity.Order;

public interface OrderRepo extends JpaRepository<Order,Long>{
    
    @Query("SELECT o FROM Order o WHERE o.email =?1 AND o.id = ?2")
    Order findOrderByEmailAndOrderId(String email ,Long cartId);

    List<Order> findAllByEmail(String emailId);
}
