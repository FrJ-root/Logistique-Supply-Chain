package org.logistics.dto;

import lombok.Builder;
import lombok.Data;
import org.logistics.enums.Role;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private Role role;
    private boolean active;
    private String passwordHash;
}
