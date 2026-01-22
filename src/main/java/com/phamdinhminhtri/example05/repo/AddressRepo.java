package com.phamdinhminhtri.example05.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.phamdinhminhtri.example05.entity.Address;

@Repository
public interface AddressRepo extends JpaRepository<Address,Long> {
    Address findByCountryAndStateAndCityAndPinCodeAndStreetAndBuildingName(String country,String state,String city,String pinCode,String street,String buildingName);
    
} 