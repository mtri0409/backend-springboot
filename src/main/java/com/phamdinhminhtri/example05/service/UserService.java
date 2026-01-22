package com.phamdinhminhtri.example05.service;

import com.phamdinhminhtri.example05.payloads.UserDTO;
import com.phamdinhminhtri.example05.payloads.UserResponse;

public interface UserService {
    UserDTO registerUser(UserDTO userDTO);

    UserResponse getAllUsers(Integer pageNumber,Integer pageSize,String sortBy,String sortOrder);
    UserDTO getUserById(Long userId);
    UserDTO updateUser(Long userId, UserDTO userDTO);
    UserDTO updateUserEmail(String email, UserDTO userDTO);
    String deleteUser (Long userId);
    UserDTO getUserByEmail(String email);
}
