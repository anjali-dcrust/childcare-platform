package net.anjali.childcare.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.anjali.childcare.enums.Role;


@Getter @Setter
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String email;
    private String name;
    private Role role;
}