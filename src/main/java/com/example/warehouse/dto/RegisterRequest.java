package com.example.warehouse.dto;

import com.example.warehouse.entity.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private Role role;
}
