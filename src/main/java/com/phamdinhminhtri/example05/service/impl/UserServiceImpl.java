package com.phamdinhminhtri.example05.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.phamdinhminhtri.example05.config.AppConstants;
import com.phamdinhminhtri.example05.entity.Address;
import com.phamdinhminhtri.example05.entity.Cart;
import com.phamdinhminhtri.example05.entity.Role;
import com.phamdinhminhtri.example05.entity.User;
import com.phamdinhminhtri.example05.exceptions.APIException;
import com.phamdinhminhtri.example05.exceptions.ResourceNotFoundException;
import com.phamdinhminhtri.example05.payloads.AddressDTO;
import com.phamdinhminhtri.example05.payloads.CartDTO;
import com.phamdinhminhtri.example05.payloads.ProductDTO;
import com.phamdinhminhtri.example05.payloads.UserDTO;
import com.phamdinhminhtri.example05.payloads.UserResponse;

import com.phamdinhminhtri.example05.repo.AddressRepo;
import com.phamdinhminhtri.example05.repo.RoleRepo;
import com.phamdinhminhtri.example05.repo.UserRepo;
import com.phamdinhminhtri.example05.service.UserService;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private RoleRepo roleRepo;

    @Autowired
    private AddressRepo addressRepo;

    // @Autowired
    // private CartService cartService; // Đã comment

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UserDTO registerUser(UserDTO userDTO) {
        try {
            User user = modelMapper.map(userDTO, User.class);

            Cart cart = new Cart(); 
            cart.setTotalPrice(0.0);
            cart.setUser(user); 
            user.setCart(cart);
            Role role = roleRepo.findById(AppConstants.USER_ID).get();
            user.getRoles().add(role);

            String country = userDTO.getAddress().getCountry();
            String state = userDTO.getAddress().getState();
            String city = userDTO.getAddress().getCity();
            String pincode = userDTO.getAddress().getPincode();
            String street = userDTO.getAddress().getStreet();
            String buildingName = userDTO.getAddress().getBuildingName();

            Address address = addressRepo.findByCountryAndStateAndCityAndPinCodeAndStreetAndBuildingName(country, state, city, pincode, street, buildingName);

            if (address == null) {
                address = new Address(country, state, city, pincode, street, buildingName);
                address = addressRepo.save(address);
            }

            user.setAddresses(List.of(address));
            User registeredUser = userRepo.save(user);

            cart.setUser(registeredUser);

            userDTO = modelMapper.map(registeredUser, UserDTO.class);
            userDTO.setAddress(modelMapper.map(registeredUser.getAddresses().stream().findFirst().get(), AddressDTO.class));
            
            return userDTO;

        } catch (DataIntegrityViolationException e) {
            throw new APIException("User already exists with emailId: " + userDTO.getEmail());
        }
    }

    @Override
    public UserDTO getUserByEmail(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
            userDTO.setAddress(modelMapper.map(user.getAddresses().stream().findFirst().get(), AddressDTO.class));
        }
        
        if (user.getCart() != null) {
            CartDTO cart = modelMapper.map(user.getCart(), CartDTO.class);
            List<ProductDTO> products = user.getCart().getCartItems().stream()
                    .map(item -> modelMapper.map(item.getProduct(), ProductDTO.class))
                    .collect(Collectors.toList());
            cart.setProducts(products);
            userDTO.setCart(cart);
        }
        return userDTO;
    }
    @Override
    public UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sort);

        Page<User> pageUsers = userRepo.findAll(pageDetails);
        List<User> users = pageUsers.getContent();

        if (users.size() == 0) {
            throw new APIException("No User exists !!!");
        }

        List<UserDTO> userDTOs = users.stream().map(user -> {
            UserDTO userDTO = modelMapper.map(user, UserDTO.class);

            if (user.getAddresses().size() != 0) {
                userDTO.setAddress(modelMapper.map(user.getAddresses().stream().findFirst().get(), AddressDTO.class));
            }

            // if(user.getCart() !=null){
                
            // }
            // CartDTO cart = modelMapper.map(user.getCart(), CartDTO.class); // Đã comment
            // List<ProductDTO> products = user.getCart().getCartItems().stream() // Đã comment
            //     .map(item -> modelMapper.map(item.getProduct(), ProductDTO.class)) // Đã comment
            //     .collect(Collectors.toList()); // Đã comment

            // userDTO.getCart().setProducts(products); // Đã comment
            return userDTO;
        }).collect(Collectors.toList());

        UserResponse userResponse = new UserResponse();

        userResponse.setContent(userDTOs);
        userResponse.setPageNumber(pageUsers.getNumber());
        userResponse.setPageSize(pageUsers.getSize());
        userResponse.setTotalElements(pageUsers.getTotalElements());
        userResponse.setTotalPages(pageUsers.getTotalPages());
        userResponse.setLastPage(pageUsers.isLast());

        return userResponse;
    }

    @Override
    public UserDTO getUserById(Long userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);
        userDTO.setAddress(modelMapper.map(user.getAddresses().stream().findFirst().get(), AddressDTO.class));

        // CartDTO cart = modelMapper.map(user.getCart(), CartDTO.class); // Đã comment
        // List<ProductDTO> products = user.getCart().getCartItems().stream() // Đã comment
        //     .map(item -> modelMapper.map(item.getProduct(), ProductDTO.class)) // Đã comment
        //     .collect(Collectors.toList()); // Đã comment

        // userDTO.getCart().setProducts(products); // Đã comment
        return userDTO;
    }

    @Override
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        String encodedPass = passwordEncoder.encode(userDTO.getPassword());
        
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setMobileNumber(userDTO.getMobileNumber());
        user.setEmail(userDTO.getEmail());
        user.setPassword(encodedPass);

        if (userDTO.getAddress() != null) {
            String country = userDTO.getAddress().getCountry();
            String state = userDTO.getAddress().getState();
            String city = userDTO.getAddress().getCity();
            String pincode = userDTO.getAddress().getPincode();
            String street = userDTO.getAddress().getStreet();
            String buildingName = userDTO.getAddress().getBuildingName();

            Address address = addressRepo.findByCountryAndStateAndCityAndPinCodeAndStreetAndBuildingName(country, state, city, pincode, street, buildingName);

            if (address == null) {
                address = new Address(country, state, city, pincode, street, buildingName);
                address = addressRepo.save(address);
            }

            user.setAddresses(List.of(address));
        }

        user = userRepo.save(user);

        userDTO = modelMapper.map(user, UserDTO.class);
        userDTO.setAddress(modelMapper.map(user.getAddresses().stream().findFirst().get(), AddressDTO.class));

        // CartDTO cart = modelMapper.map(user.getCart(), CartDTO.class); // Đã comment
        // List<ProductDTO> products = user.getCart().getCartItems().stream() // Đã comment
        //     .map(item -> modelMapper.map(item.getProduct(), ProductDTO.class)) // Đã comment
        //     .collect(Collectors.toList()); // Đã comment

        // userDTO.getCart().setProducts(products); // Đã comment
        
        return userDTO;
    }

    @Override
    public UserDTO updateUserEmail(String email, UserDTO userDTO) {
       
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setMobileNumber(userDTO.getMobileNumber());

        // ✅ chỉ update password nếu có
        if (userDTO.getPassword() != null && !userDTO.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        // ✅ address
        if (userDTO.getAddress() != null) {
            AddressDTO dto = userDTO.getAddress();

            Address address = addressRepo
                .findByCountryAndStateAndCityAndPinCodeAndStreetAndBuildingName(
                    dto.getCountry(),
                    dto.getState(),
                    dto.getCity(),
                    dto.getPincode(),
                    dto.getStreet(),
                    dto.getBuildingName()
                );

            if (address == null) {
                address = addressRepo.save(
                    new Address(
                        dto.getCountry(),
                        dto.getState(),
                        dto.getCity(),
                        dto.getPincode(),
                        dto.getStreet(),
                        dto.getBuildingName()
                    )
                );
            }

            user.setAddresses(List.of(address));
        }

        user = userRepo.save(user);

        UserDTO result = modelMapper.map(user, UserDTO.class);

        if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
            result.setAddress(
                modelMapper.map(user.getAddresses().get(0), AddressDTO.class)
            );
        }

        return result;
 
    }

    @Override
    public String deleteUser(Long userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));

        // List<CartItem> cartItems = user.getCart().getCartItems(); // Đã comment
        // Long cartId = user.getCart().getCartId(); // Đã comment

        // cartItems.forEach(item -> { // Đã comment
        //     Long productId = item.getProduct().getProductId(); // Đã comment
        //     cartService.deleteProductFromCart(cartId, productId); // Đã comment
        // }); // Đã comment

        userRepo.delete(user);

        return "User with userId " + userId + " deleted successfully!!!";
    }
}