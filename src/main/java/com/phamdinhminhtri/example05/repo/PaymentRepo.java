package com.phamdinhminhtri.example05.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.phamdinhminhtri.example05.entity.Payment;

public interface PaymentRepo extends JpaRepository<Payment,Long>{
    
}
