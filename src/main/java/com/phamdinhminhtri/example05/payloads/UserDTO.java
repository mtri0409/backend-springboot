package com.phamdinhminhtri.example05.payloads;

import java.util.HashSet;
import java.util.Set;

import com.phamdinhminhtri.example05.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private String userId;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String email;
    private String password;
    private Set<Role> roles = new HashSet<>(); 
    private AddressDTO address;
    private CartDTO cart;
}
