package com.phamdinhminhtri.example05.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CartItemDTO {
    private Long cartIemId;
    private CartDTO cart;
    private ProductDTO product;
    private Integer quantity;
    private double discount;
    private double productPrice;
    
}
