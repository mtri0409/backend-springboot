package com.phamdinhminhtri.example05.service.impl;


import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.phamdinhminhtri.example05.entity.Cart;
import com.phamdinhminhtri.example05.entity.CartItem;
import com.phamdinhminhtri.example05.entity.Product;
import com.phamdinhminhtri.example05.exceptions.APIException;
import com.phamdinhminhtri.example05.exceptions.ResourceNotFoundException;
import com.phamdinhminhtri.example05.payloads.CartDTO;
import com.phamdinhminhtri.example05.payloads.ProductDTO;
import com.phamdinhminhtri.example05.repo.CartItemRepo;
import com.phamdinhminhtri.example05.repo.CartRepo;
import com.phamdinhminhtri.example05.repo.ProductRepo;
import com.phamdinhminhtri.example05.service.CartService;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private CartItemRepo cartItemRepo;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CartDTO addProductToCart(Long cartId, Long productId, Integer quantity) {

        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem != null) {
            throw new APIException("Product " + product.getProductName() + " already exists in the cart");
        }

        if (product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }

        if (product.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + product.getProductName()
                    + " less than or equal to the quantity " + product.getQuantity() + ".");
        }

        CartItem newCartItem = new CartItem();

        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        cartItemRepo.save(newCartItem);

        product.setQuantity(product.getQuantity() - quantity);

        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

        List<ProductDTO> productDTOs = cart.getCartItems().stream()
                .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

        cartDTO.setProducts(productDTOs);

        return cartDTO;

    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> carts = cartRepo.findAll();

        if (carts.size() == 0) {
            throw new APIException("No cart exists");
        }

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

            cartDTO.setProducts(products);

            return cartDTO;

        }).collect(Collectors.toList());

        return cartDTOs;
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepo.findCartByEmailAndCartId(emailId, cartId);

        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "cartId", cartId);
        }

        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

       List<ProductDTO> products = cart.getCartItems()
    .stream()
    .map(cartItem -> {
        ProductDTO dto = modelMapper.map(
            cartItem.getProduct(),
            ProductDTO.class
        );

        dto.setQuantity(cartItem.getQuantity()); // 👈 APPEND Ở ĐÂY
        return dto;
    })
    .collect(Collectors.toList());

cartDTO.setProducts(products);

 
        cartDTO.setProducts(products);
        
        return cartDTO;
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
        }

        double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecialPrice());

        cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItem = cartItemRepo.save(cartItem);
    }

 @Override
public CartDTO updateProductQuantityInCart(Long cartId, Long productId, Integer quantity) {

    Cart cart = cartRepo.findById(cartId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

    Product product = productRepo.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

    CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);

    if (cartItem == null) {
        throw new APIException("Product not found in cart");
    }

    if (product.getQuantity() < quantity) {
        throw new APIException("Not enough stock");
    }

    // ✅ update stock
    int diff = quantity - cartItem.getQuantity();
    product.setQuantity(product.getQuantity() - diff);

    // ✅ giữ nguyên productPrice snapshot
    cartItem.setQuantity(quantity);
    cartItem.setDiscount(product.getDiscount());

    cartItemRepo.save(cartItem);

    // ✅ TÍNH LẠI TOTAL PRICE
    BigDecimal total = cart.getCartItems().stream()
            .map(item ->
                    BigDecimal.valueOf(item.getProductPrice())
                            .multiply(BigDecimal.valueOf(item.getQuantity()))
            )
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    cart.setTotalPrice(total.doubleValue());

    cartRepo.save(cart);

    return modelMapper.map(cart, CartDTO.class);
}


    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepo.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new ResourceNotFoundException("Product", "productId", productId);
        }

        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));

        Product product = cartItem.getProduct();
        product.setQuantity(product.getQuantity() + cartItem.getQuantity());

        cartItemRepo.deleteCartItemByProductIdAndCartId(cartId, productId);

        return "Product " + cartItem.getProduct().getProductName() + " removed from the cart !!!";
    }
    @Transactional
    public String clearCart(String email, Long cartId) {

        Cart cart = cartRepo.findCartByEmailAndCartId(email, cartId);

        if (cart == null) {
            throw new APIException("Không tìm thấy giỏ hàng");
        }

        List<CartItem> cartItems = cartItemRepo.findByCartCartId(cartId);

        if (cartItems.isEmpty()) {
           throw new APIException("Giỏ hàng trống");
        }
        // ✅ xóa cart items
        // cartItemRepo.deleteAll(cartItems);
        // ✅ reset tổng tiền
        cartItemRepo.deleteByCartId(cartId);
        cart.setTotalPrice(0.0);
        cartRepo.save(cart);

        return "Đã xoá toàn bộ sản phẩm trong giỏ hàng" ;
    }

}