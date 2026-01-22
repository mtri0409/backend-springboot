package com.phamdinhminhtri.example05.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.phamdinhminhtri.example05.entity.OrderItem;

public interface OrderItemRepo extends JpaRepository<OrderItem,Long>{
    
}
