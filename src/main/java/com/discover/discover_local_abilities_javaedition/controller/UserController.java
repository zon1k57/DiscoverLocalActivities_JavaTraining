package com.discover.discover_local_abilities_javaedition.controller;

import com.discover.discover_local_abilities_javaedition.dto.UserDTO;
import com.discover.discover_local_abilities_javaedition.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDTO> listAll(){
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserDTO findByIndex(@PathVariable Long id){
        return userService.findByIndex(id);
    }
}
