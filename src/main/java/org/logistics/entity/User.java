package org.logistics.entity;

import jakarta.persistence.*;
import lombok.*;
import org.logistics.enums.Role;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean active = true;

    @Column(unique = true)
    private String keycloakId;

    public static UserBuilder builder() { return new UserBuilder(); }

    public static class UserBuilder {
        private String email;
        private String passwordHash;
        private Role role;
        private boolean active = true;
        private String keycloakId;

        public UserBuilder email(String email) { this.email = email; return this; }
        public UserBuilder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public UserBuilder role(Role role) { this.role = role; return this; }
        public UserBuilder active(boolean active) { this.active = active; return this; }
        public UserBuilder keycloakId(String keycloakId) { this.keycloakId = keycloakId; return this; }

        public User build() {
            return new User(null, email, passwordHash, role, active, keycloakId);
        }
    }
}