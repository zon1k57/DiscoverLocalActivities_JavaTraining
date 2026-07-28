package com.discover.discover_local_abilities_javaedition.service;

import com.discover.discover_local_abilities_javaedition.dto.UserDTO;

import java.util.List;

public interface UserService {
    List<UserDTO> findAll();
    UserDTO findByIndex(Long id);
}
