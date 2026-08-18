package com.discover.discover_local_abilities_javaedition.service.impl;

import com.discover.discover_local_abilities_javaedition.dto.UserDTO;
import com.discover.discover_local_abilities_javaedition.model.User;
import com.discover.discover_local_abilities_javaedition.repository.UserRepository;
import com.discover.discover_local_abilities_javaedition.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    protected Integer DEfAULT_LIMIT = 20;
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDTO> findAll(Integer limit) {
        int chosenLimit = (limit == null) ? this.DEfAULT_LIMIT : limit;
        return userRepository.findAll()
                .stream()
                .limit(chosenLimit)
                .map(UserDTO::from)
                .toList();

    }

    @Override
    public UserDTO findByIndex(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id "+id));

        return UserDTO.from(user);
    }
}
