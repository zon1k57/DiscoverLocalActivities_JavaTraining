package com.discover.discover_local_abilities_javaedition.dto;

import com.discover.discover_local_abilities_javaedition.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String name;
    private String surname;
    private String email;
    private String destination;
    private Double latitude;
    private Double longitude;


    public static UserDTO from(User user) {
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getEmail(),
                user.getDestination(),
                user.getLatitude(),
                user.getLongitude()
        );
    }
}
